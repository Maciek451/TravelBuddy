package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EditTripScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val trip = Utils.trip
    val context = LocalContext.current

    if (trip == null) {
        Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }

    val title = stringResource(id = R.string.edit_details)
    var tripTitle by rememberSaveable { mutableStateOf(trip.title) }
    var destination by rememberSaveable { mutableStateOf(trip.destination) }
    var startDate by rememberSaveable {
        mutableStateOf(LocalDate.parse(trip.startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
    }
    var endDate by rememberSaveable {
        mutableStateOf(LocalDate.parse(trip.endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
    }

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
                val updatedTrip = trip.copy(
                    title = tripTitle,
                    destination = destination,
                    startDate = startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    endDate = endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                )
                firebaseViewModel.updateTrip(updatedTrip) {
                    Utils.trip = updatedTrip
                    Toast.makeText(context, R.string.trip_updated, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
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

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text(stringResource(R.string.destination)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )
    }
}