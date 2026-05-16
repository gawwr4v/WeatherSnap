package com.gourav.weathersnap.domain.model

// this class represents the current state of a report being written
data class ReportDraft(
    // the weather information associated with this report
    val weather: WeatherSnapshot,
    // the text notes the user has added
    val notes: String,
    // disk paths for the photos
    val originalImagePath: String?,
    val compressedImagePath: String?,
    // file sizes to track optimization
    val originalSizeBytes: Long?,
    val compressedSizeBytes: Long?,
)
