package uk.ac.aber.dcs.chm9360.travelbuddy

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.ac.aber.dcs.chm9360.travelbuddy.ui.navigation.Screens

@RunWith(AndroidJUnit4::class)
@LargeTest
class MyNavigationBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupMyNavigationBar() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BuildNavigationGraph(navController, Screens.MyTrips.route)
        }
    }

    @Test
    fun navigationBarItems_areDisplayed() {
        composeTestRule.onNodeWithTag(composeTestRule.activity.getString(R.string.add)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(composeTestRule.activity.getString(R.string.my_trips)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(composeTestRule.activity.getString(R.string.explore)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(composeTestRule.activity.getString(R.string.social)).assertIsDisplayed()
    }

    @Test
    fun clickingAddSheetButton_showsBottomSheet() {
        composeTestRule.onNodeWithTag(composeTestRule.activity.getString(R.string.add)).performClick()
        composeTestRule.onNodeWithTag("AddTrip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AddPhrase").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AddFriend").assertIsDisplayed()
    }

    @Test
    fun clickingMyTrips_navigatesToMyTripsScreen() {
        composeTestRule.onNodeWithTag("MyTrips").performClick()
        assert(navController.currentDestination?.route.equals(Screens.MyTrips.route))
    }

    @Test
    fun clickingExplore_navigatesToExploreScreen() {
        composeTestRule.onNodeWithTag("Explore").performClick()
        assert(navController.currentDestination?.route.equals(Screens.Explore.route))
    }

    @Test
    fun clickingSocial_navigatesToSocialScreen() {
        composeTestRule.onNodeWithTag("Social").performClick()
        assert(navController.currentDestination?.route.equals(Screens.Social.route))
    }
}
