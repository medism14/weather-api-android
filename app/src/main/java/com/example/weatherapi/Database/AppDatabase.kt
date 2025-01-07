package com.example.weatherapi.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapi.Dao.CityInfoDao
import com.example.weatherapi.Dao.SearchResultDao
import com.example.weatherapi.Model.CityInfo
import com.example.weatherapi.Model.SearchResult
import com.example.weatherapi.Utils.Converters

@Database(
    entities = [
        CityInfo::class,
        SearchResult::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityInfoDao(): CityInfoDao
    abstract fun searchResultDao(): SearchResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
