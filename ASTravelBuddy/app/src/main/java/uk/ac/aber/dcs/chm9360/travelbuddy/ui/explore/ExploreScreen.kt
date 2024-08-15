package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import android.location.Geocoder
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import org.osmdroid.util.GeoPoint
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
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
    firebaseViewModel: FirebaseViewModel = viewModel(),
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val appBarTitle = stringResource(R.string.explore)

    val placeDetails = Utils.placeDetails
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
    val (cityFocusRequester, categoryFocusRequester) = remember { FocusRequester.createRefs() }
    var cityHasFocus by rememberSaveable { mutableStateOf(false) }
    var categoryHasFocus by rememberSaveable { mutableStateOf(false) }
    val friendRequests by firebaseViewModel.friendRequests.collectAsState()

    val filteredCategories = categoryList
        .filter { it.second.contains(categoryText, ignoreCase = true) }
        .sortedBy { it.second }

    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogTitle = remember { mutableStateOf("") }

    placeDetails?.properties?.formatted?.let { address ->
        val geocoder = Geocoder(context, Locale.getDefault())
        geocoder.getFromLocationName(address, 1)?.firstOrNull()?.let {
            GeoPoint(it.latitude, it.longitude)
        }
    }

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
        appBarTitle = appBarTitle,
        friendRequestCount = friendRequests.size
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
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
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
                                                Utils.destinationName = cityText
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
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search_icon)
                                    )
                                    Text(stringResource(id = R.string.search))
                                }
                                Button(
                                    onClick = {
                                        navController.navigate(Screens.Map.route)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Map,
                                        contentDescription = stringResource(R.string.map_icon)
                                    )
                                    Text(stringResource(id = R.string.map_search))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        isComponentEnabled = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = stringResource(id = R.string.arrow_down_icon)
                                    )
                                    Text(stringResource(id = R.string.new_search))
                                }
                                Button(
                                    onClick = {
                                        Utils.featureList = places
                                        navController.navigate(Screens.MapView.route)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Map,
                                        contentDescription = stringResource(id = R.string.map_icon)
                                    )
                                    Text(stringResource(id = R.string.map_view))
                                }
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