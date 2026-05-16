package com.gourav.weathersnap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// this is the base application class for our app
// the hilt annotation here is needed to trigger the dependency injection code generation
@HiltAndroidApp
class WeatherSnapApp : Application()
