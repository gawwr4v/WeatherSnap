package com.gourav.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gourav.weathersnap.data.WeatherResult
import com.gourav.weathersnap.data.repository.WeatherRepository
import com.gourav.weathersnap.domain.model.CitySuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    // private state that we modify internally
    private val _uiState = MutableStateFlow(WeatherUiState())
    // public state that the ui listens to
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // keep track of the search job so we can cancel it if the user types fast
    private var suggestionJob: Job? = null
    // keep track of the weather fetch job
    private var weatherJob: Job? = null

    // triggered every time the user types in the search box
    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                selectedWeather = null,
                errorMessage = null,
                emptyMessage = null,
            )
        }

        // cancel the previous search attempt if it is still running
        suggestionJob?.cancel()

        // we only start looking for cities if there are more than 2 letters
        if (query.trim().length <= 2) {
            _uiState.update {
                it.copy(
                    suggestions = emptyList(),
                    isLoadingSuggestions = false,
                    emptyMessage = null,
                )
            }
            return
        }

        // wait for a short period before calling the api to avoid unnecessary requests
        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            loadSuggestions(query)
        }
    }

    // triggered when the user clicks the search icon or presses enter
    fun onSearchClicked() {
        val query = uiState.value.query
        if (query.trim().length <= 2) {
            _uiState.update {
                it.copy(
                    suggestions = emptyList(),
                    emptyMessage = "Enter more than 2 letters to start city suggestions.",
                    errorMessage = null,
                )
            }
            return
        }

        // if they click search we want results immediately so we cancel any pending debounce
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch {
            loadSuggestions(query, selectFirstSuggestion = true)
        }
    }

    // called when the user selects a city from the dropdown list
    fun onSuggestionSelected(city: CitySuggestion) {
        suggestionJob?.cancel()
        _uiState.update {
            it.copy(
                query = city.displayName,
                suggestions = emptyList(),
                isLoadingSuggestions = false,
                selectedWeather = null,
                errorMessage = null,
                emptyMessage = null,
            )
        }
        // fetch the actual weather data for the selected city
        loadWeather(city)
    }

    // removes any error or info message from the screen
    fun dismissMessage() {
        _uiState.update { it.copy(errorMessage = null, emptyMessage = null) }
    }

    // internal function to fetch city suggestions from the repository
    private suspend fun loadSuggestions(
        query: String,
        selectFirstSuggestion: Boolean = false,
    ) {
        _uiState.update {
            it.copy(
                isLoadingSuggestions = true,
                errorMessage = null,
                emptyMessage = null,
            )
        }

        when (val result = weatherRepository.searchCities(query)) {
            is WeatherResult.Success -> {
                val suggestions = result.value
                _uiState.update {
                    it.copy(
                        suggestions = if (selectFirstSuggestion) emptyList() else suggestions,
                        isLoadingSuggestions = false,
                        emptyMessage = if (suggestions.isEmpty()) {
                            "No city suggestions found for \"$query\"."
                        } else {
                            null
                        },
                    )
                }

                // if we are doing a direct search then automatically pick the first result
                if (selectFirstSuggestion && suggestions.isNotEmpty()) {
                    onSuggestionSelected(suggestions.first())
                }
            }

            is WeatherResult.Error -> {
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isLoadingSuggestions = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    // internal function to fetch weather details for a specific city
    private fun loadWeather(city: CitySuggestion) {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingWeather = true,
                    errorMessage = null,
                    emptyMessage = null,
                )
            }

            when (val result = weatherRepository.getWeather(city)) {
                is WeatherResult.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedWeather = result.value,
                            isLoadingWeather = false,
                        )
                    }
                }

                is WeatherResult.Error -> {
                    _uiState.update {
                        it.copy(
                            selectedWeather = null,
                            isLoadingWeather = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        // time to wait after the last key press before fetching suggestions
        const val SUGGESTION_DEBOUNCE_MS = 300L
    }
}
