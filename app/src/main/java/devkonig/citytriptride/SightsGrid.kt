package devkonig.citytriptride

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SightsGrid(
    sights: List<Sight>,
    onSightClick: (Sight) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 800.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sights) { sight ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onSightClick(sight) }
                    .border(
                        width = 2.dp,
                        color = Color(0xFF444444).copy(alpha = 0.4f), // subtle dark gray with lower opacity
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = sight.name,
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = rememberAsyncImagePainter(sight.imageUrl),
                        contentDescription = sight.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                    )
                }
            }
        }
    }
}