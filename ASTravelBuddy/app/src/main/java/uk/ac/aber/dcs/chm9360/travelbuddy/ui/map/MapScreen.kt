package uk.ac.aber.dcs.chm9360.travelbuddy.ui.map

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Destination
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold
import java.util.Locale

@Composable
fun MapScreen(
    navController: NavHostController,
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val context = LocalContext.current
    val mapViewModel: MapViewModel = viewModel()
    val searchQuery = remember { mutableStateOf("") }
    val searchBarVisible = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogTitle = remember { mutableStateOf("") }

    val cities by retrofitViewModel.cities.collectAsState()
    val countries by retrofitViewModel.countries.collectAsState()
    val loading by retrofitViewModel.loading.collectAsState()
    val showCityList by retrofitViewModel.showCityList.collectAsState()
    val showCountryList by retrofitViewModel.showCountryList.collectAsState()
    val searchType = remember { mutableStateOf("city") }

    fun moveToCurrentLocation() {
        mapViewModel.currentLocation.value?.let {
            mapViewModel.updateMapCenter(it)
            mapViewModel.updateMapZoom(16.0)
        }
    }

    fun performSearch(query: String) {
        val cityZoomLevel = 15.0
        val countryZoomLevel = 7.0

        when {
            query.isEmpty() -> {
                dialogTitle.value = context.getString(R.string.empty_query_title)
                dialogMessage.value = context.getString(R.string.search_query_empty)
                showDialog.value = true
            }

            else -> {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address = geocoder.getFromLocationName(query, 1)?.firstOrNull()

                if (address != null) {
                    val location = GeoPoint(address.latitude, address.longitude)
                    val zoomLevel =
                        if (searchType.value == "city") cityZoomLevel else countryZoomLevel

                    mapViewModel.updateMapCenter(location)
                    mapViewModel.updateMapZoom(zoomLevel)
                } else {
                    dialogTitle.value = context.getString(R.string.no_results_title)
                    dialogMessage.value = context.getString(R.string.search_no_results)
                    showDialog.value = true
                }
                searchBarVisible.value = false
            }
        }
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

    TopLevelScaffold(
        navController = navController,
        appBarTitle = stringResource(R.string.map),
        showLocationButton = true,
        onLocationButtonClick = ::moveToCurrentLocation,
        onSearchButtonClick = { searchBarVisible.value = !searchBarVisible.value }
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
                        onSearchQueryChange = {
                            searchQuery.value = it
                            if (it.length > 2) {
                                if (searchType.value == "city") {
                                    retrofitViewModel.searchCities(it)
                                } else {
                                    retrofitViewModel.searchCountries(it)
                                }
                            } else {
                                retrofitViewModel.hideCityList()
                                retrofitViewModel.hideCountryList()
                            }
                        },
                        onSearch = { performSearch(searchQuery.value) },
                        cities = cities,
                        countries = countries,
                        showCityList = showCityList && searchType.value == "city",
                        showCountryList = showCountryList && searchType.value == "country",
                        onSelectSuggestion = {
                            searchQuery.value = it
                        },
                        searchType = searchType.value,
                        onSearchTypeChange = { searchType.value = it }
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
    cities: List<Destination> = emptyList(),
    countries: List<Destination> = emptyList(),
    showCityList: Boolean = false,
    showCountryList: Boolean = false,
    onSelectSuggestion: (String) -> Unit,
    searchType: String,
    onSearchTypeChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val radioButtonsVisible = searchQuery.isEmpty()

    Column(
        modifier = Modifier
            .padding(16.dp)
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

        if (radioButtonsVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onSearchTypeChange("city") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = searchType == "city",
                        onClick = { onSearchTypeChange("city") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.city))
                }
                Row(
                    modifier = Modifier
                        .clickable { onSearchTypeChange("country") }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = searchType == "country",
                        onClick = { onSearchTypeChange("country") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.country))
                }
            }
        }

        if (showCityList || showCountryList) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                if (showCityList) {
                    items(cities) { city ->
                        Text(
                            text = "${city.name}, ${city.country}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSuggestion("${city.name}, ${city.country}")
                                }
                                .padding(16.dp)
                        )
                    }
                }

                if (showCountryList) {
                    items(countries) { country ->
                        Text(
                            text = country.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSuggestion(country.name)
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapViewComposable(context: Context, mapViewModel: MapViewModel) {
    val mapView = remember { MapView(context) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val mapZoom by mapViewModel.mapZoom.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val myLocationText = stringResource(id = R.string.my_location)

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
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
            .fillMaxSize()
            .padding(top = 8.dp),
        factory = {
            mapView.apply {
                Configuration.getInstance()
                    .load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(mapZoom)
                controller.setCenter(mapCenter)
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?) = true
                    override fun longPressHelper(p: GeoPoint?) = true
                }))

                val currentLocationMarker = Marker(this).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
                    title = myLocationText
                }
                overlays.add(currentLocationMarker)

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
        update = { mapView ->
            mapView.controller.setCenter(mapCenter)
            mapView.controller.setZoom(mapZoom)

            val marker = mapView.overlays.find { it is Marker } as? Marker
            marker?.apply {
                currentLocation?.let { location ->
                    position = location
                    title = myLocationText
                    mapView.invalidate()
                }
            }
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

    fun updateMapCenter(newCenter: GeoPoint) {
        _mapCenter.value = newCenter
    }

    fun updateMapZoom(newZoom: Double) {
        _mapZoom.value = newZoom
    }

    fun updateCurrentLocation(location: GeoPoint) {
        _currentLocation.value = location
    }
}