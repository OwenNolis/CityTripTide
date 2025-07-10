package devkonig.citytriptride

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FavoriteListScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var favoriteCities by remember { mutableStateOf<List<CityWithId>>(emptyList()) }
    var favoriteSightsByCity by remember { mutableStateOf<Map<String, List<FavoriteSight>>>(emptyMap()) }

    // Fetch favorite cities
    LaunchedEffect(userId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener { favDocs ->
                    val cityIds = favDocs.documents.mapNotNull { it.id }
                    if (cityIds.isNotEmpty()) {
                        db.collection("cities")
                            .whereIn(FieldPath.documentId(), cityIds)
                            .get()
                            .addOnSuccessListener { cityDocs ->
                                favoriteCities = cityDocs.documents.mapNotNull { doc ->
                                    doc.toObject(City::class.java)?.let { city ->
                                        CityWithId(doc.id, city)
                                    }
                                }
                            }
                    } else {
                        favoriteCities = emptyList()
                    }
                }
        }
    }

    // Fetch favorite sights and group by cityId
    LaunchedEffect(userId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("favoriteSights")
                .get()
                .addOnSuccessListener { docs ->
                    val grouped = docs.documents.mapNotNull { doc ->
                        val cityId = doc.getString("cityId")
                        val sightName = doc.getString("sightName")
                        if (cityId != null && sightName != null) {
                            FavoriteSight(cityId, sightName)
                        } else null
                    }.groupBy { it.cityId }
                    favoriteSightsByCity = grouped
                }
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
        bottomBar = { BottomNavBar(navController, currentRoute = "favorites") }
    ) { innerPadding ->
        if (favoriteCities.isEmpty() && favoriteSightsByCity.isEmpty()) {
            Text(
                "No favorite cities or sights yet.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                items(favoriteCities) { cityWithId ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                cityWithId.city.name,
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { navController.navigate("city/${cityWithId.id}") }
                            )
                            // Show favorite sights for this city
                            val sights = favoriteSightsByCity[cityWithId.id] ?: emptyList()
                            if (sights.isNotEmpty()) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                    sights.forEach { favSight ->
                                        Text(
                                            favSight.sightName,
                                            style = MaterialTheme.typography.body1,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navController.navigate("sight/${cityWithId.id}/${favSight.sightName}")
                                                }
                                                .padding(vertical = 2.dp)
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