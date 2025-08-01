@file:Suppress("UNCHECKED_CAST")

package devkonig.citytriptride

import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AddCityScreen(navController: NavController) {
    // City form state
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showSightDialog by remember { mutableStateOf(false) }

    // Sight form state
    var sightName by remember { mutableStateOf("") }
    var sightDescription by remember { mutableStateOf("") }
    var sightLatitude by remember { mutableStateOf("") }
    var sightLongitude by remember { mutableStateOf("") }
    var sightImageUrl by remember { mutableStateOf("") }
    var sights by remember { mutableStateOf(listOf<Sight>()) }

    // Coroutine scope for async operations
    val scope = rememberCoroutineScope()

    // Scaffold for the screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CityTripTide") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // City form
                Text("Add city", style = MaterialTheme.typography.h5)
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") })
                TextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") })
                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                TextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })

                // Sight form
                Divider()
                Text("Add Sight", style = MaterialTheme.typography.h6)
                TextField(value = sightName, onValueChange = { sightName = it }, label = { Text("Sight Name") })
                TextField(value = sightDescription, onValueChange = { sightDescription = it }, label = { Text("Sight Description") })
                TextField(value = sightLatitude, onValueChange = { sightLatitude = it }, label = { Text("Sight Latitude") })
                TextField(value = sightLongitude, onValueChange = { sightLongitude = it }, label = { Text("Sight Longitude") })
                TextField(value = sightImageUrl, onValueChange = { sightImageUrl = it }, label = { Text("Sight Image URL") })
                Button(
                    onClick = {
                        // Validate sight input
                        val lat = sightLatitude.toDoubleOrNull()
                        val lon = sightLongitude.toDoubleOrNull()
                        if (sightName.isNotBlank() && lat != null && lon != null) {
                            scope.launch {
                                // 1. Local list check (case-insensitive)
                                if (sights.any { it.name.lowercase() == sightName.lowercase() }) {
                                    showSightDialog = true
                                    return@launch
                                }
                                // 2. If city exists, check in Firestore for this city's sights field (case-insensitive)
                                val cityDocs = FirebaseFirestore.getInstance()
                                    .collection("cities")
                                    .whereEqualTo("name", name)
                                    .get()
                                    .await()
                                if (!cityDocs.isEmpty) {
                                    val cityData = cityDocs.documents[0].data
                                    val citySights = cityData?.get("sights") as? List<Map<String, Any>>
                                    if (citySights != null && citySights.any { (it["name"] as? String)?.lowercase() == sightName.lowercase() }) {
                                        showSightDialog = true
                                        return@launch
                                    }
                                }
                                // 3. All checks passed, add sight
                                sights = sights + Sight(
                                    name = sightName,
                                    description = sightDescription,
                                    location = GeoPoint(lat, lon),
                                    imageUrl = sightImageUrl
                                )
                                // Clear sight fields
                                sightName = ""
                                sightDescription = ""
                                sightLatitude = ""
                                sightLongitude = ""
                                sightImageUrl = ""
                            }
                        }
                    }
                ) {
                    Text("Add Sight")
                }

                // Show added sights
                sights.forEach { sight ->
                    Text("- ${sight.name} (${sight.location.latitude}, ${sight.location.longitude})")
                }

                // Error message display
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colors.error)
                }

                // Save city button
                Button(
                    onClick = {
                        isSaving = true
                        errorMessage = null
                        val lat = latitude.toDoubleOrNull()
                        val lon = longitude.toDoubleOrNull()
                        if (lat == null || lon == null) {
                            errorMessage = "Invalid latitude or longitude"
                            isSaving = false
                            return@Button
                        }
                        // Check if city exists (case-insensitive)
                        FirebaseFirestore.getInstance()
                            .collection("cities")
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.any { (it.getString("name") ?: "").lowercase() == name.lowercase() }) {
                                    isSaving = false
                                    showDialog = true
                                } else {
                                    val city = hashMapOf(
                                        "name" to name,
                                        "description" to description,
                                        "imageUrl" to imageUrl,
                                        "location" to GeoPoint(lat, lon),
                                        "sights" to sights
                                    )
                                    // Save city to Firestore
                                    FirebaseFirestore.getInstance()
                                        .collection("cities")
                                        .add(city)
                                        .addOnSuccessListener { navController.popBackStack() }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Failed to save: ${e.message}"
                                            isSaving = false
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Failed to check city: ${e.message}"
                                isSaving = false
                            }
                    },
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            }
            // Cancel button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Text("Cancel")
            }
            // Show error dialogs
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Error") },
                    text = { Text("City already exists.") },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) { Text("OK") }
                    }
                )
            }
            // Show sight error dialog
            if (showSightDialog) {
                AlertDialog(
                    onDismissRequest = { showSightDialog = false },
                    title = { Text("Error") },
                    text = { Text("Sight already exists.") },
                    confirmButton = {
                        Button(onClick = { showSightDialog = false }) { Text("OK") }
                    }
                )
            }
        }
    }
}