package com.gourav.weathersnap.ui.weather

import com.gourav.weathersnap.domain.model.CitySuggestion
import com.gourav.weathersnap.domain.model.WeatherSnapshot

// this data class holds everything the weather screen needs to show
data class WeatherUiState(
    // the text currently typed in the search bar
    val query: String = "",
    // list of cities that match the search query
    val suggestions: List<CitySuggestion> = emptyList(),
    // the weather data for the city the user actually clicked on
    val selectedWeather: WeatherSnapshot? = null,
    // show a spinner when we are looking for city names
    val isLoadingSuggestions: Boolean = false,
    // show a spinner when we are fetching the actual weather details
    val isLoadingWeather: Boolean = false,
    // error message to show if something goes wrong with the api
    val errorMessage: String? = null,
    // message to show if no cities were found for the query
    val emptyMessage: String? = null,
) {
    // we only allow creating a report if we have weather data and it is not loading
    val canCreateReport: Boolean
        get() = selectedWeather != null && !isLoadingWeather
}
