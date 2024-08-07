package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class Trip(
    val id: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val friends: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val tripPlans: List<TripPlanItem> = emptyList()
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