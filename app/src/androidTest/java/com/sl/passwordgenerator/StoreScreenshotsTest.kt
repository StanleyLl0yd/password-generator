package com.sl.passwordgenerator

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StoreScreenshotsTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureMaskedCurrentApplication() {
        if (Build.VERSION.SDK_INT < 36) return
        val context = rule.activity
        rule.waitUntil(5_000) {
            runCatching {
                rule.onNodeWithContentDescription(context.getString(R.string.copy_button)).assertIsEnabled()
            }.isSuccess
        }
        rule.onNodeWithContentDescription(context.getString(R.string.show_password)).assertIsDisplayed()
        capture("01-generator.png")
        rule.onNodeWithContentDescription(context.getString(R.string.about_open)).performClick()
        rule.waitUntil(5_000) {
            runCatching {
                rule.onNodeWithText(context.getString(R.string.about_description)).assertIsDisplayed()
            }.isSuccess
        }
        capture("02-about.png")
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.waitForIdle(500, 5_000)
        val bitmap = requireNotNull(automation.takeScreenshot())
        val directory = File(rule.activity.getExternalFilesDir(null), "store-screenshots")
        assertTrue(directory.isDirectory || directory.mkdirs())
        File(directory, name).outputStream().use {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        bitmap.recycle()
    }
}
