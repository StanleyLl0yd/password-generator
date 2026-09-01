package com.sl.passwordgenerator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sl.passwordgenerator.ui.PasswordGeneratorScreen
import com.sl.passwordgenerator.ui.theme.PasswordGeneratorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasswordGeneratorApp(
                onSensitiveContentVisibilityChanged = ::setSensitiveContentVisible
            )
        }
    }

    private fun setSensitiveContentVisible(visible: Boolean) {
        if (visible) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun PasswordGeneratorApp(
    onSensitiveContentVisibilityChanged: (Boolean) -> Unit = {}
) {
    PasswordGeneratorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PasswordGeneratorScreen(
                onSensitiveContentVisibilityChanged = onSensitiveContentVisibilityChanged
            )
        }
    }
}
