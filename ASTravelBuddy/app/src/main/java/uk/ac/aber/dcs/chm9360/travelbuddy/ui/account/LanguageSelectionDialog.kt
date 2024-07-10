package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun LanguageSelectionDialog(
    showDialog: Boolean,
    currentLanguage: Int,
    onDismiss: () -> Unit,
    onLanguageSelected: (Int) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.select_language),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column {
                    RadioButtonOption(
                        text = stringResource(id = R.string.english),
                        isSelected = currentLanguage == 0,
                        onSelect = { onLanguageSelected(0) }
                    )
                    RadioButtonOption(
                        text = stringResource(id = R.string.polish),
                        isSelected = currentLanguage == 1,
                        onSelect = { onLanguageSelected(1) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLanguageSelected(currentLanguage)
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RadioButtonOption(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}