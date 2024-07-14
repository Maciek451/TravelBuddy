package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun ConfirmFriendRemovalDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRemoveConfirmed: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = stringResource(id = R.string.remove_friend_headline))
            },
            text = {
                Text(text = stringResource(id = R.string.confirm_remove_friend))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveConfirmed()
                    }
                ) {
                    Text(stringResource(id = R.string.remove))
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