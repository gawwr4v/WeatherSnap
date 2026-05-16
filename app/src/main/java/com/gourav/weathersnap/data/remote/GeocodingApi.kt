package com.gourav.weathersnap.data.remote

import com.gourav.weathersnap.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// this interface defines how we search for cities using the open meteo geocoding api
interface GeocodingApi {
    // we use a get request to search for cities by name
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        // we only want up to 8 results to keep the list short
        @Query("count") count: Int = 8,
        // results should be in english
        @Query("language") language: String = "en",
        // the api should return json format
        @Query("format") format: String = "json",
    ): GeocodingResponseDto
}
