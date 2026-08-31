package com.sl.passwordgenerator

import android.app.LocaleManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.LocaleList
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
class RuStoreScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureRuStorePhoneScreenshots() {
        forceRussianLocale()

        val context = composeRule.activity
        val copyLabel = context.getString(R.string.copy_button)
        val showPasswordLabel = context.getString(R.string.show_password)
        val aboutLabel = context.getString(R.string.about_open)
        val generateLabel = context.getString(R.string.generate_button)

        waitForGeneratedPassword(copyLabel)

        composeRule.onNodeWithContentDescription(showPasswordLabel).performClick()
        composeRule.waitForIdle()
        saveScreenshot("01-generator.png")

        composeRule.onNodeWithText("32").performClick()
        composeRule.onNodeWithText(context.getString(R.string.symbols_compact)).performClick()
        composeRule.onNodeWithText(generateLabel).performClick()
        waitForGeneratedPassword(copyLabel)
        composeRule.waitForIdle()
        saveScreenshot("02-custom-options.png")

        composeRule.onNodeWithContentDescription(aboutLabel).performClick()
        composeRule.waitForIdle()
        saveScreenshot("03-about.png")
    }

    private fun forceRussianLocale() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        composeRule.runOnUiThread {
            composeRule.activity
                .getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags("ru-RU")
        }
        composeRule.waitForIdle()
    }

    private fun waitForGeneratedPassword(copyLabel: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithContentDescription(copyLabel).assertIsEnabled()
            }.isSuccess
        }
    }

    private fun saveScreenshot(fileName: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val outputDir = File(targetContext.getExternalFilesDir(null), "rustore")
        check(outputDir.exists() || outputDir.mkdirs())
        val outputFile = File(outputDir, fileName)

        composeRule.runOnUiThread {
            val view = composeRule.activity.window.decorView
            check(view.width > 0 && view.height > 0)

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            FileOutputStream(outputFile).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            }
            bitmap.recycle()
        }
    }
}
