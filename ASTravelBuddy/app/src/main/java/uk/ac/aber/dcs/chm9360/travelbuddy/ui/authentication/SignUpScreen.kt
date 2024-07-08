package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun SignUpScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = R.string.app_name
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var showVerificationDialog by rememberSaveable { mutableStateOf(false) }

    var errorMessage by rememberSaveable { mutableStateOf("") }
    val passwordError by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    AfterSignUpDialog(
        navController = navController,
        showDialog = showVerificationDialog,
        onDismiss = { showVerificationDialog = false }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBarWithArrowBack(navController, title, showMoreIcon = false)
        Text(
            text = stringResource(id = R.string.create_account),
            modifier = Modifier.padding(bottom = 25.dp, top = 120.dp),
            fontSize = 30.sp
        )
        OutlinedTextField(
            value = email,
            label = {
                Text(stringResource(id = R.string.email_address))
            },
            onValueChange = {
                email = it
                errorMessage = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 8.dp, end = 8.dp),
            isError = errorMessage.isNotEmpty()
        )
        OutlinedTextField(
            value = username,
            label = {
                Text(text = stringResource(id = R.string.username))
            },
            onValueChange = {
                username = it
                errorMessage = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            isError = errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var passwordConfirmationVisible by rememberSaveable { mutableStateOf(false) }
        OutlinedTextField(
            value = password,
            label = {
                Text(text = stringResource(id = R.string.password))
            },
            onValueChange = {
                password = it
                errorMessage = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            isError = passwordError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "" else ""

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )
        OutlinedTextField(
            value = passwordConfirmation,
            label = {
                Text(text = stringResource(id = R.string.confirm_password))
            },
            onValueChange = {
                passwordConfirmation = it
                errorMessage = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            isError = passwordError,
            visualTransformation = if (passwordConfirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordConfirmationVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordConfirmationVisible) "" else ""

                IconButton(onClick = {
                    passwordConfirmationVisible = !passwordConfirmationVisible
                }) {
                    Icon(imageVector = image, description)
                }
            }
        )
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error
        )

        Button(
            onClick = {
                if (password == passwordConfirmation) {
                    firebaseViewModel.signUp(email, password, username)
                }
            },
            modifier = Modifier
                .padding(top = 25.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && passwordConfirmation.isNotEmpty()
        ) {
            Text(text = stringResource(id = R.string.sign_up_button))
        }
    }
}
