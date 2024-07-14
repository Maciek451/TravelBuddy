package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun RemoveAllDataDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDataRemoved: () -> Unit
) {
    var confirmText by rememberSaveable { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = stringResource(id = R.string.remove_headline))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(id = R.string.confirm_text_description)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text(stringResource(id = R.string.confirm_text_upper)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmText.equals("CONFIRM", ignoreCase = true)) {
                            onDataRemoved()
                        }
                    },
                    enabled = confirmText.equals("CONFIRM", ignoreCase = true)
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