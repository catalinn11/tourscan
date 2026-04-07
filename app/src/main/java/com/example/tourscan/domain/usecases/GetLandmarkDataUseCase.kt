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
    suspend operator fun invoke(label: String): LandmarkData? = withContext(Dispatchers.IO) {
        try {
            // Replace spaces with underscores and lowercase, just in case
            val fileName = label.replace(" ", "_").lowercase() + ".json"
            val inputStream = context.assets.open("landmark_data/$fileName")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, LandmarkData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
