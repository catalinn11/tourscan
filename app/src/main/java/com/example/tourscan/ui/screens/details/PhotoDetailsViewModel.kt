package com.example.tourscan.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.model.LandmarkData
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.domain.usecases.GetLandmarkDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhotoDetailsUiState(
    val photo: PhotoEntity? = null,
    val landmarkData: LandmarkData? = null
)

class PhotoDetailsViewModel(
    private val repo: PhotoRepository,
    private val getLandmarkDataUseCase: GetLandmarkDataUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val createdAt: Long = savedStateHandle["createdAt"]!!

    private val _uiState = MutableStateFlow(PhotoDetailsUiState())
    val uiState: StateFlow<PhotoDetailsUiState> = _uiState

    init {
        viewModelScope.launch {
            val photoEntity = repo.getPhotoByCreatedAt(createdAt)
            _uiState.update { it.copy(photo = photoEntity) }

            if (photoEntity?.description != null) {
                val data = getLandmarkDataUseCase(photoEntity.description)
                _uiState.update { it.copy(landmarkData = data) }
            }
        }
    }

    fun deletePhoto(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repo.deletePhoto(createdAt)
            onDeleted()
        }
    }
}
