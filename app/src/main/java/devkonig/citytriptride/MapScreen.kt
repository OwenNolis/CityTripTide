package devkonig.citytriptride

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val cities by viewModel.cities.collectAsState()
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }
    var expandedCityId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f)
    }

    // Filter out the default/empty city
    val filteredCities = cities.filter {
        it.id != "cityId" && it.city.location.latitude != 0.0 && it.city.location.longitude != 0.0
    }

    // Filter cities and sights by search query
    val searchResults = if (searchQuery.isBlank()) {
        filteredCities
    } else {
        filteredCities.mapNotNull { cityWithId ->
            val city = cityWithId.city
            val cityMatches = city.name.contains(searchQuery, ignoreCase = true)
            val matchingSights = city.sights.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (cityMatches || matchingSights.isNotEmpty()) {
                cityWithId.copy(city = city.copy(sights = if (cityMatches) city.sights else matchingSights))
            } else null
        }
    }

    // Move camera and update address when selectedLocation changes
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 12f))
            selectedLocationName = LocationUtils.getLocationName(context, it.latitude, it.longitude)
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
        bottomBar = { BottomNavBar(navController, currentRoute = "map") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Map at the top
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> selectedLocation = latLng }
                ) {
                    filteredCities.forEach { cityWithId ->
                        val city = cityWithId.city
                        val cityLoc = city.location
                        Marker(
                            state = MarkerState(position = LatLng(cityLoc.latitude, cityLoc.longitude)),
                            title = city.name
                        )
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
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = selectedLocationName ?: "Selected Location",
                            snippet = "Lat: %.5f, Lng: %.5f".format(it.latitude, it.longitude)
                        )
                    }
                }
            }
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search cities or sights") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            // List of cities and sights
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(searchResults) { cityWithId ->
                    val city = cityWithId.city
                    val cityLoc = city.location
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                expandedCityId = if (expandedCityId == cityWithId.id) null else cityWithId.id
                                selectedLocation = LatLng(cityLoc.latitude, cityLoc.longitude)
                            },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(city.name, style = MaterialTheme.typography.h6)
                            if (expandedCityId == cityWithId.id || (searchQuery.isNotBlank() && city.sights.isNotEmpty())) {
                                Spacer(modifier = Modifier.height(4.dp))
                                city.sights.forEach { sight ->
                                    val sightLoc = sight.location
                                    if (sightLoc.latitude != 0.0 && sightLoc.longitude != 0.0) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedCityId = cityWithId.id
                                                    selectedLocation = LatLng(sightLoc.latitude, sightLoc.longitude)
                                                }
                                                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Text("• ${sight.name}", style = MaterialTheme.typography.body2)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}