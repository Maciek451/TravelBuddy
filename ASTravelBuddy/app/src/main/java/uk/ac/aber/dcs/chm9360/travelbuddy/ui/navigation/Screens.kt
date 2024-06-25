package uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation

sealed class Screens(
    val route: String
) {
    object MyTrips : Screens("my_trips")
    object Map : Screens("map")
    object Add : Screens("add")
    object Explore : Screens("explore")
    object Friends : Screens("friends")
    object Account : Screens("account")
    object About : Screens("about")
    object Notification: Screens("notification")
    object SignIn: Screens("sign_in")
    object SignUp: Screens("sign_up")
}

val screens = listOf(
    Screens.MyTrips,
    Screens.Map,
    Screens.Add,
    Screens.Explore,
    Screens.Friends
)