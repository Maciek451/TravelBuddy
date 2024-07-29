package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack

@Composable
fun TripPlanScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {

    Column(modifier = Modifier.fillMaxSize())
    {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = "",
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = true,
            onSave = {}
        )

        Text(text = "This is the Trip plan Screen")
    }
}