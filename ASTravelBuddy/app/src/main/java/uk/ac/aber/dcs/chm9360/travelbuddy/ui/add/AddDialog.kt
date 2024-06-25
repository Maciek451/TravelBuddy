package uk.ac.aber.dcs.chm9360.travelbuddy.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R

@Composable
fun AddDialog(
    navController: NavHostController,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AddButton(stringResource(R.string.add_a_trip), Icons.Default.Add, onDismiss)
                    AddButton(stringResource(R.string.add_a_phrase), Icons.Default.Add, onDismiss)
                    AddButton(stringResource(R.string.add_a_friend), Icons.Default.Add, onDismiss)
                }
            },
            confirmButton = {
                // No additional confirm button needed
            },
            dismissButton = {
                // No additional dismiss button needed
            }
        )
    }
}

@Composable
private fun AddButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, contentDescription = stringResource(id = R.string.add_icon))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}