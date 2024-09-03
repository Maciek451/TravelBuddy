package uk.ac.aber.dcs.chm9360.travelbuddy

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.AddTripScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.about.AboutScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.AccountScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.ProfileScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.TermsOfServiceScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication.SetUsernameScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication.SignInScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication.SignUpScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.ExploreScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.MapScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.MapViewScreen
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
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.phrase.EditPhraseScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.theme.TravelBuddyTheme
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.LocaleManager
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.getLanguagePreference

class MainActivity : ComponentActivity() {
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var showNetworkDialog by mutableStateOf(false)
    private var startDestination by mutableStateOf(Screens.SignIn.route)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = applicationContext
        val prefsFlow = getLanguagePreference(context)
        lifecycleScope.launch {
            prefsFlow.collect { language ->
                LocaleManager.setLocale(context, if (language == 0) "en" else "pl")
            }
        }

        if (!isNetworkAvailable(context)) {
            showNetworkDialog = true
        }

        lifecycleScope.launch {
            if (firebaseViewModel.isUserLoggedIn()) {
                firebaseViewModel.checkUsernameAndNavigate { isUsernameRequired ->
                    startDestination = if (isUsernameRequired) {
                        Screens.SetUsername.route
                    } else {
                        Screens.MyTrips.route
                    }
                    setContent {
                        TravelBuddyTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                val navController = rememberNavController()
                                BuildNavigationGraph(navController, startDestination)

                                if (showNetworkDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNetworkDialog = false },
                                        title = { Text(stringResource(R.string.no_internet_connection)) },
                                        text = { Text(stringResource(R.string.no_internet_connection_message)) },
                                        confirmButton = {
                                            Button(onClick = { showNetworkDialog = false }) {
                                                Text(stringResource(R.string.ok))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                setContent {
                    TravelBuddyTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val navController = rememberNavController()
                            BuildNavigationGraph(navController, Screens.SignIn.route)

                            if (showNetworkDialog) {
                                AlertDialog(
                                    onDismissRequest = { showNetworkDialog = false },
                                    title = { Text(stringResource(R.string.no_internet_connection)) },
                                    text = { Text(stringResource(R.string.no_internet_connection_message)) },
                                    confirmButton = {
                                        Button(onClick = { showNetworkDialog = false }) {
                                            Text(stringResource(R.string.ok))
                                        }
                                    }
                                )
                            }
                        }
                    }
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
            composable(Screens.MapView.route) { MapViewScreen(navController) }
            composable(Screens.PlaceDetails.route) { PlaceDetailsScreen(navController) }
            composable(Screens.AddTripPlanFromExplore.route) { AddTripPlanFromExploreScreen(navController) }
            composable(Screens.Friends.route) { FriendsScreen(navController) }
            composable(Screens.Account.route) { AccountScreen(navController) }
            composable(Screens.TermsOfService.route) { TermsOfServiceScreen(navController) }
            composable(Screens.About.route) { AboutScreen(navController) }
            composable(Screens.Notification.route) { NotificationScreen(navController) }
            composable(Screens.SignIn.route) { SignInScreen(navController, firebaseViewModel) }
            composable(Screens.SetUsername.route) { SetUsernameScreen(navController, firebaseViewModel) }
            composable(Screens.SignUp.route) { SignUpScreen(navController, firebaseViewModel) }
            composable(Screens.AddTrip.route) { AddTripScreen(navController) }
            composable(Screens.AddPhrase.route) { AddPhraseScreen(navController) }
            composable(Screens.EditPhrase.route) { EditPhraseScreen(navController) }
            composable(Screens.AddFriend.route) { AddFriendScreen(navController) }
            composable(Screens.FriendsList.route) { FriendsListScreen(navController) }
            composable(Screens.Profile.route) { ProfileScreen(navController) }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}