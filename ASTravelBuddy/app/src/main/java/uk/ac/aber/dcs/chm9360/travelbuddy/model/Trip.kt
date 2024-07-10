package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class Trip(
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val friends: List<String> = emptyList()
)
