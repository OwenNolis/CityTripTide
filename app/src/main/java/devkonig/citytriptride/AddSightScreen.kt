@file:Suppress("UNCHECKED_CAST")

package devkonig.citytriptride

import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

@Composable
fun AddSightScreen(cityId: String, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Sight") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Sight Name") })
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            TextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude") })
            TextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude") })
            TextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colors.error)
            }

            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    if (name.isBlank() || lat == null || lon == null) {
                        errorMessage = "Please fill all fields with valid values"
                        return@Button
                    }
                    isSaving = true
                    errorMessage = null

                    // Duplicate check
                    FirebaseFirestore.getInstance()
                        .collection("cities")
                        .document(cityId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val sights = doc.get("sights") as? List<Map<String, Any>>
                            val exists = sights?.any {
                                (it["name"] as? String)?.trim()?.lowercase() == name.trim().lowercase()
                            } ?: false
                            if (exists) {
                                isSaving = false
                                showDuplicateDialog = true
                            } else {
                                val newSight = hashMapOf(
                                    "name" to name,
                                    "description" to description,
                                    "location" to GeoPoint(lat, lon),
                                    "imageUrl" to imageUrl
                                )
                                doc.reference.update("sights", FieldValue.arrayUnion(newSight))
                                    .addOnSuccessListener {
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to add sight: ${e.message}"
                                        isSaving = false
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Failed to check for duplicates: ${e.message}"
                            isSaving = false
                        }
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }

            if (showDuplicateDialog) {
                AlertDialog(
                    onDismissRequest = { showDuplicateDialog = false },
                    title = { Text("Error") },
                    text = { Text("A sight with this name already exists in this city.") },
                    confirmButton = {
                        Button(onClick = { showDuplicateDialog = false }) { Text("OK") }
                    }
                )
            }
        }
    }
}