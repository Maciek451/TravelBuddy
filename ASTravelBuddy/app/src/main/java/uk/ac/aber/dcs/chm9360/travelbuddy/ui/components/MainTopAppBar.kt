package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    navController: NavHostController,
    appBarTitle: String,
    notificationCount: Int,
) {
    TopAppBar(
        title = {
            Text(appBarTitle)
        },
        actions = {
            IconButton(onClick = { navController.navigate(Screens.Notification.route) }) {
                Box {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.notification_button)
                    )
                    if (notificationCount > 0) {
                        BadgeBox(notificationCount)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeBox(notificationCount: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .offset(x = (-5).dp, y = 5.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Badge {
            Text(text = notificationCount.toString())
        }
    }
}