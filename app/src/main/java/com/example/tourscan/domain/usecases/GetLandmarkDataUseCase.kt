package com.example.tourscan.domain.usecases

import android.content.Context
import com.example.tourscan.data.model.LandmarkData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetLandmarkDataUseCase(
    private val context: Context,
    private val gson: Gson
) {
    suspend operator fun invoke(label: String, languageCode: String = "ro"): LandmarkData? = withContext(Dispatchers.IO) {
        try {
            val fileName = label.replace(" ", "_").lowercase() + ".json"
            val folder = if (languageCode == "ro") "landmark_data_ro" else "landmark_data"
            val inputStream = context.assets.open("$folder/$fileName")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, LandmarkData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
