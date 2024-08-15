package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun TermsOfServiceScreen(
    navController: NavHostController
) {
    val appBarTitle = stringResource(id = R.string.terms_of_service)

    Column(modifier = Modifier.fillMaxSize()) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = appBarTitle,
            showMoreIcon = false,
            )

        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.terms_of_service_text),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}