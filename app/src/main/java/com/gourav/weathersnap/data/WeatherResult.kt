package com.gourav.weathersnap.data

// a simple wrapper to handle success and failure cases for our operations
sealed interface WeatherResult<out T> {
    // used when the operation is successful and contains the data
    data class Success<T>(val value: T) : WeatherResult<T>
    
    // used when something goes wrong and contains the error message
    data class Error(val message: String, val cause: Throwable? = null) : WeatherResult<Nothing>
}
