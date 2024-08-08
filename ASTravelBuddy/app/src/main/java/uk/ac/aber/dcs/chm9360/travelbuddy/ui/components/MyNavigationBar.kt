package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.add.AddBottomSheet
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun MyNavigationBar(
    navController: NavHostController
) {
    val icons = mapOf(
        Screens.AddSheet to IconGroup(
            filledIcon = Icons.Filled.AddCircle,
            outlineIcon = Icons.Outlined.AddCircle,
            label = stringResource(id = R.string.add)
        ),
        Screens.MyTrips to IconGroup(
            filledIcon = Icons.Filled.CardTravel,
            outlineIcon = Icons.Outlined.CardTravel,
            label = stringResource(id = R.string.my_trips)
        ),
        Screens.Explore to IconGroup(
            filledIcon = Icons.Filled.TravelExplore,
            outlineIcon = Icons.Outlined.TravelExplore,
            label = stringResource(id = R.string.explore)
        ),
        Screens.Friends to IconGroup(
            filledIcon = Icons.Filled.Group,
            outlineIcon = Icons.Outlined.Group,
            label = stringResource(id = R.string.friends)
        )
    )

    var showBottomSheet by remember { mutableStateOf(false) }

    AddBottomSheet(
        navController = navController,
        showSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false }
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        icons.forEach { (screen, iconGroup) ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = iconGroup.label
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) iconGroup.filledIcon else iconGroup.outlineIcon,
                        contentDescription = labelText
                    )
                },
                label = { Text(labelText) },
                selected = isSelected,
                onClick = {
                    if (screen == Screens.AddSheet) {
                        showBottomSheet = true
                    } else {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}