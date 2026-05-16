package com.gourav.weathersnap.data.remote.dto

import com.gourav.weathersnap.domain.model.CitySuggestion
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// this function converts a city dto from the api into our domain model
fun CitySuggestionDto.toCitySuggestion(): CitySuggestion? {
    // we make sure all required fields are present
    val safeId = id ?: return null
    val safeName = name?.trim().orEmpty()
    val safeCountry = country?.trim().orEmpty()
    val safeLatitude = latitude ?: return null
    val safeLongitude = longitude ?: return null

    // if name or country is missing we cant really use this result
    if (safeName.isBlank() || safeCountry.isBlank()) return null

    return CitySuggestion(
        id = safeId,
        name = safeName,
        country = safeCountry,
        adminArea = adminArea?.trim()?.takeIf { it.isNotBlank() },
        latitude = safeLatitude,
        longitude = safeLongitude,
    )
}

// this function transforms the raw weather data into a clean snapshot
fun ForecastResponseDto.toWeatherSnapshot(city: CitySuggestion): WeatherSnapshot? {
    // we extract the current weather object and check if all data points are there
    val safeCurrent = current ?: return null
    val safeTemperature = safeCurrent.temperatureCelsius ?: return null
    val safeHumidity = safeCurrent.humidityPercent ?: return null
    val safeWeatherCode = safeCurrent.weatherCode ?: return null
    val safePressure = safeCurrent.pressureHPa ?: return null
    val safeWindSpeed = safeCurrent.windSpeedMetersPerSecond ?: return null

    return WeatherSnapshot(
        cityName = city.name,
        country = city.country,
        latitude = city.latitude,
        longitude = city.longitude,
        temperatureCelsius = safeTemperature,
        // we convert the numeric weather code into a readable string
        condition = weatherCodeToCondition(safeWeatherCode),
        weatherCode = safeWeatherCode,
        humidityPercent = safeHumidity,
        windSpeedMetersPerSecond = safeWindSpeed,
        pressureHPa = safePressure,
    )
}

// this maps the wmo weather codes to actual weather descriptions
fun weatherCodeToCondition(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing rain"
    71, 73, 75 -> "Snow"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "Unknown"
}
