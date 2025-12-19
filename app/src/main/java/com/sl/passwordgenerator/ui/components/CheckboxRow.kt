package com.sl.passwordgenerator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    tooltipText: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 36.dp else 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(if (compact) 36.dp else 40.dp)
            )

            Text(
                text = text,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (compact) 2.dp else 4.dp),
                maxLines = if (compact) 3 else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (compact) {
                    MaterialTheme.typography.bodySmall.lineHeight
                } else {
                    MaterialTheme.typography.bodyMedium.lineHeight
                }
            )

            IconButton(
                onClick = { showTooltip = !showTooltip },
                modifier = Modifier.size(if (compact) 36.dp else 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = tooltipText,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                )
            }
        }

        if (showTooltip) {
            TooltipPopup(
                text = tooltipText,
                maxWidth = if (compact) 280.dp else 320.dp,
                onDismiss = { showTooltip = false }
            )
        }
    }
}

@Composable
private fun TooltipPopup(
    text: String,
    maxWidth: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            shadowElevation = 8.dp,
            tonalElevation = 8.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}