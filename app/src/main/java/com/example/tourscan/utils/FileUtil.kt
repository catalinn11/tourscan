package com.example.tourscan.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import com.example.tourscan.data.remote.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object FileUtil {

    @SuppressLint("HardwareIds")
    suspend fun uploadToSupabase(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Full quality, no resize (50MB bucket limit)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()

        bitmap.recycle()

        // Upload to Supabase Storage (in device-specific folder)
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val fileName = "$deviceId/tourscan_${UUID.randomUUID()}.jpg"
        val bucket = SupabaseClient.client.storage.from(SupabaseClient.BUCKET_NAME)
        bucket.upload(fileName, imageBytes)

        // Return public URL
        bucket.publicUrl(fileName)
    }

    suspend fun saveImageToGallery(context: Context, imageSource: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get image bytes from URL or local file
            val imageBytes: ByteArray = if (imageSource.startsWith("http")) {
                java.net.URL(imageSource).readBytes()
            } else {
                val file = File(imageSource)
                if (!file.exists()) return@withContext false
                file.readBytes()
            }

            val fileName = "TourScan_${System.currentTimeMillis()}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TourScan")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val imageUri = context.contentResolver.insert(collection, contentValues)
                ?: return@withContext false

            context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                outputStream.write(imageBytes)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(imageUri, contentValues, null, null)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

