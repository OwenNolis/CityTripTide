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
                            onSignup = { email, password -> /* handle sign up */ },
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLogin = { email, password -> /* handle login */ },
                            onNavigateToSignup = { navController.navigate("signup") }
                        )
                    }
                }
            }
        }
    }
}