package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun SetUsernameScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val context = LocalContext.current
    val title = stringResource(id = R.string.set_username)

    var username by rememberSaveable { mutableStateOf("") }
    var isUsernameAvailable by rememberSaveable { mutableStateOf(true) }
    var isSaveButtonEnabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            firebaseViewModel.isUsernameTaken(username) { isTaken ->
                isUsernameAvailable = !isTaken
            }
        } else {
            isUsernameAvailable = true
        }
        isSaveButtonEnabled = username.isNotEmpty() && isUsernameAvailable
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showNavIcon = false,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSave = {
                if (isSaveButtonEnabled) {
                    firebaseViewModel.updateUsername(username) { isSuccess ->
                        val messageResId = if (isSuccess) {
                            R.string.username_updated
                        } else {
                            R.string.username_update_failed
                        }
                        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
                        if (isSuccess) {
                            navController.navigate(Screens.MyTrips.route) { popUpTo(0) }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.username_required_message),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { newUsername ->
                username = newUsername
            },
            label = { Text(stringResource(id = R.string.username)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ,
            isError = !isUsernameAvailable && username.isNotEmpty()
        )

        if (!isUsernameAvailable && username.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.error_username_taken),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}