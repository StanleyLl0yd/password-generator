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
fun LengthSliderCard(
    length: Float,
    onLengthChange: (Float) -> Unit,
    onLengthChangeFinished: () -> Unit,
    title: String,
    decreaseContentDescription: String,
    increaseContentDescription: String,
    modifier: Modifier = Modifier
) {
    fun setLength(value: Int) {
        val clamped = value.coerceIn(PasswordConstants.MIN_LENGTH, PasswordConstants.MAX_LENGTH)
        onLengthChange(clamped.toFloat())
        onLengthChangeFinished()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { setLength(length.toInt() - 1) },
                        enabled = length > PasswordConstants.MIN_LENGTH
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = decreaseContentDescription
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = length.toInt().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(min = 48.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = { setLength(length.toInt() + 1) },
                        enabled = length < PasswordConstants.MAX_LENGTH
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = increaseContentDescription
                        )
                    }
                }
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
                onValueChangeFinished = onLengthChangeFinished,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = PasswordConstants.MIN_LENGTH.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = PasswordConstants.MAX_LENGTH.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
