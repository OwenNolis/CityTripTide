package devkonig.citytriptride

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Set the background color for the entire screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CityTripTide",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Divider(
            color = Color(0xFF444444).copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Button(
            onClick = onNavigateToSignup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign up")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}