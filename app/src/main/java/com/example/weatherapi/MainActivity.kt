package com.example.weatherapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.weather_api.ui.theme.WeatherapiTheme
import androidx.navigation.compose.NavHost
import com.example.weatherapi.View.CityInfoViewModel
import com.example.weatherapi.View.WeatherScreen
import com.example.weatherapi.View.WeatherDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WeatherapiTheme {
                // Rappel de la navigation
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp() {
    val navController = rememberNavController()
    val cityInfoViewModel: CityInfoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "weather_screen") {
        composable("weather_screen") { WeatherScreen(navController, cityInfoViewModel) }
        composable("weather_details_screen") { WeatherDetailsScreen(navController, cityInfoViewModel)}
    }
}



@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    WeatherapiTheme {
        WeatherApp()
    }
}
