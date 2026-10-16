package com.sl.passwordgenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.sl.passwordgenerator.util.SecureClipboard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SecureClipboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun cleanupClearsOnlyItsOwnCopyEvenWhenReplacementHasSameText() {
        rule.runOnIdle {
            val context = rule.activity
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            SecureClipboard.copyPassword(context, "clipboard-test-fixture")
            val extras = requireNotNull(clipboard.primaryClipDescription?.extras)
            assertTrue(extras.getBoolean("android.content.extra.IS_SENSITIVE"))
            val token = requireNotNull(extras.getString("com.sl.passwordgenerator.CLEAR_TOKEN"))
            clipboard.setPrimaryClip(ClipData.newPlainText("Other app", "clipboard-test-fixture"))
            SecureClipboard.clearIfStillCurrent(clipboard, token)
            assertTrue(clipboard.primaryClip?.getItemAt(0)?.text == "clipboard-test-fixture")
            SecureClipboard.copyPassword(context, "clipboard-test-fixture")
            val latestToken = requireNotNull(
                clipboard.primaryClipDescription?.extras?.getString("com.sl.passwordgenerator.CLEAR_TOKEN")
            )
            SecureClipboard.clearIfStillCurrent(clipboard, latestToken)
            assertFalse(clipboard.primaryClip?.getItemAt(0)?.text?.isNotEmpty() == true)
        }
    }
}
