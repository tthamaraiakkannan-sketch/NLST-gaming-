package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.NlstGamingTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app_name matches NLST Gaming`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NLST Gaming", appName)
    }

    @Test
    fun `splash opens gaming dashboard and all navigation tabs work`() {
        composeRule.setContent {
            NlstGamingTheme {
                NlstAppRoot()
            }
        }
        // Splash screen shows first, then opens Gaming Dashboard
        composeRule.onNodeWithTag("nlst_splash_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_continue_button").performClick()

        // Gaming Dashboard launches directly with Start Live, Create Room, Join Room
        composeRule.onNodeWithTag("hub_start_live_button").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_create_room_button").assertIsDisplayed()
        composeRule.onNodeWithTag("hub_join_room_button").assertIsDisplayed()

        // Switch language to Tamil & verify async DataStore update
        composeRule.onNodeWithTag("language_toggle_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("லைவ் தொடங்கு").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("லைவ் தொடங்கு").assertIsDisplayed()

        // Navigate through all functional tabs
        composeRule.onNodeWithTag("nav_stream").performClick()
        composeRule.onNodeWithTag("start_screen_capture_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_rooms").performClick()
        composeRule.onNodeWithTag("create_room_submit_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_youtube").performClick()
        composeRule.onNodeWithTag("save_rtmp_config_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.onNodeWithTag("profile_lang_ta").assertIsDisplayed()
    }
}
