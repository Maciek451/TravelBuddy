package uk.ac.aber.dcs.chm9360.travelbuddy.model

data class Trip(
    val id: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val friends: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val id: String = "",
    val task: String = "",
    val isChecked: Boolean = false
)