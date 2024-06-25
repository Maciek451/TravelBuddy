package uk.ac.aber.dcs.chm9360.travelbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.about.AboutScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.AccountScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.ExploreScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends.FriendsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.map.MapScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.MyTripsScreen
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.theme.TravelBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BuildNavigationGraph()
                }
            }
        }
    }
}

@Composable
private fun BuildNavigationGraph() {
    val navController = rememberNavController()
    val startScreenRoute = Screens.MyTrips.route

    NavHost(
        navController = navController,
        startDestination = startScreenRoute
    ) {
        composable(Screens.MyTrips.route) { MyTripsScreen(navController) }
        composable(Screens.Map.route) { MapScreen(navController) }
        composable(Screens.Explore.route) { ExploreScreen(navController) }
        composable(Screens.Friends.route) { FriendsScreen(navController) }
        composable(Screens.Account.route) { AccountScreen(navController) }
        composable(Screens.About.route) { AboutScreen(navController) }
    }
}