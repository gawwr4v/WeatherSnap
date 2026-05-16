package com.gourav.weathersnap.ui.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gourav.weathersnap.domain.model.CitySuggestion
import com.gourav.weathersnap.domain.model.WeatherSnapshot
import com.gourav.weathersnap.ui.common.formatOneDecimal
import com.gourav.weathersnap.ui.theme.WeatherAmber
import com.gourav.weathersnap.ui.theme.WeatherBackground
import com.gourav.weathersnap.ui.theme.WeatherBlue
import com.gourav.weathersnap.ui.theme.WeatherMutedText
import com.gourav.weathersnap.ui.theme.WeatherPrimary
import com.gourav.weathersnap.ui.theme.WeatherPrimaryDark
import com.gourav.weathersnap.ui.theme.WeatherSnapTheme
import com.gourav.weathersnap.ui.theme.WeatherSurface
import com.gourav.weathersnap.ui.theme.WeatherSurfaceDark
import com.gourav.weathersnap.ui.theme.WeatherTeal
import com.gourav.weathersnap.ui.theme.WeatherText
import kotlin.math.roundToInt

// this is the main entry for the weather screen
// it connects the viewmodel state to our ui components
@Composable
fun WeatherRoute(
    onCreateReport: (WeatherSnapshot) -> Unit,
    onOpenReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    // we observe the ui state from the viewmodel here
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherScreen(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onSearchClicked = viewModel::onSearchClicked,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        onCreateReport = onCreateReport,
        onOpenReports = onOpenReports,
        onDismissMessage = viewModel::dismissMessage,
    )
}

// this is the actual layout of our weather screen
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onSuggestionSelected: (CitySuggestion) -> Unit,
    onCreateReport: (WeatherSnapshot) -> Unit,
    onOpenReports: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WeatherBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // the top bar with the app name and reports button
        WeatherHeader(onOpenReports = onOpenReports)

        // the search input and suggestion list
        SearchCard(
            uiState = uiState,
            onQueryChanged = onQueryChanged,
            onSearchClicked = onSearchClicked,
            onSuggestionSelected = onSuggestionSelected,
        )

        // show an error or info box if there is a message
        AnimatedVisibility(
            visible = uiState.errorMessage != null || uiState.emptyMessage != null,
        ) {
            MessageCard(
                message = uiState.errorMessage ?: uiState.emptyMessage.orEmpty(),
                isError = uiState.errorMessage != null,
                onDismiss = onDismissMessage,
            )
        }

        // here we decide what to show based on the current state
        AnimatedContent(
            targetState = uiStateContentKey(uiState),
            label = "weather-card-state",
        ) { contentKey ->
            when (contentKey) {
                // show a loading spinner while fetching weather
                WeatherContentKey.Loading -> LoadingWeatherCard()
                // show the weather details if we have them
                WeatherContentKey.Success -> {
                    uiState.selectedWeather?.let { weather ->
                        WeatherSummaryCard(
                            weather = weather,
                            canCreateReport = uiState.canCreateReport,
                            onCreateReport = { onCreateReport(weather) },
                        )
                    }
                }
                WeatherContentKey.Idle -> Unit
            }
        }
    }
}

// simple header with a nice gradient and a button to view saved reports
@Composable
private fun WeatherHeader(onOpenReports: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFD0DB83), Color(0xFFA9D4D1)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "WeatherSnap",
                color = Color(0xFF26301E),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Live weather reports with camera evidence",
                color = Color(0xFF566248),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onOpenReports,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WeatherPrimaryDark,
                contentColor = WeatherPrimary,
            ),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Text(text = "Reports")
        }
    }
}

// this component contains the city search input field
@Composable
private fun SearchCard(
    uiState: WeatherUiState,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onSuggestionSelected: (CitySuggestion) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = WeatherSurfaceDark,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("City") },
                    singleLine = true,
                    // triggers search when the keyboard search button is pressed
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchClicked() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = WeatherText,
                        unfocusedTextColor = WeatherText,
                        focusedLabelColor = WeatherText,
                        unfocusedLabelColor = WeatherMutedText,
                        focusedIndicatorColor = WeatherMutedText,
                        unfocusedIndicatorColor = WeatherMutedText,
                        cursorColor = WeatherPrimary,
                    ),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Button(
                    onClick = onSearchClicked,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WeatherPrimary,
                        contentColor = WeatherPrimaryDark,
                    ),
                    modifier = Modifier.height(54.dp),
                ) {
                    Text("Search")
                }
            }

            Text(
                text = "Enter more than 2 letters to start city suggestions.",
                color = WeatherMutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )

            // only show the suggestion list if we are loading or have results
            AnimatedVisibility(
                visible = uiState.isLoadingSuggestions || uiState.suggestions.isNotEmpty(),
            ) {
                SuggestionList(
                    isLoading = uiState.isLoadingSuggestions,
                    suggestions = uiState.suggestions,
                    onSuggestionSelected = onSuggestionSelected,
                )
            }
        }
    }
}

// this shows the list of cities returned by the search api
@Composable
private fun SuggestionList(
    isLoading: Boolean,
    suggestions: List<CitySuggestion>,
    onSuggestionSelected: (CitySuggestion) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1D2418)),
    ) {
        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = WeatherPrimary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Finding cities", color = WeatherMutedText, fontSize = 13.sp)
            }
        } else {
            suggestions.forEach { suggestion ->
                Text(
                    text = suggestion.displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    color = WeatherText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// a simple reusable card to show messages or errors
@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isError) Color(0xFF422727) else Color(0xFF2C3527),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                color = WeatherText,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = WeatherPrimary)
            }
        }
    }
}

// a card shown when we are waiting for weather data to load
@Composable
private fun LoadingWeatherCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                color = WeatherPrimary,
                strokeWidth = 3.dp,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Loading live weather",
                color = WeatherText,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// this is where the main weather details like temp and humidity are displayed
@Composable
private fun WeatherSummaryCard(
    weather: WeatherSnapshot,
    canCreateReport: Boolean,
    onCreateReport: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // large badge showing the temperature
                TemperatureBadge(temperature = weather.temperatureCelsius)
            }

            // row showing three key weather metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard(
                    label = "Humidity",
                    value = "${weather.humidityPercent}%",
                    accent = WeatherTeal,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Wind",
                    value = "${formatOneDecimal(weather.windSpeedMetersPerSecond)} m/s",
                    accent = WeatherBlue,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Pressure",
                    value = weather.pressureHPa.roundToInt().toString(),
                    accent = WeatherAmber,
                    modifier = Modifier.weight(1f),
                )
            }

            // info bar showing that the system is ready to take a report
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4A4C40))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Report readiness",
                    color = WeatherMutedText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Camera and Room DB enabled",
                    color = WeatherText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }

            // the button to start the report creation flow
            Button(
                onClick = onCreateReport,
                enabled = canCreateReport,
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
                Text("Create Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// small reusable card for weather metrics
@Composable
private fun MetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(12.dp),
    ) {
        Text(
            text = label,
            color = WeatherMutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = accent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

// badge used to display the temperature clearly
@Composable
private fun TemperatureBadge(temperature: Double) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF546400))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${temperature.roundToInt()}°C",
            color = WeatherPrimary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// helper to keep track of which main component to show in the animated content
private enum class WeatherContentKey {
    Idle,
    Loading,
    Success,
}

// maps the ui state into a key for our content animation
private fun uiStateContentKey(uiState: WeatherUiState): WeatherContentKey = when {
    uiState.isLoadingWeather -> WeatherContentKey.Loading
    uiState.selectedWeather != null -> WeatherContentKey.Success
    else -> WeatherContentKey.Idle
}

// dummy data for previews
private val previewCity = CitySuggestion(
    id = 1,
    name = "Ben",
    country = "Iran",
    adminArea = null,
    latitude = 32.0,
    longitude = 52.0,
)

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
private fun WeatherScreenLoadedPreview() {
    WeatherSnapTheme {
        WeatherScreen(
            uiState = WeatherUiState(
                query = "Ben, Iran",
                selectedWeather = previewWeather,
            ),
            onQueryChanged = {},
            onSearchClicked = {},
            onSuggestionSelected = {},
            onCreateReport = {},
            onOpenReports = {},
            onDismissMessage = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WeatherScreenSuggestionsPreview() {
    WeatherSnapTheme {
        WeatherScreen(
            uiState = WeatherUiState(
                query = "Ben",
                suggestions = listOf(previewCity, previewCity.copy(id = 2, adminArea = "Isfahan")),
            ),
            onQueryChanged = {},
            onSearchClicked = {},
            onSuggestionSelected = {},
            onCreateReport = {},
            onOpenReports = {},
            onDismissMessage = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WeatherScreenLoadingPreview() {
    WeatherSnapTheme {
        WeatherScreen(
            uiState = WeatherUiState(
                query = "Ben, Iran",
                isLoadingWeather = true,
            ),
            onQueryChanged = {},
            onSearchClicked = {},
            onSuggestionSelected = {},
            onCreateReport = {},
            onOpenReports = {},
            onDismissMessage = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun WeatherScreenErrorPreview() {
    WeatherSnapTheme {
        WeatherScreen(
            uiState = WeatherUiState(
                query = "Bennn",
                errorMessage = "Could not load city suggestions. Check your connection and try again.",
            ),
            onQueryChanged = {},
            onSearchClicked = {},
            onSuggestionSelected = {},
            onCreateReport = {},
            onOpenReports = {},
            onDismissMessage = {},
        )
    }
}
