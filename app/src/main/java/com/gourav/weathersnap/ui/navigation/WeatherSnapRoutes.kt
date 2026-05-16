package com.gourav.weathersnap.ui.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// this object defines all the screen names and routes for navigation
object WeatherSnapRoutes {
    const val WEATHER = "weather"
    const val REPORTS = "reports"
    const val CAMERA = "camera"

    // this route requires a lot of parameters to pass weather data between screens
    const val CREATE_REPORT =
        "createReport/{cityName}/{country}/{latitude}/{longitude}/{temperature}/{condition}/{weatherCode}/{humidity}/{windSpeed}/{pressure}"

    // we define the type for each argument so the nav controller knows how to parse them
    val createReportArguments = listOf(
        navArgument("cityName") { type = NavType.StringType },
        navArgument("country") { type = NavType.StringType },
        navArgument("latitude") { type = NavType.StringType },
        navArgument("longitude") { type = NavType.StringType },
        navArgument("temperature") { type = NavType.StringType },
        navArgument("condition") { type = NavType.StringType },
        navArgument("weatherCode") { type = NavType.IntType },
        navArgument("humidity") { type = NavType.IntType },
        navArgument("windSpeed") { type = NavType.StringType },
        navArgument("pressure") { type = NavType.StringType },
    )

    // helper function to build the create report route with actual data
    fun createReportRoute(weather: WeatherSnapshot): String =
        "createReport/" +
            "${Uri.encode(weather.cityName)}/" +
            "${Uri.encode(weather.country)}/" +
            "${weather.latitude}/" +
            "${weather.longitude}/" +
            "${weather.temperatureCelsius}/" +
            "${Uri.encode(weather.condition)}/" +
            "${weather.weatherCode}/" +
            "${weather.humidityPercent}/" +
            "${weather.windSpeedMetersPerSecond}/" +
            weather.pressureHPa
}
