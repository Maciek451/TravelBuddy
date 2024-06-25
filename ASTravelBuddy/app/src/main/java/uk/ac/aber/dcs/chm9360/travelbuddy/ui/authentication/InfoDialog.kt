package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun InfoDialog(
    navController: NavController,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.verification_needed_title),
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.verification_needed_2),
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss
                        navController.navigate(Screens.SignIn.route)
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            }

        )
    }
}