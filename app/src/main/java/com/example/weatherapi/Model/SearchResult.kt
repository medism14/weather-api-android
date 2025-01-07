package com.example.weatherapi.Model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "search_result")
data class SearchResult (
    @PrimaryKey
    val searchName: String = "",
    val listCityInfo : List<CityInfo>,
    val entranceAt : LocalDateTime
)