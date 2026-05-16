package com.gourav.weathersnap.domain.model

// this class represents a weather report that has been saved to the database
data class SavedReport(
    val id: Long,
    // the weather conditions that were captured
    val weather: WeatherSnapshot,
    // notes written by the user
    val notes: String,
    // where the compressed photo is stored
    val imagePath: String,
    // sizes to show how much space we saved
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    // when this report was created
    val savedAtMillis: Long,
)
