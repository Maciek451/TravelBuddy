package uk.ac.aber.dcs.chm9360.travelbuddy.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold

@Composable
fun MapScreen(
    navController: NavHostController
) {
    val appBarTitle = stringResource(R.string.map)

    TopLevelScaffold(navController = navController, appBarTitle = appBarTitle)
}