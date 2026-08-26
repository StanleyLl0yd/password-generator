package com.sl.passwordgenerator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
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
fun LengthControl(
    length: Float,
    onLengthChange: (Float) -> Unit,
    onLengthChangeFinished: () -> Unit,
    label: String,
    decreaseContentDescription: String,
    increaseContentDescription: String,
    modifier: Modifier = Modifier
) {
    fun setLength(value: Int) {
        val clamped = value.coerceIn(PasswordConstants.MIN_LENGTH, PasswordConstants.MAX_LENGTH)
        onLengthChange(clamped.toFloat())
        onLengthChangeFinished()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                    onClick = { setLength(length.toInt() - 1) },
                    enabled = length > PasswordConstants.MIN_LENGTH
                ) {
                    Icon(Icons.Rounded.Remove, decreaseContentDescription)
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = length.toInt().toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .widthIn(min = 42.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = { setLength(length.toInt() + 1) },
                    enabled = length < PasswordConstants.MAX_LENGTH
                ) {
                    Icon(Icons.Rounded.Add, increaseContentDescription)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PasswordConstants.MIN_LENGTH.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = length,
                    onValueChange = { newValue ->
                        onLengthChange(
                            newValue.coerceIn(
                                PasswordConstants.MIN_LENGTH.toFloat(),
                                PasswordConstants.MAX_LENGTH.toFloat()
                            ).roundToInt().toFloat()
                        )
                    },
                    valueRange = PasswordConstants.MIN_LENGTH.toFloat()..PasswordConstants.MAX_LENGTH.toFloat(),
                    steps = PasswordConstants.MAX_LENGTH - PasswordConstants.MIN_LENGTH - 1,
                    onValueChangeFinished = onLengthChangeFinished,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                Text(
                    text = PasswordConstants.MAX_LENGTH.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(16, 24, 32).forEach { preset ->
                    FilterChip(
                        selected = length.toInt() == preset,
                        onClick = { setLength(preset) },
                        label = {
                            Text(
                                text = preset.toString(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    )
                }
            }
        }
    }
}
