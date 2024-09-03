package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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

@Composable
fun AddFriendScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = stringResource(id = R.string.add_a_friend)
    val context = LocalContext.current

    var emailOrUsername by rememberSaveable { mutableStateOf("") }
    var isDropdownVisible by rememberSaveable { mutableStateOf(false) }
    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var cityHasFocus by rememberSaveable { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val authState by firebaseViewModel.authState.collectAsState()
    val friends by firebaseViewModel.friends.collectAsState()

    LaunchedEffect(emailOrUsername, friends) {
        if (emailOrUsername.isNotBlank()) {
            firebaseViewModel.fetchUsers { fetchedUsers ->
                filteredUsers = fetchedUsers
                    .filter { user ->
                        user.email.contains(emailOrUsername, ignoreCase = true) ||
                                user.username.contains(emailOrUsername, ignoreCase = true)
                    }
                    .filter { user ->
                        user.userId != authState?.uid &&
                                friends.none { it.userId == user.userId }
                    }
                    .filter { it != selectedUser }
                isDropdownVisible = filteredUsers.isNotEmpty()
            }
        } else {
            isDropdownVisible = false
        }
    }

    val isSaveButtonEnabled by rememberSaveable(emailOrUsername, selectedUser) {
        mutableStateOf(emailOrUsername.isNotBlank() && selectedUser != null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSave = {
                selectedUser?.let { user ->
                    firebaseViewModel.sendFriendRequest(user.userId) { success, reason ->
                        when {
                            success -> {
                                Toast.makeText(context, R.string.friend_request_sent, Toast.LENGTH_SHORT).show()
                                navController.navigate(Screens.Friends.route) { popUpTo(0) }
                            }
                            reason == "already_friends" -> {
                                Toast.makeText(context, R.string.already_friends, Toast.LENGTH_SHORT).show()
                            }
                            reason == "already_sent" -> {
                                Toast.makeText(context, R.string.already_sent_request, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, R.string.failed_to_send_request, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailOrUsername,
            onValueChange = { newValue ->
                emailOrUsername = newValue
                selectedUser = null
                isDropdownVisible = newValue.isNotEmpty()
            },
            label = { Text(stringResource(R.string.email_address)) },
            trailingIcon = {
                if (emailOrUsername.isNotEmpty() && cityHasFocus) {
                    IconButton(
                        onClick = {
                            emailOrUsername = ""
                            isDropdownVisible = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear_icon)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged { focusState ->
                    cityHasFocus = focusState.isFocused
                }
        )

        if (isDropdownVisible) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .align(Alignment.Start)
            ) {
                items(filteredUsers) { user ->
                    Text(
                        text = user.email,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                emailOrUsername = user.email
                                selectedUser = user
                                isDropdownVisible = false
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}