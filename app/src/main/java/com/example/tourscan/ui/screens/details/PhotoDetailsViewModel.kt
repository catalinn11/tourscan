package com.example.tourscan.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhotoDetailsViewModel(
    private val repo: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val createdAt: Long = savedStateHandle["createdAt"]!!

    private val _uiState = MutableStateFlow<PhotoEntity?>(null)
    val uiState: StateFlow<PhotoEntity?> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = repo.getPhotoByCreatedAt(createdAt)
        }
    }
}


