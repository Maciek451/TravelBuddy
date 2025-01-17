package uk.ac.aber.dcs.chm9360.travelbuddy.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.AuthenticationState

@Composable
fun SignInScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showVerificationDialog by rememberSaveable { mutableStateOf(false) }

    ForgotPasswordDialog(
        showDialog = showPasswordDialog,
        onDismiss = { showPasswordDialog = false },
        firebaseViewModel = firebaseViewModel
    )

    VerificationDialog(
        showDialog = showVerificationDialog,
        onDismiss = { showVerificationDialog = false },
        onResendEmail = {
            firebaseViewModel.sendVerificationEmail { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(context, R.string.verification_email_resent, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        R.string.failed_to_resend_verification_email,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(150.dp),
            painter = painterResource(id = R.drawable.luggage),
            contentDescription = stringResource(id = R.string.app_icon),
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier
                .padding(top = 25.dp),
            fontSize = 30.sp
        )
        TextField(
            value = email,
            label = {
                Text(text = stringResource(id = R.string.email_address))
            },
            onValueChange = {
                email = it
                errorMessage = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 3.dp),
            isError = errorMessage.isNotEmpty()
        )

        Spacer(modifier = Modifier.padding(4.dp))

        var passwordVisible by rememberSaveable { mutableStateOf(false) }
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
                .padding(bottom = 8.dp),
            isError = errorMessage.isNotEmpty(),
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
        Text(
            text = "",
            color = MaterialTheme.colorScheme.error
        )

        Button(
            onClick = {
                firebaseViewModel.signInWithEmailAndPassword(email, password) { errorCode ->
                    when (errorCode) {
                        AuthenticationState.LOGGED_IN_SUCCESSFULLY -> {
                            Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT)
                                .show()
                            firebaseViewModel.checkUsernameAndNavigate { needsUsername ->
                                if (needsUsername) {
                                    navController.navigate(Screens.SetUsername.route) { popUpTo(0) }
                                } else {
                                    navController.navigate(Screens.MyTrips.route) { popUpTo(0) }
                                }
                            }
                        }

                        AuthenticationState.USER_IS_NOT_VERIFIED -> {
                            showVerificationDialog = true
                            Toast.makeText(
                                context,
                                R.string.error_user_not_verified,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        AuthenticationState.PASSWORD_WRONG -> {
                            errorMessage = context.getString(R.string.error_wrong_password_or_email)
                            Toast.makeText(
                                context,
                                R.string.error_wrong_password_or_email,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        AuthenticationState.ACCOUNT_DOES_NOT_EXIST -> {
                            errorMessage = context.getString(R.string.error_account_does_not_exist)
                            Toast.makeText(
                                context,
                                R.string.error_account_does_not_exist,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        AuthenticationState.EMAIL_WRONG_FORMAT -> {
                            errorMessage = context.getString(R.string.error_wrong_email_format)
                            Toast.makeText(
                                context,
                                R.string.error_wrong_email_format,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            errorMessage = context.getString(R.string.unknown_error)
                            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(top = 10.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = stringResource(id = R.string.login_icon)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = stringResource(id = R.string.sign_in_button))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    navController.navigate(Screens.SignUp.route)
                },
                modifier = Modifier
                    .padding(top = 10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.create_account),
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            TextButton(
                onClick = {
                    showPasswordDialog = true
                },
                modifier = Modifier
                    .padding(top = 10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.forgot_password),
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}