# WeatherSnap

WeatherSnap is a native Android app for creating weather reports with camera evidence. The app lets a user search live weather by city, capture a photo with a custom CameraX screen, compress the photo, add notes, save the report locally, and review saved reports later.

## Stack:

- Kotlin
- Jetpack Compose
- MVVM
- ViewModel
- StateFlow
- Coroutines
- Hilt
- Navigation Compose
- Retrofit with Gson
- OkHttp logging interceptor
- Room
- CameraX
- Material 3

Minimum supported Android version:

- API 29 and above

## Current Status

Foundation work:
- `minSdk` is set to 29.
- Required dependency entries are added to the Gradle version catalog.
- Hilt is wired into the app with an `Application` class.
- Project ignore rules now cover common Android build and IDE noise.
- Debug Kotlin compilation passes with the required foundation dependencies.
- Open Meteo Retrofit APIs are wired for city search and current weather.
- City suggestions are cached in memory by normalized query.
- Weather API DTOs are mapped into small app models before UI usage.
- OkHttp network logging is enabled only for debug builds.
- Weather, Create Report, Custom Camera, and Saved Reports screens are wired with Navigation Compose.
- Room stores saved reports and the active report draft.
- CameraX captures photos through a custom camera screen.
- Captured photos are compressed before the final report is saved.
- Saved reports show the captured image, weather snapshot, notes, original size, compressed size, and timestamp.

## Build Notes

This project uses AGP 9 built in Kotlin support. KSP currently generates Kotlin sources in a way that AGP guards by default, so `android.disallowKotlinSourceSets=false` is set in `gradle.properties`.

That is a small compatibility setting for KSP generated sources. It keeps the project on the current AGP setup while still allowing Room and Hilt code generation.

## Planned App Flow

1. Search for a city using Open Meteo geocoding.
2. Select a suggestion and load current weather from Open Meteo forecast.
3. Create a report from the selected weather snapshot.
4. Capture a photo in a custom CameraX screen.
5. Compress the captured photo and show original and compressed sizes.
6. Add field notes.
7. Save the report in Room.
8. View saved reports offline.

## Lifecycle Recovery Plan

WeatherSnap keeps an in progress report as a local Room draft before the user taps Save Report. The draft stores the selected weather snapshot, notes, photo paths, file sizes, and update time.

A small Room backed draft table is used with at most one active draft. This is slightly more code than only using `SavedStateHandle`, but it gives better recovery if the user rotates the device or backgrounds and later reopens the app before saving.

The final saved report is created from the draft, then the draft is cleared. This prevents duplicate saved reports and keeps the original selected weather snapshot stable.

Temporary original camera files are removed after a report is saved. If the user replaces or discards a draft, the draft image files are also removed.

## Setup

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or physical device.

Camera features should be tested on a device or emulator with a working camera source.

Useful verification commands:

```powershell
$env:GRADLE_USER_HOME='.gradle-user'; ./gradlew :app:lintDebug --console=plain
$env:GRADLE_USER_HOME='.gradle-user'; ./gradlew :app:assembleDebug --console=plain
```

## API

WeatherSnap uses Open Meteo. No API key is required.

- City search: `https://geocoding-api.open-meteo.com/v1/search`
- Forecast/current weather: `https://api.open-meteo.com/v1/forecast`

The data layer keeps Retrofit DTOs separate from UI models. This keeps Compose screens from depending on the exact Open Meteo response shape.

## Notes:

This app is intentionally simple. It does not include login, onboarding, settings, or a splash screen.