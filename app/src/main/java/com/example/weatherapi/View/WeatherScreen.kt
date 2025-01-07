package com.example.weatherapi.View

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.weatherapi.Controller.WeatherController
import com.example.weatherapi.Model.CityInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.example.weatherapi.Database.AppDatabase
import com.example.weatherapi.Model.SearchResult
import com.example.weatherapi.Utils.NetworkUtils
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.location.LocationListener
import android.os.Bundle
import kotlin.math.abs

@Composable
fun WeatherScreen(
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isPortrait: Boolean
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val searchResultDao = remember { db.searchResultDao() }
    val controller = remember { WeatherController(searchResultDao) }
    var filteredCities by remember { mutableStateOf(mutableListOf<CityInfo>()) }
    var actualCity by remember { mutableStateOf(CityInfo()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingFavorites by remember { mutableStateOf(false) }
    var isLoadingSearch by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var enterPressed by remember { mutableStateOf(false) }
    var onSearch by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    var isLocationEnabled by remember { mutableStateOf(true) }
    var internetConnection by remember { mutableStateOf(NetworkUtils.isInternetAvailable(context)) }

    var favoritesList by remember { mutableStateOf<List<CityInfo>>(emptyList()) }
    var searchsList by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var showNoConnectionDialog by remember { mutableStateOf(false) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(navController.context)
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }

    fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                navController.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLatitude = location.latitude
                            currentLongitude = location.longitude
                            isLocationEnabled = true
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Erreur lors de la récupération de la position : ${e.message}")
                        isLocationEnabled = false
                    }
            } catch (e: SecurityException) {
                println("Erreur de permission : ${e.message}")
                isLocationEnabled = false
            }
        } else {
            try {
                ActivityCompat.requestPermissions(
                    navController.context as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
            } catch (e: Exception) {
                println("Erreur lors de la demande de permissions : ${e.message}")
            }
        }
    }


    // Créer un LocationListener
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Ne mettre à jour que si la différence est significative
                val significantChange = abs(currentLatitude - location.latitude) > 0.0001 ||
                                      abs(currentLongitude - location.longitude) > 0.0001
                
                if (significantChange) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            }

            override fun onProviderEnabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    isLocationEnabled = true
                    getUserLocation()
                }
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    isLocationEnabled = false
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Requis mais non utilisé
            }
        }
    }

    // Effet pour gérer l'enregistrement/désenregistrement du listener
    DisposableEffect(locationManager) {
        try {
            isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            
            if (ContextCompat.checkSelfPermission(
                    navController.context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L,  // Augmentation de l'intervalle à 5 secondes
                    10f,    // Distance minimum de 10 mètres
                    locationListener
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    // Effet pour mettre à jour la ville actuelle quand la localisation change
    LaunchedEffect(isLocationEnabled, currentLatitude, currentLongitude, internetConnection) {
        if (isLocationEnabled && 
            internetConnection && 
            currentLatitude != 0.0 && 
            currentLongitude != 0.0 &&
            !isLoading  // Éviter les requêtes multiples pendant le chargement
        ) {
            isLoading = true
            try {
                val newCity = controller.fetchByPosition(currentLatitude, currentLongitude)
                // Ne mettre à jour que si les données sont différentes
                if (newCity != actualCity) {
                    actualCity = newCity
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement de la position actuelle : ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }


    // Surveiller les changements de connexion internet
    DisposableEffect(connectivityManager) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                internetConnection = true
            }

            override fun onLost(network: Network) {
                internetConnection = false
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    LaunchedEffect(Unit) {
        getUserLocation()
    }

    LaunchedEffect(Unit) {
        try {
            db.searchResultDao().getAllSearchs().collect { results ->
                searchsList = results
                isLoadingSearch = false
            }
        } catch (e: Exception) {
            println("Erreur lors du chargement de l'historique : ${e.message}")
            isLoadingSearch = false
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNullOrEmpty()) {
            onSearch = false
        }
    }

    LaunchedEffect(Unit) {
        isLoadingFavorites = true
        try {
            db.cityInfoDao().getAllCities().collect { cities ->
                favoritesList = cities
                isLoadingFavorites = false
            }
        } catch (e: Exception) {
            println("Erreur lors du chargement des favoris : ${e.message}")
            isLoadingFavorites = false
        }
    }

    LaunchedEffect(enterPressed) {
        if (enterPressed) {
            if (searchQuery.isEmpty()) {
                onSearch = false
            } else {
                isLoading = true
                var resultExist = searchResultDao.getSearchById(searchQuery)

                if (resultExist != null) {
                    onSearch = true
                    filteredCities = resultExist.listCityInfo.toMutableList()
                    println("Voici les city filtrés:")
                    println(filteredCities)
                    isLoading = false
                } else if (!internetConnection) {
                    showNoConnectionDialog = true
                    onSearch = false
                    isLoading = false
                } else {
                    try {
                        onSearch = true
                        filteredCities = controller.fetchCities(searchQuery)
                    } catch (e: Exception) {
                        filteredCities = mutableListOf()
                    }
                    isLoading = false
                }
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
                .padding(if (isPortrait) 24.dp else 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isPortrait) 16.dp else 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
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
                            textStyle = MaterialTheme.typography.bodyLarge,
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

                    if (searchQuery.isNotEmpty() && !onSearch) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(searchsList.filter {
                                it.searchName.contains(searchQuery, ignoreCase = true)
                            }) { searchResult ->
                                HistoryItem(
                                    searchResult = searchResult,
                                    onItemClick = {
                                        searchQuery = searchResult.searchName
                                        enterPressed = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (!internetConnection && !onSearch) {
                NoInternetCard()

                actualCityDisplay(
                    city = CityInfo(),
                    navController = navController,
                    cityInfoViewModel = cityInfoViewModel,
                    isLoading = isLoading,
                    isLoadingFavorites = isLoadingFavorites,
                    favoritesCities = favoritesList,
                    isPortrait = isPortrait,
                    showLocationSection = false
                )
            } else {
                when {
                    onSearch -> {
                        searchResults(
                            filteredCities = filteredCities,
                            isLoading = isLoading,
                            navController = navController,
                            cityInfoViewModel = cityInfoViewModel,
                            isPortrait = isPortrait
                        )
                    }
                    else -> {
                        Column(
                            modifier = if (!isPortrait) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            if (!isLocationEnabled) {
                                LocationDisabledCard(navController)
                            }

                            actualCityDisplay(
                                city = if (isLocationEnabled) actualCity else CityInfo(),
                                navController = navController,
                                cityInfoViewModel = cityInfoViewModel,
                                isLoading = isLoading,
                                isLoadingFavorites = isLoadingFavorites,
                                favoritesCities = favoritesList,
                                isPortrait = isPortrait,
                                showLocationSection = isLocationEnabled
                            )
                        }
                    }
                }
            }
        }

        if (showNoConnectionDialog) {
            AlertDialog(
                onDismissRequest = { showNoConnectionDialog = false },
                title = {
                    Text(
                        text = "Connexion Internet requise",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Cette recherche n'existe pas dans l'historique. Une connexion Internet est nécessaire pour obtenir de nouvelles données.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            navController.context.startActivity(intent)
                            showNoConnectionDialog = false
                        }
                    ) {
                        Text("Ouvrir les paramètres")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoConnectionDialog = false }) {
                        Text("Fermer")
                    }
                }
            )
        }
    }
}

@Composable
fun searchResults(
    filteredCities: List<CityInfo>,
    isLoading: Boolean,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isPortrait: Boolean
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
                modifier = if (!isPortrait) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.fillMaxSize()
                },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredCities) { city ->
                    WeatherCityCard(
                        city = city,
                        navController = navController,
                        cityInfoViewModel = cityInfoViewModel,
                        isPortrait = isPortrait
                    )
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
    isLoading: Boolean,
    isLoadingFavorites: Boolean,
    favoritesCities: List<CityInfo>,
    isPortrait: Boolean,
    showLocationSection: Boolean
) {
    if (isLoading || isLoadingFavorites) {
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
        if (isPortrait) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showLocationSection) {
                    item {
                        CurrentLocationSection(city, navController, cityInfoViewModel, isPortrait)
                    }
                }

                item {
                    Text(
                        text = "⭐ Vos Favoris (${favoritesCities.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                if (favoritesCities.isEmpty()) {
                    item {
                        Text(
                            text = "Aucun favori pour le moment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(favoritesCities) { favoriteCity ->
                        WeatherCityCard(
                            city = favoriteCity,
                            navController = navController,
                            cityInfoViewModel = cityInfoViewModel,
                            isPortrait
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showLocationSection) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CurrentLocationSection(city, navController, cityInfoViewModel, isPortrait)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "⭐ Vos Favoris (${favoritesCities.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (favoritesCities.isEmpty()) {
                        Text(
                            text = "Aucun favori pour le moment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favoritesCities) { favoriteCity ->
                                WeatherCityCard(
                                    city = favoriteCity,
                                    navController = navController,
                                    cityInfoViewModel = cityInfoViewModel,
                                    isPortrait
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationSection(
    city: CityInfo,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isPortrait: Boolean
) {
    if (city.country.isNullOrEmpty()) {
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
            WeatherCityCard(
                city = city,
                navController = navController,
                cityInfoViewModel = cityInfoViewModel,
                isPortrait = isPortrait
            )
        }
    }
}

@Composable
fun HistoryItem(
    searchResult: SearchResult,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Historique",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = searchResult.searchName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = searchResult.entranceAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun NoInternetCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Pas de connexion Internet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Vérifiez votre connexion internet pour accéder à toutes les fonctionnalités.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LocationDisabledCard(navController: NavController) {
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
}