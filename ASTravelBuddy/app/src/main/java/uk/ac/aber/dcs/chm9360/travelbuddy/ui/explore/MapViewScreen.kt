package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@SuppressLint("RememberReturnType")
@Composable
fun MapViewScreen(
    navController: NavHostController,
) {
    val appBarTitle = stringResource(id = R.string.map_view)
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

    LaunchedEffect(Unit) {
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

    val fetchedPlaces = Utils.featureList
    var markers by remember { mutableStateOf(emptyList<MapMarker>()) }
    if (fetchedPlaces != null) {
        markers = fetchedPlaces.map { currentPlace ->
            currentPlace.properties.formatted.let { address ->
                createMapMarkerFromDestination(address, context)
            }
        }
    }

    val mapCenter = createMapMarkerFromDestination(Utils.destinationName, context)

    Scaffold(
        topBar = {
            AppBarWithArrowBack(
                navController = navController,
                appBarTitle = appBarTitle,
                showMoreIcon = false
            )
        }
    ) { innerPadding ->
        MapComposable(
            innerPadding = innerPadding,
            context = context,
            centerMarker = mapCenter,
            placesMarkers = markers
        )
    }
}