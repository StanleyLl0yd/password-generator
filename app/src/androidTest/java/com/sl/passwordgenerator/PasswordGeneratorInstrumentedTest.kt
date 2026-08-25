package com.sl.passwordgenerator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordGeneratorInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndGeneratesPassword() {
        val context = composeRule.activity
        val generateLabel = context.getString(R.string.generate_button)
        val copyLabel = context.getString(R.string.copy_button)

        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeRule.onNodeWithText(copyLabel).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithText(copyLabel).assertIsEnabled()
            }.isSuccess
        }
        composeRule.onNodeWithText(generateLabel).assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(generateLabel).assertIsDisplayed()
    }
}
