package uk.ac.aber.dcs.chm9360.travelbuddy.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoLuggage
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.R
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Phrase
import uk.ac.aber.dcs.chm9360.travelbuddy.model.User
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.FirebaseViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.RetrofitViewModel
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.components.TopLevelScaffold
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.my_trips.TripCard
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.phrase.ConfirmPhraseRemovalDialog
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Utils.phrase

@Composable
fun SocialScreen(
    navController: NavHostController,
    firebaseViewModel: FirebaseViewModel = viewModel(),
    retrofitViewModel: RetrofitViewModel = viewModel()
) {
    val appBarTitle = stringResource(id = R.string.social)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val phrases by firebaseViewModel.phrases.collectAsState()
    val trips by firebaseViewModel.trips.collectAsState()
    val isRefreshing by firebaseViewModel.isRefreshing.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    val coroutineScope = rememberCoroutineScope()
    val imageUrls by retrofitViewModel.imageUrls.collectAsState()
    val imageLoadingStates by retrofitViewModel.imageLoadingStates.collectAsState()
    val friendRequests by firebaseViewModel.friendRequests.collectAsState()
    var showConfirmRemovalDialog by remember { mutableStateOf(false) }
    val authState = firebaseViewModel.authState.collectAsState().value
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(authState?.uid) {
        authState?.uid?.let { userId ->
            firebaseViewModel.getUserData(userId) { user ->
                currentUser = user
            }
        }
    }

    LaunchedEffect(phrases) {
        firebaseViewModel.fetchPhrases()
    }

    LaunchedEffect(trips) {
        trips.forEach { trip ->
            retrofitViewModel.fetchImage(trip.destination)
        }
    }

    TopLevelScaffold(
        navController = navController,
        appBarTitle = appBarTitle,
        friendRequestCount = friendRequests.size
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxSize()
                    .testTag("Social"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    Tab(
                        text = { Text(stringResource(id = R.string.trips)) },
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    )
                    Tab(
                        text = { Text(stringResource(id = R.string.phrases)) },
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    )
                }
                when (selectedTabIndex) {
                    0 -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = {
                                coroutineScope.launch {
                                    firebaseViewModel.fetchTrips()
                                }
                            }
                        ) {
                            val sharedTrips = trips.filter { it.shared }

                            if (sharedTrips.isNotEmpty()) {
                                LazyColumn {
                                    items(sharedTrips) { trip ->
                                        TripCard(
                                            trip = trip,
                                            imageUrl = imageUrls[trip.destination],
                                            isLoading = imageLoadingStates[trip.destination] ?: false,
                                            onItemClick = {
                                                Utils.trip = trip
                                                navController.navigate(Screens.TripDetails.route)
                                            },
                                            onShareClick = {},
                                            isShareButtonEnabled = false
                                        )
                                    }
                                }
                            } else {
                                EmptyTripsScreen(firebaseViewModel = firebaseViewModel)
                            }
                        }
                    }

                    1 -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = {
                                coroutineScope.launch {
                                    firebaseViewModel.fetchPhrases()
                                }
                            }
                        ) {
                            if (phrases.isNotEmpty()) {
                                LazyColumn {
                                    items(phrases) { phrase ->
                                        val isUserAuthor = currentUser?.username == phrase.username
                                        PhraseCard(
                                            phrase = phrase,
                                            onEditClick = {
                                                Utils.phrase = phrase
                                                navController.navigate(Screens.EditPhrase.route)
                                            },
                                            onRemoveClick = {
                                                Utils.phrase = phrase
                                                showConfirmRemovalDialog = true
                                            },
                                            isMoreIconEnabled = isUserAuthor
                                        )
                                    }
                                }
                            } else {
                                EmptyPhrasesScreen(firebaseViewModel = firebaseViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showConfirmRemovalDialog) {
        ConfirmPhraseRemovalDialog(
            showDialog = showConfirmRemovalDialog,
            onDismiss = { showConfirmRemovalDialog = false },
            onRemoveConfirmed = {
                phrase?.let { phraseToRemove ->
                    firebaseViewModel.removePhrase(phraseToRemove) { isSuccess ->
                        if (isSuccess) {
                            showConfirmRemovalDialog = false
                        }
                    }
                }
                firebaseViewModel.fetchPhrases()
            }
        )
    }
}

@Composable
fun PhraseCard(
    phrase: Phrase,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit,
    isMoreIconEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phrase.username.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = phrase.username,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isMoreIconEnabled) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.more_icon)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.edit_phrase)) },
                                onClick = {
                                    expanded = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.remove_phrase)) },
                                onClick = {
                                    expanded = false
                                    onRemoveClick()
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = phrase.language,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "\"${phrase.phrase}\"",
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier.padding(bottom = 15.dp),
                text = "(${phrase.translation})",
                fontSize = 20.sp,
            )
        }
    }
}

//            Spacer(modifier = Modifier.height(20.dp))
//            IconButton(onClick = { }) {
//                Icon(
//                    imageVector = Icons.Default.FavoriteBorder,
//                    contentDescription = stringResource(R.string.like)
//                )
//            }

@Composable
fun EmptyTripsScreen(firebaseViewModel: FirebaseViewModel = viewModel()) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            coroutineScope.launch {
                firebaseViewModel.fetchTrips()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(100.dp)
                    .alpha(0.3f),
                imageVector = Icons.Default.NoLuggage,
                contentDescription = stringResource(id = R.string.empty_trips_screen_icon)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.alpha(0.3f),
                text = stringResource(id = R.string.no_trips_text),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyPhrasesScreen(firebaseViewModel: FirebaseViewModel = viewModel()) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            coroutineScope.launch {
                firebaseViewModel.fetchPhrases()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(100.dp)
                    .alpha(0.3f),
                imageVector = Icons.Default.SpeakerNotesOff,
                contentDescription = stringResource(id = R.string.empty_phrases_screen_icon)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.alpha(0.3f),
                text = stringResource(id = R.string.no_phrases_text),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}