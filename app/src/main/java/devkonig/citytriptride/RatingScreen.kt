package devkonig.citytriptride

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RatingScreen(
    navController: NavController,
    currentRoute: String?,
    viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val cities by viewModel.cities.collectAsState()
    var expandedCityId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var ratings by remember { mutableStateOf<List<CityRating>>(emptyList()) }
    var ratingsLoading by remember { mutableStateOf(false) }
    var sightRatingsMap by remember { mutableStateOf<Map<String, Map<String, Double>>>(emptyMap()) }
    var sightRatingsListMap by remember { mutableStateOf<Map<String, Map<String, List<SightRating>>>>(emptyMap()) }

    // Filter out the default/empty city
    val filteredCities = cities.filter {
        it.id != "cityId" && it.city.location.latitude != 0.0 && it.city.location.longitude != 0.0
    }

    // Filter cities and sights by search query
    val searchResults = if (searchQuery.isBlank()) {
        filteredCities
    } else {
        filteredCities.mapNotNull { cityWithId ->
            val city = cityWithId.city
            val cityMatches = city.name.contains(searchQuery, ignoreCase = true)
            val matchingSights = city.sights.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (cityMatches || matchingSights.isNotEmpty()) {
                cityWithId.copy(city = city.copy(sights = if (cityMatches) city.sights else matchingSights))
            } else null
        }
    }

    // Load city ratings and sight ratings when a city is expanded
    LaunchedEffect(expandedCityId) {
        if (expandedCityId != null) {
            ratingsLoading = true
            FirebaseFirestore.getInstance()
                .collection("cities").document(expandedCityId!!)
                .collection("ratings")
                .get()
                .addOnSuccessListener { snapshot ->
                    ratings = snapshot.documents.mapNotNull { it.toObject(CityRating::class.java) }
                    ratingsLoading = false
                }
                .addOnFailureListener {
                    ratings = emptyList()
                    ratingsLoading = false
                }

            // Fetch sight ratings for the expanded city
            FirebaseFirestore.getInstance()
                .collection("cities").document(expandedCityId!!)
                .collection("sightRatings")
                .get()
                .addOnSuccessListener { snapshot ->
                    val ratingsBySight = mutableMapOf<String, MutableList<Int>>()
                    val ratingsListBySight = mutableMapOf<String, MutableList<SightRating>>()
                    snapshot.documents.forEach { doc ->
                        val sightName = doc.getString("sightName") ?: return@forEach
                        val rating = doc.getLong("rating")?.toInt() ?: return@forEach
                        ratingsBySight.getOrPut(sightName) { mutableListOf() }.add(rating)
                        val sightRating = doc.toObject(SightRating::class.java)
                        if (sightRating != null) {
                            ratingsListBySight.getOrPut(sightName) { mutableListOf() }.add(sightRating)
                        }
                    }
                    val avgMap = ratingsBySight.mapValues { (_, ratings) ->
                        if (ratings.isNotEmpty()) ratings.average() else 0.0
                    }
                    sightRatingsMap = sightRatingsMap.toMutableMap().apply {
                        put(expandedCityId!!, avgMap)
                    }
                    sightRatingsListMap = sightRatingsListMap.toMutableMap().apply {
                        put(expandedCityId!!, ratingsListBySight)
                    }
                }
        } else {
            ratings = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CityTripTide")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search cities or sights") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(searchResults) { cityWithId ->
                    val city = cityWithId.city
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                expandedCityId = if (expandedCityId == cityWithId.id) null else cityWithId.id
                            },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(city.name, style = MaterialTheme.typography.h6)
                            if (expandedCityId == cityWithId.id || (searchQuery.isNotBlank() && city.sights.isNotEmpty())) {
                                Spacer(modifier = Modifier.height(4.dp))
                                // Ratings section
                                Text("Ratings:", style = MaterialTheme.typography.subtitle1)
                                // --- Average city rating ---
                                val averageCityRating = if (ratings.isNotEmpty()) {
                                    ratings.map { it.rating }.average()
                                } else 0.0
                                Text(
                                    text = "Average City Rating: ${"%.1f".format(averageCityRating)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                if (ratingsLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else if (ratings.isEmpty()) {
                                    Text("No ratings yet.", style = MaterialTheme.typography.body2)
                                } else {
                                    ratings.forEach { rating ->
                                        Text("${rating.userId}: ${rating.comment} (${rating.rating}/5)", style = MaterialTheme.typography.body2)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Sights section
                                Text("Sights:", style = MaterialTheme.typography.subtitle1)
                                city.sights.forEach { sight ->
                                    val avgRating = sightRatingsMap[cityWithId.id]?.get(sight.name) ?: 0.0
                                    val sightRatingsList = sightRatingsListMap[cityWithId.id]?.get(sight.name) ?: emptyList()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Text("• ${sight.name}", style = MaterialTheme.typography.body2)
                                        if (avgRating > 0.0) {
                                            Text(
                                                text = "Average Rating: ${"%.1f".format(avgRating)}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "No ratings yet.",
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                        sightRatingsList.forEach { r ->
                                            Text(
                                                text = "${r.userId}: ${r.comment} (${r.rating}/5)",
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}