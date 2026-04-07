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
import com.example.tourscan.ml.EfficientnetFloat32
import com.example.tourscan.ml.MobilenetFloat32
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import kotlin.math.ln

class HomeViewModel(
    private val app: Application,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val getLandmarkDataUseCase: GetLandmarkDataUseCase
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun switchModel(modelType: ModelType) {
        _uiState.update { it.copy(selectedModel = modelType) }
    }

    fun resetPhoto() {
        _uiState.update { it.copy(isAnalyzing = false, detectedLabel = null, confidence = null, inferenceTimeMs = null, landmarkData = null) }
    }

    // Modified function to accept the source of the image
    fun onPhotoCaptured(uriString: String, isFromCamera: Boolean) {
        viewModelScope.launch {
            val imageUri = uriString.toUri()

            // 1. Update UI to show analysis is in progress
            _uiState.update { it.copy(isAnalyzing = true, detectedLabel = null, confidence = null, inferenceTimeMs = null, landmarkData = null) }

            // 2. Run AI Analysis (happens for both Camera and Gallery)
            val (detectedLabel, confidence, timeMs) = withContext(Dispatchers.Default) {
                analyzeImage(imageUri)
            }

            Log.d("TourScanML", "Model: ${_uiState.value.selectedModel.displayName} | Result: $detectedLabel | Confidence: $confidence | Time: ${timeMs}ms")

            // 3. Update UI with the result
            _uiState.update {
                it.copy(isAnalyzing = false, detectedLabel = detectedLabel, confidence = confidence, inferenceTimeMs = timeMs)
            }

            // Fetch Landmark JSON Data if we have a match
            if (detectedLabel != null) {
                val data = getLandmarkDataUseCase(detectedLabel)
                _uiState.update { it.copy(landmarkData = data) }
            }

            // 4. SAVE LOGIC: Only save to DB if it comes from the Camera and was actually recognized
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

    private fun shannonEntropy(probs: Map<String, Float>): Float {
        return -probs.values
            .filter { it > 0f }
            .sumOf { p -> (p * ln(p.toDouble())).toFloat().toDouble() }
            .toFloat()
    }

    private fun analyzeImage(uri: Uri): Triple<String?, Float?, Long> {
        try {
            val contentResolver = app.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return Triple(null, null, 0)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null && bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }

            if (bitmap == null) return Triple(null, null, 0)

            val selectedModel = _uiState.value.selectedModel

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(selectedModel.inputSize, selectedModel.inputSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(127.5f, 127.5f))
                .build()

            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            val processedImage = imageProcessor.process(tensorImage)

            val labels = FileUtil.loadLabels(app, "labels.txt")
            val startTime = System.nanoTime()

            val outputBuffer = when (selectedModel) {
                ModelType.MOBILENET_V2 -> {
                    val model = MobilenetFloat32.newInstance(app)
                    val outputs = model.process(processedImage.tensorBuffer)
                    val result = outputs.outputFeature0AsTensorBuffer
                    model.close()
                    result
                }
                ModelType.EFFICIENT_NET -> {
                    val model = EfficientnetFloat32.newInstance(app)
                    val outputs = model.process(processedImage.tensorBuffer)
                    val result = outputs.outputFeature0AsTensorBuffer
                    model.close()
                    result
                }
            }

            val inferenceTimeMs = (System.nanoTime() - startTime) / 1_000_000

            val labeledProbability = TensorLabel(labels, outputBuffer).mapWithFloatValue
            val bestMatch = labeledProbability.entries.maxByOrNull { it.value }

            if (bestMatch != null) {
                val confidence = bestMatch.value
                val entropy = shannonEntropy(labeledProbability)
                val sorted = labeledProbability.entries.sortedByDescending { it.value }
                val gap = sorted[0].value - sorted[1].value

                Log.d("TourScanML", "Label: ${bestMatch.key} | Confidence: $confidence | Entropy: $entropy | Gap: $gap")

                if (confidence > 0.85f && entropy < 1.0f && gap > 0.25f) {
                    return Triple(bestMatch.key, confidence, inferenceTimeMs)
                } else {
                    Log.d("TourScanML", "REJECTED — confidence=$confidence, entropy=$entropy, gap=$gap")
                }
            }

            return Triple(null, null, inferenceTimeMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Triple(null, null, 0)
    }





}