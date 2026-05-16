package com.gourav.weathersnap.domain.model

// this class holds all the weather info for a specific location at a specific time
data class WeatherSnapshot(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    // a readable string like "sunny" or "cloudy"
    val condition: String,
    // the original numeric code from the api
    val weatherCode: Int,
    val humidityPercent: Int,
    val windSpeedMetersPerSecond: Double,
    val pressureHPa: Double,
)
