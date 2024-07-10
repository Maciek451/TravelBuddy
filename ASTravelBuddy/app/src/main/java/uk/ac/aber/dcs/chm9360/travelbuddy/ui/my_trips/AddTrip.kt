package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun AddTripScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = R.string.add_a_trip
    val nextDestination = Screens.MyTrips.route

    var tripTitle by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var friends by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController,
            appBarTitle = title,
            showSaveButton = true,
            showMoreIcon = false,
            navDestination = nextDestination,
            onSave = {
                val newTrip = Trip(
                    tripTitle,
                    destination,
                    startDate,
                    endDate,
                    friends.split(",").map { it.trim() })
                firebaseViewModel.addTrip(newTrip)
                Toast.makeText(context, R.string.trip_saved, Toast.LENGTH_SHORT).show()
                navController.navigate(nextDestination)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tripTitle,
            onValueChange = { tripTitle = it },
            label = { Text(stringResource(R.string.trip_title)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text(stringResource(R.string.destination)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text(stringResource(R.string.start_date)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text(stringResource(R.string.end_date)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = friends,
            onValueChange = { friends = it },
            label = { Text(stringResource(R.string.friends)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}