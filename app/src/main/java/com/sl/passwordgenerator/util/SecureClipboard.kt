package com.sl.passwordgenerator.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import com.sl.passwordgenerator.R
import java.util.UUID

object SecureClipboard {
    const val AUTO_CLEAR_DELAY_MS = 60_000L

    private const val SENSITIVE_KEY = "android.content.extra.IS_SENSITIVE"
    private const val CLEAR_TOKEN_KEY = "com.sl.passwordgenerator.CLEAR_TOKEN"

    private val handler = Handler(Looper.getMainLooper())
    private var pendingClear: Runnable? = null

    fun copyPassword(context: Context, password: String) {
        if (password.isEmpty()) return

        val appContext = context.applicationContext
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clearToken = UUID.randomUUID().toString()
        val clip = ClipData.newPlainText(appContext.getString(R.string.password_label), password)

        clip.description.extras = PersistableBundle().apply {
            putBoolean(SENSITIVE_KEY, true)
            putString(CLEAR_TOKEN_KEY, clearToken)
        }
        clipboard.primaryClip = clip

        pendingClear?.let(handler::removeCallbacks)
        pendingClear = Runnable {
            val currentToken = clipboard.primaryClipDescription
                ?.extras
                ?.getString(CLEAR_TOKEN_KEY)

            if (currentToken == clearToken) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    clipboard.primaryClip = ClipData.newPlainText("", "")
                }
            }
            pendingClear = null
        }.also { handler.postDelayed(it, AUTO_CLEAR_DELAY_MS) }
    }
}
