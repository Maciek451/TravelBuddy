package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.DeleteAccountDialog
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.account.RemoveAllDataDialog
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithArrowBack(
    navController: NavHostController,
    appBarTitle: Int,
    showMoreIcon: Boolean = true,
    showRemoveIcon: Boolean = false,
    tripMenu: Boolean = false,
    showSaveButton: Boolean = false,
    navDestination: String? = null,
    onSave: (() -> Unit)? = null,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showRemoveDataDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    DeleteAccountDialog(
        showDialog = showDeleteAccountDialog,
        onDismiss = { showDeleteAccountDialog = false },
        onDeleteAccount = {
            firebaseViewModel.deleteUserAccount { isSuccess ->
                if (isSuccess) {
                    firebaseViewModel.signOut()
                    navController.navigate(Screens.SignIn.route) {
                        popUpTo(0)
                    }
                    Toast.makeText(context, R.string.account_deleted, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.failed_to_delete_account, Toast.LENGTH_LONG)
                        .show()
                }
                showDeleteAccountDialog = false
            }
        }
    )

    RemoveAllDataDialog(
        showDialog = showRemoveDataDialog,
        onDismiss = { showRemoveDataDialog = false },
        onDataRemoved = {
            firebaseViewModel.removeAllUserData { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(context, R.string.data_removed, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.failed_to_remove_data, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            showRemoveDataDialog = false
        }
    )

    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = appBarTitle)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.arrow_back)
                )
            }
        },
        actions = {
            if (showSaveButton && onSave != null) {
                IconButton(onClick = { onSave() }) {
                    Icon(
                        Icons.Outlined.Done,
                        contentDescription = stringResource(R.string.done_icon)
                    )
                }
            }
            if (showRemoveIcon) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.delete_sweep_icon)
                    )
                }
            }
            if (showMoreIcon) {
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more_icon)
                    )
                }
                if (tripMenu) {
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_trip)) },
                            onClick = { }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove_trip)) },
                            onClick = { }
                        )
                    }
                } else {
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_account)) },
                            onClick = {
                                showDeleteAccountDialog = true
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove_all_data)) },
                            onClick = {
                                showRemoveDataDialog = true
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.log_out)) },
                            onClick = {
                                firebaseViewModel.signOut()
                                navController.navigate(Screens.SignIn.route) { popUpTo(0) }
                                Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT)
                                    .show()
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}