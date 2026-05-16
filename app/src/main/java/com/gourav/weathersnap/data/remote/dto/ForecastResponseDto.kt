package com.gourav.weathersnap.data.remote.dto

import com.google.gson.annotations.SerializedName

// this class represents the main response we get from the weather api
data class ForecastResponseDto(
    // the api returns a current object containing all the weather details
    @SerializedName("current")
    val current: CurrentWeatherDto?,
)

// this holds the specific weather measurements for a location
data class CurrentWeatherDto(
    @SerializedName("temperature_2m")
    val temperatureCelsius: Double?,
    @SerializedName("relative_humidity_2m")
    val humidityPercent: Int?,
    // weather code tells us if it is sunny, rainy, etc based on wmo codes
    @SerializedName("weather_code")
    val weatherCode: Int?,
    @SerializedName("surface_pressure")
    val pressureHPa: Double?,
    @SerializedName("wind_speed_10m")
    val windSpeedMetersPerSecond: Double?,
)
