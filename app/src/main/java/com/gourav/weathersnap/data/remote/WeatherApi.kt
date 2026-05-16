package com.gourav.weathersnap.data.remote

import com.gourav.weathersnap.data.remote.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// this interface defines how we talk to the open meteo weather api
interface WeatherApi {
    // we use a get request to fetch the weather for specific coordinates
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        // we specify which weather data fields we want in the response
        @Query("current") current: String = CURRENT_FIELDS,
        // we want wind speed in meters per second
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
    ): ForecastResponseDto

    companion object {
        // these are the specific fields we care about like temp and wind speed
        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,weather_code,surface_pressure,wind_speed_10m"
    }
}
