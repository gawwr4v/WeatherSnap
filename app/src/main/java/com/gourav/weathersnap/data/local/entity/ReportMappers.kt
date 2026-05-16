package com.gourav.weathersnap.data.local.entity

import com.gourav.weathersnap.domain.model.ReportDraft
import com.gourav.weathersnap.domain.model.SavedReport
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// converts a weather snapshot into a database entity so we can save it as a draft
fun WeatherSnapshot.toDraftEntity(notes: String = ""): ReportDraftEntity =
    ReportDraftEntity(
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperatureCelsius = temperatureCelsius,
        condition = condition,
        weatherCode = weatherCode,
        humidityPercent = humidityPercent,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        pressureHPa = pressureHPa,
        notes = notes,
    )

// converts the database draft back into our domain model used by the ui
fun ReportDraftEntity.toReportDraft(): ReportDraft =
    ReportDraft(
        weather = toWeatherSnapshot(),
        notes = notes,
        originalImagePath = originalImagePath,
        compressedImagePath = compressedImagePath,
        originalSizeBytes = originalSizeBytes,
        compressedSizeBytes = compressedSizeBytes,
    )

// takes a draft and turns it into a permanent saved report entity
fun ReportDraftEntity.toSavedReportEntity(savedAtMillis: Long): SavedReportEntity =
    SavedReportEntity(
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperatureCelsius = temperatureCelsius,
        condition = condition,
        weatherCode = weatherCode,
        humidityPercent = humidityPercent,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        pressureHPa = pressureHPa,
        notes = notes,
        // we require the image to be present before saving
        imagePath = requireNotNull(compressedImagePath),
        originalSizeBytes = requireNotNull(originalSizeBytes),
        compressedSizeBytes = requireNotNull(compressedSizeBytes),
        savedAtMillis = savedAtMillis,
    )

// converts a saved report from the database into a domain model for display
fun SavedReportEntity.toSavedReport(): SavedReport =
    SavedReport(
        id = id,
        weather = toWeatherSnapshot(),
        notes = notes,
        imagePath = imagePath,
        originalSizeBytes = originalSizeBytes,
        compressedSizeBytes = compressedSizeBytes,
        savedAtMillis = savedAtMillis,
    )

// private helper to extract weather info from a draft entity
private fun ReportDraftEntity.toWeatherSnapshot(): WeatherSnapshot =
    WeatherSnapshot(
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperatureCelsius = temperatureCelsius,
        condition = condition,
        weatherCode = weatherCode,
        humidityPercent = humidityPercent,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        pressureHPa = pressureHPa,
    )

// private helper to extract weather info from a saved report entity
private fun SavedReportEntity.toWeatherSnapshot(): WeatherSnapshot =
    WeatherSnapshot(
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperatureCelsius = temperatureCelsius,
        condition = condition,
        weatherCode = weatherCode,
        humidityPercent = humidityPercent,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        pressureHPa = pressureHPa,
    )
