package devkonig.citytriptride

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.google.firebase.firestore.GeoPoint

@Composable
fun HomeScreen(navController: NavController) {
    var cities by remember { mutableStateOf<List<City>>(emptyList()) }

    // Fetch cities from Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("cities")
            .get()
            .addOnSuccessListener { result ->
                val cityList = result.documents.mapNotNull { doc ->
                    doc.toObject(City::class.java)
                }
                cities = cityList
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CityTripTide",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                items(cities) { city ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = city.name, style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberImagePainter(city.imageUrl),
                                contentDescription = city.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("addCity") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add City")
        }
    }
}