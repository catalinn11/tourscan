package com.example.tourscan.ui.screens.photolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.tourscan.data.remote.SupabaseClient
import com.example.tourscan.data.repository.PhotoRepository
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoListViewModel(
    private val app: Application,
    private val repo: PhotoRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(PhotoListUiState())
    val uiState: StateFlow<PhotoListUiState> = _uiState

    init {
        viewModelScope.launch {
            repo.getAllPhotos().collect { list ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    imagesReady = false,
                    photos = list
                )

                // Preload
                if (list.isNotEmpty()) {
                    preloadImages(list.map { it.uri })
                } else {
                    _uiState.value = _uiState.value.copy(imagesReady = true)
                }
            }
        }
    }

    private suspend fun preloadImages(urls: List<String>) {
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(app)
            urls.map { url ->
                async {
                    try {
                        val request = ImageRequest.Builder(app)
                            .data(url)
                            .build()
                        loader.execute(request)
                    } catch (_: Exception) { }
                }
            }.awaitAll()
        }
        _uiState.value = _uiState.value.copy(imagesReady = true)
    }

    fun deletePhoto(createdAt: Long) {
        viewModelScope.launch {
            val photo = repo.getAllPhotos().first().find { it.createdAt == createdAt } ?: return@launch

            withContext(Dispatchers.IO) {
                try {
                    val bucket = SupabaseClient.client.storage.from(SupabaseClient.BUCKET_NAME)
                    val filePath = photo.uri.substringAfter("${SupabaseClient.BUCKET_NAME}/", "").takeIf { it.isNotEmpty() }
                    if (filePath != null) {
                        bucket.delete(listOf(filePath))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            repo.deletePhoto(createdAt)
        }
    }

    fun deleteAllPhotos() {
        viewModelScope.launch {
            val urls = repo.getAllPhotos().first().map { it.uri }

            withContext(Dispatchers.IO) {
                try {
                    val bucket = SupabaseClient.client.storage.from(SupabaseClient.BUCKET_NAME)
                    val filePaths = urls.mapNotNull { url ->
                        // tourscan_images/deviceId/file.jpg 
                        url.substringAfter("${SupabaseClient.BUCKET_NAME}/", "").takeIf { it.isNotEmpty() }
                    }
                    if (filePaths.isNotEmpty()) {
                        bucket.delete(filePaths)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. Clear all records from DB
            repo.deleteAllPhotos()
        }
    }
}




