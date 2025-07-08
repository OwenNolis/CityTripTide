package devkonig.citytriptride

import com.google.firebase.firestore.GeoPoint

data class City(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val sights: List<Sight> = emptyList()
)