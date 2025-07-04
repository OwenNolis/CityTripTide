package devkonig.citytriptride

import com.google.firebase.firestore.GeoPoint

data class Sight(
    val name: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val imageUrl: String = ""
)