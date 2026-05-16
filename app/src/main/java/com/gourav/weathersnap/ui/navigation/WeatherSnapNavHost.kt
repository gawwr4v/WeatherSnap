package com.gourav.weathersnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gourav.weathersnap.ui.camera.CameraRoute
import com.gourav.weathersnap.ui.report.CreateReportRoute
import com.gourav.weathersnap.ui.reports.SavedReportsRoute
import com.gourav.weathersnap.ui.weather.WeatherRoute

// this key is used to pass the file path of a taken photo back from camera to report screen
const val CAPTURED_IMAGE_PATH_KEY = "capturedImagePath"

@Composable
fun WeatherSnapNavHost() {
    // the nav controller is the main object that manages app navigation
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WeatherSnapRoutes.WEATHER,
    ) {
        // this is the first screen the user sees
        composable(WeatherSnapRoutes.WEATHER) {
            WeatherRoute(
                onCreateReport = { weather ->
                    // when they click create report we pass the weather data in the url
                    navController.navigate(WeatherSnapRoutes.createReportRoute(weather))
                },
                onOpenReports = {
                    navController.navigate(WeatherSnapRoutes.REPORTS)
                },
            )
        }

        // the screen where users write notes and add a photo
        composable(
            route = WeatherSnapRoutes.CREATE_REPORT,
            arguments = WeatherSnapRoutes.createReportArguments,
        ) { backStackEntry ->
            // we check if a photo path was sent back from the camera screen
            val capturedImagePath by backStackEntry.savedStateHandle
                .getStateFlow<String?>(CAPTURED_IMAGE_PATH_KEY, null)
                .collectAsStateWithLifecycle()

            CreateReportRoute(
                // we convert the url parameters back into a weather snapshot object
                weather = backStackEntry.toWeatherSnapshot(),
                capturedImagePath = capturedImagePath,
                onCapturedImageConsumed = {
                    // once we use the image path we clear it so it doesnt stay there forever
                    backStackEntry.savedStateHandle.remove<String>(CAPTURED_IMAGE_PATH_KEY)
                },
                onBack = {
                    navController.popBackStack()
                },
                onCapturePhoto = {
                    navController.navigate(WeatherSnapRoutes.CAMERA)
                },
                onSaved = {
                    // after saving we go to the reports list and clear the stack back to the start
                    navController.navigate(WeatherSnapRoutes.REPORTS) {
                        popUpTo(WeatherSnapRoutes.WEATHER)
                    }
                },
            )
        }

        // the camera screen for taking evidence photos
        composable(WeatherSnapRoutes.CAMERA) {
            CameraRoute(
                onClose = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { path ->
                    // we save the path of the new photo in the previous screen state handle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(CAPTURED_IMAGE_PATH_KEY, path)
                    navController.popBackStack()
                },
            )
        }

        // the screen showing the list of all saved weather reports
        composable(WeatherSnapRoutes.REPORTS) {
            SavedReportsRoute(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
