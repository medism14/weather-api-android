package com.example.weatherapi.Model

import androidx.room.TypeConverters
import com.example.weatherapi.Utils.Converters

@TypeConverters(Converters::class)
data class WeatherModel(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Int,
    val hourly_units: HourlyUnitDataClass,
    val hourly: HourlyDataClass
)

@TypeConverters(Converters::class)
data class HourlyUnitDataClass(
    val time: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val apparent_temperature: String,
    val rain: String,
    val wind_speed_10m: String
)

@TypeConverters(Converters::class)
data class HourlyDataClass(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val apparent_temperature: List<Double>,
    val rain: List<Double>,
    val wind_speed_10m: List<Double>
)
