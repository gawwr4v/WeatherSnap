package com.gourav.weathersnap.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gourav.weathersnap.data.file.ImageCompressor
import com.gourav.weathersnap.data.repository.ReportRepository
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val imageCompressor: ImageCompressor,
) : ViewModel() {

    // this holds the screen data like the current draft and loading states
    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    // we use a shared flow for one time events like navigation after saving
    private val _events = MutableSharedFlow<CreateReportEvent>()
    val events: SharedFlow<CreateReportEvent> = _events.asSharedFlow()

    init {
        // we start observing the draft as soon as the viewmodel is created
        viewModelScope.launch {
            reportRepository.observeDraft().collect { draft ->
                _uiState.update { it.copy(draft = draft) }
            }
        }
    }

    // initializes the report draft with the chosen city weather
    fun startDraft(weather: WeatherSnapshot) {
        viewModelScope.launch {
            reportRepository.startDraft(weather)
        }
    }

    // called whenever the user types in the notes text field
    fun onNotesChanged(notes: String) {
        // we update the local state immediately for a responsive ui
        _uiState.update { state ->
            state.copy(draft = state.draft?.copy(notes = notes))
        }
        // then we save it to the database in the background
        viewModelScope.launch {
            reportRepository.updateDraftNotes(notes)
        }
    }

    // triggered after the user successfully takes a photo with the camera
    fun onPhotoCaptured(originalPath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCompressing = true, errorMessage = null) }
            runCatching { 
                // we compress the high quality photo to save space
                imageCompressor.compress(originalPath) 
            }
                .onSuccess { compressedImage ->
                    // save the paths and sizes of the images to the draft
                    reportRepository.updateDraftPhoto(
                        originalImagePath = compressedImage.originalPath,
                        compressedImagePath = compressedImage.compressedPath,
                        originalSizeBytes = compressedImage.originalSizeBytes,
                        compressedSizeBytes = compressedImage.compressedSizeBytes,
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not compress the captured photo.")
                    }
                }
            _uiState.update { it.copy(isCompressing = false) }
        }
    }

    // finalizes the report and saves it to the permanent list
    fun saveReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val saved = reportRepository.saveDraft()
            _uiState.update { it.copy(isSaving = false) }

            if (saved) {
                // tell the ui that we are done so it can go to the list screen
                _events.emit(CreateReportEvent.Saved)
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Capture a photo before saving this report.")
                }
            }
        }
    }

    // clears any error message shown at the top of the screen
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
