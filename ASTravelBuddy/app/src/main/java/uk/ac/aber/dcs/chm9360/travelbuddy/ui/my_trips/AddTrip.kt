package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.CustomDatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddTripScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel(),
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val title = stringResource(id = R.string.add_a_trip)
    val nextDestination = Screens.MyTrips.route

    var tripTitle by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var endDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val cities by retrofitViewModel.cities.collectAsState()
    val countries by retrofitViewModel.countries.collectAsState()
    val loading by retrofitViewModel.loading.collectAsState()
    val showCityList by retrofitViewModel.showCityList.collectAsState()
    val showCountryList by retrofitViewModel.showCountryList.collectAsState()
    var searchType by rememberSaveable { mutableStateOf("city") }
    var isDropdownVisible by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val isSaveButtonEnabled by rememberSaveable(tripTitle, destination, startDate, endDate) {
        mutableStateOf(
            tripTitle.isNotBlank() &&
                    destination.isNotBlank() &&
                    startDate <= endDate
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSave = {
                val newTrip = Trip(
                    title = tripTitle,
                    destination = destination,
                    startDate = startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    endDate = endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                )
                firebaseViewModel.addTrip(newTrip)
                Toast.makeText(context, R.string.trip_saved, Toast.LENGTH_SHORT).show()
                navController.navigate(nextDestination) { popUpTo(0) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomDatePicker(
                value = startDate,
                onValueChange = { startDate = it },
                label = stringResource(R.string.start_date),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            CustomDatePicker(
                value = endDate,
                onValueChange = { endDate = it },
                label = stringResource(R.string.end_date),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tripTitle,
            onValueChange = { tripTitle = it },
            label = { Text(stringResource(R.string.trip_title)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = searchType == "city",
                    onClick = { searchType = "city" }
                )
                Text(text = stringResource(id = R.string.city))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = searchType == "country",
                    onClick = { searchType = "country" }
                )
                Text(text = stringResource(id = R.string.country))
            }
        }

        OutlinedTextField(
            value = destination,
            onValueChange = {
                destination = it
                if (it.length > 2) {
                    if (searchType == "city") {
                        retrofitViewModel.searchCities(it)
                    } else {
                        retrofitViewModel.searchCountries(it)
                    }
                } else {
                    retrofitViewModel.hideCityList()
                    retrofitViewModel.hideCountryList()
                }
            },
            label = { Text(stringResource(R.string.destination)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .clickable { isDropdownVisible = !isDropdownVisible }
        )

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (showCityList) {
                items(cities) { city ->
                    Text(
                        text = "${city.name}, ${city.country}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                destination = "${city.name}, ${city.country}"
                                retrofitViewModel.hideCityList()
                                keyboardController?.hide()
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
                                destination = country.name
                                retrofitViewModel.hideCountryList()
                                keyboardController?.hide()
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}