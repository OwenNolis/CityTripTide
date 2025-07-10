package devkonig.citytriptride

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val cities by viewModel.cities.collectAsState()
    val sights by viewModel.sights.collectAsState()
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material.Text("CityTripTide")
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController, currentRoute = "map") }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng -> selectedLocation = latLng }
            ) {
                // Inside GoogleMap { ... }
                cities.forEach { cityWithId ->
                    val city = cityWithId.city
                    val cityLoc = city.location
                    // Only show valid city locations
                    if (cityLoc.latitude != 0.0 && cityLoc.longitude != 0.0) {
                        Marker(
                            state = MarkerState(position = LatLng(cityLoc.latitude, cityLoc.longitude)),
                            title = city.name
                        )
                    }
                    // Show all sights for this city
                    city.sights.forEach { sight ->
                        val sightLoc = sight.location
                        if (sightLoc.latitude != 0.0 && sightLoc.longitude != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(sightLoc.latitude, sightLoc.longitude)),
                                title = sight.name
                            )
                        }
                    }
                }
                // Optionally, show selected location
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Selected Location"
                    )
                }
            }
        }
    }
}