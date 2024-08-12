package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.MapComposable
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.createMapMarkerFromDestination
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun TripMapScreen(
    navController: NavHostController,
) {
    val trip = Utils.trip
    val appBarTitle = trip?.title
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

    val destinationMarker = trip?.let { createMapMarkerFromDestination(it.destination, context) }
    val tripMarkers = trip?.tripPlans?.map { tripPlan ->
        createMapMarkerFromDestination(tripPlan.place, context)
    } ?: emptyList()

    LaunchedEffect(trip?.destination, trip?.tripPlans) {
        if (trip?.destination != null) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted.value = true
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Scaffold(
        topBar = {
            if (appBarTitle != null) {
                AppBarWithArrowBack(
                    navController = navController,
                    appBarTitle = appBarTitle,
                    showMoreIcon = false
                )
            }
        }
    ) { innerPadding ->
        MapComposable(context = context,
            centerMarker = destinationMarker!!,
            placesMarkers = tripMarkers,
            innerPadding = innerPadding)
    }
}