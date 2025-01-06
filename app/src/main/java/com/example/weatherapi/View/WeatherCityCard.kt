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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapi.Model.CityInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherCityCard(
    city: CityInfo,
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel
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

    // Box pour contenir l'image en fond et le contenu au-dessus
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                cityInfoViewModel.setCityInfo(city)
                navController.navigate("weather_details_screen")
            }
    ) {

        // Image en fond
        Image(
            painter = image,
            contentDescription = "Weather Background",
            modifier = Modifier
                .matchParentSize() // L'image remplit toute la Box
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop // L'image est coupée pour remplir l'espace sans déformation
        )


        // Superposition du contenu
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 20.dp, 15.dp, 20.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = if (favoris) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (favoris) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (favoris) Color(0xFFFFD700) else Color.White, // Couleur dorée si favori
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            favoris = !favoris
                        }
                )
            }

            CityHeader(city = city)

            WeatherInfo(
                city = city,
                pluieEmoji = pluieEmoji,
                ensoleilleEmoji = ensoleilleEmoji,
                i = i
            )

            WindInfo(city = city, ventEmoji = ventEmoji, i = i)
        }
    }
}


@Composable
fun CityHeader(city: CityInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Text(
            text = buildString {
                append(city.country)
                if (!city.name.isNullOrEmpty()) append(", ${city.name}")
                if (!city.admin1.isNullOrEmpty()) append(", ${city.admin1}")
                if (!city.admin2.isNullOrEmpty()) append(", ${city.admin2}")
                if (!city.admin3.isNullOrEmpty()) append(", ${city.admin3}")
            },
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Ajout des points de suspension
        )
    }
}

@Composable
fun WeatherInfo(city: CityInfo, pluieEmoji: String, ensoleilleEmoji: String, i: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Affichage du temps
            if (city.weatherInfo?.hourly?.rain?.get(i) ?: 0.0 > 0) {
                Text(text = "$pluieEmoji", style = TextStyle(fontSize = 40.sp))
                Text(
                    text = "Pluvieux",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            } else {
                Text(text = "$ensoleilleEmoji", style = TextStyle(fontSize = 40.sp))
                Text(
                    text = "Ensoleillé",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        }


        // Affichage de la temperature
        if (i >= 0) {
            // Récupérer la température et l'afficher avec un "°C"
            Text(
                text = "${city.weatherInfo?.hourly?.temperature_2m?.get(i)}°C", // Ajout de "°C"
                color = Color.White, // Texte en blanc
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        } else {
            // Afficher Inconnu si on trouve pas de temperature
            Text(
                text = "Inconnu",
                color = Color.White,
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun WindInfo(city: CityInfo, ventEmoji: String, i: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center // Correct pour centrer le contenu horizontalement
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espacement entre les éléments de la Column
        ) {
            Text(
                text = "$ventEmoji",
                style = TextStyle(fontSize = 30.sp)
            )

            Text(
                text = "${city?.weatherInfo?.hourly?.wind_speed_10m?.get(i)} km/h",
                color = Color.White,
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }

}