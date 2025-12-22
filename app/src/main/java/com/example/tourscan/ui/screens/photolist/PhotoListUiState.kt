package com.example.tourscan.ui.screens.photolist

import com.example.tourscan.data.local.PhotoEntity

data class PhotoListUiState(
    val loading: Boolean = true,
    val photos: List<PhotoEntity> = emptyList()
)
