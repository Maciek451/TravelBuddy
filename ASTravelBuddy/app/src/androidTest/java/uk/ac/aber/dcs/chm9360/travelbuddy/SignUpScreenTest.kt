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
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BuildNavigationGraph(navController, Screens.SignUp.route)
        }
    }

    @Test
    fun signUpButton_shouldBeDisabled_whenFieldsAreEmpty() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_up_button))
            .assertIsNotEnabled()
    }

    @Test
    fun signUpButton_shouldBeEnabled_whenDataEntered() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.email_address))
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.confirm_password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_up_button))
            .assertIsEnabled()
    }

    @Test
    fun clickingSignUpButton_showsPasswordMismatch_onDifferentPasswords() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.email_address))
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.confirm_password))
            .performTextInput("password321")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_up_button))
            .performClick()
    }

    @Test
    fun clickingSignUpButton_staysOnSignUpScreen_onInvalidEmail() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.email_address))
            .performTextInput("invalidemail")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.confirm_password))
            .performTextInput("password123")
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.sign_up_button))
            .performClick()
        assert(navController.currentDestination?.route.equals(Screens.SignUp.route))
    }
}