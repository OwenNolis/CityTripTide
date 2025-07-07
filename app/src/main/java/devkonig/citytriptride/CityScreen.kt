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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun CityScreen(cityId: String, navController: NavController) {
    var city by remember { mutableStateOf<City?>(null) }

    LaunchedEffect(cityId) {
        FirebaseFirestore.getInstance()
            .collection("cities")
            .document(cityId)
            .get()
            .addOnSuccessListener { doc ->
                city = doc.toObject(City::class.java)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CityTripTide") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "city")
        }
    ) { innerPadding ->
        city?.let { c ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Text(
                        text = c.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Image(
                        painter = rememberImagePainter(c.imageUrl),
                        contentDescription = c.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = c.description,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Latitude: ${c.location.latitude}", fontSize = 20.sp, style = MaterialTheme.typography.body1)
                    Text(text = "Longitude: ${c.location.longitude}", fontSize = 20.sp, style = MaterialTheme.typography.body2)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Sights", fontSize = 23.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h4)
                }
                item {
                    SightsGrid(c.sights)
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}