package com.gourav.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// this entity stores a report that is currently being created
@Entity(tableName = "report_drafts")
data class ReportDraftEntity(
    // we only keep one active draft at a time with a fixed id
    @PrimaryKey val id: Int = ACTIVE_DRAFT_ID,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val condition: String,
    val weatherCode: Int,
    val humidityPercent: Int,
    val windSpeedMetersPerSecond: Double,
    val pressureHPa: Double,
    val notes: String = "",
    // paths to the images stored on the device disk
    val originalImagePath: String? = null,
    val compressedImagePath: String? = null,
    // we track sizes to show how much space we saved by compressing
    val originalSizeBytes: Long? = null,
    val compressedSizeBytes: Long? = null,
    val updatedAtMillis: Long = System.currentTimeMillis(),
) {
    companion object {
        // the constant id for our single active draft
        const val ACTIVE_DRAFT_ID = 1
    }
}
