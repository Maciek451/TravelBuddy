package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun AddFriendScreen(
    navController: NavHostController
) {
    val title = R.string.add_a_friend
    val nextDestination = Screens.Account.route

    Column() {
        AppBarWithArrowBack(navController, appBarTitle = title, showSaveButton = true, showMoreIcon = false, navDestination = nextDestination)
    }
}