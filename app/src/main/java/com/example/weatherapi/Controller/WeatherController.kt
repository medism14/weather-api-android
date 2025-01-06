package com.example.weatherapi.Controller

import com.example.weatherapi.Model.CityInfo
import com.example.weatherapi.Model.CityResponse
import com.example.weatherapi.Model.WeatherModel
import java.util.logging.Level
import java.util.logging.Logger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherController {
    private val client = HttpClient(CIO)
    private val logger = Logger.getLogger(WeatherController::class.java.name)
    private val gson = Gson()

    suspend fun fetchCities(cityName: String): MutableList<CityInfo> {

        val results: MutableList<CityInfo> = mutableListOf()

        try {
            val responseCity: HttpResponse = client.get("https://geocoding-api.open-meteo.com/v1/search") {
                parameter("name", cityName)
            }
            if (responseCity.status.value in 200..299) {
                val responseBodyCity = responseCity.bodyAsText()
                val cities = gson.fromJson(responseBodyCity, CityResponse::class.java)

                val CityResults = cities.results.map { city ->
                    val responseWeatherCity: HttpResponse = client.get("https://api.open-meteo.com/v1/forecast") {
                        parameter("latitude", city.latitude)
                        parameter("longitude", city.longitude)
                        parameter("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature,rain,wind_speed_10m")
                        parameter("models", "meteofrance_seamless")
                    }
                    if (responseWeatherCity.status.value in 200..299) {
                        val responseBodyWeatherCity = responseWeatherCity.bodyAsText()
                        val weatherInfo = gson.fromJson(responseBodyWeatherCity, WeatherModel::class.java)

                        val cityInfo = CityInfo(
                            name = city.name ?: "",
                            latitude = city.latitude ?: 0.0,
                            longitude = city.longitude ?: 0.0,
                            country = city.country ?: "",
                            admin1 = city.admin1,
                            admin2 = city.admin2,
                            admin3 = city.admin3,
                            weatherInfo = weatherInfo
                        )

                        results.add(cityInfo)
                    } else {
                        logger.log(Level.WARNING, "Erreur de réponse responseWeatherCity : ${responseWeatherCity.status.value}")
                    }
                }
            } else {
                logger.log(Level.WARNING, "Erreur de réponse responseCity : ${responseCity.status.value}")
            }

            return results
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Erreur lors de la requête", e)
            return mutableListOf()
        }
    }

    suspend fun fetchByPosition(latitude: Double, longitude: Double) : CityInfo {
        try {

            var cityFetched: Map<String, Any> = emptyMap() // Initialisation pour éviter un accès non initialisé

            // Effectuer la requête pour obtenir les informations de la ville
            val responseGetCity: HttpResponse = client.get("https://nominatim.openstreetmap.org/reverse?format=json") {
                parameter("lat", latitude)
                parameter("lon", longitude)
            }

            // Vérification du statut de la réponse
            if (responseGetCity.status.value in 200..299) {
                val responseBodyCity = responseGetCity.bodyAsText()

                // Utilisation de TypeToken pour convertir en Map<String, Any>
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val cityResponse: Map<String, Any> = gson.fromJson(responseBodyCity, type)

                cityFetched = cityResponse
            } else {
                logger.log(Level.WARNING, "Erreur de réponse pour récupérer la ville : ${responseGetCity.status.value}")
            }

            // Effectuer la requête pour obtenir les informations de la ville
            val responseGetWeather: HttpResponse = client.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("hourly", "temperature_2m,relative_humidity_2m,apparent_temperature,rain,wind_speed_10m")
                parameter("models", "meteofrance_seamless")
            }

            val address = cityFetched["address"] as? Map<String, Any>
            val country = address?.get("country") as? String
            val city = address?.get("city") as? String
            var admin1 = address?.get("suburb") as? String
            var admin2 = address?.get("neighbourhood") as? String

            if (admin2.isNullOrEmpty()) {
                admin2 = address?.get("road") as? String
            }

            if (admin1.isNullOrEmpty() && !admin2.isNullOrEmpty()){
                admin1 = admin2
                admin2 = ""
            }

            if (responseGetWeather.status.value in 200..299) {
                val responseBodyWeather = responseGetWeather.bodyAsText()
                val weatherInfo = gson.fromJson(responseBodyWeather, WeatherModel::class.java)

                val cityInfo = CityInfo(
                    name = city ?: "",
                    latitude = latitude ?: 0.0,
                    longitude = longitude ?: 0.0,
                    country = country ?: "",
                    admin1 = admin1 ?: "",
                    admin2 = admin2 ?: "",
                    admin3 = "",
                    weatherInfo = weatherInfo
                )

                return cityInfo
            } else {
                logger.log(Level.WARNING, "Erreur de réponse pour récupérer la ville : ${responseGetCity.status.value}")
            }

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Erreur lors de la requête", e)
        }
        return CityInfo()
    }
}

