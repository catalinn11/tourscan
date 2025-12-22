package com.example.tourscan.ui.screens.photolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoListViewModel(private val repo: PhotoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoListUiState())
    val uiState: StateFlow<PhotoListUiState> = _uiState

    init {
        viewModelScope.launch {
            repo.getAllPhotos().collect { list ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    photos = list
                )
            }
        }
    }
}



