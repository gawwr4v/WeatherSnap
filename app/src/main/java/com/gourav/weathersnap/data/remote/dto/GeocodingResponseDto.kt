package com.gourav.weathersnap.data.remote.dto

import com.google.gson.annotations.SerializedName

// this class holds the list of cities returned by the geocoding api
data class GeocodingResponseDto(
    @SerializedName("results")
    val results: List<CitySuggestionDto> = emptyList(),
)

// this represents a single city entry from the search results
data class CitySuggestionDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("country")
    val country: String?,
    // admin1 usually refers to the state or province
    @SerializedName("admin1")
    val adminArea: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
)
