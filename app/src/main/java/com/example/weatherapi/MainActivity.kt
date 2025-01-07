package com.example.weatherapi

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.weather_api.ui.theme.WeatherapiTheme
import androidx.navigation.compose.NavHost
import com.example.weatherapi.View.CityInfoViewModel
import com.example.weatherapi.View.WeatherScreen
import com.example.weatherapi.View.WeatherDetailsScreen
import androidx.compose.ui.platform.LocalConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WeatherapiTheme {
                val configuration = LocalConfiguration.current
                val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

                WeatherApp(isPortrait = isPortrait)
            }
        }
    }
}

@Composable
fun WeatherApp(isPortrait: Boolean) {
    val navController = rememberNavController()
    val cityInfoViewModel: CityInfoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "weather_screen") {
        composable("weather_screen") { 
            WeatherScreen(
                navController = navController, 
                cityInfoViewModel = cityInfoViewModel,
                isPortrait = isPortrait
            ) 
        }
        composable("weather_details_screen") { 
            WeatherDetailsScreen(
                navController = navController, 
                cityInfoViewModel = cityInfoViewModel,
                isPortrait = isPortrait
            )
        }
    }
}
