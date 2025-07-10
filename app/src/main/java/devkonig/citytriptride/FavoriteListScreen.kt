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
        if (favoriteCities.isEmpty()) {
            Text(
                "No favorite cities yet.",
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
                            .padding(8.dp)
                            .clickable { navController.navigate("city/${cityWithId.id}") },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(cityWithId.city.name, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}