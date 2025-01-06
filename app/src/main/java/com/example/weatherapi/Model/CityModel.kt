package com.example.weatherapi.Model

data class CityModel(
    val id: Int?,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val elevation: Int?,
    val feature_code: String?,
    val country_code: String?,
    val admin1_id: Int?,
    val admin2_id: Int?,
    val admin3_id: Int?,
    val timezone: String?,
    val population: Int?,
    val postcodes: Array<String>?,
    val country_id: Int?,
    val country: String?,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?,
)
