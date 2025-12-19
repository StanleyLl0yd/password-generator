package com.sl.passwordgenerator.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = password,
        transitionSpec = {
            if (isGenerating) {
                (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 })
                    .togetherWith(fadeOut(animationSpec = tween(150)) + slideOutVertically { -it / 2 })
            } else {
                fadeIn(animationSpec = tween(150))
                    .togetherWith(fadeOut(animationSpec = tween(150)))
            }
        },
        label = "password_animation"
    ) { animatedPassword ->
        OutlinedTextField(
            value = animatedPassword,
            onValueChange = onPasswordChange,
            label = { Text(text = label) },
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "Скрыть пароль"
                        } else {
                            "Показать пароль"
                        }
                    )
                }
            }
        )
    }
}