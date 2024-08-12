package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.ChooseTripDialog
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun PlaceDetailsScreen(
    navController: NavHostController,
) {
    val placeDetails = Utils.placeDetails
    val appBarTitle = placeDetails?.properties?.name
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { locationPermissionGranted.value = it }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted.value = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val placeMarker =
        placeDetails?.properties?.let { createMapMarkerFromDestination(it.formatted, context) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (appBarTitle != null) {
            AppBarWithArrowBack(
                navController = navController,
                appBarTitle = appBarTitle,
                showSaveButton = true,
                showMoreIcon = false,
                isSaveButtonEnabled = false
            )
        }

        if (placeDetails != null) {
            Text(
                text = placeDetails.properties.formatted,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    Utils.placeDetails = placeDetails
                    showDialog = true
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(id = R.string.add_to_trip))
            }

            if (showDialog) {
                ChooseTripDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    navController = navController,
                )
            }

            if (placeMarker != null) {
                MapCard(
                    placeMarker = placeMarker
                )
            }
        }
    }
}


@Composable
fun MapCard(
    placeMarker: MapMarker
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(300.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        MapComposable(context = context, centerMarker = placeMarker)
    }
}