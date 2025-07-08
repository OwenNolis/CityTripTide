package devkonig.citytriptride

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
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
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SightScreen(cityId: String, sightName: String, navController: NavController) {
    var sight by remember { mutableStateOf<Sight?>(null) }

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
        }
    ) { innerPadding ->
        sight?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(s.name, style = MaterialTheme.typography.h4)
                Spacer(modifier = Modifier.height(8.dp))
                if (s.imageUrl.isNotBlank()) {
                    Image(
                        painter = rememberImagePainter(s.imageUrl),
                        contentDescription = s.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(s.description, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Latitude: ${s.location.latitude}")
                Text("Longitude: ${s.location.longitude}")
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