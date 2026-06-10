package com.example.tourscan.ui.language

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val flag: String) {
    EN("en", "🇬🇧"),
    RO("ro", "🇷🇴");

    fun toggle(): AppLanguage = when (this) {
        EN -> RO
        RO -> EN
    }
}
val LocalAppLanguage = compositionLocalOf { AppLanguage.RO }

object Strings {

    private val translations: Map<AppLanguage, Map<StringKey, String>> = mapOf(
        AppLanguage.EN to mapOf(
            StringKey.SUBTITLE to "Discover the places of Romania",
            StringKey.READY_TO_SCAN to "Ready to Scan?",
            StringKey.TAP_CAMERA to "Tap the camera below to start.",

            StringKey.ANALYZING to "Analyzing...",
            StringKey.LANDMARK_NOT_RECOGNIZED to "Landmark could not be recognized",

            StringKey.CAMERA_PERMISSION_DENIED to "Camera permission denied",
            StringKey.GALLERY_PERMISSION_DENIED to "Gallery permission denied",

            // Landmark cards
            StringKey.QUICK_FACTS to "ℹ\uFE0F Quick Facts",
            StringKey.OFFICIAL_WEBSITE to "Official Website",
            StringKey.LOCATION to "\uD83D\uDCCD Location",
            StringKey.OPEN_GOOGLE_MAPS to "Open in Google Maps",
            StringKey.CLOSE to "Close",

            // PhotoListScreen
            StringKey.PHOTO_LIST to "Photo List",
            StringKey.DELETE_ALL_PHOTOS to "Delete All Photos",
            StringKey.DELETE_ALL_CONFIRM to "Are you sure you want to delete all photos? This action cannot be undone.",
            StringKey.DELETE to "Delete",
            StringKey.CANCEL to "Cancel",

            // PhotoDetailsScreen
            StringKey.PHOTO_SAVED to "Photo saved to gallery",
            StringKey.PHOTO_SAVE_FAILED to "Failed to save photo",
            StringKey.NO_DESCRIPTION to "No description.",
            StringKey.CAPTURED to "Captured",
            StringKey.AI_INFERENCE to "AI Inference",
            StringKey.DETECTION to "Detection",
            StringKey.MODEL to "Model",
            StringKey.ACCURACY to "Accuracy",
            StringKey.LOADING_PHOTO to "Loading photo…",

            // EmptyState
            StringKey.NO_PHOTOS_YET to "No photos yet",
            StringKey.TAKE_OR_IMPORT to "Take or import a photo to get started."
        ),
        AppLanguage.RO to mapOf(
            StringKey.SUBTITLE to "Descoperă locurile din România",
            StringKey.READY_TO_SCAN to "Gata de scanare?",
            StringKey.TAP_CAMERA to "Apasă camera de mai jos pentru a începe.",

            StringKey.ANALYZING to "Se analizează...",
            StringKey.LANDMARK_NOT_RECOGNIZED to "Obiectivul nu a putut fi recunoscut",

            StringKey.CAMERA_PERMISSION_DENIED to "Permisiunea camerei a fost refuzată",
            StringKey.GALLERY_PERMISSION_DENIED to "Permisiunea galeriei a fost refuzată",

            StringKey.QUICK_FACTS to "ℹ\uFE0F Informații rapide",
            StringKey.OFFICIAL_WEBSITE to "Site oficial",
            StringKey.LOCATION to "\uD83D\uDCCD Locație",
            StringKey.OPEN_GOOGLE_MAPS to "Deschide în Google Maps",
            StringKey.CLOSE to "Închide",

            StringKey.PHOTO_LIST to "Lista de poze",
            StringKey.DELETE_ALL_PHOTOS to "Șterge toate pozele",
            StringKey.DELETE_ALL_CONFIRM to "Ești sigur că vrei să ștergi toate pozele? Această acțiune nu poate fi anulată.",
            StringKey.DELETE to "Șterge",
            StringKey.CANCEL to "Anulează",

            StringKey.PHOTO_SAVED to "Poza a fost salvată în galerie",
            StringKey.PHOTO_SAVE_FAILED to "Salvarea pozei a eșuat",
            StringKey.NO_DESCRIPTION to "Fără descriere.",
            StringKey.CAPTURED to "Capturat",
            StringKey.AI_INFERENCE to "Inferență AI",
            StringKey.DETECTION to "Detecție",
            StringKey.MODEL to "Model",
            StringKey.ACCURACY to "Acuratețe",
            StringKey.LOADING_PHOTO to "Se încarcă poza…",

            StringKey.NO_PHOTOS_YET to "Nicio poză încă",
            StringKey.TAKE_OR_IMPORT to "Fă sau importă o poză pentru a începe."
        )
    )

    operator fun get(language: AppLanguage, key: StringKey): String {
        return translations[language]?.get(key) ?: translations[AppLanguage.EN]?.get(key) ?: key.name
    }
}

enum class StringKey {
    SUBTITLE,
    READY_TO_SCAN,
    TAP_CAMERA,
    ANALYZING,
    LANDMARK_NOT_RECOGNIZED,
    CAMERA_PERMISSION_DENIED,
    GALLERY_PERMISSION_DENIED,
    QUICK_FACTS,
    OFFICIAL_WEBSITE,
    LOCATION,
    OPEN_GOOGLE_MAPS,
    CLOSE,
    PHOTO_LIST,
    DELETE_ALL_PHOTOS,
    DELETE_ALL_CONFIRM,
    DELETE,
    CANCEL,
    PHOTO_SAVED,
    PHOTO_SAVE_FAILED,
    NO_DESCRIPTION,
    CAPTURED,
    AI_INFERENCE,
    DETECTION,
    MODEL,
    ACCURACY,
    LOADING_PHOTO,
    NO_PHOTOS_YET,
    TAKE_OR_IMPORT
}
