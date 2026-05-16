package com.gourav.weathersnap.di

import com.gourav.weathersnap.BuildConfig
import com.gourav.weathersnap.data.remote.GeocodingApi
import com.gourav.weathersnap.data.remote.WeatherApi
import com.gourav.weathersnap.data.repository.OpenMeteoWeatherRepository
import com.gourav.weathersnap.data.repository.ReportRepository
import com.gourav.weathersnap.data.repository.RoomReportRepository
import com.gourav.weathersnap.data.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// we use qualifiers to tell hilt which retrofit instance to inject
// since we have two different base urls
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // provides a single okhttp client for the whole app
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // this interceptor helps us see network logs in the console
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // creates a retrofit instance specifically for searching cities
    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // creates a retrofit instance specifically for getting weather data
    @Provides
    @Singleton
    @ForecastRetrofit
    fun provideForecastRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // provides the api interface for city search
    @Provides
    @Singleton
    fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)

    // provides the api interface for weather forecasts
    @Provides
    @Singleton
    fun provideWeatherApi(@ForecastRetrofit retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // tells hilt to use OpenMeteoWeatherRepository whenever a WeatherRepository is needed
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        repository: OpenMeteoWeatherRepository,
    ): WeatherRepository

    // tells hilt to use RoomReportRepository whenever a ReportRepository is needed
    @Binds
    @Singleton
    abstract fun bindReportRepository(
        repository: RoomReportRepository,
    ): ReportRepository
}
