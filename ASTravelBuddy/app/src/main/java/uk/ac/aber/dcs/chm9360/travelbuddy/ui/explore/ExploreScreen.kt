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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.categoryList
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExploreScreen(
    navController: NavHostController,
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val appBarTitle = stringResource(R.string.explore)

    var cityText by rememberSaveable { mutableStateOf("") }
    var categoryText by rememberSaveable { mutableStateOf("") }
    var selectedCategoryKey by rememberSaveable { mutableStateOf("") }
    val places by retrofitViewModel.places.collectAsState()
    val isLoading by retrofitViewModel.loading.collectAsState()
    val autocompleteSuggestions by retrofitViewModel.autocompleteSuggestions.collectAsState()
    val scrollState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isCityDropdownVisible by rememberSaveable { mutableStateOf(false) }
    var isCategoryDropdownVisible by rememberSaveable { mutableStateOf(false) }
    var isSearchClicked by rememberSaveable { mutableStateOf(false) }
    var isComponentEnabled by rememberSaveable { mutableStateOf(true) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val (cityFocusRequester, categoryFocusRequester) = remember { FocusRequester.createRefs() }
    var cityHasFocus by rememberSaveable { mutableStateOf(false) }
    var categoryHasFocus by rememberSaveable { mutableStateOf(false) }

    val filteredCategories = categoryList.filter {
        it.second.contains(categoryText, ignoreCase = true)
    }

    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogTitle = remember { mutableStateOf("") }

    LaunchedEffect(places) {
        if (places.isNotEmpty()) {
            isComponentEnabled = false
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(dialogTitle.value) },
            text = { Text(dialogMessage.value) },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    TopLevelScaffold(
        navController = navController,
        appBarTitle = appBarTitle
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        text = { Text(stringResource(id = R.string.search)) },
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    )
                    Tab(
                        text = { Text(stringResource(id = R.string.map)) },
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    )
                }
                when (selectedTabIndex) {
                    0 -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                            ) {
                                if (isComponentEnabled) {
                                    TextField(
                                        value = cityText,
                                        onValueChange = { newCityText ->
                                            cityText = newCityText
                                            if (newCityText.length > 2) {
                                                retrofitViewModel.fetchAutocompleteSuggestions(
                                                    newCityText
                                                )
                                                isCityDropdownVisible = true
                                            } else {
                                                isCityDropdownVisible = false
                                            }
                                        },
                                        label = { Text(stringResource(id = R.string.destination)) },
                                        trailingIcon = {
                                            if (cityText.isNotEmpty() && cityHasFocus) {
                                                IconButton(
                                                    onClick = {
                                                        cityText = ""
                                                        isCityDropdownVisible = false
                                                        keyboardController?.hide()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Close,
                                                        contentDescription = stringResource(R.string.clear_icon)
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .focusRequester(cityFocusRequester)
                                            .onFocusChanged { focusState ->
                                                cityHasFocus = focusState.isFocused
                                            }
                                    )
                                    if (isCityDropdownVisible && autocompleteSuggestions.isNotEmpty()) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                        ) {
                                            items(autocompleteSuggestions) { suggestion ->
                                                Text(
                                                    text = suggestion,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            cityText = suggestion
                                                            isCityDropdownVisible = false
                                                            keyboardController?.hide()
                                                        }
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = categoryText,
                                        onValueChange = {
                                            categoryText = it
                                            isCategoryDropdownVisible = true
                                        },
                                        label = { Text(stringResource(id = R.string.category)) },
                                        trailingIcon = {
                                            if (categoryText.isNotEmpty() && categoryHasFocus) {
                                                IconButton(
                                                    onClick = {
                                                        categoryText = ""
                                                        isCategoryDropdownVisible = false
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Close,
                                                        contentDescription = stringResource(R.string.clear_icon)
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .focusRequester(categoryFocusRequester)
                                            .onFocusChanged { focusState ->
                                                categoryHasFocus = focusState.isFocused
                                            }
                                    )
                                    if (isCategoryDropdownVisible) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .heightIn(max = 300.dp),
                                            contentPadding = PaddingValues(8.dp)
                                        ) {
                                            items(filteredCategories) { (categoryKey, categoryDescription) ->
                                                Text(
                                                    text = categoryDescription,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            categoryText = categoryDescription
                                                            selectedCategoryKey = categoryKey
                                                            isCategoryDropdownVisible = false
                                                        }
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            when {
                                                cityText.isEmpty() -> {
                                                    dialogTitle.value =
                                                        context.getString(R.string.empty_destination_title)
                                                    dialogMessage.value =
                                                        context.getString(R.string.empty_destination_message)
                                                    showDialog.value = true
                                                }

                                                categoryText.isEmpty() -> {
                                                    dialogTitle.value =
                                                        context.getString(R.string.empty_category_title)
                                                    dialogMessage.value =
                                                        context.getString(R.string.empty_category_message)
                                                    showDialog.value = true
                                                }

                                                else -> {
                                                    retrofitViewModel.searchPlaces(
                                                        cityText,
                                                        categories = selectedCategoryKey
                                                    )
                                                    keyboardController?.hide()
                                                    isSearchClicked = true
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = stringResource(R.string.search_icon)
                                        )
                                        Text(stringResource(id = R.string.search))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                } else {
                                    Button(
                                        onClick = {
                                            isComponentEnabled = true
                                        },
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = stringResource(id = R.string.arrow_down_icon)
                                        )
                                        Text(stringResource(id = R.string.search))
                                    }
                                }

                                if (!isSearchClicked) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Search,
                                                contentDescription = stringResource(id = R.string.search_icon),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(text = stringResource(id = R.string.search_to_get_information))
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = scrollState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        if (isLoading && isSearchClicked) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        CircularProgressIndicator()
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(text = stringResource(id = R.string.loading))
                                                    }
                                                }
                                            }
                                        } else if (places.isNotEmpty()) {
                                            items(places) { place ->
                                                PlaceItem(
                                                    title = place.properties.name,
                                                    subtext = place.properties.formatted,
                                                    onClick = {
                                                        Utils.placeDetails = place
                                                        navController.navigate(Screens.PlaceDetails.route)
                                                    }
                                                )
                                            }
                                        } else if (isSearchClicked) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(vertical = 32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ManageSearch,
                                                            contentDescription = stringResource(R.string.search_icon),
                                                            modifier = Modifier.size(64.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(text = stringResource(id = R.string.no_results_found))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            MapScreen(
                                retrofitViewModel = retrofitViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceItem(
    title: String,
    subtext: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext ?: stringResource(R.string.no_subtext_available),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MapScreen(
    retrofitViewModel: RetrofitViewModel = viewModel(),
) {
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
        val address = geocoder.getFromLocationName(query, 1)?.firstOrNull()

        if (address != null) {
            val location = GeoPoint(address.latitude, address.longitude)
            mapViewModel.updateMapCenter(location)
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

    Scaffold { innerPadding ->
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

            FloatingActionButton(
                onClick = { moveToCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.my_location)
                )
            }

            FloatingActionButton(
                onClick = { searchBarVisible.value = !searchBarVisible.value },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset(y = (-72).dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
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
fun MapViewComposable(context: Context, mapViewModel: MapViewModel) {
    val mapView = remember { MapView(context) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val mapCenter by mapViewModel.mapCenter.collectAsState()
    val mapZoom by mapViewModel.mapZoom.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val markers by mapViewModel.markers.collectAsState()
    val myLocation = stringResource(id = R.string.my_location)

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
            .fillMaxSize()
            .padding(top = 8.dp),
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
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { mapViewModel.addMarker(it) }
                        return true
                    }

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
        update = { mapView ->
            mapView.controller.setCenter(mapCenter)
            mapView.controller.setZoom(mapZoom)

            mapView.overlays.removeIf { it -> it is Marker && it != mapView.overlays.find { it is Marker && (it).title == myLocation } }

            val currentLocationMarker = Marker(mapView).apply {
                currentLocation?.let { location ->
                    position = location
                    title = myLocation
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_location_marker)
            }
            mapView.overlays.add(currentLocationMarker)

            markers.forEach { geoPoint ->
                val marker = Marker(mapView).apply {
                    position = geoPoint
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_marker)
                    setOnMarkerClickListener { _, _ ->
                        mapViewModel.removeMarker(geoPoint)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
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

    fun updateMapCenter(newCenter: GeoPoint) {
        _mapCenter.value = newCenter
    }

    fun updateMapZoom(newZoom: Double) {
        _mapZoom.value = newZoom
    }

    fun updateCurrentLocation(location: GeoPoint) {
        _currentLocation.value = location
    }

    fun addMarker(geoPoint: GeoPoint) {
        _markers.value += geoPoint
    }

    fun removeMarker(geoPoint: GeoPoint) {
        _markers.value -= geoPoint
    }
}