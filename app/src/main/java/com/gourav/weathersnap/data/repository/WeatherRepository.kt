package com.gourav.weathersnap.data.repository

import com.gourav.weathersnap.data.WeatherResult
import com.gourav.weathersnap.domain.model.CitySuggestion
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// this interface defines what our weather data source should be able to do
interface WeatherRepository {
    // search for cities that match a given name
    suspend fun searchCities(query: String): WeatherResult<List<CitySuggestion>>

    // get the current weather conditions for a specific city
    suspend fun getWeather(city: CitySuggestion): WeatherResult<WeatherSnapshot>
}
