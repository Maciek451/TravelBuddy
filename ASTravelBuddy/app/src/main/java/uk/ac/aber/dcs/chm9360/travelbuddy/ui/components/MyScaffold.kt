package uk.ac.aber.dcs.chm9360.travelbuddy.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun TopLevelScaffold(
    navController: NavHostController,
    appBarTitle: String,
    pageContent: @Composable (innerPadding: PaddingValues) -> Unit = {},
) {

    Scaffold(
        topBar = {
            MainTopAppBar(navController, appBarTitle)
        },
        bottomBar = {
            MyNavigationBar(navController)
        },
        content = { innerPadding ->
            pageContent(innerPadding)
        }
    )
}