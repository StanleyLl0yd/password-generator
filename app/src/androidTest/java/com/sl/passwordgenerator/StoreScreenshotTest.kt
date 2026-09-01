package com.sl.passwordgenerator

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureRuStoreScreenshots() {
        val context = composeRule.activity
        val copyLabel = context.getString(R.string.copy_button)
        val showPasswordLabel = context.getString(R.string.show_password)
        val excludeSimilarLabel = context.getString(R.string.exclude_similar_label)
        val excludeDuplicatesLabel = context.getString(R.string.exclude_duplicates_label)
        val aboutLabel = context.getString(R.string.about_open)

        val screenshotDirectory = File(context.getExternalFilesDir(null), "screenshots")
        screenshotDirectory.deleteRecursively()
        check(screenshotDirectory.mkdirs() || screenshotDirectory.isDirectory)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                composeRule.onNodeWithContentDescription(copyLabel).assertIsEnabled()
            }.isSuccess
        }

        composeRule.onNodeWithContentDescription(showPasswordLabel).performClick()
        saveScreenshot(screenshotDirectory, "01-generator.png")

        composeRule.onNodeWithText(excludeSimilarLabel).performClick()
        composeRule.onNodeWithText(excludeDuplicatesLabel).performClick()
        composeRule.waitForIdle()
        saveScreenshot(screenshotDirectory, "02-options.png")

        composeRule.onNodeWithContentDescription(aboutLabel).performClick()
        composeRule.onNodeWithText(context.getString(R.string.about_author_label)).assertIsDisplayed()
        saveScreenshot(screenshotDirectory, "03-about.png")
    }

    private fun saveScreenshot(directory: File, filename: String) {
        composeRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        FileOutputStream(File(directory, filename)).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}
