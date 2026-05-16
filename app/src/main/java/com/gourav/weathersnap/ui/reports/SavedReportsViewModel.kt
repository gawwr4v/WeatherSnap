package com.gourav.weathersnap.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gourav.weathersnap.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    reportRepository: ReportRepository,
) : ViewModel() {

    // we observe the list of reports from the repository and convert it to ui state
    val uiState: StateFlow<SavedReportsUiState> =
        reportRepository.observeReports()
            .map { SavedReportsUiState(reports = it) }
            // stateIn converts a cold flow into a hot state flow that the ui can collect
            .stateIn(
                scope = viewModelScope,
                // the flow stays active for 5 seconds after the last ui collector leaves
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SavedReportsUiState(),
            )
}
