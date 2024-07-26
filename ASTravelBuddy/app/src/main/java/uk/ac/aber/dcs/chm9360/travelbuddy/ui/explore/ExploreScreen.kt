package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
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
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavHostController
) {
    val appBarTitle = stringResource(R.string.explore)

    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val chipState = remember { mutableStateOf(listOf(false, false)) }

    TopLevelScaffold(navController = navController, appBarTitle = appBarTitle) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
        ) {
            Column {
                SearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = {  },
                    active = isSearching,
                    onActiveChange = {
                        isSearching = !isSearching
                        if (!isSearching) {
                            searchText = ""
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSearching) Icons.Filled.ArrowBack else Icons.Default.Search,
                            contentDescription = stringResource(
                                if (isSearching) R.string.arrow_back else R.string.search_icon
                            ),
                            modifier = Modifier.clickable {
                                if (isSearching) {
                                    isSearching = false
                                    searchText = ""
                                }
                            }
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_icon),
                                modifier = Modifier.clickable {
                                    searchText = ""
                                }
                            )
                        }
                    },
                    placeholder = {
                        Text(text = "Search:")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    content = {  }
                )

                Divider(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.filters),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = chipState.value[0],
                        onClick = { chipState.value = chipState.value.toMutableList().apply { set(0, !this[0]) } },
                        label = { Text(stringResource(R.string.recommended)) },
                        leadingIcon = {
                            if (chipState.value[0]) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = chipState.value[1],
                        onClick = { chipState.value = chipState.value.toMutableList().apply { set(1, !this[1]) } },
                        label = { Text(stringResource(R.string.popular)) },
                        leadingIcon = {
                            if (chipState.value[1]) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}