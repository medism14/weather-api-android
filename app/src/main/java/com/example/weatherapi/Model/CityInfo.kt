package com.example.weatherapi.Model

data class CityInfo (
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val admin1: String? = "",
    val admin2: String? = "",
    val admin3: String? = "",
    val weatherInfo: WeatherModel? = null
)
