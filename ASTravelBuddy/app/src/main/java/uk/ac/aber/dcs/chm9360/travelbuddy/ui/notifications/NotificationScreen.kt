package uk.ac.aber.dcs.chm9360.travelbuddy.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.FriendRequest
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun NotificationScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val appBarTitle = stringResource(id = R.string.notifications)
    val friendRequests by firebaseViewModel.friendRequests.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = appBarTitle,
            showMoreIcon = false
        )

        if (friendRequests.isEmpty()) {
            EmptyNotificationScreen()
        } else {
            LazyColumn {
                items(friendRequests) { request ->
                    FriendRequestCard(
                        friendRequest = request,
                        onAccept = {
                            firebaseViewModel.acceptFriendRequest(request) {  }
                                   },
                        onDecline = {
                            firebaseViewModel.declineFriendRequest(request) {  }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(friendRequest: FriendRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Friend request from: ${friendRequest.senderUsername} (${friendRequest.senderEmail})")
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onAccept) {
                    Text(text = "Accept")
                }
                TextButton(onClick = onDecline) {
                    Text(text = "Decline")
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .size(100.dp)
                .alpha(0.3f),
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = stringResource(id = R.string.empty_notifications_screen_icon)
        )
        Text(
            modifier = Modifier
                .alpha(0.3f),
            text = stringResource(id = R.string.no_notifications_text),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

//        val postNotificationPermission =
//            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
//        val notificationHandler = NotificationHandler(context)
//
//        LaunchedEffect(key1 = true) {
//            if (!postNotificationPermission.status.isGranted) {
//                postNotificationPermission.launchPermissionRequest()
//            }
//        }
//        Button(
//            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
//            onClick = { notificationHandler.showSimpleNotification() }
//        ) {
//            Text(text = "Test notification")
//        }