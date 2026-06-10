package com.example.tourscan.ui.screens.home

import com.example.tourscan.data.model.LandmarkData

data class HomeUiState(
    val isAnalyzing: Boolean = false,
    val detectedLabel: String? = null,
    val confidence: Float? = null,
    val selectedModel: ModelType = ModelType.MOBILENET_V2,
    val inferenceTimeMs: Long? = null,
    val landmarkData: LandmarkData? = null
)