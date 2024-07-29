package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class Destination(
    val name: String,
    val country: String? = null
)

data class GeoDbResponse(
    val data: List<Destination>
)

data class PixabayResponse(
    val hits: List<Hit>
)

data class Hit(
    val largeImageURL: String
)