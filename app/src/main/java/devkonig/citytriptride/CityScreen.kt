package devkonig.citytriptride

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color

@Composable
fun CityScreen(cityId: String, navController: NavController) {
    // --- State variables ---
    var city by remember { mutableStateOf<City?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedLatitude by remember { mutableStateOf("") }
    var editedLongitude by remember { mutableStateOf("") }
    var editedImageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // --- Rating dialog state ---
    var showRatingDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var ratingError by remember { mutableStateOf<String?>(null) }

    // --- City ratings state ---
    var cityRatings by remember { mutableStateOf<List<CityRating>>(emptyList()) }
    var ratingsLoading by remember { mutableStateOf(false) }
    var averageCityRating by remember { mutableDoubleStateOf(0.0) }

    // Load city data
    LaunchedEffect(cityId) {
        FirebaseFirestore.getInstance()
            .collection("cities")
            .document(cityId)
            .get()
            .addOnSuccessListener { doc ->
                city = doc.toObject(City::class.java)
            }
    }

    // Check if city is favorite for this user
    LaunchedEffect(cityId, userId) {
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("favorites").document(cityId)
                .get()
                .addOnSuccessListener { doc ->
                    isFavorite = doc.exists()
                }
        }
    }

    // Fetch city ratings
    LaunchedEffect(cityId, showRatingDialog) {
        ratingsLoading = true
        FirebaseFirestore.getInstance()
            .collection("cities").document(cityId)
            .collection("ratings")
            .get()
            .addOnSuccessListener { snapshot ->
                val ratingsList = snapshot.documents.mapNotNull { it.toObject(CityRating::class.java) }
                cityRatings = ratingsList
                averageCityRating = if (ratingsList.isNotEmpty()) {
                    ratingsList.map { it.rating }.average()
                } else 0.0
                ratingsLoading = false
            }
            .addOnFailureListener {
                cityRatings = emptyList()
                averageCityRating = 0.0
                ratingsLoading = false
            }
    }

    // --- Scaffold with top bar, bottom bar, and content ---
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
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute = "city")
        }
    ) { innerPadding ->
        city?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
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
                            IconButton(
                                onClick = {
                                    if (userId != null) {
                                        val favRef = FirebaseFirestore.getInstance()
                                            .collection("users").document(userId)
                                            .collection("favorites").document(cityId)
                                        if (isFavorite) {
                                            favRef.delete()
                                        } else {
                                            favRef.set(mapOf("cityId" to cityId))
                                        }
                                        isFavorite = !isFavorite
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite"
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
                                painter = rememberAsyncImagePainter(c.imageUrl),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                                Button(onClick = { showRatingDialog = true }) {
                                    Text("Give rating")
                                }
                            }
                            // --- City ratings display ---
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Average City Rating: ${"%.1f".format(averageCityRating)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (ratingsLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else if (cityRatings.isEmpty()) {
                                Text("No ratings yet.", style = MaterialTheme.typography.body2)
                            } else {
                                cityRatings.forEach { r ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .border(
                                                width = 2.dp,
                                                color = Color(0xFF444444).copy(alpha = 0.4f),
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Rating: ${r.rating}", fontWeight = FontWeight.Bold)
                                            Text("Comment: ${r.comment}")
                                            Text("User: ${r.userId}", style = MaterialTheme.typography.caption)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sights",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.h4,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { navController.navigate("addSight/$cityId") }
                            ) {
                                Text("Add Sight")
                            }
                        }
                    }
                    item {
                        SightsGrid(
                            sights = c.sights,
                            onSightClick = { sight ->
                                navController.navigate("sight/$cityId/${sight.name}")
                            }
                        )
                    }
                }
                if (!isEditing) {
                    Button(
                        onClick = {
                            isDeleting = true
                            errorMessage = null
                            val db = FirebaseFirestore.getInstance()
                            db.collection("cities").document(cityId)
                                .delete()
                                .addOnSuccessListener {
                                    isDeleting = false
                                    navController.navigate("home") {
                                        popUpTo("city/$cityId") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Failed to delete city: ${e.message}"
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
                // --- Error message display ---
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = MaterialTheme.colors.error)
                }
                // --- Rating Dialog ---
                if (showRatingDialog) {
                    AlertDialog(
                        onDismissRequest = { showRatingDialog = false },
                        title = { Text("Give rating") },
                        text = {
                            Column {
                                HeartRatingBar(rating = rating, onRatingChanged = { rating = it })
                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Your comment") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (ratingError != null) {
                                    Text(ratingError!!, color = MaterialTheme.colors.error)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (userId == null) {
                                        ratingError = "You must be logged in."
                                        return@Button
                                    }
                                    if (rating == 0) {
                                        ratingError = "Please select a rating."
                                        return@Button
                                    }
                                    isSubmitting = true
                                    ratingError = null
                                    val db = FirebaseFirestore.getInstance()
                                    val ratingObj = mapOf(
                                        "userId" to userId,
                                        "rating" to rating,
                                        "comment" to comment
                                    )
                                    db.collection("cities").document(cityId)
                                        .collection("ratings").document(userId)
                                        .set(ratingObj)
                                        .addOnSuccessListener {
                                            isSubmitting = false
                                            showRatingDialog = false
                                            rating = 0
                                            comment = ""
                                        }
                                        .addOnFailureListener { e ->
                                            ratingError = "Failed to submit: ${e.message}"
                                            isSubmitting = false
                                        }
                                },
                                enabled = !isSubmitting
                            ) {
                                Text(if (isSubmitting) "Submitting..." else "Submit")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showRatingDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
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