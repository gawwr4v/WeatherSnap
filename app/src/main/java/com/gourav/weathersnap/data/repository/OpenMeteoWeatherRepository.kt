package com.gourav.weathersnap.data.repository

import com.gourav.weathersnap.data.WeatherResult
import com.gourav.weathersnap.data.remote.GeocodingApi
import com.gourav.weathersnap.data.remote.WeatherApi
import com.gourav.weathersnap.data.remote.dto.toCitySuggestion
import com.gourav.weathersnap.data.remote.dto.toWeatherSnapshot
import com.gourav.weathersnap.domain.model.CitySuggestion
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import javax.inject.Inject
import javax.inject.Singleton

// this implementation uses the open meteo api to get real weather data
@Singleton
class OpenMeteoWeatherRepository @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
) : WeatherRepository {

    // we keep a simple cache to avoid making the same network call multiple times
    private val suggestionCache = mutableMapOf<String, List<CitySuggestion>>()

    override suspend fun searchCities(query: String): WeatherResult<List<CitySuggestion>> {
        val cacheKey = query.trim().lowercase()
        // we dont search for very short names to save api hits
        if (cacheKey.length <= 2) return WeatherResult.Success(emptyList())

        // if we already searched for this name just return the cached list
        suggestionCache[cacheKey]?.let { cachedSuggestions ->
            return WeatherResult.Success(cachedSuggestions)
        }

        return runCatching {
            // call the geocoding api and convert the results to our domain model
            geocodingApi.searchCities(name = cacheKey)
                .results
                .mapNotNull { it.toCitySuggestion() }
                .distinctBy { it.id }
        }.fold(
            onSuccess = { suggestions ->
                // save the results in our cache for later
                suggestionCache[cacheKey] = suggestions
                WeatherResult.Success(suggestions)
            },
            onFailure = { error ->
                // if the network fails we return a friendly error message
                WeatherResult.Error(
                    message = "Could not load city suggestions. Check your connection and try again.",
                    cause = error,
                )
            },
        )
    }

    override suspend fun getWeather(city: CitySuggestion): WeatherResult<WeatherSnapshot> {
        return runCatching {
            // call the weather api using the coordinates of the selected city
            weatherApi.getCurrentWeather(
                latitude = city.latitude,
                longitude = city.longitude,
            ).toWeatherSnapshot(city)
        }.fold(
            onSuccess = { weather ->
                if (weather == null) {
                    WeatherResult.Error("Weather data was incomplete for ${city.displayName}.")
                } else {
                    WeatherResult.Success(weather)
                }
            },
            onFailure = { error ->
                // show a message if we cant get the weather details
                WeatherResult.Error(
                    message = "Could not load weather for ${city.displayName}. Try again.",
                    cause = error,
                )
            },
        )
    }
}
