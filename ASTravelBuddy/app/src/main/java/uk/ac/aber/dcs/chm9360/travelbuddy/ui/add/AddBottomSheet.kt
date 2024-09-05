package uk.ac.aber.dcs.chm9360.travelbuddy.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    navController: NavHostController,
    showSheet: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    AddButton(R.string.add_trip_bottom_sheet, Icons.Default.Luggage, testTag = "AddTrip") {
                        navController.navigate(Screens.AddTrip.route)
                        onDismiss()
                    }
                    AddButton(R.string.add_phrase_bottom_sheet, Icons.Default.FormatQuote, testTag = "AddPhrase") {
                        navController.navigate(Screens.AddPhrase.route)
                        onDismiss()
                    }
                    AddButton(R.string.add_friend_bottom_sheet, Icons.Default.PersonAdd, testTag = "AddFriend") {
                        navController.navigate(Screens.AddFriend.route)
                        onDismiss()
                    }
                }
            }
        }
        LaunchedEffect(sheetState) {
            scope.launch {
                sheetState.show()
            }
        }
    }
}

@Composable
private fun AddButton(
    text: Int,
    icon: ImageVector,
    testTag: String = "",
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(80.dp).testTag(testTag),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(icon, contentDescription = stringResource(id = R.string.add_icon), modifier = Modifier.size(50.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}