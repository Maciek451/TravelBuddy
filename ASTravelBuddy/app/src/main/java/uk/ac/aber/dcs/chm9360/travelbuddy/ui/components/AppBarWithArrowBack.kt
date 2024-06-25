package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import android.widget.Button
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithArrowBack(
    navController: NavHostController,
    appBarTitle: Int,
    showMoreIcon: Boolean = true,
    showRemoveIcon: Boolean = false,
    alternateMenu: Boolean = false,
    showSaveButton: Boolean = false,
    navDestination: String? = null
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = appBarTitle)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.arrow_back)
                )
            }
        },
        actions = {
            if (showSaveButton && navDestination != null) {
                IconButton(onClick = { navController.navigate(navDestination) }) {
                    Icon(
                        Icons.Outlined.Done,
                        contentDescription = stringResource(R.string.done_icon)
                    )
                }
            }
            if (showRemoveIcon) {
                IconButton(onClick = {  }) {
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
                if (alternateMenu) {
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
                }
                else {
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        // Define your menu items here
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_account)) },
                            onClick = { }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove_all_data)) },
                            onClick = { }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.log_out)) },
                            onClick = { navController.navigate(Screens.SignIn.route) { popUpTo(0)}
                                Toast.makeText(context, R.string.signedOut, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    )
}