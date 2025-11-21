package com.sl.passwordgenerator.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import android.content.ClipData
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sl.passwordgenerator.R
import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

private data class PasswordGeneratorActions(
    val onPasswordChange: (String) -> Unit,
    val onLengthChange: (Float) -> Unit,
    val onLowercaseChange: (Boolean) -> Unit,
    val onUppercaseChange: (Boolean) -> Unit,
    val onDigitsChange: (Boolean) -> Unit,
    val onSymbolsChange: (Boolean) -> Unit,
    val onExcludeDuplicatesChange: (Boolean) -> Unit,
    val onExcludeSimilarChange: (Boolean) -> Unit
)

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PasswordGeneratorUiEvent.Error -> {
                    val message = when (event.reason) {
                        PasswordGenerationError.NO_CHARSETS ->
                            context.getString(R.string.error_no_charsets)
                        PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS ->
                            context.getString(R.string.error_no_enough_unique_chars)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    val actions = remember(viewModel) {
        PasswordGeneratorActions(
            onPasswordChange = viewModel::onPasswordChanged,
            onLengthChange = viewModel::onLengthChanged,
            onLowercaseChange = viewModel::onLowercaseChanged,
            onUppercaseChange = viewModel::onUppercaseChanged,
            onDigitsChange = viewModel::onDigitsChanged,
            onSymbolsChange = viewModel::onSymbolsChanged,
            onExcludeDuplicatesChange = viewModel::onExcludeDuplicatesChanged,
            onExcludeSimilarChange = viewModel::onExcludeSimilarChanged
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
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
            actions = actions,
            isGenerating = isGenerating,
            onGenerateClick = {
                isGenerating = true
                performHapticFeedback(context)
                scope.launch {
                    delay(100)
                    viewModel.generatePassword()
                    delay(200)
                    isGenerating = false
                }
            },
            onCopyClick = {
                val password = uiState.password
                if (password.isNotEmpty()) {
                    scope.launch {
                        val clipData = ClipData.newPlainText(
                            context.getString(R.string.password_label),
                            password
                        )
                        clipboard.setClipEntry(clipData.toClipEntry())
                    }
                    performHapticFeedback(context, HapticType.LIGHT)
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
    actions: PasswordGeneratorActions,
    isGenerating: Boolean,
    onGenerateClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = context.getString(R.string.charsets_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        CharsetCheckboxRow(
            checked = state.useLowercase,
            onCheckedChange = actions.onLowercaseChange,
            text = context.getString(R.string.lowercase_label),
            tooltipText = context.getString(R.string.lowercase_hint)
        )
        CharsetCheckboxRow(
            checked = state.useUppercase,
            onCheckedChange = actions.onUppercaseChange,
            text = context.getString(R.string.uppercase_label),
            tooltipText = context.getString(R.string.uppercase_hint)
        )
        CharsetCheckboxRow(
            checked = state.useDigits,
            onCheckedChange = actions.onDigitsChange,
            text = context.getString(R.string.digits_label),
            tooltipText = context.getString(R.string.digits_hint)
        )
        CharsetCheckboxRow(
            checked = state.useSymbols,
            onCheckedChange = actions.onSymbolsChange,
            text = context.getString(R.string.symbols_label),
            tooltipText = context.getString(R.string.symbols_hint)
        )
        CharsetCheckboxRow(
            checked = state.excludeDuplicates,
            onCheckedChange = actions.onExcludeDuplicatesChange,
            text = context.getString(R.string.exclude_duplicates_label),
            tooltipText = context.getString(R.string.exclude_duplicates_hint)
        )
        CharsetCheckboxRow(
            checked = state.excludeSimilar,
            onCheckedChange = actions.onExcludeSimilarChange,
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
                    onLengthChange = actions.onLengthChange
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
                AnimatedContent(
                    targetState = state.password,
                    transitionSpec = {
                        if (isGenerating) {
                            (fadeIn(animationSpec = tween(300)) +
                                    slideInVertically { it / 2 })
                                .togetherWith(
                                    fadeOut(animationSpec = tween(150)) +
                                            slideOutVertically { -it / 2 }
                                )
                        } else {
                            fadeIn(animationSpec = tween(150))
                                .togetherWith(fadeOut(animationSpec = tween(150)))
                        }
                    },
                    label = "password_animation"
                ) { animatedPassword ->
                    OutlinedTextField(
                        value = animatedPassword,
                        onValueChange = actions.onPasswordChange,
                        label = { Text(text = context.getString(R.string.password_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
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
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    }
                                )
                            }
                        }
                    )
                }

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

                    val buttonScale by animateFloatAsState(
                        targetValue = if (isGenerating) 0.95f else 1f,
                        animationSpec = spring(),
                        label = "button_scale"
                    )

                    Button(
                        onClick = onGenerateClick,
                        enabled = !isGenerating,
                        modifier = Modifier.scale(buttonScale)
                    ) {
                        Text(text = context.getString(R.string.generate_button))
                    }
                }
            }
        }

        AnimatedStrengthSection(strengthScore = state.strengthScore)
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
                text = PasswordConstants.MIN_LENGTH.toString(),
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
                text = PasswordConstants.MAX_LENGTH.toString(),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        Slider(
            value = length,
            onValueChange = { new ->
                val clamped = new
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
private fun CharsetCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    tooltipText: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
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
        }

        if (expanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = tooltipText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedStrengthSection(strengthScore: Int) {
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

    val animatedFraction by animateFloatAsState(
        targetValue = scoreClamped / 100f,
        animationSpec = spring(),
        label = "strength_progress"
    )

    val red = Color(0xFFD32F2F)
    val yellow = Color(0xFFFBC02D)
    val green = Color(0xFF388E3C)

    val targetColor = when {
        animatedFraction <= 0.5f -> lerp(red, yellow, animatedFraction / 0.5f)
        else -> lerp(yellow, green, (animatedFraction - 0.5f) / 0.5f)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "strength_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = context.getString(R.string.strength_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 6.dp),
            color = animatedColor,
            trackColor = animatedColor.copy(alpha = 0.2f)
        )

        Text(
            text = strengthLabel,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            color = animatedColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun getPasswordStrength(score: Int): PasswordStrength = when {
    score < 20 -> PasswordStrength.VERY_WEAK
    score < 40 -> PasswordStrength.WEAK
    score < 60 -> PasswordStrength.MEDIUM
    score < 80 -> PasswordStrength.STRONG
    else -> PasswordStrength.VERY_STRONG
}

private enum class HapticType {
    LIGHT,
    MEDIUM
}

@Suppress("DEPRECATION")
private fun performHapticFeedback(
    context: Context,
    type: HapticType = HapticType.MEDIUM
) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                it.vibrate(effect)
            } else {
                val duration = when (type) {
                    HapticType.LIGHT -> 20L
                    HapticType.MEDIUM -> 40L
                }
                it.vibrate(duration)
            }
        }
    } catch (e: Exception) {
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}