package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun TopLevelScaffold(
    navController: NavHostController,
    appBarTitle: String,
    showLocationButton: Boolean = false,
    onLocationButtonClick: (() -> Unit)? = null,
    onSearchButtonClick: (() -> Unit)? = null,
    pageContent: @Composable (innerPadding: PaddingValues) -> Unit = {}
) {
    Scaffold(
        topBar = {
            MainTopAppBar(
                navController = navController,
                appBarTitle = appBarTitle,
                showLocationButton = showLocationButton,
                onLocationButtonClick = onLocationButtonClick,
                onSearchButtonClick = onSearchButtonClick
            )
        },
        bottomBar = {
            MyNavigationBar(navController)
        },
        content = { innerPadding ->
            pageContent(innerPadding)
        }
    )
}