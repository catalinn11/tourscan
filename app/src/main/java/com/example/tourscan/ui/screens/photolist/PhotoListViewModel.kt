package com.example.tourscan.ui.screens.photolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.data.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    fun deleteAllPhotos() {
        viewModelScope.launch {
            // 1. Get all file paths from existing flow
            val uris = repo.getAllPhotos().first().map { it.uri }

            // 2. Delete physical files from disk
            withContext(Dispatchers.IO) {
                uris.forEach { path ->
                    File(path).delete()
                }
            }

            // 3. Clear all records from DB
            repo.deleteAllPhotos()
        }
    }
}



