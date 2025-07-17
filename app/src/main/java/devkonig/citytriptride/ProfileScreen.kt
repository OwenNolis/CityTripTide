package devkonig.citytriptride

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter

private const val PREFS_NAME = "profile_prefs"
private const val KEY_IMAGE_URI_PREFIX = "profile_image_uri_"

// ProfileScreen displays the user's profile information and allows them to change their profile picture.
@Composable
fun ProfileScreen(navController: NavController) {
    // Get the current context and shared preferences
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val user = FirebaseAuth.getInstance().currentUser
    val userUid = user?.uid
    val userEmail = user?.email ?: "No email"
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Load saved URI for this user after composition
    LaunchedEffect(userUid) {
        if (userUid != null) {
            val savedUri = prefs.getString(KEY_IMAGE_URI_PREFIX + userUid, null)
            if (savedUri != null && imageUri == null) {
                imageUri = savedUri.toUri()
            }
        }
    }

    // Launcher to pick an image from the gallery
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && userUid != null) {
            imageUri = uri
            prefs.edit { putString(KEY_IMAGE_URI_PREFIX + userUid, uri.toString()) }
        }
    }

    // Scaffold to provide a top bar and bottom navigation
    Scaffold(
        bottomBar = {
            BottomNavBar(navController, currentRoute = "profile")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp)) // Add space at the top
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clickable { launcher.launch("image/*") }
                    .background(Color.Gray, shape = CircleShape)
                    .border(2.dp, Color.DarkGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(imageUri)
                                .size(Size.ORIGINAL)
                                .build()
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(128.dp)
                    )
                } else {
                    Text(
                        text = userEmail.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userEmail, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}