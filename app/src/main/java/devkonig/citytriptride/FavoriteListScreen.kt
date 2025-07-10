package devkonig.citytriptride

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material.TopAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FavoriteListScreen(navController: NavController) {
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
        bottomBar = { BottomNavBar(navController, currentRoute = "favorites") }
    ) { innerPadding ->
        Text(
            "Favorite List Screen",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        )
    }
}