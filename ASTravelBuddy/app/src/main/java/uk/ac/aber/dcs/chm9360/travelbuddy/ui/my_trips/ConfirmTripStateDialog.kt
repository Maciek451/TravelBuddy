package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun ConfirmTripStateDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPostConfirmed: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = stringResource(id = R.string.click_to_confirm))
            },
            text = {
                Text(text = stringResource(id = R.string.confirm_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPostConfirmed()
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}