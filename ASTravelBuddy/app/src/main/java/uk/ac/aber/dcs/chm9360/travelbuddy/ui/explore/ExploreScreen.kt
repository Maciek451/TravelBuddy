package uk.ac.aber.dcs.chm9360.travelbuddy.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    onSearch = { searchText = it },
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                }
            }
        }
    }
}