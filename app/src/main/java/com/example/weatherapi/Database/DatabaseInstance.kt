package com.example.weatherapi

import android.content.Context
import androidx.room.Room
import com.example.weatherapi.Database.AppDatabase

object DatabaseInstance {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "weather_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
