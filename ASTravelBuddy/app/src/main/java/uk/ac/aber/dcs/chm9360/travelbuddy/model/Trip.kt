package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class Trip(
    val id: String = "",
    val author: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val checklist: List<ChecklistItem> = emptyList(),
    val tripPlans: List<TripPlanItem> = emptyList(),
    val shared: Boolean = false
)

data class ChecklistItem(
    val id: String = "",
    val task: String = "",
    val checked: String = ""
)

data class TripPlanItem(
    val id: String = "",
    val place: String = "",
    val dateOfVisit: String = ""
)