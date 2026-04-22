package com.example.tourscan.ui.screens.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.domain.usecases.GetLandmarkDataUseCase
import com.example.tourscan.domain.usecases.SavePhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.ln

class HomeViewModel(
    private val app: Application,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val getLandmarkDataUseCase: GetLandmarkDataUseCase
) : AndroidViewModel(app) {

    companion object {
        private const val TAG = "TourScanML"
        private const val ALTELE_LABEL = "altele"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Etichetele claselor — se incarca o singura data din labels.txt
    private val labels: List<String> by lazy {
        FileUtil.loadLabels(app, "labels.txt")
    }

    private val alteleIdx: Int by lazy {
        labels.indexOf(ALTELE_LABEL)
    }

    fun switchModel(modelType: ModelType) {
        _uiState.update { it.copy(selectedModel = modelType) }
    }

    fun resetPhoto() {
        _uiState.update {
            it.copy(
                isAnalyzing = false,
                detectedLabel = null,
                confidence = null,
                inferenceTimeMs = null,
                landmarkData = null
            )
        }
    }

    fun onPhotoCaptured(uriString: String, isFromCamera: Boolean) {
        viewModelScope.launch {
            val imageUri = uriString.toUri()

            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    detectedLabel = null,
                    confidence = null,
                    inferenceTimeMs = null,
                    landmarkData = null
                )
            }

            val (detectedLabel, confidence, timeMs) = withContext(Dispatchers.Default) {
                analyzeImage(imageUri)
            }

            Log.d(TAG, "Model: ${_uiState.value.selectedModel.displayName} | " +
                    "Result: $detectedLabel | Confidence: $confidence | Time: ${timeMs}ms")

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    detectedLabel = detectedLabel,
                    confidence = confidence,
                    inferenceTimeMs = timeMs
                )
            }

            // Incarca datele despre obiectiv daca a fost recunoscut
            if (detectedLabel != null) {
                val data = getLandmarkDataUseCase(detectedLabel)
                _uiState.update { it.copy(landmarkData = data) }
            }

            // Salveaza in DB doar daca vine de la camera si a fost recunoscut
            if (isFromCamera && detectedLabel != null) {
                savePhotoUseCase(
                    imageUri,
                    description = detectedLabel,
                    model = _uiState.value.selectedModel.displayName,
                    accuracy = confidence ?: 0f
                )
            }
        }
    }

    // ---------------------------------------------------------------
    //  PIPELINE DE CLASIFICARE
    //  Input pixels [0,255] → model (preprocessing baked-in) → logits
    //  → temperature scaling → softmax → rejectie multi-nivel
    // ---------------------------------------------------------------

    private fun analyzeImage(uri: Uri): Triple<String?, Float?, Long> {
        try {
            // 1. Incarca bitmap-ul
            val bitmap = loadBitmapFromUri(uri) ?: return Triple(null, null, 0)
            val selectedModel = _uiState.value.selectedModel

            // 2. Pregateste imaginea: DOAR resize la 224x224
            //    NU aplicam NormalizeOp — modelul are preprocesarea
            //    integrata si asteapta pixeli bruti in [0, 255]
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(
                    selectedModel.inputSize,
                    selectedModel.inputSize,
                    ResizeOp.ResizeMethod.BILINEAR
                ))
                .build()

            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            val processedImage = imageProcessor.process(tensorImage)

            // 3. Ruleaza inferenta cu TFLite Interpreter
            val startTime = System.nanoTime()

            val modelFile = FileUtil.loadMappedFile(app, selectedModel.fileName)
            val interpreter = Interpreter(modelFile)

            val numClasses = labels.size
            val outputArray = Array(1) { FloatArray(numClasses) }

            val inputBuffer = processedImage.buffer
            inputBuffer.rewind()
            interpreter.run(inputBuffer, outputArray)
            interpreter.close()

            val inferenceTimeMs = (System.nanoTime() - startTime) / 1_000_000
            val logits = outputArray[0]

            // 4. Temperature scaling + softmax
            val T = selectedModel.temperature
            val probs = temperatureScaledSoftmax(logits, T)

            // 5. Energy score (pentru logging/debugging)
            val energyScore = computeEnergyScore(logits, T)

            // 6. Gaseste clasa cu probabilitatea maxima
            val bestIdx = probs.indices.maxByOrNull { probs[it] }
                ?: return Triple(null, null, inferenceTimeMs)
            val bestLabel = labels[bestIdx]
            val bestProb = probs[bestIdx]

            // 7. Gap intre top-1 si top-2
            val sortedProbs = probs.sortedDescending()
            val gap = if (sortedProbs.size >= 2) sortedProbs[0] - sortedProbs[1] else 1f

            Log.d(TAG, "Logits: ${logits.contentToString()}")
            Log.d(TAG, "Label: $bestLabel | Prob: %.4f | Gap: %.4f | Energy: %.4f".format(
                bestProb, gap, energyScore
            ))

            // 8. Decizia de rejectie multi-nivel
            //    a) Daca modelul zice "altele" → nu e niciun obiectiv
            //    b) Confidenta softmax sub prag → nesigur
            //    c) Gap prea mic intre top-2 → confuzie intre clase
            val isAltele = (bestIdx == alteleIdx)
            val isConfident = (bestProb >= selectedModel.softmaxMin)
            val hasGap = (gap >= selectedModel.gapMin)

            if (isAltele) {
                Log.d(TAG, "REJECTED — modelul a clasificat ca 'altele' (nu e obiectiv turistic)")
                return Triple(null, null, inferenceTimeMs)
            }

            if (!isConfident) {
                Log.d(TAG, "REJECTED — confidenta prea mica: %.4f < %.2f".format(
                    bestProb, selectedModel.softmaxMin
                ))
                return Triple(null, null, inferenceTimeMs)
            }

            if (!hasGap) {
                Log.d(TAG, "REJECTED — gap prea mic: %.4f < %.2f".format(
                    gap, selectedModel.gapMin
                ))
                return Triple(null, null, inferenceTimeMs)
            }

            // Acceptat!
            Log.d(TAG, "ACCEPTED — $bestLabel (%.1f%%)".format(bestProb * 100))
            return Triple(bestLabel, bestProb, inferenceTimeMs)

        } catch (e: Exception) {
            Log.e(TAG, "Eroare la clasificare", e)
        }
        return Triple(null, null, 0)
    }

    // ---------------------------------------------------------------
    //  Functii auxiliare
    // ---------------------------------------------------------------

    /**
     * Aplica temperature scaling pe logits si returneaza probabilitati softmax.
     * Formula: softmax(logits / T)
     * T < 1 ascute distributia (model mai decisiv)
     * T > 1 aplatizeaza distributia (model mai incert)
     */
    private fun temperatureScaledSoftmax(logits: FloatArray, T: Float): FloatArray {
        val scaled = FloatArray(logits.size) { logits[it] / T }

        // Softmax cu stabilitate numerica (scadem max-ul)
        val maxVal = scaled.max()
        val exps = FloatArray(scaled.size) { exp((scaled[it] - maxVal).toDouble()).toFloat() }
        val sumExps = exps.sum()

        return FloatArray(exps.size) { exps[it] / sumExps }
    }

    /**
     * Energy score: E(x) = -T * ln( sum( exp(logit_i / T) ) )
     * Scor mai mic (mai negativ) → model mai putin sigur
     * Folosit pentru detectia OOD (out-of-distribution)
     */
    private fun computeEnergyScore(logits: FloatArray, T: Float): Float {
        val scaled = FloatArray(logits.size) { logits[it] / T }
        val maxVal = scaled.max()
        val sumExp = scaled.sumOf { exp((it - maxVal).toDouble()) }.toFloat()
        return -T * (maxVal + ln(sumExp.toDouble()).toFloat())
    }

    /**
     * Incarca un Bitmap din URI, convertindu-l la ARGB_8888 daca e necesar.
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        val inputStream = app.contentResolver.openInputStream(uri) ?: return null
        var bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (bitmap != null && bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        return bitmap
    }
}
