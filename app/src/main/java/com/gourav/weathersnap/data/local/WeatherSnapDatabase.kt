package com.gourav.weathersnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gourav.weathersnap.data.local.entity.ReportDraftEntity
import com.gourav.weathersnap.data.local.entity.SavedReportEntity

// this is the main room database for the entire app
@Database(
    entities = [
        ReportDraftEntity::class,
        SavedReportEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WeatherSnapDatabase : RoomDatabase() {
    // access point for temporary report draft data
    abstract fun reportDraftDao(): ReportDraftDao

    // access point for the final saved reports
    abstract fun savedReportDao(): SavedReportDao
}
