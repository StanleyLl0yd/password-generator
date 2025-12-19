package com.sl.passwordgenerator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StrengthIndicator(
    strengthScore: Int,
    strengthLabel: String,
    title: String,
    modifier: Modifier = Modifier
) {
    val scoreClamped = strengthScore.coerceIn(0, 100)

    val animatedFraction by animateFloatAsState(
        targetValue = scoreClamped / 100f,
        animationSpec = spring(),
        label = "strength_progress"
    )

    val targetColor = getStrengthColor(animatedFraction)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "strength_color"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.bodySmall,
                color = animatedColor,
                fontWeight = FontWeight.Medium
            )
        }

        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 6.dp),
            color = animatedColor,
            trackColor = animatedColor.copy(alpha = 0.2f)
        )
    }
}

private fun getStrengthColor(fraction: Float): Color {
    val red = Color(0xFFD32F2F)
    val yellow = Color(0xFFFBC02D)
    val green = Color(0xFF388E3C)

    return when {
        fraction <= 0.5f -> lerp(red, yellow, fraction / 0.5f)
        else -> lerp(yellow, green, (fraction - 0.5f) / 0.5f)
    }
}