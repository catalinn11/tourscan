package com.example.tourscan.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.domain.usecases.SavePhotoUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val savePhotoUseCase: SavePhotoUseCase
) : ViewModel() {

    fun savePhoto(uri: String) {
        viewModelScope.launch {
            try {
                // Convert string to uri
                val imageUri = Uri.parse(uri)

                // Call UseCase to save on file storage & database
                savePhotoUseCase(imageUri)
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.printStackTrace().toString())
            }
        }
    }

}

