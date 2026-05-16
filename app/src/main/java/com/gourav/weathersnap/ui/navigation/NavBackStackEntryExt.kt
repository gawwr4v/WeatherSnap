package com.gourav.weathersnap.ui.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// this extension function helps us extract weather data from the navigation arguments
// we use this when moving from the weather screen to the report screen
fun NavBackStackEntry.toWeatherSnapshot(): WeatherSnapshot {
    val args = requireNotNull(arguments)
    return WeatherSnapshot(
        // we decode strings because they might contain special characters or spaces
        cityName = Uri.decode(requireNotNull(args.getString("cityName"))),
        country = Uri.decode(requireNotNull(args.getString("country"))),
        latitude = requireNotNull(args.getString("latitude")).toDouble(),
        longitude = requireNotNull(args.getString("longitude")).toDouble(),
        temperatureCelsius = requireNotNull(args.getString("temperature")).toDouble(),
        condition = Uri.decode(requireNotNull(args.getString("condition"))),
        weatherCode = args.getInt("weatherCode"),
        humidityPercent = args.getInt("humidity"),
        windSpeedMetersPerSecond = requireNotNull(args.getString("windSpeed")).toDouble(),
        pressureHPa = requireNotNull(args.getString("pressure")).toDouble(),
    )
}
