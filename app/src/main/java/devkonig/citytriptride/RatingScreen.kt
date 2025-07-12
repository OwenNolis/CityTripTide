package devkonig.citytriptride

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Load ratings when a city is expanded
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Text("• ${sight.name}", style = MaterialTheme.typography.body2)
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