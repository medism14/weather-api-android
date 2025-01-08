package com.example.weatherapi.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weatherapi.Model.CityInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.weatherapi.Database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun WeatherDetailsScreen(
    navController: NavController,
    cityInfoViewModel: CityInfoViewModel,
    isPortrait: Boolean
) {
    var favoris by remember { mutableStateOf(false) }
    val cityInfo = cityInfoViewModel.cityInfo // Récupérer la ville actuelle
    val image: Painter = painterResource(id = com.example.weatherapi.R.drawable.weather_image)
    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val formatterHourAndMinutes = DateTimeFormatter.ofPattern("HH:mm")
    val formattedDateTime = currentDateTime.format(formatter)
    val formattedDateTimeHourAndMinutes = currentDateTime.format(formatterHourAndMinutes)

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // Vérifier si la ville est dans les favoris au chargement
    LaunchedEffect(cityInfo) {
        cityInfo?.let { city ->
            scope.launch {
                try {
                    favoris = db.cityInfoDao().isCityFavorite(city.id).first()
                } catch (e: Exception) {
                    println("Erreur lors de la vérification des favoris : ${e.message}")
                }
            }
        }
    }

    // Mettre à jour l'heure toutes les minutes
    DisposableEffect(Unit) {
        val job = scope.launch {
            while(true) {
                delay(60000) // Attendre 1 minute
                currentDateTime = LocalDateTime.now()
            }
        }
        onDispose {
            job.cancel()
        }
    }

    var i by remember { mutableStateOf(-1) }

    // Mettre à jour l'index i en fonction de l'heure actuelle
    LaunchedEffect(currentDateTime) {
        cityInfo?.weatherInfo?.hourly?.time?.let { timeList ->
            i = timeList.indexOfFirst { time ->
                val timeDate = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
                timeDate.isAfter(currentDateTime) || timeDate.isEqual(currentDateTime)
            }.coerceAtLeast(0)
        }
    }

    val ventEmoji = "\uD83D\uDCA8"  // Vent
    val ensoleilleEmoji = "\u2600\uFE0F"  // Ensoleillé
    val pluieEmoji = "\uD83C\uDF27\uFE0F"  // Pluie
    val humiditeEmoji = "\uD83D\uDCA7"  // Humidité
    val temperatureEmoji = "\uD83C\uDF21"  // Température


    cityInfo?.weatherInfo?.hourly?.time?.let { timeList ->
        timeList.forEachIndexed { index, time ->
            val timeDate = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
            if (formattedDateTime > timeDate.format(formatter)) {
                i = index
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond
        WeatherBackground(image)

        // Contenu principal avec scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(if (isPortrait) 16.dp else 16.dp)
        ) {
            // Barre supérieure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(navController)

                cityInfo?.let { city ->
                    Icon(
                        imageVector = if (favoris) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (favoris) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (favoris) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                favoris = !favoris
                                scope.launch {
                                    try {
                                        if (favoris) {
                                            db.cityInfoDao().insertCity(cityInfo = city)
                                        } else {
                                            db.cityInfoDao().deleteCity(cityInfo = city)
                                        }
                                    } catch (e: Exception) {
                                        println("Erreur lors de la gestion des favoris : ${e.message}")
                                    }
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isPortrait) 16.dp else 8.dp))

            CityName(cityInfo, isPortrait)
            CityAdmin(cityInfo)
            Spacer(modifier = Modifier.height(if (isPortrait) 40.dp else 15.dp))
            CurrentTime(formattedDateTimeHourAndMinutes)
            CityTemperature(cityInfo, i, isPortrait)
            Spacer(modifier = Modifier.height(if (isPortrait) 30.dp else 15.dp))
            WeatherStatsBox(
                city = cityInfo,
                ventEmoji = ventEmoji,
                pluieEmoji = pluieEmoji,
                ensoleilleEmoji = ensoleilleEmoji,
                temperatureEmoji = temperatureEmoji,
                humiditeEmoji = humiditeEmoji,
                i = i,
                isPortrait = isPortrait
            )
            Spacer(modifier = Modifier.height(if (isPortrait) 40.dp else 30.dp))

            // Pour les prévisions, on garde un LazyRow car c'est un défilement horizontal
            Box(modifier = Modifier.fillMaxWidth()) {
                WeatherPrevisionsList(
                    city = cityInfo,
                    i = i,
                    ventEmoji = ventEmoji,
                    pluieEmoji = pluieEmoji,
                    ensoleilleEmoji = ensoleilleEmoji,
                    isPortrait = isPortrait
                )
            }

            // Ajouter un espace en bas pour éviter que le contenu soit coupé
            Spacer(modifier = Modifier.height(16.dp))
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
    IconButton(
        onClick = { navController.navigateUp() },
        modifier = Modifier
            .size(48.dp)
            .background(Color(0x33FFFFFF))
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Retour",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CityName(city: CityInfo?, isPortrait: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (city?.name.isNullOrEmpty()) "${city?.country}" else "${city?.name}",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = if (isPortrait) 38.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
fun CityAdmin(city: CityInfo?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        city?.let {
            // Filtrer les parties non nulles et non vides
            val adminParts = listOfNotNull(
                it.name?.takeIf { name -> name.isNotBlank() },
                it.admin1?.takeIf { admin -> admin.isNotBlank() },
                it.admin2?.takeIf { admin -> admin.isNotBlank() },
                it.admin3?.takeIf { admin -> admin.isNotBlank() }
            ).distinct() // Éviter les doublons potentiels

            // N'afficher que s'il y a des informations à montrer
            if (adminParts.isNotEmpty()) {
                Text(
                    text = adminParts.joinToString(" • "), // Utiliser un point comme séparateur au lieu d'une virgule
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun CurrentTime(formattedDateTimeHourAndMinutes: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = formattedDateTimeHourAndMinutes,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33FFFFFF))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun CityTemperature(city: CityInfo?, i: Int, isPortrait: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isPortrait) 8.dp else 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "${city?.weatherInfo?.hourly?.temperature_2m?.get(i)}",
            color = Color.White,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isPortrait) 120.sp else 80.sp,
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.2f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
        Text(
            text = "°C",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = if (isPortrait) 32.sp else 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = if (isPortrait) 24.dp else 16.dp)
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
    i: Int,
    isPortrait: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isPortrait) 16.dp else 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0x33000000))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isPortrait) 16.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (isPortrait) 16.dp else 12.dp)
        ) {
            WeatherRow(city, ventEmoji, pluieEmoji, ensoleilleEmoji, i, isPortrait)
            WeatherRow2(city, temperatureEmoji, humiditeEmoji, i, isPortrait)
        }
    }
}

@Composable
fun WeatherRow(
    city: CityInfo?,
    ventEmoji: String,
    pluieEmoji: String,
    ensoleilleEmoji: String,
    i: Int,
    isPortrait: Boolean
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
fun WeatherRow2(city: CityInfo?, temperatureEmoji: String, humiditeEmoji: String, i: Int, isPortrait: Boolean) {
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
    ensoleilleEmoji: String,
    isPortrait: Boolean
) {
    val indexTimeList = mutableListOf<Int>()
    val currentHour = LocalDateTime.now().hour

    city?.weatherInfo?.hourly?.time?.let { timeList ->
        timeList.forEachIndexed { index, time ->
            // Afficher les prévisions pour les 24 prochaines heures à partir de l'heure actuelle
            if (index > i && index < i + 24) {
                indexTimeList.add(index)
            }
        }
    }

    // Affichage avec LazyRow
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isPortrait) 8.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isPortrait) 8.dp else 12.dp)
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
                hourAndMinutes = time?.substring(11, 16) ?: "--:--",
                temperature = temperature,
                emoji = emoji,
                isPortrait = isPortrait
            )
        }
    }
}

@Composable
fun WeatherPrevisionCard(
    hourAndMinutes: String,
    temperature: String,
    emoji: String,
    isPortrait: Boolean
) {
    Box(
        modifier = Modifier
            .padding(horizontal = if (isPortrait) 4.dp else 6.dp)
            .width(if (isPortrait) 85.dp else 100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x40FFFFFF))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isPortrait) 8.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Heure
            Text(
                text = hourAndMinutes,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isPortrait) 14.sp else 16.sp
                )
            )

            Spacer(modifier = Modifier.height(if (isPortrait) 2.dp else 4.dp))

            // Température
            Text(
                text = "$temperature°",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isPortrait) 24.sp else 28.sp
                )
            )

            // Emoji météo avec condition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    fontSize = if (isPortrait) 24.sp else 28.sp
                )
                Text(
                    text = if (emoji == "🌧️") "Pluie" else "Soleil",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = if (isPortrait) 12.sp else 14.sp
                    )
                )
            }
        }
    }
}