package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.User
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun TripDetailsScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val trip = Utils.trip
    val context = LocalContext.current
    val appBarTitle = trip?.title
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showConfirmStateDialog by remember { mutableStateOf(false) }
    var checklist by remember { mutableStateOf(trip?.checklist ?: emptyList()) }
    var tripPlans by remember { mutableStateOf(trip?.tripPlans ?: emptyList()) }
    val authState = firebaseViewModel.authState.collectAsState().value
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isUserAuthor by remember { mutableStateOf(false) }

    LaunchedEffect(authState?.uid) {
        authState?.uid?.let { userId ->
            firebaseViewModel.getUserData(userId) { user ->
                currentUser = user
                isUserAuthor = (user?.username == trip?.author)
            }
        }
    }

    LaunchedEffect(trip?.id) {
        trip?.id?.let { tripId ->
            firebaseViewModel.fetchChecklist(tripId) { fetchedChecklist ->
                checklist = fetchedChecklist
                Utils.trip = trip.copy(checklist = fetchedChecklist)
            }
            firebaseViewModel.fetchTripPlans(tripId) { fetchedTripPlans ->
                tripPlans = fetchedTripPlans
                Utils.trip = trip.copy(tripPlans = fetchedTripPlans)
            }
        }
    }

    val uncheckedItemsPreview = checklist.filter { it.checked == "false" }.take(5)
    val tripPlansPreview = tripPlans.take(5)

    fun getWeatherLink(destination: String?): String {
        val location = destination?.substringBefore(",")?.trim() ?: ""
        return "https://www.google.com/search?q=weather+$location"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (appBarTitle != null) {
            AppBarWithArrowBack(
                navController = navController,
                appBarTitle = appBarTitle,
                tripMenu = isUserAuthor,
                showMoreIcon = isUserAuthor,
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
                        text = stringResource(id = R.string.trip_start_date, trip.startDate),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.Black)
                    )

                    Text(
                        text = stringResource(id = R.string.trip_end_date, trip.endDate),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(id = R.string.destination),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = trip.destination,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(end = 8.dp)
                            .clickable {
                                Utils.trip = trip
                                navController.navigate(Screens.TripMap.route)
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = stringResource(id = R.string.map_icon),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.show_on_map),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp)
                            .clickable {
                                val weatherLink = getWeatherLink(trip.destination)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(weatherLink))
                                navController.context.startActivity(intent)
                            },
                        elevation = CardDefaults.cardElevation(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = stringResource(id = R.string.weather_icon),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.check_weather),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            Utils.trip = trip
                            navController.navigate(Screens.Checklist.route)
                        },
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.checklist_colon),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (uncheckedItemsPreview.isNotEmpty()) {
                            uncheckedItemsPreview.forEachIndexed { index, item ->
                                Text(
                                    text = "${index + 1}. ${item.task}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(id = R.string.list_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            Utils.trip = trip
                            navController.navigate(Screens.TripPlan.route)
                        },
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.trip_plan_colon),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (tripPlansPreview.isNotEmpty()) {
                            tripPlansPreview.forEachIndexed { index, plan ->
                                Text(
                                    text = "${index + 1}. ${plan.place}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(id = R.string.no_plans),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                if ((currentUser?.username == trip.author)) {
                    Text(
                        text = if (trip.shared) stringResource(R.string.status_public) else stringResource(R.string.status_private),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showConfirmStateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        enabled = trip.shared,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(text = stringResource(R.string.set_not_shared))
                    }
                }
            }
        }
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

    if (showConfirmStateDialog) {
        ConfirmTripStateDialog(
            showDialog = showConfirmStateDialog,
            onDismiss = { showConfirmStateDialog = false },
            onPostConfirmed = {
                Utils.trip = trip
                if (trip != null) {
                    firebaseViewModel.tripToPrivate(trip) {
                        Toast.makeText(context, R.string.trip_not_shared, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
                showConfirmStateDialog = false
                navController.navigate(Screens.MyTrips.route)
            }
        )
    }
}