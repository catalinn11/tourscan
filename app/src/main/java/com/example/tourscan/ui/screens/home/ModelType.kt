package com.example.tourscan.ui.screens.home

enum class ModelType(
    val fileName: String,
    val displayName: String,
    val inputSize: Int
) {
    MOBILENET_V2("mobilenet_float32.tflite", "MobileNet V2", 224),
    EFFICIENT_NET("efficientnet_float32.tflite", "EfficientNet B0", 224)
}
