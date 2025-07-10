package devkonig.citytriptride

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SightScreen(cityId: String, sightName: String, navController: NavController) {
    var sight by remember { mutableStateOf<Sight?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedLatitude by remember { mutableStateOf("") }
    var editedLongitude by remember { mutableStateOf("") }
    var editedImageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Favorite state
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var isFavorite by remember { mutableStateOf(false) }

    // Check if sight is favorite
    LaunchedEffect(cityId, sightName, userId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("favoriteSights")
                .document("$cityId|$sightName")
                .get()
                .addOnSuccessListener { doc ->
                    isFavorite = doc.exists()
                }
        }
    }

    // Load sight
    LaunchedEffect(cityId, sightName) {
        FirebaseFirestore.getInstance()
            .collection("cities")
            .document(cityId)
            .get()
            .addOnSuccessListener { doc ->
                val sights = doc.get("sights") as? List<Map<String, Any>>
                val found = sights?.firstOrNull {
                    (it["name"] as? String)?.trim()?.equals(sightName, ignoreCase = true) == true
                }
                found?.let {
                    sight = Sight(
                        name = it["name"] as String,
                        description = it["description"] as? String ?: "",
                        location = it["location"] as GeoPoint,
                        imageUrl = it["imageUrl"] as? String ?: ""
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sight Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "sight")
        }
    ) { innerPadding ->
        sight?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isEditing) {
                            TextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Sight Name") },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = s.name,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Favorite star icon (only when not editing)
                        if (!isEditing) {
                            IconButton(
                                onClick = {
                                    if (userId != null && sight != null) {
                                        val db = FirebaseFirestore.getInstance()
                                        val favSightRef = db.collection("users").document(userId)
                                            .collection("favoriteSights").document("$cityId|$sightName")
                                        val favCityRef = db.collection("users").document(userId)
                                            .collection("favorites").document(cityId)
                                        if (isFavorite) {
                                            favSightRef.delete()
                                                .addOnSuccessListener { isFavorite = false }
                                        } else {
                                            // Add city to favorites if not already
                                            favCityRef.get().addOnSuccessListener { cityDoc ->
                                                if (!cityDoc.exists()) {
                                                    favCityRef.set(mapOf("timestamp" to System.currentTimeMillis()))
                                                }
                                                favSightRef.set(
                                                    mapOf(
                                                        "cityId" to cityId,
                                                        "sightName" to sightName,
                                                        "timestamp" to System.currentTimeMillis()
                                                    )
                                                ).addOnSuccessListener { isFavorite = true }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                                    tint = if (isFavorite) MaterialTheme.colors.primary else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (isEditing) {
                            Button(
                                onClick = {
                                    val lat = editedLatitude.toDoubleOrNull()
                                    val lon = editedLongitude.toDoubleOrNull()
                                    if (editedName.isBlank() || lat == null || lon == null) {
                                        errorMessage = "Please fill all fields with valid values"
                                        return@Button
                                    }
                                    isSaving = true
                                    errorMessage = null
                                    val db = FirebaseFirestore.getInstance()
                                    val cityRef = db.collection("cities").document(cityId)
                                    cityRef.get().addOnSuccessListener { doc ->
                                        val sights = doc.get("sights") as? List<Map<String, Any>> ?: emptyList()
                                        val oldSight = sights.firstOrNull {
                                            (it["name"] as? String)?.trim()?.equals(s.name, ignoreCase = true) == true
                                        }
                                        if (oldSight != null) {
                                            val newSight = hashMapOf(
                                                "name" to editedName,
                                                "description" to editedDescription,
                                                "location" to GeoPoint(lat, lon),
                                                "imageUrl" to editedImageUrl
                                            )
                                            cityRef.update("sights", com.google.firebase.firestore.FieldValue.arrayRemove(oldSight))
                                                .addOnSuccessListener {
                                                    cityRef.update("sights", com.google.firebase.firestore.FieldValue.arrayUnion(newSight))
                                                        .addOnSuccessListener {
                                                            sight = Sight(
                                                                name = editedName,
                                                                description = editedDescription,
                                                                location = GeoPoint(lat, lon),
                                                                imageUrl = editedImageUrl
                                                            )
                                                            isEditing = false
                                                            isSaving = false
                                                        }
                                                        .addOnFailureListener { e ->
                                                            errorMessage = "Failed to update sight: ${e.message}"
                                                            isSaving = false
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Failed to update sight: ${e.message}"
                                                    isSaving = false
                                                }
                                        } else {
                                            errorMessage = "Sight not found"
                                            isSaving = false
                                        }
                                    }.addOnFailureListener { e ->
                                        errorMessage = "Failed to update sight: ${e.message}"
                                        isSaving = false
                                    }
                                },
                                enabled = !isSaving
                            ) {
                                Text(if (isSaving) "Saving..." else "Save")
                            }
                        } else {
                            IconButton(onClick = {
                                isEditing = true
                                editedName = s.name
                                editedDescription = s.description
                                editedLatitude = s.location.latitude.toString()
                                editedLongitude = s.location.longitude.toString()
                                editedImageUrl = s.imageUrl
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Edit Sight", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Sight")
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
                    } else if (s.imageUrl.isNotBlank()) {
                        Image(
                            painter = rememberImagePainter(s.imageUrl),
                            contentDescription = s.name,
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
                            text = s.description,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Latitude: ${s.location.latitude}",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.body1
                        )
                        Text(
                            text = "Longitude: ${s.location.longitude}",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = MaterialTheme.colors.error)
                    }
                }
                // Delete button at the bottom
                if (!isEditing) {
                    Button(
                        onClick = {
                            isDeleting = true
                            errorMessage = null
                            val db = FirebaseFirestore.getInstance()
                            val cityRef = db.collection("cities").document(cityId)
                            cityRef.get().addOnSuccessListener { doc ->
                                val sights = doc.get("sights") as? List<Map<String, Any>> ?: emptyList()
                                val oldSight = sights.firstOrNull {
                                    (it["name"] as? String)?.trim()?.equals(s.name, ignoreCase = true) == true
                                }
                                if (oldSight != null) {
                                    cityRef.update("sights", com.google.firebase.firestore.FieldValue.arrayRemove(oldSight))
                                        .addOnSuccessListener {
                                            isDeleting = false
                                            navController.navigate("city/$cityId") {
                                                popUpTo("sight/$cityId/$sightName") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Failed to delete sight: ${e.message}"
                                            isDeleting = false
                                        }
                                } else {
                                    errorMessage = "Sight not found"
                                    isDeleting = false
                                }
                            }.addOnFailureListener { e ->
                                errorMessage = "Failed to delete sight: ${e.message}"
                                isDeleting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = !isDeleting
                    ) {
                        Text(if (isDeleting) "Deleting..." else "Delete", color = Color.White)
                    }
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