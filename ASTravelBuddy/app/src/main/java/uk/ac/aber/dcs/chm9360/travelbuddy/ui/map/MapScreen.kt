package uk.ac.aber.dcs.chm9360.travelbuddy.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val mapViewModel: MapViewModel = viewModel()

    TopLevelScaffold(
        navController = navController,
        appBarTitle = stringResource(R.string.map),
        showLocationButton = true,
        onLocationButtonClick = {  }
    ) {
        MapViewComposable(context, mapViewModel)
    }
}

@Composable
fun MapViewComposable(context: Context, mapViewModel: MapViewModel) {
    val mapView = remember { MapView(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val mapZoom by mapViewModel.mapZoom.collectAsState()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted.value = true
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(locationPermissionGranted.value) {
        if (locationPermissionGranted.value && !mapViewModel.initialLocationSet) {
            getLastKnownLocation(fusedLocationClient) { location ->
                location?.let {
                    mapViewModel.updateMapCenter(GeoPoint(it.latitude, it.longitude))
                    mapViewModel.updateMapZoom(15.0)
                    mapViewModel.initialLocationSet = true
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(mapZoom)
                controller.setCenter(mapCenter)
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = true
                    override fun longPressHelper(p: GeoPoint?) = true
                }))
                addOnFirstLayoutListener { _, _, _, _, _ ->
                    controller.setCenter(mapCenter)
                    controller.setZoom(mapZoom)
                }
                setMapListener(object : MapAdapter() {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        event?.let { mapViewModel.updateMapCenter(it.source.mapCenter as GeoPoint) }
                        return super.onScroll(event)
                    }
                    override fun onZoom(event: ZoomEvent?): Boolean {
                        event?.let { mapViewModel.updateMapZoom(it.zoomLevel) }
                        return super.onZoom(event)
                    }
                })
            }
        },
        update = {
            it.controller.setCenter(mapCenter)
            it.controller.setZoom(mapZoom)
        }
    )
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(fusedLocationClient: FusedLocationProviderClient, onLocationResult: (Location?) -> Unit) {
    fusedLocationClient.lastLocation.addOnCompleteListener { task ->
        onLocationResult(task.result.takeIf { task.isSuccessful && it != null })
    }
}

class MapViewModel : ViewModel() {
    private val _mapCenter = MutableStateFlow(GeoPoint(0.0, 0.0))
    val mapCenter: StateFlow<GeoPoint> = _mapCenter
    private val _mapZoom = MutableStateFlow(15.0)
    val mapZoom: StateFlow<Double> = _mapZoom
    var initialLocationSet = false

    fun updateMapCenter(newCenter: GeoPoint) {
        _mapCenter.value = newCenter
    }
    fun updateMapZoom(newZoom: Double) {
        _mapZoom.value = newZoom
    }
}