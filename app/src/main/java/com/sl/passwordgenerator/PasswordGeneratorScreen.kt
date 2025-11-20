package com.sl.passwordgenerator

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.math.ln
import kotlin.math.roundToInt

private const val MIN_LENGTH = 4
private const val MAX_LENGTH = 64
private const val FULL_CHARSPACE = 95.0
private const val REF_LENGTH_FOR_MAX_SCORE = 20.0
private const val SIMILAR_CHARS = "iIl1oO0"

private val secureRandom = SecureRandom()

private data class PasswordGeneratorUiState(
    val password: String = "",
    val length: Float = 16f,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true
)

private data class PasswordGenerationConfig(
    val length: Int,
    val useLowercase: Boolean,
    val useUppercase: Boolean,
    val useDigits: Boolean,
    val useSymbols: Boolean,
    val excludeSimilar: Boolean,
    val excludeDuplicates: Boolean
)

@Composable
fun PasswordGeneratorScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    var uiState by remember { mutableStateOf(PasswordGeneratorUiState()) }
    var isInitialized by remember { mutableStateOf(false) }

    val strengthScore = remember(uiState.password) { estimatePasswordScore(uiState.password) }

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
            strengthScore = strengthScore,
            onStateChange = { uiState = it },
            onGenerateClick = {
                val config = uiState.toGenerationConfig()
                val result = generatePasswordWithValidation(
                    config = config,
                    context = context,
                    onError = { errorMessage ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message = errorMessage)
                        }
                    }
                )
                if (result != null) {
                    uiState = uiState.copy(password = result)
                }
            },
            onCopyClick = {
                if (uiState.password.isNotEmpty()) {
                    clipboardManager.setText(AnnotatedString(uiState.password))
                    showToast(context, context.getString(R.string.copied_to_clipboard))
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        val storedPreferences = settingsRepository.preferencesFlow.first()
        uiState = uiState.copy(
            password = storedPreferences.password,
            length = storedPreferences.length.coerceIn(MIN_LENGTH.toFloat(), MAX_LENGTH.toFloat()),
            useLowercase = storedPreferences.useLowercase,
            useUppercase = storedPreferences.useUppercase,
            useDigits = storedPreferences.useDigits,
            useSymbols = storedPreferences.useSymbols,
            excludeDuplicates = storedPreferences.excludeDuplicates,
            excludeSimilar = storedPreferences.excludeSimilar
        )
        isInitialized = true
    }

    LaunchedEffect(uiState, isInitialized) {
        if (isInitialized) {
            settingsRepository.savePreferences(
                GeneratorPreferences(
                    password = uiState.password,
                    length = uiState.length,
                    useLowercase = uiState.useLowercase,
                    useUppercase = uiState.useUppercase,
                    useDigits = uiState.useDigits,
                    useSymbols = uiState.useSymbols,
                    excludeDuplicates = uiState.excludeDuplicates,
                    excludeSimilar = uiState.excludeSimilar
                )
            )
        }
    }

    LaunchedEffect(isInitialized) {
        if (!isInitialized || uiState.password.isNotEmpty()) return@LaunchedEffect

        val initialPassword = generatePasswordWithValidation(
            config = uiState.toGenerationConfig(),
            context = context,
            onError = {}
        )
        if (initialPassword != null) {
            uiState = uiState.copy(password = initialPassword)
        }
    }
}

private fun PasswordGeneratorUiState.toGenerationConfig(): PasswordGenerationConfig =
    PasswordGenerationConfig(
        length = length.toInt(),
        useLowercase = useLowercase,
        useUppercase = useUppercase,
        useDigits = useDigits,
        useSymbols = useSymbols,
        excludeSimilar = excludeSimilar,
        excludeDuplicates = excludeDuplicates
    )

@Composable
private fun PasswordGeneratorContent(
    innerPadding: PaddingValues,
    state: PasswordGeneratorUiState,
    strengthScore: Int,
    onStateChange: (PasswordGeneratorUiState) -> Unit,
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
                onCheckedChange = { onStateChange(state.copy(useLowercase = it)) },
                text = context.getString(R.string.lowercase_label),
                tooltipText = context.getString(R.string.lowercase_hint)
            )
            CharsetCheckboxRow(
                checked = state.useUppercase,
                onCheckedChange = { onStateChange(state.copy(useUppercase = it)) },
                text = context.getString(R.string.uppercase_label),
                tooltipText = context.getString(R.string.uppercase_hint)
            )
            CharsetCheckboxRow(
                checked = state.useDigits,
                onCheckedChange = { onStateChange(state.copy(useDigits = it)) },
                text = context.getString(R.string.digits_label),
                tooltipText = context.getString(R.string.digits_hint)
            )
            CharsetCheckboxRow(
                checked = state.useSymbols,
                onCheckedChange = { onStateChange(state.copy(useSymbols = it)) },
                text = context.getString(R.string.symbols_label),
                tooltipText = context.getString(R.string.symbols_hint)
            )
            CharsetCheckboxRow(
                checked = state.excludeDuplicates,
                onCheckedChange = { onStateChange(state.copy(excludeDuplicates = it)) },
                text = context.getString(R.string.exclude_duplicates_label),
                tooltipText = context.getString(R.string.exclude_duplicates_hint)
            )
            CharsetCheckboxRow(
                checked = state.excludeSimilar,
                onCheckedChange = { onStateChange(state.copy(excludeSimilar = it)) },
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
                            onStateChange(
                                state.copy(
                                    length = newLength.coerceIn(
                                        MIN_LENGTH.toFloat(),
                                        MAX_LENGTH.toFloat()
                                    )
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
                        onValueChange = { onStateChange(state.copy(password = it)) },
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

        StrengthSection(strengthScore = strengthScore)
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

    val baseColor = when {
        fraction <= 0.5f -> lerp(red, yellow, fraction / 0.5f)
        else -> lerp(yellow, green, (fraction - 0.5f) / 0.5f)
    }

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
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 6.dp),
            color = baseColor,
            trackColor = baseColor.copy(alpha = 0.2f)
        )
        Text(
            text = strengthLabel,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            color = baseColor
        )
    }
}

private data class CharPool(
    val groups: List<String>,
    val allChars: String
)

private fun buildCharPool(
    useLowercase: Boolean,
    useUppercase: Boolean,
    useDigits: Boolean,
    useSymbols: Boolean,
    excludeSimilar: Boolean
): CharPool {
    val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
    val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val digitChars = "0123456789"
    val symbolChars = "!@#\$%^&*()-_=+[]{};:,.<>?/|"

    fun String.filterSimilar(): String =
        if (excludeSimilar) filterNot { it in SIMILAR_CHARS } else this

    val groups = buildList {
        if (useLowercase) add(lowercaseChars.filterSimilar())
        if (useUppercase) add(uppercaseChars.filterSimilar())
        if (useDigits) add(digitChars.filterSimilar())
        if (useSymbols) add(symbolChars.filterSimilar())
    }.filter { it.isNotEmpty() }

    return CharPool(
        groups = groups,
        allChars = groups.joinToString("")
    )
}

private fun generatePasswordWithValidation(
    config: PasswordGenerationConfig,
    context: Context,
    onError: (String) -> Unit
): String? {
    val (length, useLowercase, useUppercase, useDigits, useSymbols, excludeSimilar, excludeDuplicates) =
        config

    if (!useLowercase && !useUppercase && !useDigits && !useSymbols) {
        onError(context.getString(R.string.error_no_charsets))
        return null
    }

    val pool = buildCharPool(
        useLowercase = useLowercase,
        useUppercase = useUppercase,
        useDigits = useDigits,
        useSymbols = useSymbols,
        excludeSimilar = excludeSimilar
    )

    if (pool.allChars.isEmpty()) {
        onError(context.getString(R.string.error_no_charsets))
        return null
    }

    val distinctCount = pool.allChars.toSet().size
    if (excludeDuplicates && length > distinctCount) {
        onError(context.getString(R.string.error_no_enough_unique_chars))
        return null
    }

    return generatePassword(
        length = length,
        pool = pool,
        excludeDuplicates = excludeDuplicates
    )
}

private fun generatePassword(
    length: Int,
    pool: CharPool,
    excludeDuplicates: Boolean
): String {
    if (pool.groups.isEmpty() || pool.allChars.isEmpty() || length <= 0) return ""

    val result = StringBuilder(length)
    val usedChars = if (excludeDuplicates) mutableSetOf<Char>() else null

    for (group in pool.groups) {
        if (result.length >= length) break

        val availableChars = if (excludeDuplicates) {
            group.filterNot { it in usedChars!! }
        } else {
            group
        }

        if (availableChars.isEmpty()) continue

        val ch = availableChars[secureRandom.nextInt(availableChars.length)]
        result.append(ch)
        usedChars?.add(ch)
    }

    while (result.length < length) {
        val availableChars = if (excludeDuplicates) {
            pool.allChars.filterNot { it in usedChars!! }.ifEmpty { pool.allChars }
        } else {
            pool.allChars
        }

        val ch = availableChars[secureRandom.nextInt(availableChars.length)]
        result.append(ch)
        usedChars?.add(ch)
    }

    return result.toList().shuffled(secureRandom).joinToString("")
}

private enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

private fun estimatePasswordScore(password: String): Int {
    if (password.isEmpty()) return 0

    val length = password.length
    val charSpace = calculateCharSpace(password)
    val entropyScore = calculateEntropyScore(length, charSpace)
    val penalty = calculatePasswordPenalty(password, length)

    return (entropyScore + penalty).coerceIn(0, 100)
}

private fun calculateCharSpace(password: String): Int {
    var charSpace = 0

    if (password.any { it.isLowerCase() }) charSpace += 26
    if (password.any { it.isUpperCase() }) charSpace += 26
    if (password.any { it.isDigit() }) charSpace += 10
    if (password.any { !it.isLetterOrDigit() }) charSpace += 33

    return if (charSpace == 0) 1 else charSpace
}

private fun calculateEntropyScore(length: Int, charSpace: Int): Int {
    val entropyBits = length * (ln(charSpace.toDouble()) / ln(2.0))
    val maxEntropy = REF_LENGTH_FOR_MAX_SCORE * (ln(FULL_CHARSPACE) / ln(2.0))
    return (entropyBits * 100.0 / maxEntropy).toInt()
}

private fun calculatePasswordPenalty(password: String, length: Int): Int {
    var scoreAdjustment = 0

    if (length < 8) {
        scoreAdjustment -= 25
        if (length < 6) scoreAdjustment -= 10
    }

    val hasLower = password.any { it.isLowerCase() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }

    if (length < 10 && hasDigit && !hasLower && !hasUpper && !hasSymbol) {
        scoreAdjustment -= 15
    }

    if (isSequential(password)) scoreAdjustment -= 20
    if (hasManyRepeats(password)) scoreAdjustment -= 10
    if (password.toSet().size == 1 && length >= 3) scoreAdjustment -= 10

    return scoreAdjustment
}

private fun getPasswordStrength(score: Int): PasswordStrength = when {
    score < 20 -> PasswordStrength.VERY_WEAK
    score < 40 -> PasswordStrength.WEAK
    score < 60 -> PasswordStrength.MEDIUM
    score < 80 -> PasswordStrength.STRONG
    else -> PasswordStrength.VERY_STRONG
}

private fun isSequential(password: String): Boolean {
    if (password.length < 3) return false

    var ascending = true
    var descending = true

    for (i in 1 until password.length) {
        val diff = password[i] - password[i - 1]
        if (diff != 1) ascending = false
        if (diff != -1) descending = false
        if (!ascending && !descending) break
    }

    return ascending || descending
}

private fun hasManyRepeats(password: String): Boolean {
    if (password.length < 4) return false
    val ratio = password.toSet().size.toDouble() / password.length
    return ratio < 0.5
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
