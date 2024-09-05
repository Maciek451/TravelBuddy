package uk.ac.aber.dcs.chm9360.travelbuddy

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class SignInScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BuildNavigationGraph(navController, Screens.SignIn.route)
        }
    }

    @Test
    fun signInButton_shouldBeDisabled_whenFieldsAreEmpty() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_in_button))
            .assertIsNotEnabled()
    }

    @Test
    fun signInButton_shouldBeEnabled_whenValidDataEntered() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.email_address))
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_in_button))
            .assertIsEnabled()
    }

    @Test
    fun clickingSignInButton_staysOnSignInScreen_onInvalidCredentials() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.email_address))
            .performTextInput("invalid@example.com")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.password))
            .performTextInput("wrongpassword")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_in_button))
            .performClick()
        assert(navController.currentDestination?.route.equals(Screens.SignIn.route))
    }
}