package com.example.weatherapi.Dao

import androidx.room.*
import com.example.weatherapi.Model.CityInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface CityInfoDao {
    @Query("SELECT * FROM favorites_city_info")
    fun getAllCities(): Flow<List<CityInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(cityInfo: CityInfo)

    @Delete
    suspend fun deleteCity(cityInfo: CityInfo)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites_city_info WHERE id = :cityId)")
    fun isCityFavorite(cityId: String): Flow<Boolean>

    @Query("DELETE FROM favorites_city_info")
    suspend fun deleteAll()

    @Update
    suspend fun updateCity(cityInfo: CityInfo)
}
