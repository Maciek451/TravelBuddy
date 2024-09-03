package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.AuthenticationState

@Composable
fun SignUpScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val title = stringResource(id = R.string.app_name)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
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
        TextField(
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

        Spacer(modifier = Modifier.padding(4.dp))

        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var passwordConfirmationVisible by rememberSaveable { mutableStateOf(false) }
        TextField(
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

        Spacer(modifier = Modifier.padding(4.dp))

        TextField(
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
            text = "",
            color = MaterialTheme.colorScheme.error
        )

        Button(
            onClick = {
                when {
                    password != passwordConfirmation -> {
                        Toast.makeText(context, R.string.password_mismatch, Toast.LENGTH_SHORT)
                            .show()
                    }

                    password.length < 6 -> {
                        Toast.makeText(context, R.string.password_too_short, Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {
                        firebaseViewModel.signUpWithEmailAndPassword(
                            email,
                            password
                        ) { errorCode ->
                            when (errorCode) {
                                AuthenticationState.SIGNED_UP_SUCCESSFULLY -> {
                                    Toast.makeText(
                                        context,
                                        R.string.verification_email_sent,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showVerificationDialog = true
                                }

                                AuthenticationState.EMAIL_WRONG_FORMAT -> {
                                    errorMessage =
                                        context.getString(R.string.error_wrong_email_format)
                                    Toast.makeText(
                                        context,
                                        R.string.error_wrong_email_format,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                AuthenticationState.PASSWORD_WRONG_FORMAT -> {
                                    errorMessage = context.getString(R.string.password_too_short)
                                    Toast.makeText(
                                        context,
                                        R.string.password_too_short,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    errorMessage = context.getString(R.string.unknown_error)
                                    Toast.makeText(
                                        context,
                                        R.string.unknown_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(top = 25.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && passwordConfirmation.isNotEmpty()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = stringResource(id = R.string.sign_up_icon)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = stringResource(id = R.string.sign_up_button))
            }
        }
    }
}