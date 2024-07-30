package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithArrowBack(
    navController: NavHostController,
    appBarTitle: String,
    showMoreIcon: Boolean = true,
    showRemoveIcon: Boolean = false,
    tripMenu: Boolean = false,
    showSaveButton: Boolean = false,
    showSignOut: Boolean = false,
    isSaveButtonEnabled: Boolean = true,
    onSave: (() -> Unit)? = null,
    firebaseViewModel: FirebaseViewModel = viewModel(),
    onRemoveTrip: () -> Unit = {}
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val trip = Utils.trip

    DisposableEffect(Unit) {
        onDispose {
            isMenuExpanded = false
        }
    }

    CenterAlignedTopAppBar(
        title = { Text(appBarTitle) },
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
                IconButton(
                    onClick = { onSave() },
                    enabled = isSaveButtonEnabled
                ) {
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
            if (showSignOut) {
                IconButton(
                    onClick = {
                        firebaseViewModel.signOut()
                        navController.navigate(Screens.SignIn.route) { popUpTo(0) }
                        Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.sign_out),
                        tint = MaterialTheme.colorScheme.error
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
                            text = { Text(stringResource(R.string.edit_details)) },
                            onClick = {
                                Utils.trip = trip
                                navController.navigate(Screens.EditTrip.route)
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove_trip)) },
                            onClick = {
                                onRemoveTrip()
                                isMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}