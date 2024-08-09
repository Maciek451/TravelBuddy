package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun ChooseTripDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val placeDetails = Utils.placeDetails
    val appBarTitle = stringResource(R.string.choose_trip)
    val trips by firebaseViewModel.trips.collectAsState()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = appBarTitle, style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                if (trips.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(trips) { trip ->
                            TripItem(
                                trip = trip,
                                onItemClick = {
                                    Utils.placeDetails = placeDetails
                                    Utils.trip = trip
                                    navController.navigate(Screens.AddTripPlanFromExplore.route)
                                    onDismiss()
                                }
                            )
                        }
                    }
                } else {
                    Text(text = stringResource(id = R.string.no_trips_text))
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripItem(
    trip: Trip,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onItemClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = trip.destination,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}