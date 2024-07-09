package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun VerificationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onResendEmail: () -> Unit
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
                    text = stringResource(id = R.string.verification_needed_content),
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResendEmail()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.resend_email))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(text = stringResource(R.string.try_again))
                }
            }
        )
    }
}