package com.example.tourscan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        // Open source image
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Create a new file in app files
        // "filesDir" safe location until app uninstall
        val fileName = "tourscan_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        val outputStream = FileOutputStream(file)
        // 90% quality compression
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()

        // Return image path (ex: /data/data/com.app/files/tourscan_123.jpg)
        return@withContext file.absolutePath
    }
}