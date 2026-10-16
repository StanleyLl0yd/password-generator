package com.sl.passwordgenerator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val generator = capture("01-generator.png", rule.onRoot())
        rule.onNodeWithContentDescription(context.getString(R.string.about_open)).performClick()
        rule.waitUntil(5_000) {
            runCatching {
                rule.onNodeWithText(context.getString(R.string.about_description)).assertIsDisplayed()
            }.isSuccess
        }
        val about = capture(
            "02-about.png",
            rule.onNode(
                isDialog() and hasAnyDescendant(hasText(context.getString(R.string.about_description))),
                useUnmergedTree = true
            ),
            background = generator
        )
        assertFalse(
            "Distinct screens must have distinct captures",
            generator.sameAs(about)
        )
        generator.recycle()
        about.recycle()
    }

    private fun capture(
        name: String,
        node: SemanticsNodeInteraction,
        background: Bitmap? = null
    ): Bitmap {
        val foreground = node.assertIsDisplayed().captureToImage().asAndroidBitmap()
        val bitmap = if (background == null) {
            foreground
        } else {
            assertEquals(background.width, foreground.width)
            assertEquals(background.height, foreground.height)
            background.copy(Bitmap.Config.ARGB_8888, true).also {
                Canvas(it).drawBitmap(foreground, 0f, 0f, null)
                foreground.recycle()
            }
        }
        bitmap.setHasAlpha(false)
        val directory = File(rule.activity.getExternalFilesDir(null), "store-screenshots")
        assertTrue(directory.isDirectory || directory.mkdirs())
        File(directory, name).outputStream().use {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        return bitmap
    }
}
