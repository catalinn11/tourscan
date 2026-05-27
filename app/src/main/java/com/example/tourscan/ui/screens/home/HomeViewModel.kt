package com.example.tourscan.ui.screens.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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
    //  Input pixels [0,255] → letterbox resize 224x224
    //  → model (preprocessing baked-in) → logits
    //  → temperature scaling → softmax → rejectie multi-nivel
    // ---------------------------------------------------------------

    private fun analyzeImage(uri: Uri): Triple<String?, Float?, Long> {
        try {
            // 1. Incarca bitmap-ul
            val bitmap = loadBitmapFromUri(uri) ?: return Triple(null, null, 0)
            val selectedModel = _uiState.value.selectedModel
            val targetSize = selectedModel.inputSize

            // 2. LETTERBOX RESIZE — identic cu antrenarea!
            //    Scaleaza proportional pana cand latura mare = 224,
            //    apoi completeaza cu negru pe margini.
            //    NU stretch direct — asta distorsioneaza proportiile.
            val letterboxed = letterboxResize(bitmap, targetSize)

            // 3. Converteste bitmap-ul in ByteBuffer FLOAT32 [0, 255]
            //    NU aplicam NormalizeOp — modelul are preprocesarea
            //    integrata si asteapta pixeli bruti in [0, 255]
            val inputBuffer = bitmapToFloatBuffer(letterboxed, targetSize)

            // 4. Ruleaza inferenta cu TFLite Interpreter
            val startTime = System.nanoTime()

            val modelFile = FileUtil.loadMappedFile(app, selectedModel.fileName)
            val interpreter = Interpreter(modelFile)

            val numClasses = labels.size
            val outputArray = Array(1) { FloatArray(numClasses) }

            interpreter.run(inputBuffer, outputArray)
            interpreter.close()

            val inferenceTimeMs = (System.nanoTime() - startTime) / 1_000_000
            val logits = outputArray[0]

            // 5. Temperature scaling + softmax
            val T = selectedModel.temperature
            val probs = temperatureScaledSoftmax(logits, T)

            // 6. Energy score (pentru logging/debugging)
            val energyScore = computeEnergyScore(logits, T)

            // 7. Gaseste clasa cu probabilitatea maxima
            val bestIdx = probs.indices.maxByOrNull { probs[it] }
                ?: return Triple(null, null, inferenceTimeMs)
            val bestLabel = labels[bestIdx]
            val bestProb = probs[bestIdx]

            // 8. Gap intre top-1 si top-2
            val sortedProbs = probs.sortedDescending()
            val gap = if (sortedProbs.size >= 2) sortedProbs[0] - sortedProbs[1] else 1f

            Log.d(TAG, "Logits: ${logits.contentToString()}")
            Log.d(TAG, "Label: $bestLabel | Prob: %.4f | Gap: %.4f | Energy: %.4f".format(
                bestProb, gap, energyScore
            ))

            // 9. Decizia de rejectie multi-nivel
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
     * Letterbox resize — identic cu pipeline-ul de antrenare.
     * Scaleaza imaginea proportional pana cand latura cea mai mare
     * atinge dimensiunea tinta, apoi completeaza cu negru (padding).
     * Pastreaza proportiile originale, fara distorsionare.
     */
    private fun letterboxResize(src: Bitmap, targetSize: Int): Bitmap {
        val srcW = src.width
        val srcH = src.height

        // Calculeaza factorul de scalare (cel mai mic, ca sa incapa)
        val scale = min(targetSize.toFloat() / srcW, targetSize.toFloat() / srcH)
        val newW = (srcW * scale).roundToInt()
        val newH = (srcH * scale).roundToInt()

        // Scaleaza proportional
        val scaled = Bitmap.createScaledBitmap(src, newW, newH, true)

        // Creeaza canvas negru de targetSize x targetSize
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.BLACK)

        // Centreaza imaginea scalata pe canvas
        val left = (targetSize - newW) / 2f
        val top = (targetSize - newH) / 2f
        canvas.drawBitmap(scaled, left, top, null)

        if (scaled != src) scaled.recycle()

        return result
    }

    /**
     * Converteste un bitmap ARGB_8888 in ByteBuffer FLOAT32 format RGB [0, 255].
     * Layout: [1, H, W, 3] — batch=1, height, width, channels(RGB)
     */
    private fun bitmapToFloatBuffer(bitmap: Bitmap, size: Int): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * size * size * 3 * 4) // 4 bytes per float
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(size * size)
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)

        for (pixel in pixels) {
            // ARGB → RGB, as float [0, 255]
            buffer.putFloat(((pixel shr 16) and 0xFF).toFloat()) // R
            buffer.putFloat(((pixel shr 8) and 0xFF).toFloat())  // G
            buffer.putFloat((pixel and 0xFF).toFloat())           // B
        }

        buffer.rewind()
        return buffer
    }

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
