package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import java.util.Locale

@Composable
fun MapScreen(
    navController: NavHostController,
    retrofitViewModel: RetrofitViewModel = viewModel(),
) {
    val appBarTitle = stringResource(R.string.map_view)
    val context = LocalContext.current
    val mapViewModel: MapViewModel = viewModel()
    val searchQuery = remember { mutableStateOf("") }
    val searchBarVisible = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogTitle = remember { mutableStateOf("") }
    val loading by retrofitViewModel.loading.collectAsState()
    var isDropdownVisible by rememberSaveable { mutableStateOf(false) }
    val autocompleteSuggestions by retrofitViewModel.autocompleteSuggestions.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    fun moveToCurrentLocation() {
        mapViewModel.currentLocation.value?.let {
            mapViewModel.updateMapCenter(it)
            mapViewModel.updateMapZoom(16.0)
        }
    }

    fun performSearch(query: String) {
        if (query.isEmpty()) {
            dialogTitle.value = context.getString(R.string.empty_query_title)
            dialogMessage.value = context.getString(R.string.search_query_empty)
            showDialog.value = true
            return
        }

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(query, 5)

        val newMarkers = mutableListOf<GeoPoint>()
        val newTitles = mutableMapOf<GeoPoint, String>()

        addresses?.forEach { address ->
            val location = GeoPoint(address.latitude, address.longitude)
            newMarkers.add(location)
            newTitles[location] = query
        }

        if (newMarkers.isNotEmpty()) {
            mapViewModel.updateMapCenter(newMarkers.first())
            mapViewModel.updateMapZoom(16.0)
            mapViewModel.updateMarkers(newMarkers)
            mapViewModel.updateMarkerTitles(newTitles)
        } else {
            dialogTitle.value = context.getString(R.string.no_results_title)
            dialogMessage.value = context.getString(R.string.search_no_results)
            showDialog.value = true
        }
        searchBarVisible.value = false
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(dialogTitle.value) },
            text = { Text(dialogMessage.value) },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(stringResource(id = R.string.ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppBarWithArrowBack(
                navController = navController,
                appBarTitle = appBarTitle,
                showMoreIcon = false
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            ) {
                FloatingActionButton(onClick = {
                    searchBarVisible.value = !searchBarVisible.value
                }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
                FloatingActionButton(onClick = { moveToCurrentLocation() }) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.my_location)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MapViewComposable(context, mapViewModel)

            if (searchBarVisible.value) {
                Column {
                    SearchBar(
                        searchQuery = searchQuery.value,
                        onSearchQueryChange = { newQuery ->
                            searchQuery.value = newQuery
                            if (newQuery.length > 2) {
                                retrofitViewModel.fetchAutocompleteSuggestions(newQuery)
                                isDropdownVisible = true
                            } else {
                                isDropdownVisible = false
                            }
                        },
                        onSearch = { performSearch(searchQuery.value) },
                        onSelectSuggestion = { suggestion ->
                            searchQuery.value = suggestion
                            isDropdownVisible = false
                            keyboardController?.hide()
                        },
                        isDropdownVisible = isDropdownVisible,
                        autocompleteSuggestions = autocompleteSuggestions
                    )
                }
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectSuggestion: (String) -> Unit,
    isDropdownVisible: Boolean,
    autocompleteSuggestions: List<String>
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(stringResource(id = R.string.search)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.clear_icon)
                            )
                        }
                    }
                    IconButton(onClick = {
                        onSearch()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(id = R.string.search_icon)
                        )
                    }
                }
            }
        )

        if (isDropdownVisible && autocompleteSuggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                items(autocompleteSuggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSuggestion(suggestion)
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MapViewComposable(
    context: Context,
    mapViewModel: MapViewModel
) {
    val mapView = remember { MapView(context) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val mapZoom by mapViewModel.mapZoom.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val markers by mapViewModel.markers.collectAsState()
    val markerTitles by mapViewModel.markerTitles.collectAsState()
    val myLocationString = stringResource(id = R.string.my_location)

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

    val minZoomLevel = 4.0

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

    LaunchedEffect(locationPermissionGranted.value) {
        if (locationPermissionGranted.value && !mapViewModel.initialLocationSet) {
            getLastKnownLocation(LocationServices.getFusedLocationProviderClient(context)) { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    mapViewModel.updateMapCenter(geoPoint)
                    mapViewModel.updateMapZoom(16.0)
                    mapViewModel.updateCurrentLocation(geoPoint)
                    mapViewModel.initialLocationSet = true
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = {
            mapView.apply {
                Configuration.getInstance()
                    .load(
                        context,
                        context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
                    )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(mapZoom)
                controller.setCenter(mapCenter)
                controller.setZoom(minZoomLevel)

                setMapListener(object : MapAdapter() {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        event?.let { mapViewModel.updateMapCenter(it.source.mapCenter as GeoPoint) }
                        return super.onScroll(event)
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        event?.let {
                            if (event.zoomLevel < minZoomLevel) {
                                controller.setZoom(minZoomLevel)
                            } else {
                                mapViewModel.updateMapZoom(event.zoomLevel)
                            }
                        }
                        return super.onZoom(event)
                    }
                })
            }
        },
        update = { currentMapView ->
            currentMapView.controller.setCenter(mapCenter)
            currentMapView.controller.setZoom(mapZoom)

            currentMapView.overlays.clear()

            val currentLocationMarker = Marker(currentMapView).apply {
                currentLocation?.let { location ->
                    position = location
                    title = myLocationString
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, R.drawable.red_marker)
            }
            currentMapView.overlays.add(currentLocationMarker)

            markers.forEach { geoPoint ->
                val marker = Marker(currentMapView).apply {
                    position = geoPoint
                    icon = ContextCompat.getDrawable(context, R.drawable.blue_marker)
                    title = markerTitles[geoPoint]
                }
                currentMapView.overlays.add(marker)
            }
            currentMapView.invalidate()
        }
    )
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Location?) -> Unit
) {
    fusedLocationClient.lastLocation.addOnCompleteListener { task ->
        onLocationResult(task.result.takeIf { task.isSuccessful && it != null })
    }
}

class MapViewModel : ViewModel() {
    private val _mapCenter = MutableStateFlow(GeoPoint(0.0, 0.0))
    val mapCenter: StateFlow<GeoPoint> = _mapCenter
    private val _mapZoom = MutableStateFlow(16.0)
    val mapZoom: StateFlow<Double> = _mapZoom
    var initialLocationSet = false

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation

    private val _markers = MutableStateFlow<List<GeoPoint>>(emptyList())
    val markers: StateFlow<List<GeoPoint>> = _markers

    private val _markerTitles = MutableStateFlow<Map<GeoPoint, String>>(emptyMap())
    val markerTitles: StateFlow<Map<GeoPoint, String>> = _markerTitles

    fun updateMapCenter(newCenter: GeoPoint) {
        _mapCenter.value = newCenter
    }

    fun updateMapZoom(newZoom: Double) {
        _mapZoom.value = newZoom
    }

    fun updateCurrentLocation(location: GeoPoint) {
        _currentLocation.value = location
    }

    fun updateMarkers(newMarkers: List<GeoPoint>) {
        _markers.value = newMarkers
    }

    fun updateMarkerTitles(newTitles: Map<GeoPoint, String>) {
        _markerTitles.value = newTitles
    }
}