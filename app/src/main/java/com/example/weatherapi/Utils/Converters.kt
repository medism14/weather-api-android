package com.example.weatherapi.Utils

import androidx.room.TypeConverter
import com.example.weatherapi.Model.CityInfo
import com.example.weatherapi.Model.WeatherModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherModel(value: WeatherModel?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherModel(value: String?): WeatherModel? {
        return value?.let {
            gson.fromJson(it, WeatherModel::class.java)
        }
    }

    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? {
        val listType = object : TypeToken<List<Double>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCityInfoList(value: List<CityInfo>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCityInfoList(value: String?): List<CityInfo>? {
        if (value == null) return null
        val listType = object : TypeToken<List<CityInfo>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}