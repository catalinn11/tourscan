package com.example.tourscan.data.model

import com.google.gson.annotations.SerializedName

data class LandmarkData(
    @SerializedName("landmark_id") val landmarkId: String,
    @SerializedName("landmark_name") val landmarkName: String,
    @SerializedName("location") val location: String,
    @SerializedName("coordinates") val coordinates: Coordinates?,
    @SerializedName("google_maps_link") val googleMapsLink: String?,
    @SerializedName("quick_facts") val quickFacts: Map<String, String>?,
    @SerializedName("cards") val cards: List<LandmarkCard> = emptyList()
)

data class Coordinates(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

//data class QuickFacts(
//    @SerializedName("built") val built: String?,
//    @SerializedName("architecture_style") val architectureStyle: String?,
//    @SerializedName("recommended_visit_time") val recommendedVisitTime: String?,
//    @SerializedName("official_website") val officialWebsite: String?
//)

data class LandmarkCard(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
