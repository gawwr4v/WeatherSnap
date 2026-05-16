package com.gourav.weathersnap.ui.reports

import com.gourav.weathersnap.domain.model.SavedReport

// this simple state class holds the list of reports to show on the saved reports screen
data class SavedReportsUiState(
    // a list of all weather reports that the user has successfully saved
    val reports: List<SavedReport> = emptyList(),
)
