package com.example.tourscan.domain.usecases

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.tourscan.data.local.PhotoEntity
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.utils.FileUtil

class SavePhotoUseCase(
    private val context: Context,
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(uri: Uri, description: String? = null, model: String, accuracy: Float): String {
        val remoteUrl = FileUtil.uploadToSupabase(context, uri)
        Log.d("TourScan", "Supabase URL stored: $remoteUrl")

        repository.insertPhoto(
            PhotoEntity(
                uri = remoteUrl,
                description = description,
                model = model,
                accuracy = accuracy,
                analyzed = true
            )
        )

        return remoteUrl
    }
}