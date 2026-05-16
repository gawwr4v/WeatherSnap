package com.gourav.weathersnap.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// this helper function turns a byte count into a readable string like 150 KB or 1.2 MB
fun formatFileSize(bytes: Long?): String {
    if (bytes == null) return "Not captured"
    val kb = bytes / 1024.0
    return if (kb < 1024) {
        "${kb.roundToInt()} KB"
    } else {
        // for larger files we show one decimal place in MB
        String.format(Locale.US, "%.1f", kb / 1024.0) + " MB"
    }
}

// converts a timestamp into a nice date and time string for the ui
fun formatSavedTime(savedAtMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US).format(Date(savedAtMillis))

// formats a double to show one decimal place, or none if it is a whole number
fun formatOneDecimal(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
