package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold

@Composable
fun FriendsScreen(
    navController: NavHostController
) {
    val appBarTitle = stringResource(R.string.friends)

    TopLevelScaffold(navController = navController, appBarTitle = appBarTitle)
}