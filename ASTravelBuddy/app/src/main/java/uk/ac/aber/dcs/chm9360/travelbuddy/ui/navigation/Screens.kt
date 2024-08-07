package uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation

sealed class Screens(
    val route: String
) {
    object MyTrips : Screens("my_trips")
    object TripDetails : Screens("trip_details")
    object EditTrip : Screens("edit_trip")
    object Checklist : Screens("checklist")
    object TripPlan : Screens("trip_plan")
    object AddTripPlan : Screens("add_trip_plan")
    object TripMap : Screens("trip_map")
    object Map : Screens("map")
    object AddSheet : Screens("add")
    object Explore : Screens("explore")
    object Friends : Screens("friends")
    object Account : Screens("account")
    object About : Screens("about")
    object Notification: Screens("notification")
    object SignIn: Screens("sign_in")
    object SignUp: Screens("sign_up")
    object AddTrip: Screens("add_trip")
    object AddPhrase: Screens("add_phrase")
    object AddFriend: Screens("add_friend")
    object FriendsList: Screens("list_of_friends")
    object Profile: Screens("profile")
}

val screens = listOf(
    Screens.MyTrips,
    Screens.Map,
    Screens.AddSheet,
    Screens.Explore,
    Screens.Friends
)