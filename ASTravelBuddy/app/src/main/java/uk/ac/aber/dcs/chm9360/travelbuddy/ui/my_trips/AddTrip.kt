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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
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

@OptIn(ExperimentalComposeUiApi::class)
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
    val loading by retrofitViewModel.loading.collectAsState()
    var isDropdownVisible by rememberSaveable { mutableStateOf(false) }
    val autocompleteSuggestions by retrofitViewModel.autocompleteSuggestions.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val (tripTitleFocusRequester, destinationFocusRequester) = remember { FocusRequester.createRefs() }
    var tripTitleHasFocus by rememberSaveable { mutableStateOf(false) }
    var destinationHasFocus by rememberSaveable { mutableStateOf(false) }

    val isSaveButtonEnabled by rememberSaveable(tripTitle, destination, startDate, endDate) {
        mutableStateOf(
            tripTitle.isNotBlank() &&
                    destination.isNotBlank() &&
                    startDate <= endDate
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().testTag("AddTripScreen")
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
                .padding(horizontal = 16.dp),
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
            trailingIcon = {
                if (tripTitle.isNotEmpty() && tripTitleHasFocus) {
                    IconButton(
                        onClick = {
                            tripTitle = ""
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.delete_icon))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(tripTitleFocusRequester)
                .onFocusChanged { focusState ->
                    tripTitleHasFocus = focusState.isFocused
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = {
                destination = it
                if (it.length > 2) {
                    retrofitViewModel.fetchAutocompleteSuggestions(it)
                    isDropdownVisible = true
                } else {
                    isDropdownVisible = false
                }
            },
            label = { Text(stringResource(R.string.destination)) },
            trailingIcon = {
                if (destination.isNotEmpty() && destinationHasFocus) {
                    IconButton(
                        onClick = {
                            destination = ""
                            isDropdownVisible = false
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.delete_icon))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(destinationFocusRequester)
                .onFocusChanged { focusState ->
                    destinationHasFocus = focusState.isFocused
                }
        )

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (isDropdownVisible && autocompleteSuggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Start)
            ) {
                items(autocompleteSuggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                destination = suggestion
                                isDropdownVisible = false
                                keyboardController?.hide()
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}