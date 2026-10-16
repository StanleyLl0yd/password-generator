package com.sl.passwordgenerator

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sl.passwordgenerator.ui.components.PasswordField
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PasswordFieldSecurityTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun replacementIsMaskedBeforeItsFirstFrameAndOldAnimationCannotRevealIt() {
        val password = mutableStateOf("test-secret-one")
        rule.setContent {
            PasswordField(password.value, "Password", "Copy", "Show", "Hide", {})
        }
        rule.onNodeWithContentDescription("Show").performClick()
        rule.onNodeWithText("test-secret-one").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(WindowManager.LayoutParams.FLAG_SECURE, secureFlag())
        }
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { password.value = "test-secret-two" }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("test-secret-two").assertDoesNotExist()
        rule.mainClock.advanceTimeBy(500)
        rule.onNodeWithText("test-secret-one").assertDoesNotExist()
        rule.onNodeWithText("•".repeat(password.value.length)).assertIsDisplayed()
        rule.runOnIdle { assertEquals(0, secureFlag()) }
        rule.onNodeWithContentDescription("Show").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("test-secret-two").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(WindowManager.LayoutParams.FLAG_SECURE, secureFlag())
        }
        rule.onNodeWithContentDescription("Hide").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("test-secret-two").assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, secureFlag()) }
    }

    private fun secureFlag(): Int =
        rule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE
}
