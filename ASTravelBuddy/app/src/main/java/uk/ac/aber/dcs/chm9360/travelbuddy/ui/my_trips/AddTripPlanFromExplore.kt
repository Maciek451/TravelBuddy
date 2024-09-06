package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.CustomDatePicker
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AddTripPlanFromExploreScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = stringResource(id = R.string.add_trip_plan)
    val trip = Utils.trip
    val placeDetails = Utils.placeDetails
    val keyboardController = LocalSoftwareKeyboardController.current

    var place by rememberSaveable {
        mutableStateOf(placeDetails?.properties?.let { it.formatted } ?: "")
    }
    var dateOfVisit by rememberSaveable { mutableStateOf(LocalDate.now()) }

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
                val newTripPlan = uk.ac.aber.dcs.chm9360.travelbuddy.model.TripPlanItem(
                    id = UUID.randomUUID().toString(),
                    place = place,
                    dateOfVisit = dateOfVisit.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                )
                trip?.let {
                    firebaseViewModel.addTripPlan(it.id, newTripPlan) { success ->
                        if (success) {
                            navController.navigate(Screens.Explore.route)
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
            },
            label = { Text(stringResource(R.string.place_label)) },
            trailingIcon = {
                if (place.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            place = ""
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
    }
}
