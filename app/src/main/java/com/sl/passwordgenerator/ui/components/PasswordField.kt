package com.sl.passwordgenerator.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordField(
    password: String,
    label: String,
    copyLabel: String,
    showPasswordContentDescription: String,
    hidePasswordContentDescription: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val window = LocalContext.current.findActivity()?.window

    DisposableEffect(window, passwordVisible) {
        window.setSensitiveContentProtection(passwordVisible)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    LaunchedEffect(password) {
        passwordVisible = false
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                enabled = password.isNotEmpty()
            ) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = if (passwordVisible) {
                        hidePasswordContentDescription
                    } else {
                        showPasswordContentDescription
                    }
                )
            }

            IconButton(
                onClick = onCopyClick,
                enabled = password.isNotEmpty() && !isGenerating
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = copyLabel,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedContent(
            targetState = password,
            transitionSpec = {
                if (isGenerating) {
                    (fadeIn(animationSpec = tween(200)) + slideInVertically { it / 4 })
                        .togetherWith(fadeOut(animationSpec = tween(100)) + slideOutVertically { -it / 4 })
                } else {
                    fadeIn(animationSpec = tween(120)).togetherWith(fadeOut(animationSpec = tween(100)))
                }
            },
            label = "password_animation"
        ) { animatedPassword ->
            val displayedPassword = when {
                animatedPassword.isEmpty() -> "—"
                passwordVisible -> animatedPassword
                else -> "•".repeat(animatedPassword.length)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = displayedPassword,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = if (passwordVisible) 16.sp else 15.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (animatedPassword.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = if (passwordVisible) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun Window?.setSensitiveContentProtection(enabled: Boolean) {
    if (enabled) {
        this?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        this?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
