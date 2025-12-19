package com.sl.passwordgenerator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sl.passwordgenerator.domain.PasswordConstants
import kotlin.math.roundToInt

@Composable
fun LengthSliderCard(
    length: Float,
    onLengthChange: (Float) -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            LengthSlider(
                length = length,
                onLengthChange = onLengthChange
            )
        }
    }
}

@Composable
private fun LengthSlider(
    length: Float,
    onLengthChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SliderLabel(
                text = PasswordConstants.MIN_LENGTH.toString(),
                modifier = Modifier.weight(1f),
                alignment = TextAlign.Start
            )
            SliderLabel(
                text = length.toInt().toString(),
                modifier = Modifier.weight(1f),
                alignment = TextAlign.Center
            )
            SliderLabel(
                text = PasswordConstants.MAX_LENGTH.toString(),
                modifier = Modifier.weight(1f),
                alignment = TextAlign.End
            )
        }

        Slider(
            value = length,
            onValueChange = { newValue ->
                val clamped = newValue
                    .coerceIn(
                        PasswordConstants.MIN_LENGTH.toFloat(),
                        PasswordConstants.MAX_LENGTH.toFloat()
                    )
                    .roundToInt()
                    .toFloat()
                onLengthChange(clamped)
            },
            valueRange = PasswordConstants.MIN_LENGTH.toFloat()..PasswordConstants.MAX_LENGTH.toFloat(),
            steps = PasswordConstants.MAX_LENGTH - PasswordConstants.MIN_LENGTH - 1,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                thumbColor = MaterialTheme.colorScheme.surface,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SliderLabel(
    text: String,
    modifier: Modifier = Modifier,
    alignment: TextAlign
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
        textAlign = alignment
    )
}