package com.sl.passwordgenerator.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (passwordVisible) hidePasswordContentDescription else showPasswordContentDescription
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
