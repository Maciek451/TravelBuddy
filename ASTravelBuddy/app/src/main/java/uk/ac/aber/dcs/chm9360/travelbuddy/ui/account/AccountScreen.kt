package uk.ac.aber.dcs.chm9360.travelbuddy.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun AccountScreen(
    navController: NavHostController
) {
    val appBarTitle = R.string.account

    Column {
        AppBarWithArrowBack(navController, appBarTitle = appBarTitle)
    }
}