package com.example.weatherapi.Dao

import androidx.room.*
import com.example.weatherapi.Model.CityInfo

@Dao
interface CityInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(cityInfo: CityInfo)

    @Update
    suspend fun updateCity(cityInfo: CityInfo)

    @Delete
    suspend fun deleteCity(cityInfo: CityInfo)

    @Query("SELECT * FROM favorites_city_info")
    suspend fun getAllCities(): List<CityInfo>

    @Query("SELECT * FROM favorites_city_info WHERE id = :id")
    suspend fun getCityById(id: Int): CityInfo?
}
