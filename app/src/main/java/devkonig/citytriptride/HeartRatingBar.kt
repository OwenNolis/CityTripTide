package devkonig.citytriptride

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row

@Composable
fun HeartRatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChanged(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "$i hearts",
                    tint = if (i <= rating) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}