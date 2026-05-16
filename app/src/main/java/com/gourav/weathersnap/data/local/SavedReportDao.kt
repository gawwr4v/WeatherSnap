package com.gourav.weathersnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gourav.weathersnap.data.local.entity.SavedReportEntity
import kotlinx.coroutines.flow.Flow

// this dao manages the final weather reports that the user has saved
@Dao
interface SavedReportDao {
    // we get a flow of all saved reports so the list updates automatically
    // we sort them by time so the newest reports appear first
    @Query("SELECT * FROM saved_reports ORDER BY savedAtMillis DESC")
    fun observeReports(): Flow<List<SavedReportEntity>>

    // inserts a new report into the database and returns its new id
    @Insert
    suspend fun insertReport(report: SavedReportEntity): Long
}
