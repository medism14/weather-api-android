package com.example.weatherapi.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCity(
    @PrimaryKey val id: String,  // Combinaison de latitude/longitude
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?
) 