package devkonig.citytriptride

data class SightRating(
    val userId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val cityId: String = "",
)