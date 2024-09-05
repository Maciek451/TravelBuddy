package uk.ac.aber.dcs.chm9360.travelbuddy

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
class MainTopAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupMainTopAppBar() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BuildNavigationGraph(navController, Screens.MyTrips.route)
        }
    }

    @Test
    fun notificationIcon_isDisplayed() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.notification_button)).assertIsDisplayed()
    }

    @Test
    fun accountIcon_isDisplayed() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.account_button))
            .assertIsDisplayed()
    }

    @Test
    fun clickingNotificationIcon_navigatesToNotificationScreen() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.notification_button))
            .performClick()
        assert(navController.currentDestination?.route == Screens.Notification.route)
    }

    @Test
    fun clickingAccountIcon_navigatesToAccountScreen() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.account_button))
            .performClick()
        assert(navController.currentDestination?.route == Screens.Account.route)
    }
}