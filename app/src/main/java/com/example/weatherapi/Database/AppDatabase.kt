package com.example.weatherapi.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapi.Dao.CityInfoDao
import com.example.weatherapi.Model.CityInfo

@Database(entities = [CityInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityInfoDao(): CityInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "city_info_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
