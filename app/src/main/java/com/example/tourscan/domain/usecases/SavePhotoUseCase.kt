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
    suspend operator fun invoke(uri: Uri, description: String? = null) {
        // Upload to Supabase Storage and get public URL
        val remoteUrl = FileUtil.uploadToSupabase(context, uri)
        Log.d("TourScan", "Supabase URL stored: $remoteUrl")

        val photoEntity = PhotoEntity(
            uri = remoteUrl,  // Supabase public URL
            description = description,
            analyzed = true
        )

        // Insert in Room
        repository.insertPhoto(photoEntity)
    }
}