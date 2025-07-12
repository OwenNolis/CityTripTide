package devkonig.citytriptride

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun RatingScreen(navController: NavController, currentRoute: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CityTripTide") }
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentRoute)
        }
    ) { paddingValues ->
        Text(
            text = "This is the Rating Screen",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(paddingValues)
        )
    }
}