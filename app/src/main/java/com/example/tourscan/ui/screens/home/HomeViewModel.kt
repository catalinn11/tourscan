package com.example.tourscan.ui.screens.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.domain.usecases.SavePhotoUseCase
import com.example.tourscan.ml.Mobilenetv2
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

class HomeViewModel(
    private val app: Application,
    private val savePhotoUseCase: SavePhotoUseCase
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Modified function to accept the source of the image
    fun onPhotoCaptured(uriString: String, isFromCamera: Boolean) {
        viewModelScope.launch {
            val imageUri = uriString.toUri()

            // 1. Update UI to show analysis is in progress
            _uiState.update { it.copy(isAnalyzing = true, detectedLabel = null) }

            // 2. Run AI Analysis (happens for both Camera and Gallery)
            val detectedLabel = withContext(Dispatchers.Default) {
                analyzeImage(imageUri)
            }

            Log.d("TourScanML", "Inference Result: $detectedLabel")

            // 3. Update UI with the result
            _uiState.update {
                it.copy(isAnalyzing = false, detectedLabel = detectedLabel)
            }

            // 4. SAVE LOGIC: Only save to DB if it comes from the Camera
            if (isFromCamera) {
                // Pass the detected label as the description
                savePhotoUseCase(imageUri, description = detectedLabel)
            }
        }
    }

    private fun analyzeImage(uri: Uri): String? {
        // ... (analysis logic remains unchanged) ...
        try {
            val contentResolver = app.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null && bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }

            if (bitmap == null) return null

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(127.5f, 127.5f))
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val model = Mobilenetv2.newInstance(app)
            val outputs = model.process(tensorImage.tensorBuffer)
            val outputBuffer = outputs.outputFeature0AsTensorBuffer
            val labels = FileUtil.loadLabels(app, "labels.txt")
            val labeledProbability = TensorLabel(labels, outputBuffer).mapWithFloatValue
            model.close()

            val bestMatch = labeledProbability.entries.maxByOrNull { it.value }

            if (bestMatch != null && bestMatch.value > 0.5f) {
                return bestMatch.key
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}