package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.User
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun FriendsListScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = R.string.your_friends
    val friends by firebaseViewModel.friends.collectAsState()

    LaunchedEffect(Unit) {
        firebaseViewModel.fetchFriends()
    }

    val context = LocalContext.current

    val showMessage = rememberSaveable { mutableStateOf(false) }
    val messageText = rememberSaveable { mutableStateOf("") }

    if (showMessage.value) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, messageText.value, Toast.LENGTH_SHORT).show()
            showMessage.value = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showMoreIcon = false
        )
        if (friends.isEmpty()) {
            EmptyFriendsScreen()
        } else {
            LazyColumn {
                items(friends) { friend ->
                    FriendItem(friend, onDeleteClick = {
                        firebaseViewModel.removeFriend(friend.userId) { success ->
                            if (success) {
                                messageText.value =
                                    context.getString(R.string.friend_removed_successfully)
                            } else {
                                messageText.value =
                                    context.getString(R.string.failed_to_remove_friend)
                            }
                            showMessage.value = true
                        }
                    }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendItem(user: User, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.delete)
            )
        }
    }
}

@Composable
fun EmptyFriendsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier
                .size(100.dp)
                .alpha(0.3f),
            imageVector = Icons.Default.PeopleOutline,
            contentDescription = stringResource(id = R.string.empty_friends_screen_icon)
        )
        Text(
            modifier = Modifier
                .alpha(0.3f),
            text = stringResource(id = R.string.no_friends_text),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}