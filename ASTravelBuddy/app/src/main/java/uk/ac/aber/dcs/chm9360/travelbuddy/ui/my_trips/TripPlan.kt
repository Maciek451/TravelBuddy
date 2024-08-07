package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.TripPlanItem
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun TripPlanScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val trip = Utils.trip
    val appBarTitle = stringResource(id = R.string.trip_plan)
    var tripPlans by remember { mutableStateOf(trip?.tripPlans ?: emptyList()) }

    LaunchedEffect(trip?.id) {
        trip?.id?.let { tripId ->
            firebaseViewModel.fetchTripPlans(tripId) { fetchedTripPlans ->
                tripPlans = fetchedTripPlans
                Utils.trip = trip.copy(tripPlans = fetchedTripPlans)
            }
        }
    }

    fun removeTripPlan(tripPlan: TripPlanItem) {
        trip?.id?.let { tripId ->
            firebaseViewModel.removeTripPlan(tripId, tripPlan) { success ->
                if (success) {
                    tripPlans = tripPlans.filter { it.id != tripPlan.id }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = appBarTitle,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = false
        )

        LazyColumn {
            items(tripPlans) { tripPlan ->
                TripPlanItem(
                    tripPlan = tripPlan,
                    onTripPlanDelete = { removeTripPlan(it) },
                    onCardClick = {
                    }
                )
            }
            item {
                TextButton(
                    onClick = {
                        Utils.trip = trip
                        navController.navigate(Screens.AddTripPlan.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 18.sp,
                            text = stringResource(id = R.string.list_item_button),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripPlanItem(
    tripPlan: TripPlanItem,
    onTripPlanDelete: (TripPlanItem) -> Unit,
    onCardClick: () -> Unit
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = tripPlan.place,
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        }
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = tripPlan.dateOfVisit,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_icon)
                )
            }
        }
    }

    if (showDialog) {
        DeleteTripPlanDialog(
            onConfirmDelete = { onTripPlanDelete(tripPlan) },
            onDismiss = { showDialog = false }
        )
    }
}


@Composable
fun DeleteTripPlanDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.confirm_remove_trip_plan)) },
        text = { Text(text = stringResource(id = R.string.trip_plan_delete_confirmation)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirmDelete()
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}