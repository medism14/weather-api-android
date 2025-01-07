package com.example.weatherapi.Dao

import androidx.room.*
import com.example.weatherapi.Model.SearchResult
import com.example.weatherapi.Model.CityInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SearchResultDao {
    @Query("SELECT * FROM search_result ORDER BY entranceAt DESC")
    fun getAllSearchs(): Flow<List<SearchResult>>

    @Query("SELECT * FROM search_result WHERE searchName = :searchName")
    suspend fun getSearchById(searchName: String): SearchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSearchs(searches: List<SearchResult>)

    @Update
    suspend fun updateSearch(search: SearchResult)

    @Delete
    suspend fun deleteSearch(search: SearchResult)

    @Query("DELETE FROM search_result")
    suspend fun deleteAllSearchs()

    @Query("SELECT COUNT(*) FROM search_result")
    suspend fun getSearchCount(): Int
}