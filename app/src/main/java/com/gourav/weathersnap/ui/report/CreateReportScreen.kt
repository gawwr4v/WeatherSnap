package com.gourav.weathersnap.ui.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gourav.weathersnap.domain.model.ReportDraft
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import com.gourav.weathersnap.ui.common.formatFileSize
import com.gourav.weathersnap.ui.common.formatOneDecimal
import com.gourav.weathersnap.ui.theme.WeatherAmber
import com.gourav.weathersnap.ui.theme.WeatherBackground
import com.gourav.weathersnap.ui.theme.WeatherBlue
import com.gourav.weathersnap.ui.theme.WeatherMutedText
import com.gourav.weathersnap.ui.theme.WeatherPrimary
import com.gourav.weathersnap.ui.theme.WeatherPrimaryDark
import com.gourav.weathersnap.ui.theme.WeatherSnapTheme
import com.gourav.weathersnap.ui.theme.WeatherSurface
import com.gourav.weathersnap.ui.theme.WeatherTeal
import com.gourav.weathersnap.ui.theme.WeatherText
import kotlin.math.roundToInt

// this is the main entry for the report creation screen
@Composable
fun CreateReportRoute(
    weather: WeatherSnapshot,
    capturedImagePath: String?,
    onCapturedImageConsumed: () -> Unit,
    onBack: () -> Unit,
    onCapturePhoto: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // we start the draft as soon as we land on this screen with the selected weather
    LaunchedEffect(weather) {
        viewModel.startDraft(weather)
    }

    // if the user just came back from the camera screen we process the photo
    LaunchedEffect(capturedImagePath) {
        if (capturedImagePath != null) {
            viewModel.onPhotoCaptured(capturedImagePath)
            onCapturedImageConsumed()
        }
    }

    // we listen for the saved event to navigate back to the reports list
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateReportEvent.Saved -> onSaved()
            }
        }
    }

    CreateReportScreen(
        uiState = uiState,
        fallbackWeather = weather,
        onBack = onBack,
        onCapturePhoto = onCapturePhoto,
        onNotesChanged = viewModel::onNotesChanged,
        onSaveReport = viewModel::saveReport,
        onDismissError = viewModel::dismissError,
    )
}

// this is the layout for the report creation screen
@Composable
fun CreateReportScreen(
    uiState: CreateReportUiState,
    fallbackWeather: WeatherSnapshot,
    onBack: () -> Unit,
    onCapturePhoto: () -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveReport: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft
    val weather = draft?.weather ?: fallbackWeather

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WeatherBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // the screen header with title and back button
        CreateReportHeader(onBack = onBack)
        
        // a small card showing the weather details being reported
        WeatherMiniCard(weather = weather)

        // show error messages if anything goes wrong during compression or saving
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Surface(
                color = Color(0xFF422727),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = WeatherText,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                    )
                    TextButton(onClick = onDismissError) {
                        Text("Dismiss", color = WeatherPrimary)
                    }
                }
            }
        }

        // the section where users take and see their photo evidence
        PhotoCard(
            draft = draft,
            isCompressing = uiState.isCompressing,
            onCapturePhoto = onCapturePhoto,
        )

        // the section where users type their field notes
        NotesCard(
            notes = draft?.notes.orEmpty(),
            onNotesChanged = onNotesChanged,
        )

        // the final save button that commits the report to the database
        Button(
            onClick = onSaveReport,
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = WeatherPrimary,
                contentColor = WeatherPrimaryDark,
                disabledContainerColor = Color(0xFF5F6540),
                disabledContentColor = WeatherMutedText,
            ),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color = WeatherPrimaryDark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(18.dp),
                )
            } else {
                Text("Save Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// header with a back button to return to the search screen
@Composable
private fun CreateReportHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFD0DB83), Color(0xFFC8D583)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Create Report",
                color = Color(0xFF26301E),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Capture, compress, annotate",
                color = Color(0xFF566248),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = WeatherPrimaryDark,
                contentColor = WeatherPrimary,
            ),
        ) {
            Text("Back")
        }
    }
}

// shows a compact version of the weather data
@Composable
private fun WeatherMiniCard(weather: WeatherSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${weather.cityName}, ${weather.country}",
                        color = WeatherText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = weather.condition,
                        color = WeatherMutedText,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "${weather.temperatureCelsius.roundToInt()}°C",
                    color = WeatherPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniMetric("Humidity", "${weather.humidityPercent}%", WeatherTeal, Modifier.weight(1f))
                MiniMetric("Wind", "${formatOneDecimal(weather.windSpeedMetersPerSecond)} m/s", WeatherBlue, Modifier.weight(1f))
                MiniMetric("Pressure", weather.pressureHPa.roundToInt().toString(), WeatherAmber, Modifier.weight(1f))
            }
        }
    }
}

// small component to show individual metrics like wind or humidity
@Composable
private fun MiniMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.11f))
            .padding(12.dp),
    ) {
        Text(label, color = WeatherMutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// this handles displaying the photo and its compression details
@Composable
private fun PhotoCard(
    draft: ReportDraft?,
    isCompressing: Boolean,
    onCapturePhoto: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // we dim the preview placeholder if no photo has been taken yet
            val previewAlpha by animateFloatAsState(
                targetValue = if (draft?.compressedImagePath == null) 0.75f else 1f,
                label = "photo-preview-alpha",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4A4C40), Color(0xFF4F5F00)),
                        ),
                    )
                    .alpha(previewAlpha),
                contentAlignment = Alignment.Center,
            ) {
                if (draft?.compressedImagePath != null) {
                    // load and show the compressed image from disk
                    AsyncImage(
                        model = draft.compressedImagePath,
                        contentDescription = "Captured weather report photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text("Photo preview", color = WeatherText, fontWeight = FontWeight.Bold)
                }

                // show a spinner while the image is being compressed
                if (isCompressing) {
                    CircularProgressIndicator(color = WeatherPrimary)
                }
            }

            // show the file sizes so users see the benefit of compression
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SizeBadge("Original", formatFileSize(draft?.originalSizeBytes), WeatherAmber, Modifier.weight(1f))
                SizeBadge("Compressed", formatFileSize(draft?.compressedSizeBytes), WeatherTeal, Modifier.weight(1f))
            }

            // button to open the custom camera
            Button(
                onClick = onCapturePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WeatherPrimary,
                    contentColor = WeatherPrimaryDark,
                ),
            ) {
                Text("Capture Photo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// reusable component to show file sizes with labels
@Composable
private fun SizeBadge(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.11f))
            .padding(12.dp),
    ) {
        Text(label, color = WeatherMutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// card with a text field for entering additional details about the report
@Composable
private fun NotesCard(
    notes: String,
    onNotesChanged: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Field Notes",
                color = WeatherText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                placeholder = { Text("Notes") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = WeatherText,
                    unfocusedTextColor = WeatherText,
                    focusedIndicatorColor = WeatherMutedText,
                    unfocusedIndicatorColor = WeatherMutedText,
                    cursorColor = WeatherPrimary,
                ),
            )
        }
    }
}

// dummy data for previews
private val previewWeather = WeatherSnapshot(
    cityName = "Ben",
    country = "Iran",
    latitude = 32.0,
    longitude = 52.0,
    temperatureCelsius = 17.0,
    condition = "Partly cloudy",
    weatherCode = 2,
    humidityPercent = 43,
    windSpeedMetersPerSecond = 3.83,
    pressureHPa = 791.0,
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CreateReportEmptyPreview() {
    WeatherSnapTheme {
        CreateReportScreen(
            uiState = CreateReportUiState(),
            fallbackWeather = previewWeather,
            onBack = {},
            onCapturePhoto = {},
            onNotesChanged = {},
            onSaveReport = {},
            onDismissError = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CreateReportWithDraftPreview() {
    WeatherSnapTheme {
        CreateReportScreen(
            uiState = CreateReportUiState(
                draft = ReportDraft(
                    weather = previewWeather,
                    notes = "Rain started after sunset.",
                    originalImagePath = null,
                    compressedImagePath = null,
                    originalSizeBytes = 552_000,
                    compressedSizeBytes = 84_000,
                ),
            ),
            fallbackWeather = previewWeather,
            onBack = {},
            onCapturePhoto = {},
            onNotesChanged = {},
            onSaveReport = {},
            onDismissError = {},
        )
    }
}
