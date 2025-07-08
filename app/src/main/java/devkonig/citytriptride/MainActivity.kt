package devkonig.citytriptride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            Surface(color = MaterialTheme.colors.background) {
                NavHost(navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            onNavigateToSignup = { navController.navigate("signup") },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }
                    composable("signup") {
                        SignupScreen(
                            onSignup = { _, _ -> },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("home") },
                            onNavigateToSignup = { navController.navigate("signup") }
                        )
                    }
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("addCity") {
                        AddCityScreen(navController)
                    }
                    composable("map") {
                        MapScreen(navController)
                    }
                    composable("favorites") {
                        FavoriteListScreen(navController)
                    }
                    composable("profile") {
                        ProfileScreen(navController)
                    }
                    composable("city/{cityId}") { backStackEntry ->
                        val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
                        CityScreen(cityId = cityId, navController = navController)
                    }
                    composable("addSight/{cityId}") { backStackEntry ->
                        val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
                        AddSightScreen(cityId = cityId, navController = navController)
                    }
                    composable("sight/{cityId}/{sightName}") { backStackEntry ->
                        val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
                        val sightName = backStackEntry.arguments?.getString("sightName") ?: ""
                        SightScreen(cityId = cityId, sightName = sightName, navController = navController)
                    }
                }
            }
        }
    }
}