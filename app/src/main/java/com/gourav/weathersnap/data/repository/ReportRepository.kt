package com.gourav.weathersnap.data.repository

import com.gourav.weathersnap.domain.model.ReportDraft
import com.gourav.weathersnap.domain.model.SavedReport
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

// this interface defines how we manage weather reports and drafts
interface ReportRepository {
    // observe the current draft being edited
    fun observeDraft(): Flow<ReportDraft?>

    // get a list of all reports that have been saved
    fun observeReports(): Flow<List<SavedReport>>

    // start a new draft with the provided weather data
    suspend fun startDraft(weather: WeatherSnapshot)

    // update the notes section of the current draft
    suspend fun updateDraftNotes(notes: String)

    // update the photo details in the draft after compression
    suspend fun updateDraftPhoto(
        originalImagePath: String,
        compressedImagePath: String,
        originalSizeBytes: Long,
        compressedSizeBytes: Long,
    )

    // move the draft to saved reports and clear it from the database
    suspend fun saveDraft(): Boolean

    // delete the current draft and its associated image files
    suspend fun discardDraft()
}
