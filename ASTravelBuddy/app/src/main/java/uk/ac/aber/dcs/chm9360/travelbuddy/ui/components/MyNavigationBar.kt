package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Map
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.add.AddDialog
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@Composable
fun MyNavigationBar(
    navController: NavHostController
) {
    val icons = mapOf(
        Screens.MyTrips to IconGroup(
            filledIcon = Icons.Filled.CardTravel,
            outlineIcon = Icons.Outlined.CardTravel,
            label = stringResource(id = R.string.my_trips)
        ),
        Screens.Map to IconGroup(
            filledIcon = Icons.Filled.Map,
            outlineIcon = Icons.Outlined.Map,
            label = stringResource(id = R.string.map)
        ),
        Screens.Add to IconGroup(
            filledIcon = Icons.Filled.AddCircle,
            outlineIcon = Icons.Outlined.AddCircle,
            label = ""
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

    var showDialog by remember { mutableStateOf(false) }

    AddDialog(
        navController = navController,
        showDialog = showDialog,
        onDismiss = { showDialog = false }
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        icons.forEach { (screen, iconGroup) ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = iconGroup.label
            NavigationBarItem(
                icon = {
                    if (screen == Screens.Add) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                                contentDescription = labelText,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) iconGroup.filledIcon else iconGroup.outlineIcon,
                            contentDescription = labelText
                        )
                    }
                },
                label = { if (screen != Screens.Add) Text(labelText) },
                selected = isSelected,
                onClick = {
                    if (screen == Screens.Add) {
                        showDialog = true
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