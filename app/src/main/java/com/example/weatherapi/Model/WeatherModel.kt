package com.example.weatherapi.Model

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

data class HourlyUnitDataClass(
    val time: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val apparent_temperature: String,
    val rain: String,
    val wind_speed_10m: String
)

data class HourlyDataClass(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val apparent_temperature: List<Double>,
    val rain: List<Double>,
    val wind_speed_10m: List<Double>
)
