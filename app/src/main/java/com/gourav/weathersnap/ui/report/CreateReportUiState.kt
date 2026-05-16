package com.gourav.weathersnap.ui.report

import com.gourav.weathersnap.domain.model.ReportDraft

// this state holds everything needed for the report creation screen
data class CreateReportUiState(
    // the current draft being edited, contains weather and photo info
    val draft: ReportDraft? = null,
    // true while the image compressor is working on a new photo
    val isCompressing: Boolean = false,
    // true while we are saving the final report to the database
    val isSaving: Boolean = false,
    // any error message to show to the user
    val errorMessage: String? = null,
) {
    // we only allow saving if there is a compressed image and nothing is busy
    val canSave: Boolean
        get() = draft?.compressedImagePath != null && !isCompressing && !isSaving
}

// events that we send from the viewmodel to the ui
sealed interface CreateReportEvent {
    // triggered when the report is successfully saved so the ui can navigate away
    data object Saved : CreateReportEvent
}
