package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    navController: NavHostController,
    appBarTitle: String,
    showLocationButton: Boolean = false,
    onLocationButtonClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(appBarTitle)
        },
        actions = {
            if (showLocationButton) {
                IconButton(onClick = { onLocationButtonClick?.invoke() }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
            IconButton(onClick = { navController.navigate(Screens.Notification.route) }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.notification_button)
                )
            }
            IconButton(onClick = { navController.navigate(Screens.Account.route) }) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.account_button)
                )
            }
        }
    )
}