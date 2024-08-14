package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
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
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.LocaleManager
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.getLanguagePreference
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.languagePreferenceFlow
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.saveLanguagePreference

fun restartActivity(context: Context) {
    if (context is Activity) {
        val intent = Intent(context, context::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.finish()
        context.startActivity(intent)
    }
}

@Composable
fun LanguageSelectionDialog(
    showDialog: Boolean,
    currentLanguage: Int,
    onDismiss: () -> Unit,
    onLanguageSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val selectedLanguageState = rememberSaveable { mutableIntStateOf(currentLanguage) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedLanguage = context.languagePreferenceFlow.first()
        selectedLanguageState.intValue = savedLanguage
    }

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
                        isSelected = selectedLanguageState.intValue == 0,
                        onSelect = { selectedLanguageState.intValue = 0 }
                    )
                    RadioButtonOption(
                        text = stringResource(id = R.string.polish),
                        isSelected = selectedLanguageState.intValue == 1,
                        onSelect = { selectedLanguageState.intValue = 1 }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            saveLanguagePreference(context, selectedLanguageState.intValue)
                            onLanguageSelected(selectedLanguageState.intValue)
                            LocaleManager.setLocale(context, if (selectedLanguageState.intValue == 0) "en" else "pl")
                            restartActivity(context)
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