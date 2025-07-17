package devkonig.citytriptride

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview

// Data class to represent each item in the bottom navigation bar
data class NavBarItem(val icon: ImageVector, val label: String, val route: String)

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    // List of items to be displayed in the bottom navigation bar
    val items = listOf(
        NavBarItem(Icons.Default.Home, "Home", "home"),
        NavBarItem(Icons.Default.Map, "Map", "map"),
        NavBarItem(Icons.Default.Star, "Favorites", "favorites"),
        NavBarItem(Icons.Default.RateReview, "Ratings", "ratings"),
        NavBarItem(Icons.Default.Person, "Profile", "profile")
    )
    // BottomNavigation composable to display the navigation bar
    BottomNavigation {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}