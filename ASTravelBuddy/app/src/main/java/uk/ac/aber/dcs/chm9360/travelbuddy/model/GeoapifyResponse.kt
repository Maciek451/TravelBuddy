package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class GeoapifyResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: PlaceProperties
)

data class PlaceProperties(
    val name: String,
    val address: String,
)

data class GeocodeResponse(
    val features: List<GeocodeFeature>
)

data class GeocodeFeature(
    // Bounding box defining geo area
    val bbox: List<Double>?
)