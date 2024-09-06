package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.TripPlanItem
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.CustomDatePicker
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AddTripPlanScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel(),
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val title = stringResource(id = R.string.add_trip_plan)
    val trip = Utils.trip
    val keyboardController = LocalSoftwareKeyboardController.current

    var place by rememberSaveable { mutableStateOf("") }
    var dateOfVisit by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val loading by retrofitViewModel.loading.collectAsState()
    var isDropdownVisible by rememberSaveable { mutableStateOf(false) }
    val autocompleteSuggestions by retrofitViewModel.autocompleteSuggestions.collectAsState()

    val isSaveButtonEnabled by rememberSaveable(place) {
        mutableStateOf(place.isNotBlank())
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSave = {
                val newTripPlan = TripPlanItem(
                    id = UUID.randomUUID().toString(),
                    place = place,
                    dateOfVisit = dateOfVisit.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                )
                trip?.let {
                    firebaseViewModel.addTripPlan(it.id, newTripPlan) { success ->
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomDatePicker(
            value = dateOfVisit,
            onValueChange = { dateOfVisit = it },
            label = stringResource(R.string.date_of_visit),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = place,
            onValueChange = {
                place = it
                if (it.length > 2) {
                    retrofitViewModel.fetchAutocompleteSuggestions(it)
                    isDropdownVisible = true
                } else {
                    isDropdownVisible = false
                }
            },
            label = { Text(stringResource(R.string.place_label)) },
            trailingIcon = {
                if (place.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            place = ""
                            isDropdownVisible = false
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_icon)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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
                                place = suggestion
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