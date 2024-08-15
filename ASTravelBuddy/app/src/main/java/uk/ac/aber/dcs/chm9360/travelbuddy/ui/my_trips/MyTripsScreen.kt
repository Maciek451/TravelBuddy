package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoLuggage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Trip
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun MyTripsScreen(
    navController: NavHostController,
    retrofitViewModel: RetrofitViewModel = viewModel(),
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val appBarTitle = stringResource(id = R.string.my_trips)
    val friendRequests by firebaseViewModel.friendRequests.collectAsState()
    val trips by firebaseViewModel.trips.collectAsState()
    val imageUrls by retrofitViewModel.imageUrls.collectAsState()
    val imageLoadingStates by retrofitViewModel.imageLoadingStates.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    val context = LocalContext.current

    LaunchedEffect(trips) {
        trips.forEach { trip ->
            retrofitViewModel.fetchImage(trip.destination)
        }
    }

    TopLevelScaffold(
        navController = navController,
        appBarTitle = appBarTitle,
        friendRequestCount = friendRequests.size
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (trips.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxSize()
                ) {
                    items(trips) { trip ->
                        val imageUrl = imageUrls[trip.destination]
                        val isLoading = imageLoadingStates[trip.destination] ?: false

                        TripCard(
                            trip = trip,
                            imageUrl = imageUrl,
                            isLoading = isLoading,
                            onItemClick = {
                                Utils.trip = trip
                                navController.navigate(Screens.TripDetails.route)
                            },
                            onShareClick = {
                                selectedTrip = trip
                                showConfirmDialog = true
                            },
                            isShareButtonEnabled = true
                        )
                    }
                }
            } else {
                EmptyMyTripsScreen()
            }
        }
    }
    LaunchedEffect(Unit) {
        firebaseViewModel.fetchTrips()
    }
    if (showConfirmDialog) {
        ConfirmTripStateDialog(
            showDialog = showConfirmDialog,
            onDismiss = { showConfirmDialog = false },
            onPostConfirmed = {
                selectedTrip?.let {
                    firebaseViewModel.postTrip(it) {
                        Toast.makeText(context, R.string.trip_shared, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
                showConfirmDialog = false
            }
        )
    }
}

@Composable
fun TripCard(
    trip: Trip,
    imageUrl: String?,
    isLoading: Boolean,
    onItemClick: () -> Unit,
    onShareClick: () -> Unit,
    isShareButtonEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.trip_start_date, trip.startDate),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(id = R.string.trip_end_date, trip.endDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .padding(16.dp)
                    )
                } else if (imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.failed_to_load_image),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Button(
                    onClick = onItemClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.details))
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isShareButtonEnabled) {
                    IconButton(
                        onClick = onShareClick,
                    ) {
                        Icon(
                            imageVector = if (trip.shared) Icons.Filled.Share else Icons.Outlined.Share,
                            contentDescription = stringResource(id = if (trip.shared) R.string.unshare else R.string.share)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(id = R.string.author, trip.author),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun EmptyMyTripsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .size(100.dp)
                .alpha(0.3f),
            imageVector = Icons.Default.NoLuggage,
            contentDescription = stringResource(id = R.string.empty_trips_screen_icon)
        )
        Text(
            modifier = Modifier
                .alpha(0.3f),
            text = stringResource(id = R.string.no_trips_text),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}