package com.example.tourscan.ui.screens.home

enum class ModelType(
    val fileName: String,
    val displayName: String,
    val inputSize: Int,
    val temperature: Float,
    val energyThreshold: Float,
    val softmaxMin: Float,
    val gapMin: Float
) {
    MOBILENET_V2(
        fileName = "mobilenet_int8.tflite",
        displayName = "MobileNet V2",
        inputSize = 224,
        temperature = 0.4020f,
        energyThreshold = -1.8987f,
        softmaxMin = 0.50f,
        gapMin = 0.15f
    ),
    EFFICIENT_NET(
        fileName = "efficientnet_int8.tflite",
        displayName = "EfficientNet B0",
        inputSize = 224,
        temperature = 0.4010f,
        energyThreshold = -2.2416f,
        softmaxMin = 0.50f,
        gapMin = 0.15f
    )
}
