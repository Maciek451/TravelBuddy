package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = R.string.profile
    val authState by firebaseViewModel.authState.collectAsState()
    val email = authState?.email ?: stringResource(id = R.string.email_not_available)
    val monogram = email.firstOrNull()?.uppercase() ?: ""
    val username by firebaseViewModel.username.collectAsState()
    val creationDate by firebaseViewModel.creationDate.collectAsState()
    val showUsernameDialog = rememberSaveable { mutableStateOf(false) }
    val showDeleteAccountDialog = rememberSaveable { mutableStateOf(false) }
    val showRemoveAllDataDialog = rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        firebaseViewModel.fetchUsername()
        firebaseViewModel.fetchCreationDate()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(navController = navController, appBarTitle = title, showSaveButton = false, showMoreIcon = false)

        EditUsernameDialog(
            showDialog = showUsernameDialog.value,
            onDismiss = { showUsernameDialog.value = false },
            firebaseViewModel = firebaseViewModel
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            onClick = { showUsernameDialog.value = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monogram,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.padding(10.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = username ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_icon)
                        )
                    }
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.account_creation_date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = creationDate ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { showDeleteAccountDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(text = stringResource(id = R.string.delete_account), color = Color.White)
            }
            Button(
                onClick = { showRemoveAllDataDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(text = stringResource(id = R.string.remove_all_data), color = Color.White)
            }
        }
    }

    if (showDeleteAccountDialog.value) {
        DeleteAccountDialog(
            showDialog = showDeleteAccountDialog.value,
            onDismiss = { showDeleteAccountDialog.value = false },
            onDeleteAccount = {
                firebaseViewModel.deleteUserAccount { isSuccess ->
                    if (isSuccess) {
                        firebaseViewModel.signOut()
                        navController.navigate(Screens.SignIn.route) {
                            popUpTo(0)
                        }
                        Toast.makeText(context, R.string.account_deleted, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, R.string.failed_to_delete_account, Toast.LENGTH_LONG).show()
                    }
                    showDeleteAccountDialog.value = false
                }
            }
        )
    }

    if (showRemoveAllDataDialog.value) {
        RemoveAllDataDialog(
            showDialog = showRemoveAllDataDialog.value,
            onDismiss = { showRemoveAllDataDialog.value = false },
            onDataRemoved = {
                firebaseViewModel.removeAllUserData { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(context, R.string.data_removed, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.failed_to_remove_data, Toast.LENGTH_SHORT).show()
                    }
                    showRemoveAllDataDialog.value = false
                }
            }
        )
    }
}