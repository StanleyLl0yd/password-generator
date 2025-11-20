package com.sl.passwordgenerator.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.sl.passwordgenerator.R
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

private enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PasswordGeneratorUiEvent.Error -> {
                    val message = when (event.reason) {
                        com.sl.passwordgenerator.domain.model.PasswordGenerationError.NO_CHARSETS ->
                            context.getString(R.string.error_no_charsets)

                        com.sl.passwordgenerator.domain.model.PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS ->
                            context.getString(R.string.error_no_enough_unique_chars)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = context.getString(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PasswordGeneratorContent(
            innerPadding = innerPadding,
            state = uiState,
            onPasswordChange = viewModel::onPasswordChanged,
            onLengthChange = viewModel::onLengthChanged,
            onLowercaseChange = viewModel::onLowercaseChanged,
            onUppercaseChange = viewModel::onUppercaseChanged,
            onDigitsChange = viewModel::onDigitsChanged,
            onSymbolsChange = viewModel::onSymbolsChanged,
            onExcludeDuplicatesChange = viewModel::onExcludeDuplicatesChanged,
            onExcludeSimilarChange = viewModel::onExcludeSimilarChanged,
            onGenerateClick = { viewModel.generatePassword() },
            onCopyClick = {
                if (uiState.password.isNotEmpty()) {
                    clipboardManager.setText(AnnotatedString(uiState.password))
                    showToast(context, context.getString(R.string.copied_to_clipboard))
                }
            }
        )
    }
}

@Composable
private fun PasswordGeneratorContent(
    innerPadding: PaddingValues,
    state: PasswordGeneratorUiState,
    onPasswordChange: (String) -> Unit,
    onLengthChange: (Float) -> Unit,
    onLowercaseChange: (Boolean) -> Unit,
    onUppercaseChange: (Boolean) -> Unit,
    onDigitsChange: (Boolean) -> Unit,
    onSymbolsChange: (Boolean) -> Unit,
    onExcludeDuplicatesChange: (Boolean) -> Unit,
    onExcludeSimilarChange: (Boolean) -> Unit,
    onGenerateClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = context.getString(R.string.charsets_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            CharsetCheckboxRow(
                checked = state.useLowercase,
                onCheckedChange = onLowercaseChange,
                text = context.getString(R.string.lowercase_label),
                tooltipText = context.getString(R.string.lowercase_hint)
            )
            CharsetCheckboxRow(
                checked = state.useUppercase,
                onCheckedChange = onUppercaseChange,
                text = context.getString(R.string.uppercase_label),
                tooltipText = context.getString(R.string.uppercase_hint)
            )
            CharsetCheckboxRow(
                checked = state.useDigits,
                onCheckedChange = onDigitsChange,
                text = context.getString(R.string.digits_label),
                tooltipText = context.getString(R.string.digits_hint)
            )
            CharsetCheckboxRow(
                checked = state.useSymbols,
                onCheckedChange = onSymbolsChange,
                text = context.getString(R.string.symbols_label),
                tooltipText = context.getString(R.string.symbols_hint)
            )
            CharsetCheckboxRow(
                checked = state.excludeDuplicates,
                onCheckedChange = onExcludeDuplicatesChange,
                text = context.getString(R.string.exclude_duplicates_label),
                tooltipText = context.getString(R.string.exclude_duplicates_hint)
            )
            CharsetCheckboxRow(
                checked = state.excludeSimilar,
                onCheckedChange = onExcludeSimilarChange,
                text = context.getString(R.string.exclude_similar_label),
                tooltipText = context.getString(R.string.exclude_similar_hint)
            )

            Text(
                text = context.getString(R.string.length_title, state.length.toInt()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LengthSlider(
                        length = state.length,
                        onLengthChange = { newLength ->
                            onLengthChange(
                                newLength.coerceIn(
                                    MIN_LENGTH.toFloat(),
                                    MAX_LENGTH.toFloat()
                                )
                            )
                        }
                    )
                }
            }

            Text(
                text = context.getString(R.string.password_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text(text = context.getString(R.string.password_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        singleLine = false,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCopyClick) {
                            Text(text = context.getString(R.string.copy_button))
                        }
                        Button(onClick = onGenerateClick) {
                            Text(text = context.getString(R.string.generate_button))
                        }
                    }
                }
            }
        }

        StrengthSection(strengthScore = state.strengthScore)
    }
}

@Composable
private fun LengthSlider(
    length: Float,
    onLengthChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = MIN_LENGTH.toString(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = length.toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = MAX_LENGTH.toString(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        Slider(
            value = length,
            onValueChange = { new ->
                val clamped = new
                    .coerceIn(MIN_LENGTH.toFloat(), MAX_LENGTH.toFloat())
                    .roundToInt()
                    .toFloat()
                onLengthChange(clamped)
            },
            valueRange = MIN_LENGTH.toFloat()..MAX_LENGTH.toFloat(),
            steps = MAX_LENGTH - MIN_LENGTH - 1,
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
private fun CharsetCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    tooltipText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        InfoTooltip(tooltipText = tooltipText)
    }
}

@Composable
private fun InfoTooltip(
    tooltipText: String
) {
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val offsetY = with(density) { 6.dp.roundToPx() }

    Box(
        modifier = Modifier.padding(start = 4.dp)
    ) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.heightIn(min = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = tooltipText,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(x = 0, y = offsetY),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp
                ) {
                    Text(
                        text = tooltipText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun StrengthSection(strengthScore: Int) {
    val context = LocalContext.current

    val scoreClamped = strengthScore.coerceIn(0, 100)
    val strength = getPasswordStrength(scoreClamped)

    val strengthLabel = when (strength) {
        PasswordStrength.VERY_WEAK -> context.getString(R.string.strength_very_weak)
        PasswordStrength.WEAK -> context.getString(R.string.strength_weak)
        PasswordStrength.MEDIUM -> context.getString(R.string.strength_medium)
        PasswordStrength.STRONG -> context.getString(R.string.strength_strong)
        PasswordStrength.VERY_STRONG -> context.getString(R.string.strength_very_strong)
    }

    val fraction = scoreClamped / 100f

    val red = Color(0xFFD32F2F)
    val yellow = Color(0xFFFBC02D)
    val green = Color(0xFF388E3C)

    val indicatorColor = when {
        fraction < 0.5f -> lerp(red, yellow, fraction * 2)
        else -> lerp(yellow, green, (fraction - 0.5f) * 2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = context.getString(R.string.strength_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 10.dp),
                color = indicatorColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = indicatorColor
            )
        }
    }
}

private fun getPasswordStrength(score: Int): PasswordStrength = when {
    score < 20 -> PasswordStrength.VERY_WEAK
    score < 40 -> PasswordStrength.WEAK
    score < 60 -> PasswordStrength.MEDIUM
    score < 80 -> PasswordStrength.STRONG
    else -> PasswordStrength.VERY_STRONG
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
