package uk.ac.aber.dcs.chm9360.travelbuddy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.AddTripScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.about.AboutScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.AccountScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.ProfileScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication.SignInScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication.SignUpScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.ExploreScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.MapScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.PlaceDetailsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends.AddFriendScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends.FriendsListScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends.FriendsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.AddTripPlanFromExploreScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.AddTripPlanScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.ChecklistScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.EditTripPlanScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.EditTripScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.MyTripsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.TripDetailsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.TripMapScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.TripPlanDetailsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.TripPlanScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.notifications.NotificationScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.phrase.AddPhraseScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.theme.TravelBuddyTheme

class MainActivity : ComponentActivity() {
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val startDestination = if (firebaseViewModel.isUserLoggedIn()) {
                        Screens.MyTrips.route
                    } else {
                        Screens.SignIn.route
                    }
                    BuildNavigationGraph(navController, startDestination)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    private fun BuildNavigationGraph(
        navController: NavHostController,
        startDestination: String
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screens.MyTrips.route) { MyTripsScreen(navController) }
            composable(Screens.TripDetails.route) { TripDetailsScreen(navController) }
            composable(Screens.EditTrip.route) { EditTripScreen(navController) }
            composable(Screens.Checklist.route) { ChecklistScreen(navController) }
            composable(Screens.TripPlan.route) { TripPlanScreen(navController) }
            composable(Screens.AddTripPlan.route) { AddTripPlanScreen(navController) }
            composable(Screens.EditTripPlan.route) { EditTripPlanScreen(navController) }
            composable(Screens.TripPlanDetails.route) { TripPlanDetailsScreen(navController) }
            composable(Screens.TripMap.route) { TripMapScreen(navController) }
            composable(Screens.Explore.route) { ExploreScreen(navController) }
            composable(Screens.Map.route) { MapScreen(navController) }
            composable(Screens.PlaceDetails.route) { PlaceDetailsScreen(navController) }
            composable(Screens.AddTripPlanFromExplore.route) { AddTripPlanFromExploreScreen(navController) }
            composable(Screens.Friends.route) { FriendsScreen(navController) }
            composable(Screens.Account.route) { AccountScreen(navController) }
            composable(Screens.About.route) { AboutScreen(navController) }
            composable(Screens.Notification.route) { NotificationScreen(navController) }
            composable(Screens.SignIn.route) { SignInScreen(navController, firebaseViewModel) }
            composable(Screens.SignUp.route) { SignUpScreen(navController, firebaseViewModel) }
            composable(Screens.AddTrip.route) { AddTripScreen(navController) }
            composable(Screens.AddPhrase.route) { AddPhraseScreen(navController) }
            composable(Screens.AddFriend.route) { AddFriendScreen(navController) }
            composable(Screens.FriendsList.route) { FriendsListScreen(navController) }
            composable(Screens.Profile.route) { ProfileScreen(navController) }
        }
    }
}