package com.gourav.weathersnap.data.repository

import com.gourav.weathersnap.data.local.ReportDraftDao
import com.gourav.weathersnap.data.local.SavedReportDao
import com.gourav.weathersnap.data.local.entity.toDraftEntity
import com.gourav.weathersnap.data.local.entity.toReportDraft
import com.gourav.weathersnap.data.local.entity.toSavedReport
import com.gourav.weathersnap.data.local.entity.toSavedReportEntity
import com.gourav.weathersnap.domain.model.ReportDraft
import com.gourav.weathersnap.domain.model.SavedReport
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// this class connects our local database with the rest of the app for reporting
@Singleton
class RoomReportRepository @Inject constructor(
    private val draftDao: ReportDraftDao,
    private val reportDao: SavedReportDao,
) : ReportRepository {

    // returns a flow of the current draft so the ui can update as the user edits
    override fun observeDraft(): Flow<ReportDraft?> =
        draftDao.observeDraft().map { it?.toReportDraft() }

    // returns a flow of all successfully saved reports
    override fun observeReports(): Flow<List<SavedReport>> =
        reportDao.observeReports().map { reports -> reports.map { it.toSavedReport() } }

    // sets up a new draft when a user picks a city and its weather
    override suspend fun startDraft(weather: WeatherSnapshot) {
        val currentDraft = draftDao.getDraft()
        
        // if the weather is the same we dont need to restart the draft
        if (currentDraft?.cityName == weather.cityName &&
            currentDraft.country == weather.country &&
            currentDraft.latitude == weather.latitude &&
            currentDraft.longitude == weather.longitude &&
            currentDraft.temperatureCelsius == weather.temperatureCelsius &&
            currentDraft.condition == weather.condition &&
            currentDraft.weatherCode == weather.weatherCode &&
            currentDraft.humidityPercent == weather.humidityPercent &&
            currentDraft.windSpeedMetersPerSecond == weather.windSpeedMetersPerSecond &&
            currentDraft.pressureHPa == weather.pressureHPa
        ) {
            return
        }

        // if we are starting a fresh draft we delete any old temporary images
        deleteDraftFiles(currentDraft?.originalImagePath, currentDraft?.compressedImagePath)
        draftDao.upsertDraft(weather.toDraftEntity())
    }

    // simple update for the notes the user writes
    override suspend fun updateDraftNotes(notes: String) {
        draftDao.updateNotes(notes)
    }

    // updates the draft with info about the photo that was just taken
    override suspend fun updateDraftPhoto(
        originalImagePath: String,
        compressedImagePath: String,
        originalSizeBytes: Long,
        compressedSizeBytes: Long,
    ) {
        val currentDraft = draftDao.getDraft()
        // remove any previous photos to save disk space
        deleteDraftFiles(currentDraft?.originalImagePath, currentDraft?.compressedImagePath)
        draftDao.updatePhoto(
            originalImagePath = originalImagePath,
            compressedImagePath = compressedImagePath,
            originalSizeBytes = originalSizeBytes,
            compressedSizeBytes = compressedSizeBytes,
        )
    }

    // takes the current draft and moves it to the permanent reports table
    override suspend fun saveDraft(): Boolean {
        val draft = draftDao.getDraft() ?: return false
        // we can only save if a photo has been taken
        if (draft.compressedImagePath == null ||
            draft.originalSizeBytes == null ||
            draft.compressedSizeBytes == null
        ) {
            return false
        }

        // insert into saved reports and clear the draft
        reportDao.insertReport(draft.toSavedReportEntity(savedAtMillis = System.currentTimeMillis()))
        draftDao.deleteDraft()
        // we delete the original heavy photo and keep only the compressed one
        deleteFile(draft.originalImagePath)
        return true
    }

    // clears everything in the current draft and deletes its images
    override suspend fun discardDraft() {
        val draft = draftDao.getDraft()
        draftDao.deleteDraft()
        deleteDraftFiles(draft?.originalImagePath, draft?.compressedImagePath)
    }

    // helper to cleanup image files on the background thread
    private suspend fun deleteDraftFiles(
        originalPath: String?,
        compressedPath: String?,
    ) = withContext(Dispatchers.IO) {
        deleteFile(originalPath)
        deleteFile(compressedPath)
    }

    // checks if a file exists before trying to delete it
    private fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        File(path).takeIf { it.exists() }?.delete()
    }
}
