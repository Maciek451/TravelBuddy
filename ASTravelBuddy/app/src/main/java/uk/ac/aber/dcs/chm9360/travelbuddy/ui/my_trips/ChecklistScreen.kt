package uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.ChecklistItem
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.AppBarWithArrowBack
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import java.util.UUID

@Composable
fun ChecklistScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel()
) {
    val trip = Utils.trip
    val appBarTitle = stringResource(id = R.string.checklist)
    var checklist by remember { mutableStateOf(trip?.checklist ?: emptyList()) }
    val uncheckedItems = checklist.filter { it.checked == "false" }
    val checkedItems = checklist.filter { it.checked == "true" }

    LaunchedEffect(trip?.id) {
        trip?.id?.let { tripId ->
            firebaseViewModel.fetchChecklist(tripId) { fetchedChecklist ->
                checklist = fetchedChecklist
                Utils.trip = trip.copy(checklist = fetchedChecklist)
            }
        }
    }

    fun addItem() {
        val newItem = ChecklistItem(id = UUID.randomUUID().toString(), task = "", checked = false.toString())
        checklist = uncheckedItems + newItem + checkedItems
    }

    fun updateItem(updatedItem: ChecklistItem) {
        checklist = checklist.map { if (it.id == updatedItem.id) updatedItem else it }
    }

    fun removeItem(item: ChecklistItem) {
        checklist = checklist.filter { it.id != item.id }
    }

    fun saveChecklist() {
        trip?.let {
            val updatedTrip = it.copy(checklist = checklist)
            firebaseViewModel.updateTrip(updatedTrip)
            navController.popBackStack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppBarWithArrowBack(
            navController = navController,
            appBarTitle = appBarTitle,
            showSaveButton = true,
            showMoreIcon = false,
            isSaveButtonEnabled = true,
            onSave = { saveChecklist() }
        )

        Column {
            LazyColumn {
                items(uncheckedItems) { item ->
                    ChecklistItemView(
                        item = item,
                        onItemCheckedChange = { item, isChecked ->
                            val updatedItem = item.copy(checked = isChecked.toString())
                            updateItem(updatedItem)
                            trip?.id?.let { tripId ->
                                firebaseViewModel.updateChecklistItem(tripId, updatedItem)
                            }
                        },
                        onItemTextChange = { item, newText -> updateItem(item.copy(task = newText)) },
                        onItemDelete = { removeItem(it) }
                    )
                }

                item {
                    TextButton(
                        onClick = { addItem() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = stringResource(id = R.string.list_item_button),
                            )
                        }
                    }
                }

                items(checkedItems) { item ->
                    ChecklistItemView(
                        item = item,
                        onItemCheckedChange = { item, isChecked ->
                            val updatedItem = item.copy(checked = isChecked.toString())
                            updateItem(updatedItem)
                            trip?.id?.let { tripId ->
                                firebaseViewModel.updateChecklistItem(tripId, updatedItem)
                            }
                        },
                        onItemTextChange = { item, newText -> updateItem(item.copy(task = newText)) },
                        onItemDelete = { removeItem(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItemView(
    item: ChecklistItem,
    onItemCheckedChange: (ChecklistItem, Boolean) -> Unit,
    onItemTextChange: (ChecklistItem, String) -> Unit,
    onItemDelete: (ChecklistItem) -> Unit
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked == "true",
            onCheckedChange = { isChecked ->
                onItemCheckedChange(item.copy(checked = isChecked.toString()), isChecked)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.error
            )
        )
        OutlinedTextField(
            value = item.task,
            onValueChange = { newText ->
                onItemTextChange(item, newText)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .onFocusChanged { focusState ->
                    isTextFieldFocused = focusState.isFocused
                }
        )
        if (isTextFieldFocused) {
            IconButton(onClick = { onItemDelete(item) }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.close_icon))
            }
        }
    }
}