package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun AccountScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val appBarTitle = stringResource(id = R.string.account)
    val navigateToFriendsList: () -> Unit = { navController.navigate(Screens.FriendsList.route) }
    val navigateToTermsOfService: () -> Unit = { navController.navigate(Screens.TermsOfService.route) }
    val navigateToAbout: () -> Unit = { navController.navigate(Screens.About.route) }

    val authState by firebaseViewModel.authState.collectAsState()
    val email = authState?.email ?: stringResource(id = R.string.email_not_available)
    val monogram = email.firstOrNull()?.uppercase() ?: ""
    val username by firebaseViewModel.username.collectAsState()

    val showThemeDialog = rememberSaveable { mutableStateOf(false) }
    val showLanguageDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseViewModel.fetchUsername()
    }

    ThemeSelectionDialog(
        showDialog = showThemeDialog.value,
        currentTheme = 0,
        onDismiss = { showThemeDialog.value = false },
        onThemeSelected = {
            showThemeDialog.value = false
        }
    )
    LanguageSelectionDialog(
        showDialog = showLanguageDialog.value,
        currentLanguage = 0,
        onDismiss = { showLanguageDialog.value = false },
        onLanguageSelected = {
            showLanguageDialog.value = false
        }
    )

    Column {
        AppBarWithArrowBack(navController, appBarTitle = appBarTitle, showSignOut = true, showMoreIcon = false)
        UserCard(username ?: "", email, monogram)
        Button(
            onClick = {
                navController.navigate(Screens.Profile.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.edit_profile))
        }
        SettingsList(
            items = listOf(
                R.string.your_friends to navigateToFriendsList,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.settings),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
        )
        SettingsList(
            items = listOf(
                R.string.terms_of_service to navigateToTermsOfService,
                R.string.language to { showLanguageDialog.value = true },
                R.string.theme to { showThemeDialog.value = true },
                R.string.about to navigateToAbout
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.version),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = " 1.0",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Composable
fun SettingsList(
    items: List<Pair<Int, () -> Unit>>
) {
    val listItemBackgroundColor = MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items.forEachIndexed { index, (textResId, onClick) ->
            ListItem(
                text = textResId,
                onClick = onClick,
                backgroundColor = listItemBackgroundColor,
                isFirst = index == 0,
                isLast = index == items.size - 1
            )
            if (index < items.size - 1) {
                Divider()
            }
        }
    }
}

@Composable
fun ListItem(
    text: Int,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(12.dp)
        isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape ?: RoundedCornerShape(0.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun UserCard(username: String, email: String, monogram: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monogram,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(3.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}