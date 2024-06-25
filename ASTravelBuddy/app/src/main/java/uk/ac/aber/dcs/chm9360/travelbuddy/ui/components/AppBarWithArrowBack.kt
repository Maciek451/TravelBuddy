package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithArrowBack(
    navController: NavHostController,
    appBarTitle: Int,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(id = appBarTitle))
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription =
                    stringResource(R.string.arrow_back)
                )
            }
        },
    )
}