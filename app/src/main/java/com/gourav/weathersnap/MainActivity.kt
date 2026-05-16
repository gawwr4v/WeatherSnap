package com.gourav.weathersnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gourav.weathersnap.ui.navigation.WeatherSnapNavHost
import com.gourav.weathersnap.ui.theme.WeatherSnapTheme
import dagger.hilt.android.AndroidEntryPoint

// this is the main entry point of our application
// we use the android entry point annotation so hilt can inject dependencies here
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // this makes the app content go under the status and navigation bars
        enableEdgeToEdge()
        
        setContent {
            // we wrap everything in our custom theme
            WeatherSnapTheme {
                // the nav host handles switching between different screens
                WeatherSnapNavHost()
            }
        }
    }
}
