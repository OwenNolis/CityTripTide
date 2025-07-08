package devkonig.citytriptride

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CityScreen(cityId: String, navController: NavController) {
    var city by remember { mutableStateOf<City?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedLatitude by remember { mutableStateOf("") }
    var editedLongitude by remember { mutableStateOf("") }
    var editedImageUrl by remember { mutableStateOf("") }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isEditing) {
                            TextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("City Name") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = c.name,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isEditing) {
                            Button(onClick = {
                                val lat = editedLatitude.toDoubleOrNull() ?: c.location.latitude
                                val lon = editedLongitude.toDoubleOrNull() ?: c.location.longitude
                                FirebaseFirestore.getInstance()
                                    .collection("cities")
                                    .document(cityId)
                                    .update(
                                        mapOf(
                                            "name" to editedName,
                                            "description" to editedDescription,
                                            "imageUrl" to editedImageUrl,
                                            "location" to GeoPoint(lat, lon)
                                        )
                                    )
                                    .addOnSuccessListener {
                                        city = City(
                                            name = editedName,
                                            description = editedDescription,
                                            imageUrl = editedImageUrl,
                                            location = GeoPoint(lat, lon),
                                            sights = c.sights
                                        )
                                        isEditing = false
                                    }
                            }) {
                                Text("Save")
                            }
                        } else {
                            IconButton(onClick = {
                                isEditing = true
                                editedName = c.name
                                editedDescription = c.description
                                editedLatitude = c.location.latitude.toString()
                                editedLongitude = c.location.longitude.toString()
                                editedImageUrl = c.imageUrl
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Edit City", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Edit, contentDescription = "Edit City")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isEditing) {
                        TextField(
                            value = editedImageUrl,
                            onValueChange = { editedImageUrl = it },
                            label = { Text("Image URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Image(
                            painter = rememberImagePainter(c.imageUrl),
                            contentDescription = c.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEditing) {
                        TextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = editedLatitude,
                            onValueChange = { editedLatitude = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = editedLongitude,
                            onValueChange = { editedLongitude = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = c.description,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Latitude: ${c.location.latitude}",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.body1
                        )
                        Text(
                            text = "Longitude: ${c.location.longitude}",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sights",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h4
                    )
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