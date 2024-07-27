package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun TripDetailsScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val trip = Utils.trip
    val appBarTitle = trip?.title
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (appBarTitle != null) {
            AppBarWithArrowBack(
                navController = navController,
                appBarTitle = appBarTitle,
                tripMenu = true,
                onRemoveTrip = { showConfirmDialog = true }
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start: ${trip.startDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.Black)
                    )

                    Text(
                        text = "End: ${trip.endDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = "Destination: ${trip.destination}",
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {  },
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Text(
                        text = "Trip Plan",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {  },
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Text(
                        text = "Checklist",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            Text(
                text = "No trip data available",
                modifier = Modifier.padding(16.dp)
            )
        }
        if (showConfirmDialog) {
            ConfirmTripRemovalDialog(
                showDialog = showConfirmDialog,
                onDismiss = { showConfirmDialog = false },
                onRemoveConfirmed = {
                    trip?.let { tripToRemove ->
                        firebaseViewModel.removeTrip(tripToRemove) { isSuccess ->
                            if (isSuccess) {
                                navController.popBackStack()
                            }
                        }
                    }
                    showConfirmDialog = false
                }
            )
        }
    }
}