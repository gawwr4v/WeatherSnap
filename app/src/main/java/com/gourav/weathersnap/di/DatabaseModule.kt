package com.gourav.weathersnap.di

import android.content.Context
import androidx.room.Room
import com.gourav.weathersnap.data.local.ReportDraftDao
import com.gourav.weathersnap.data.local.SavedReportDao
import com.gourav.weathersnap.data.local.WeatherSnapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // this creates the room database instance for the app
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeatherSnapDatabase =
        Room.databaseBuilder(
            context,
            WeatherSnapDatabase::class.java,
            "weathersnap.db",
        ).build()

    // provides the dao to handle operations related to report drafts
    @Provides
    fun provideReportDraftDao(database: WeatherSnapDatabase): ReportDraftDao =
        database.reportDraftDao()

    // provides the dao for accessing saved weather reports
    @Provides
    fun provideSavedReportDao(database: WeatherSnapDatabase): SavedReportDao =
        database.savedReportDao()
}
