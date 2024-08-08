package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore.MapViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import java.util.Locale

@Composable
fun TripMapScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = viewModel()
) {
    val trip = Utils.trip
    val appBarTitle = trip?.title
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val mapZoom by mapViewModel.mapZoom.collectAsState()
    var tripPlanMarkers by remember { mutableStateOf(emptyList<Pair<String, GeoPoint>>()) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

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

            val geocoder = Geocoder(context, Locale.getDefault())

            val address = geocoder.getFromLocationName(trip.destination, 1)?.firstOrNull()
            if (address != null) {
                val location = GeoPoint(address.latitude, address.longitude)
                mapViewModel.updateMapCenter(location)
            } else {
                Log.e("TripMapScreen", "Error geocoding destination: ${trip.destination}")
            }

            tripPlanMarkers = trip.tripPlans.mapNotNull { tripPlan ->
                val tripPlanAddress = geocoder.getFromLocationName(tripPlan.place, 1)?.firstOrNull()
                tripPlanAddress?.let {
                    val location = GeoPoint(it.latitude, it.longitude)
                    Pair(tripPlan.place, location)
                }
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
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = {
                MapView(context).apply {
                    Configuration.getInstance()
                        .load(
                            context,
                            context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
                        )
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(mapZoom)
                    controller.setCenter(mapCenter)
                    overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { mapViewModel.addMarker(it) }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?) = true
                    }))
                }
            },
            update = { mapView ->
                mapView.controller.setCenter(mapCenter)
                mapView.controller.setZoom(mapZoom)

                mapView.overlays.removeIf { it is Marker }

                val destinationMarker = Marker(mapView).apply {
                    position = mapCenter
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = trip?.destination ?: "Destination"
                }
                mapView.overlays.add(destinationMarker)

                tripPlanMarkers.forEach { (place, location) ->
                    val marker = Marker(mapView).apply {
                        position = location
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = place
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        )
    }
}