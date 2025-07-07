package devkonig.citytriptride

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun FavoriteListScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController, currentRoute = "favorites") }
    ) { innerPadding ->
        Text(
            "Favorite List Screen",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        )
    }
}