package devkonig.citytriptride

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun MapScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController, currentRoute = "map") }
    ) { innerPadding ->
        Text(
            "Map Screen",
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        )
    }
}