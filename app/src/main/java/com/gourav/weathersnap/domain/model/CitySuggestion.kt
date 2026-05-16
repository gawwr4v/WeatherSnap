package com.gourav.weathersnap.domain.model

// this class represents a city found when searching for suggestions
data class CitySuggestion(
    val id: Int,
    val name: String,
    val country: String,
    val adminArea: String?,
    val latitude: Double,
    val longitude: Double,
) {
    // this helper property creates a nice string for the ui like "London, England, United Kingdom"
    val displayName: String
        get() = listOfNotNull(name, adminArea, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}
