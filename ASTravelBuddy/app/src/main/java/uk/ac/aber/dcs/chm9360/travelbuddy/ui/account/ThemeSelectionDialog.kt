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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.saveThemePreference
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.themePreferenceFlow

@Composable
fun ThemeSelectionDialog(
    showDialog: Boolean,
    currentTheme: Int,
    onDismiss: () -> Unit,
    onThemeSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val selectedThemeState = rememberSaveable { mutableIntStateOf(currentTheme) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedTheme = context.themePreferenceFlow.first()
        selectedThemeState.intValue = savedTheme
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.select_theme),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column {
                    RadioButtonOption(
                        text = stringResource(id = R.string.light_theme),
                        isSelected = selectedThemeState.intValue == 0,
                        onSelect = { selectedThemeState.intValue = 0 }
                    )
                    RadioButtonOption(
                        text = stringResource(id = R.string.dark_theme),
                        isSelected = selectedThemeState.intValue == 1,
                        onSelect = { selectedThemeState.intValue = 1 }
                    )
                    RadioButtonOption(
                        text = stringResource(id = R.string.system_default),
                        isSelected = selectedThemeState.intValue == 2,
                        onSelect = { selectedThemeState.intValue = 2 }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            saveThemePreference(context, selectedThemeState.intValue)
                            onThemeSelected(selectedThemeState.intValue)
                            onDismiss()
                        }
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