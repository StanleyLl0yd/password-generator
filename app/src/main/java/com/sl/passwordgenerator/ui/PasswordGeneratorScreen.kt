package com.sl.passwordgenerator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sl.passwordgenerator.R
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.domain.model.PasswordStrength
import com.sl.passwordgenerator.ui.components.*
import com.sl.passwordgenerator.util.HapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PasswordGeneratorUiEvent.Error -> {
                    val message = event.reason.toErrorMessage(context)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PasswordGeneratorTopBar(
                title = stringResource(R.string.app_name)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PasswordGeneratorContent(
            state = uiState,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun PasswordGeneratorTopBar(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun PasswordGeneratorContent(
    state: PasswordGeneratorUiState,
    viewModel: PasswordGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val useTwoColumns = configuration.screenHeightDp.dp < 700.dp

    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CheckboxGrid(
            state = state,
            viewModel = viewModel,
            useTwoColumns = useTwoColumns
        )

        LengthSliderCard(
            length = state.length,
            onLengthChange = viewModel::onLengthChanged,
            title = stringResource(R.string.length_title, state.length.toInt())
        )

        PasswordCard(
            password = state.password,
            onPasswordChange = viewModel::onPasswordChanged,
            isGenerating = isGenerating,
            onCopyClick = {
                copyPasswordToClipboard(
                    context = context,
                    password = state.password
                )
            },
            onGenerateClick = {
                scope.launch {
                    isGenerating = true
                    HapticFeedback.performMedium(context)
                    delay(100)
                    viewModel.generatePassword()
                    delay(200)
                    isGenerating = false
                }
            }
        )

        StrengthIndicator(
            strengthScore = state.strengthScore,
            strengthLabel = state.strengthScore.toStrengthLabel(context),
            title = stringResource(R.string.strength_title)
        )
    }
}

@Composable
private fun CheckboxGrid(
    state: PasswordGeneratorUiState,
    viewModel: PasswordGeneratorViewModel,
    useTwoColumns: Boolean
) {
    if (useTwoColumns) {
        TwoColumnCheckboxGrid(state = state, viewModel = viewModel)
    } else {
        SingleColumnCheckboxGrid(state = state, viewModel = viewModel)
    }
}

@Composable
private fun TwoColumnCheckboxGrid(
    state: PasswordGeneratorUiState,
    viewModel: PasswordGeneratorViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CheckboxRow(
                checked = state.useLowercase,
                onCheckedChange = viewModel::onLowercaseChanged,
                text = stringResource(R.string.lowercase_label),
                tooltipText = stringResource(R.string.lowercase_hint),
                compact = true
            )
            CheckboxRow(
                checked = state.useDigits,
                onCheckedChange = viewModel::onDigitsChanged,
                text = stringResource(R.string.digits_label),
                tooltipText = stringResource(R.string.digits_hint),
                compact = true
            )
            CheckboxRow(
                checked = state.excludeDuplicates,
                onCheckedChange = viewModel::onExcludeDuplicatesChanged,
                text = stringResource(R.string.exclude_duplicates_label),
                tooltipText = stringResource(R.string.exclude_duplicates_hint),
                compact = true
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CheckboxRow(
                checked = state.useUppercase,
                onCheckedChange = viewModel::onUppercaseChanged,
                text = stringResource(R.string.uppercase_label),
                tooltipText = stringResource(R.string.uppercase_hint),
                compact = true
            )
            CheckboxRow(
                checked = state.useSymbols,
                onCheckedChange = viewModel::onSymbolsChanged,
                text = stringResource(R.string.symbols_label),
                tooltipText = stringResource(R.string.symbols_hint),
                compact = true
            )
            CheckboxRow(
                checked = state.excludeSimilar,
                onCheckedChange = viewModel::onExcludeSimilarChanged,
                text = stringResource(R.string.exclude_similar_label),
                tooltipText = stringResource(R.string.exclude_similar_hint),
                compact = true
            )
        }
    }
}

@Composable
private fun SingleColumnCheckboxGrid(
    state: PasswordGeneratorUiState,
    viewModel: PasswordGeneratorViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CheckboxRow(
            checked = state.useLowercase,
            onCheckedChange = viewModel::onLowercaseChanged,
            text = stringResource(R.string.lowercase_label),
            tooltipText = stringResource(R.string.lowercase_hint)
        )
        CheckboxRow(
            checked = state.useUppercase,
            onCheckedChange = viewModel::onUppercaseChanged,
            text = stringResource(R.string.uppercase_label),
            tooltipText = stringResource(R.string.uppercase_hint)
        )
        CheckboxRow(
            checked = state.useDigits,
            onCheckedChange = viewModel::onDigitsChanged,
            text = stringResource(R.string.digits_label),
            tooltipText = stringResource(R.string.digits_hint)
        )
        CheckboxRow(
            checked = state.useSymbols,
            onCheckedChange = viewModel::onSymbolsChanged,
            text = stringResource(R.string.symbols_label),
            tooltipText = stringResource(R.string.symbols_hint)
        )
        CheckboxRow(
            checked = state.excludeDuplicates,
            onCheckedChange = viewModel::onExcludeDuplicatesChanged,
            text = stringResource(R.string.exclude_duplicates_label),
            tooltipText = stringResource(R.string.exclude_duplicates_hint)
        )
        CheckboxRow(
            checked = state.excludeSimilar,
            onCheckedChange = viewModel::onExcludeSimilarChanged,
            text = stringResource(R.string.exclude_similar_label),
            tooltipText = stringResource(R.string.exclude_similar_hint)
        )
    }
}

@Composable
private fun PasswordCard(
    password: String,
    onPasswordChange: (String) -> Unit,
    isGenerating: Boolean,
    onCopyClick: () -> Unit,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PasswordField(
                password = password,
                onPasswordChange = onPasswordChange,
                label = stringResource(R.string.password_label),
                isGenerating = isGenerating
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCopyClick) {
                    Text(text = stringResource(R.string.copy_button))
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
                    Text(text = stringResource(R.string.generate_button))
                }
            }
        }
    }
}

private fun copyPasswordToClipboard(
    context: Context,
    password: String
) {
    if (password.isEmpty()) return

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(
        context.getString(R.string.password_label),
        password
    )
    clipboardManager.setPrimaryClip(clipData)

    HapticFeedback.performLight(context)
    Toast.makeText(
        context,
        context.getString(R.string.copied_to_clipboard),
        Toast.LENGTH_SHORT
    ).show()
}

// Extension functions
private fun PasswordGenerationError.toErrorMessage(context: Context): String {
    return when (this) {
        PasswordGenerationError.NO_CHARSETS ->
            context.getString(R.string.error_no_charsets)
        PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS ->
            context.getString(R.string.error_no_enough_unique_chars)
    }
}

private fun Int.toStrengthLabel(context: Context): String {
    val strength = PasswordStrength.fromScore(this)
    return when (strength) {
        PasswordStrength.VERY_WEAK -> context.getString(R.string.strength_very_weak)
        PasswordStrength.WEAK -> context.getString(R.string.strength_weak)
        PasswordStrength.MEDIUM -> context.getString(R.string.strength_medium)
        PasswordStrength.STRONG -> context.getString(R.string.strength_strong)
        PasswordStrength.VERY_STRONG -> context.getString(R.string.strength_very_strong)
    }
}