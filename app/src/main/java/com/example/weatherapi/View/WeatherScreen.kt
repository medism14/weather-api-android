package com.example.weatherapi.View

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.weatherapi.Controller.WeatherController
import com.example.weatherapi.Model.CityInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

@Composable
fun WeatherScreen(navController: NavController, cityInfoViewModel: CityInfoViewModel) {
    val controller = remember { WeatherController() }
    var filteredCities by remember { mutableStateOf(mutableListOf<CityInfo>()) }
    var actualCity by remember { mutableStateOf(CityInfo()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var enterPressed by remember { mutableStateOf(false) }
    var onSearch by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(navController.context)
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var isLocationEnabled by remember { mutableStateOf(false) }

    fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                navController.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task: Task<Location> ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        isLocationEnabled = true
                    } else {
                        isLocationEnabled = false
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                navController.context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    LaunchedEffect(currentLatitude, currentLongitude) {
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            isLoading = true
            actualCity = controller.fetchByPosition(currentLatitude, currentLongitude)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        getUserLocation()
    }

    LaunchedEffect(enterPressed) {
        if (enterPressed) {
            if (searchQuery.isEmpty()) {
                onSearch = false
            } else {
                onSearch = true
                isLoading = true
                filteredCities = controller.fetchCities(searchQuery)
                isLoading = false
            }
            enterPressed = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                "Rechercher une ville...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                            .onKeyEvent { event ->
                                if (event.key == Key.Enter) {
                                    enterPressed = true
                                    keyboardController?.hide()
                                    true
                                } else {
                                    false
                                }
                            },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                onSearch = false
                                keyboardController?.hide()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Effacer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (!isLocationEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Localisation désactivée",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pour afficher la météo de votre ville, veuillez activer la localisation.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                navController.context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Activer la Localisation")
                        }
                    }
                }
            } else {
                if (onSearch) {
                    searchResults(filteredCities, isLoading, navController, cityInfoViewModel)
                } else {
                    actualCityDisplay(actualCity, navController, cityInfoViewModel, isLoading)
                }
            }
        }
    }
}

@Composable
fun searchResults(
    filteredCities: List<CityInfo>,
    isLoading: Boolean,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        if (filteredCities.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredCities) { city ->
                    WeatherCityCard(city, navController, cityInfoViewModel)
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aucune ville trouvée",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun actualCityDisplay(
    city: CityInfo,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        if (city?.country.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Localisation non disponible",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(end = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "📍 Votre Position Actuelle",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                WeatherCityCard(city, navController, cityInfoViewModel)
            }
        }
    }
}

@Composable
fun favorites (

) {

}