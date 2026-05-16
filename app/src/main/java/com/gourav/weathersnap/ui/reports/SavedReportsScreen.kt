package com.gourav.weathersnap.ui.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.gourav.weathersnap.domain.model.SavedReport
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import com.gourav.weathersnap.ui.common.formatFileSize
import com.gourav.weathersnap.ui.common.formatSavedTime
import com.gourav.weathersnap.ui.theme.WeatherAmber
import com.gourav.weathersnap.ui.theme.WeatherBackground
import com.gourav.weathersnap.ui.theme.WeatherMutedText
import com.gourav.weathersnap.ui.theme.WeatherPrimary
import com.gourav.weathersnap.ui.theme.WeatherPrimaryDark
import com.gourav.weathersnap.ui.theme.WeatherSnapTheme
import com.gourav.weathersnap.ui.theme.WeatherSurface
import com.gourav.weathersnap.ui.theme.WeatherTeal
import com.gourav.weathersnap.ui.theme.WeatherText
import kotlin.math.roundToInt

// this is the main entry for the screen that shows all saved reports
@Composable
fun SavedReportsRoute(
    onBack: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel(),
) {
    // we observe the list of reports from our viewmodel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedReportsScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

// this is the layout for the saved reports list
@Composable
fun SavedReportsScreen(
    uiState: SavedReportsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WeatherBackground)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // top header with the total count and back button
        ReportsHeader(
            count = uiState.reports.size,
            onBack = onBack,
        )

        // if there are no reports we show a placeholder message
        if (uiState.reports.isEmpty()) {
            EmptyReportsCard()
        } else {
            // we use a lazy column to efficiently show a long list of reports
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // items are keyed by id so compose can track them easily if the list changes
                items(uiState.reports, key = { it.id }) { report ->
                    SavedReportCard(report = report)
                }
            }
        }
    }
}

// simple header showing how many reports we have saved
@Composable
private fun ReportsHeader(
    count: Int,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFA9D4D1), Color(0xFFD0DB83)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Saved Reports",
                color = Color(0xFF173D37),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count ${if (count == 1) "report" else "reports"} stored locally",
                color = Color(0xFF4B6B61),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00463F),
                contentColor = WeatherPrimary,
            ),
        ) {
            Text("Back")
        }
    }
}

// component shown when the list of reports is empty
@Composable
private fun EmptyReportsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No reports yet",
                color = WeatherText,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Create a weather report to see it stored here.",
                color = WeatherMutedText,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// this represents a single saved report item in the list
@Composable
private fun SavedReportCard(report: SavedReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // we show the compressed photo taken for this report
            AsyncImage(
                model = report.imagePath,
                contentDescription = "Saved report photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentScale = ContentScale.Crop,
            )

            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${report.weather.cityName}, ${report.weather.country}",
                        color = WeatherText,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = report.weather.condition,
                        color = WeatherMutedText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    // we show when the report was saved in a nice format
                    Text(
                        text = formatSavedTime(report.savedAtMillis),
                        color = WeatherMutedText,
                        fontSize = 12.sp,
                    )
                }

                // temperature badge for the saved weather
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF546400))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${report.weather.temperatureCelsius.roundToInt()}°C",
                        color = WeatherPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // show the compression info so we can see how much space we saved
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportSizeCard("Original", formatFileSize(report.originalSizeBytes), WeatherAmber, Modifier.weight(1f))
                ReportSizeCard("Compressed", formatFileSize(report.compressedSizeBytes), WeatherTeal, Modifier.weight(1f))
            }

            // show the user notes if they are not empty
            if (report.notes.isNotBlank()) {
                Text(
                    text = report.notes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4A4C40))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    color = WeatherText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// small component to show file sizes with different accent colors
@Composable
private fun ReportSizeCard(
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
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// dummy data for previews
private val previewReport = SavedReport(
    id = 1,
    weather = WeatherSnapshot(
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
    ),
    notes = "Test_report",
    imagePath = "",
    originalSizeBytes = 552_000,
    compressedSizeBytes = 84_000,
    savedAtMillis = 1_715_154_000_000,
)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SavedReportsPreview() {
    WeatherSnapTheme {
        SavedReportsScreen(
            uiState = SavedReportsUiState(reports = listOf(previewReport)),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SavedReportsEmptyPreview() {
    WeatherSnapTheme {
        SavedReportsScreen(
            uiState = SavedReportsUiState(),
            onBack = {},
        )
    }
}
