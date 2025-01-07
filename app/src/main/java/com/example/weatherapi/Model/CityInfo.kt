package com.example.weatherapi.Model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherapi.Utils.Converters

@Entity(tableName = "favorites_city_info")
data class CityInfo(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val admin1: String? = "",
    val admin2: String? = "",
    val admin3: String? = "",
    @TypeConverters(Converters::class)
    val weatherInfo: WeatherModel? = null
)
