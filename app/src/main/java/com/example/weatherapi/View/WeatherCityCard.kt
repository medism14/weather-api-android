package com.example.weatherapi.View

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapi.Model.CityInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.weatherapi.Database.AppDatabase
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherCityCard(
    city: CityInfo,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isPortrait: Boolean
) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val formattedDateTime = currentDateTime.format(formatter)
    var favoris by remember { mutableStateOf(false) }
    val ventEmoji = "\uD83D\uDCA8"  // Vent
    val ensoleilleEmoji = "\u2600\uFE0F"  // Ensoleillé
    val pluieEmoji = "\uD83C\uDF27\uFE0F"  // Pluie

    var i = -1 // Initialiser i à -1 pour marquer l'indice comme invalide au départ
    val image: Painter = painterResource(id = com.example.weatherapi.R.drawable.weather_image)

    val context = LocalContext.current;
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(city) {
        scope.launch {
            favoris = db.cityInfoDao().isCityFavorite(city.id).first()
        }
    }

    // Vérifier si les données sont disponibles avant de traiter la liste
    city.weatherInfo?.hourly?.time?.let { timeList ->
        timeList.forEachIndexed { index, time ->
            val timeDate = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)

            // Comparer les dates
            if (formattedDateTime > timeDate.format(formatter)) {
                i = index
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isPortrait) 200.dp else 160.dp)
            .padding(
                vertical = if (isPortrait) 8.dp else 4.dp,
                horizontal = if (isPortrait) 4.dp else 4.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                cityInfoViewModel.setCityInfo(city)
                navController.navigate("weather_details_screen")
            }
    ) {
        // Fond avec effet de superposition
        Image(
            painter = image,
            contentDescription = "Weather Background",
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )

        // Overlay gradient amélioré
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isPortrait) 20.dp else 12.dp,
                    vertical = if (isPortrait) 16.dp else 10.dp
                )
        ) {
            // En-tête avec localisation et favoris
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isPortrait) 12.dp else 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Section localisation
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(if (isPortrait) 24.dp else 20.dp)
                    )

                    Column {
                        // Afficher d'abord le pays
                        Text(
                            text = city.country ?: "Pays inconnu",
                            color = Color.White,
                            style = if (isPortrait) {
                                MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)
                            } else {
                                MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Afficher la ville et l'admin en dessous
                        if (isPortrait) {
                            val detailText = buildString {
                                if (!city.name.isNullOrBlank()) {
                                    append(city.name)
                                    if (!city.admin1.isNullOrBlank()) append(" (${city.admin1})")
                                }
                            }
                            if (detailText.isNotEmpty()) {
                                Text(
                                    text = detailText,
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Bouton favoris optimisé
                Box(
                    modifier = Modifier
                        .size(if (isPortrait) 40.dp else 36.dp)
                        .background(
                            color = if (favoris)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .clickable {
                            favoris = !favoris
                            scope.launch {
                                if (favoris) {
                                    db.cityInfoDao().insertCity(city)
                                } else {
                                    db.cityInfoDao().deleteCity(city)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (favoris) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (favoris) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (favoris) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(if (isPortrait) 24.dp else 20.dp)
                    )
                }
            }

            // Informations météo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isPortrait) 16.dp else 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Température
                if (i >= 0) {
                    city.weatherInfo?.hourly?.temperature_2m?.getOrNull(i)?.let { temp ->
                        Text(
                            text = "${temp.toInt()}°",
                            color = Color.White,
                            style = if (isPortrait) {
                                MaterialTheme.typography.displayMedium
                            } else {
                                MaterialTheme.typography.headlineLarge
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Conditions météo
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(if (isPortrait) 8.dp else 4.dp)
                ) {
                    val isRainy = city.weatherInfo?.hourly?.rain?.getOrNull(i)?.toDouble() ?: 0.0 > 0
                    Text(
                        text = if (isRainy) "$pluieEmoji Pluvieux" else "$ensoleilleEmoji Ensoleillé",
                        style = if (isPortrait) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = Color.White
                    )

                    city.weatherInfo?.hourly?.wind_speed_10m?.getOrNull(i)?.let { windSpeed ->
                        Text(
                            text = "$ventEmoji ${windSpeed.toInt()} km/h",
                            color = Color.White.copy(alpha = 0.8f),
                            style = if (isPortrait) {
                                MaterialTheme.typography.bodyLarge
                            } else {
                                MaterialTheme.typography.bodyMedium
                            }
                        )
                    }
                }
            }
        }
    }
}