package devkonig.citytriptride

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    var cities by remember { mutableStateOf<List<CityWithId>>(emptyList()) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 6
    val pageCount = (cities.size + pageSize - 1) / pageSize
    val pagedCities = cities.drop(currentPage * pageSize).take(pageSize)

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("cities")
            .get()
            .addOnSuccessListener { result ->
                val cityList = result.documents
                    .filter { it.id != "cityId" }
                    .mapNotNull { doc ->
                        doc.toObject(City::class.java)?.let { city ->
                            CityWithId(doc.id, city)
                        }
                    }
                    .filter { it.city.name.isNotBlank() }
                cities = cityList
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController, currentRoute = "home")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pagedCities) { cityWithId ->
                        val city = cityWithId.city
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { navController.navigate("city/${cityWithId.id}") },
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = city.name, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(city.imageUrl),
                                    contentDescription = city.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.5f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) { Text("Previous") }

                    Text("Page ${currentPage + 1} of $pageCount")

                    Button(
                        onClick = { if (currentPage < pageCount - 1) currentPage++ },
                        enabled = currentPage < pageCount - 1
                    ) { Text("Next") }
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
}