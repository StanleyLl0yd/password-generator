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
        verticalArrangement = Arrangement.spacedBy(10.dp)
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

            FilledTonalButton(
                onClick = onCopyClick,
                enabled = password.isNotEmpty() && !isGenerating
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = copyLabel)
            }
        }

        AnimatedContent(
            targetState = password,
            transitionSpec = {
                if (isGenerating) {
                    (fadeIn(animationSpec = tween(250)) + slideInVertically { it / 3 })
                        .togetherWith(fadeOut(animationSpec = tween(120)) + slideOutVertically { -it / 3 })
                } else {
                    fadeIn(animationSpec = tween(150))
                        .togetherWith(fadeOut(animationSpec = tween(120)))
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
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = displayedPassword,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (animatedPassword.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
