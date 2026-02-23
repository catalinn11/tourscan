package com.example.tourscan.domain.usecases

import android.content.Context
import android.net.Uri
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.utils.FileUtil

class SavePhotoUseCase(
    private val context: Context,
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(uri: Uri, description: String? = null) {
        // Save file to internal storage
        val internalPath = FileUtil.saveImageToInternalStorage(context, uri)

        val photoEntity = PhotoEntity(
            uri = internalPath, // Path for image
            description = description, // Inference result
            analyzed = true
        )

        // Insert in Room
        repository.insertPhoto(photoEntity)
    }
}