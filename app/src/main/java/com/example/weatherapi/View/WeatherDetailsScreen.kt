package com.example.weatherapi.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weatherapi.Model.CityInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun WeatherDetailsScreen(
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel
) {
    var favoris by remember { mutableStateOf(false) }
    val city = cityInfoViewModel.cityInfo
    val image: Painter = painterResource(id = com.example.weatherapi.R.drawable.weather_image)
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val formatterHourAndMinutes = DateTimeFormatter.ofPattern("HH:mm")
    val formattedDateTime = currentDateTime.format(formatter)
    val formattedDateTimeHourAndMinutes = currentDateTime.format(formatterHourAndMinutes)

    val ventEmoji = "\uD83D\uDCA8"  // Vent
    val ensoleilleEmoji = "\u2600\uFE0F"  // Ensoleillé
    val pluieEmoji = "\uD83C\uDF27\uFE0F"  // Pluie
    val humiditeEmoji = "\uD83D\uDCA7"  // Humidité
    val temperatureEmoji = "\uD83C\uDF21"  // Température

    var i = -1
    city?.weatherInfo?.hourly?.time?.let { timeList ->
        timeList.forEachIndexed { index, time ->
            val timeDate = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
            if (formattedDateTime > timeDate.format(formatter)) {
                i = index
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Remplir toute la taille de l'écran (Largeur + Hauteur)
    ) {
        WeatherBackground(image)

        Column(
            modifier = Modifier
                .fillMaxSize() // Remplir toute la taille de l'écran
                .padding(16.dp) // Padding autour du contenu
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(navController)
                
                Icon(
                    imageVector = if (favoris) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (favoris) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (favoris) Color(0xFFFFD700) else Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            favoris = !favoris
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CityName(city)

            CityAdmin(city)

            Spacer(modifier = Modifier.height(100.dp))

            CurrentTime(formattedDateTimeHourAndMinutes)

            CityTemperature(city, i)

            Spacer(modifier = Modifier.height(90.dp))

            WeatherStatsBox(
                city = city,
                ventEmoji = ventEmoji,
                pluieEmoji = pluieEmoji,
                ensoleilleEmoji = ensoleilleEmoji,
                temperatureEmoji = temperatureEmoji,
                humiditeEmoji = humiditeEmoji,
                i = i
            )

            Spacer(modifier = Modifier.height(50.dp))

            WeatherPrevisionsList(
                city = city,
                i = i,
                ventEmoji = ventEmoji,
                pluieEmoji = pluieEmoji,
                ensoleilleEmoji = ensoleilleEmoji
            )
        }
    }
}

@Composable
fun WeatherBackground(image: Painter) {
    Image(
        painter = image,
        contentDescription = "Weather Background",
        modifier = Modifier
            .fillMaxSize(), // Remplir toute la Box (qui est pleine de l'écran)
        contentScale = ContentScale.Crop // L'image est coupée pour remplir l'espace sans déformation
    )
}

@Composable
fun BackButton(navController: NavController) {
    Button(onClick = { navController.navigate("weather_screen") }) {
        Text(text = "Revenir en arrière")
    }
}

@Composable
fun CityName(city: CityInfo?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        if (city?.name.isNullOrEmpty()) {
            Text(
                "${city?.country}",
                color = Color.White,
                style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold)
            )
        } else {
            Text(
                "${city?.name}",
                color = Color.White,
                style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun CityAdmin(city: CityInfo?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Affichage conditionnel pour admin1, admin2 et admin3
        city?.let {
            if (!it.admin1.isNullOrEmpty()) {
                Text(
                    "${it.admin1}",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp)
                )
            }
            if (!it.admin2.isNullOrEmpty()) {
                Text(
                    ", ${it.admin2}",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp)
                )
            }
            if (!it.admin3.isNullOrEmpty()) {
                Text(
                    ", ${it.admin3}",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp)
                )
            }
        }
    }
}

@Composable
fun CurrentTime(formattedDateTimeHourAndMinutes: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            formattedDateTimeHourAndMinutes,
            color = Color.White,
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

@Composable
fun CityTemperature(city: CityInfo?, i: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${city?.weatherInfo?.hourly?.temperature_2m?.get(i)}",
            color = Color.White,
            style = TextStyle(fontSize = 120.sp, fontWeight = FontWeight.ExtraBold)
        )

        Text(
            text = "°C",
            color = Color.White,
            style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun WeatherStatsBox(
    city: CityInfo?,
    ventEmoji: String,
    pluieEmoji: String,
    ensoleilleEmoji: String,
    temperatureEmoji: String,
    humiditeEmoji: String,
    i: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // Coins arrondis
            .background(Color(0x70000000)) // Couleur de fond plus foncée
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 0.dp),
            verticalArrangement = Arrangement.Center
        ) {
            WeatherRow(city, ventEmoji, pluieEmoji, ensoleilleEmoji, i)

            Spacer(modifier = Modifier.height(30.dp))

            WeatherRow2(city, temperatureEmoji, humiditeEmoji, i)
        }
    }
}

@Composable
fun WeatherRow(
    city: CityInfo?,
    ventEmoji: String,
    pluieEmoji: String,
    ensoleilleEmoji: String,
    i: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 15.dp, 20.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (city?.weatherInfo?.hourly?.rain?.get(i) ?: 0.0 > 0) {
                Text(text = "$pluieEmoji", style = TextStyle(fontSize = 30.sp))
                Text(
                    text = "Pluvieux",
                    color = Color.White,
                    style = TextStyle(fontSize = 20.sp)
                )
            } else {
                Text(text = "$ensoleilleEmoji", style = TextStyle(fontSize = 30.sp))
                Text(
                    text = "Ensoleillé",
                    color = Color.White,
                    style = TextStyle(fontSize = 20.sp)
                )
            }
        }
    }
}

@Composable
fun WeatherRow2(city: CityInfo?, temperatureEmoji: String, humiditeEmoji: String, i: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 0.dp, 20.dp, 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$temperatureEmoji",
                style = TextStyle(fontSize = 30.sp)
            )

            Text(
                text = "${city?.weatherInfo?.hourly?.apparent_temperature?.get(i)}%",
                color = Color.White,
                style = TextStyle(fontSize = 20.sp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$humiditeEmoji",
                style = TextStyle(fontSize = 30.sp)
            )

            Text(
                text = "${city?.weatherInfo?.hourly?.relative_humidity_2m?.get(i)}%",
                color = Color.White,
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }
}

@Composable
fun WeatherPrevisionsList(
    city: CityInfo?,
    i: Int,
    ventEmoji: String,
    pluieEmoji: String,
    ensoleilleEmoji: String
) {
    val indexTimeList = mutableListOf<Int>()

    // Récupérer les prévisions horaires
    city?.weatherInfo?.hourly?.time?.let { timeList ->
        timeList.forEachIndexed { index, time ->
            if (index > i && index < i + 24) {  // Ajouter seulement les prochaines 24 heures
                indexTimeList.add(index)
            }
        }
    }

    // Affichage avec LazyRow
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Utilisation de itemsIndexed pour accéder à l'index et à l'élément
        itemsIndexed(indexTimeList) { indexInList, index ->
            val time = city?.weatherInfo?.hourly?.time?.get(index)
            val temperature =
                city?.weatherInfo?.hourly?.temperature_2m?.get(index)?.toString() ?: "--"
            val weatherCondition = city?.weatherInfo?.hourly?.rain?.get(index)
                ?: 0.0
            val windSpeed = city?.weatherInfo?.hourly?.wind_speed_10m?.get(index) ?: 0.0

            var emoji: String;

            if (weatherCondition > 0.0) {
                emoji = pluieEmoji
            } else {
                emoji = ensoleilleEmoji
            }

            // Carte de prévision pour chaque heure
            WeatherPrevisionCard(
                hourAndMinutes = time?.substring(11, 16) ?: "--:--", // Format HH:mm
                temperature = temperature,
                emoji = emoji,
                windSpeed = windSpeed,
                windEmoji = ventEmoji
            )
        }
    }
}

@Composable
fun WeatherPrevisionCard(
    hourAndMinutes: String,
    temperature: String,
    emoji: String,
    windSpeed: Double,
    windEmoji: String,
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)) // Coins arrondis
            .background(Color(0x562B3B36)) // Fond semi-transparent
            .padding(12.dp) // Réduction de padding pour rendre la box plus petite
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centrer verticalement le contenu
        ) {
            Text(
                text = hourAndMinutes,
                color = Color.White,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ) // Taille réduite pour l'heure
            )

            // Affichage de l'emoji de la météo
            Text(
                text = emoji,
                fontSize = 24.sp // Réduire la taille de l'emoji
            )

            Spacer(modifier = Modifier.height(4.dp)) // Réduire l'espace entre les éléments

            Text(
                text = "$temperature°C",
                color = Color.White,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ) // Taille réduite pour la température
            )

            Spacer(modifier = Modifier.height(4.dp)) // Réduire l'espace entre les éléments

            // Remplacer l'emoji par du texte "Wind:" et ajuster la taille
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = windEmoji,
                    style = TextStyle(fontSize = 10.sp) // Taille réduite pour la vitesse du vent
                )

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    text = "Wind: $windSpeed km/h",
                    color = Color.White,
                    style = TextStyle(fontSize = 12.sp) // Taille réduite pour la vitesse du vent
                )
            }


            Spacer(modifier = Modifier.height(4.dp)) // Espacement réduit entre les éléments


        }
    }
}

