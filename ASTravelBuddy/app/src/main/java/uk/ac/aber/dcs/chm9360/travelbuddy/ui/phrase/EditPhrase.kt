package uk.ac.aber.dcs.chm9360.travelbuddy.ui.phrase

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils

@Composable
fun EditPhraseScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val phrase = Utils.phrase
    val context = LocalContext.current

    if (phrase == null) {
        Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }

    val title = stringResource(id = R.string.edit_phrase)
    var language by rememberSaveable { mutableStateOf(phrase.language) }
    var phraseText by rememberSaveable { mutableStateOf(phrase.phrase) }
    var translation by rememberSaveable { mutableStateOf(phrase.translation) }

    val isSaveButtonEnabled by rememberSaveable(language, phraseText, translation) {
        mutableStateOf(
            language.isNotBlank() &&
                    phraseText.isNotBlank() &&
                    translation.isNotBlank()
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = title,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSave = {
                val updatedPhrase = phrase.copy(
                    language = language,
                    phrase = phraseText,
                    translation = translation
                )
                firebaseViewModel.updatePhrase(updatedPhrase) {
                    Toast.makeText(context, R.string.phrase_updated, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text(stringResource(R.string.language)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phraseText,
            onValueChange = { phraseText = it },
            label = { Text(stringResource(R.string.phrase)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = translation,
            onValueChange = { translation = it },
            label = { Text(stringResource(R.string.translation)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )
    }
}