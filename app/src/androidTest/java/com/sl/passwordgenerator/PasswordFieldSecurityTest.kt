package com.sl.passwordgenerator

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sl.passwordgenerator.ui.components.PasswordField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class PasswordFieldSecurityTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun replacementIsMaskedBeforeItsFirstFrameAndOldAnimationCannotRevealIt() {
        val password = mutableStateOf("test-secret-one")
        val generationId = mutableStateOf(0L)
        rule.setContent {
            PasswordField(password.value, "Password", "Copy", "Show", "Hide", {}, generationId = generationId.value)
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
        rule.runOnIdle { generationId.value += 1 }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("test-secret-two").assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, secureFlag()) }
        rule.onNodeWithContentDescription("Show").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("Hide").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("test-secret-two").assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, secureFlag()) }
    }

    @Test
    fun maximumLengthPasswordIsFullyReadableInNarrowLayout() {
        val password = "Ax7!kM3@pR9#vN6%".repeat(4)
        rule.setContent {
            PasswordField(password, "Password", "Copy", "Show", "Hide", {}, Modifier.width(200.dp))
        }
        rule.onNodeWithContentDescription("Show").performClick()
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(password).performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
            it(layouts)
        }
        assertEquals(1, layouts.size)
        assertFalse(layouts.single().hasVisualOverflow)
    }

    private fun secureFlag(): Int =
        rule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE
}
