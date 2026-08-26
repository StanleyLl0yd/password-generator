package com.sl.passwordgenerator.ui

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sl.passwordgenerator.R
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.domain.model.PasswordStrength
import com.sl.passwordgenerator.ui.components.CheckboxRow
import com.sl.passwordgenerator.ui.components.LengthSliderCard
import com.sl.passwordgenerator.ui.components.PasswordField
import com.sl.passwordgenerator.ui.components.StrengthIndicator
import com.sl.passwordgenerator.util.HapticFeedback
import com.sl.passwordgenerator.util.SecureClipboard
import kotlinx.coroutines.launch

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.copied_to_clipboard)
    var showAbout by rememberSaveable { mutableStateOf(false) }

    val events = viewModel.events
    LaunchedEffect(events, resources) {
        events.collect { event ->
            when (event) {
                is PasswordGeneratorUiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.reason.toErrorMessage(resources))
                }
                PasswordGeneratorUiEvent.SettingsSaveError -> {
                    snackbarHostState.showSnackbar(
                        resources.getString(R.string.error_settings_save)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PasswordGeneratorTopBar(
                title = stringResource(R.string.app_name),
                aboutContentDescription = stringResource(R.string.about_open),
                onAboutClick = { showAbout = true }
            )
        },
        bottomBar = {
            GeneratorBottomBar(
                isGenerating = uiState.isGenerating,
                buttonText = stringResource(R.string.generate_button),
                onGenerateClick = {
                    HapticFeedback.performMedium(context)
                    viewModel.generatePassword()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PasswordGeneratorContent(
            state = uiState,
            viewModel = viewModel,
            onCopyClick = {
                copyPasswordToClipboard(context, uiState.password)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(copiedMessage)
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showAbout) {
        AboutSheet(onDismiss = { showAbout = false })
    }
}

@Composable
private fun PasswordGeneratorTopBar(
    title: String,
    aboutContentDescription: String,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onAboutClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = aboutContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GeneratorBottomBar(
    isGenerating: Boolean,
    buttonText: String,
    onGenerateClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = onGenerateClick,
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PasswordGeneratorContent(
    state: PasswordGeneratorUiState,
    viewModel: PasswordGeneratorViewModel,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrivacyBanner(text = stringResource(R.string.privacy_note))

            PasswordCard(
                password = state.password,
                strengthScore = state.strengthScore,
                strengthLabel = state.strengthScore.toStrengthLabel(),
                isGenerating = state.isGenerating,
                onCopyClick = onCopyClick
            )

            LengthSliderCard(
                length = state.length,
                onLengthChange = viewModel::onLengthChanged,
                onLengthChangeFinished = viewModel::onLengthChangeFinished,
                title = pluralStringResource(
                    R.plurals.length_title,
                    state.length.toInt(),
                    state.length.toInt()
                ),
                decreaseContentDescription = stringResource(R.string.decrease_length),
                increaseContentDescription = stringResource(R.string.increase_length)
            )

            OptionsCard(title = stringResource(R.string.character_sets_title)) {
                CheckboxRow(
                    checked = state.useLowercase,
                    onCheckedChange = viewModel::onLowercaseChanged,
                    text = stringResource(R.string.lowercase_label)
                )
                OptionDivider()
                CheckboxRow(
                    checked = state.useUppercase,
                    onCheckedChange = viewModel::onUppercaseChanged,
                    text = stringResource(R.string.uppercase_label)
                )
                OptionDivider()
                CheckboxRow(
                    checked = state.useDigits,
                    onCheckedChange = viewModel::onDigitsChanged,
                    text = stringResource(R.string.digits_label)
                )
                OptionDivider()
                CheckboxRow(
                    checked = state.useSymbols,
                    onCheckedChange = viewModel::onSymbolsChanged,
                    text = stringResource(R.string.symbols_label)
                )
            }

            OptionsCard(title = stringResource(R.string.advanced_options_title)) {
                CheckboxRow(
                    checked = state.excludeSimilar,
                    onCheckedChange = viewModel::onExcludeSimilarChanged,
                    text = stringResource(R.string.exclude_similar_label),
                    supportingText = stringResource(R.string.exclude_similar_summary)
                )
                OptionDivider()
                CheckboxRow(
                    checked = state.excludeDuplicates,
                    onCheckedChange = viewModel::onExcludeDuplicatesChanged,
                    text = stringResource(R.string.exclude_duplicates_label),
                    supportingText = stringResource(R.string.exclude_duplicates_summary)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun PrivacyBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PasswordCard(
    password: String,
    strengthScore: Int,
    strengthLabel: String,
    isGenerating: Boolean,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PasswordField(
                password = password,
                label = stringResource(R.string.password_label),
                copyLabel = stringResource(R.string.copy_button),
                showPasswordContentDescription = stringResource(R.string.show_password),
                hidePasswordContentDescription = stringResource(R.string.hide_password),
                onCopyClick = onCopyClick,
                isGenerating = isGenerating
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            StrengthIndicator(
                strengthScore = strengthScore,
                strengthLabel = strengthLabel,
                title = stringResource(R.string.strength_title)
            )
        }
    }
}

@Composable
private fun OptionsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun OptionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    )
}

private fun copyPasswordToClipboard(context: Context, password: String) {
    if (password.isEmpty()) return
    SecureClipboard.copyPassword(context, password)
    HapticFeedback.performLight(context)
}

private fun PasswordGenerationError.toErrorMessage(resources: Resources): String = when (this) {
    PasswordGenerationError.INVALID_LENGTH -> resources.getString(R.string.error_invalid_length)
    PasswordGenerationError.NO_CHARSETS -> resources.getString(R.string.error_no_charsets)
    PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS -> resources.getString(R.string.error_no_enough_unique_chars)
}

@Composable
private fun Int.toStrengthLabel(): String =
    when (PasswordStrength.fromScore(this)) {
        PasswordStrength.VERY_WEAK -> stringResource(R.string.strength_very_weak)
        PasswordStrength.WEAK -> stringResource(R.string.strength_weak)
        PasswordStrength.MEDIUM -> stringResource(R.string.strength_medium)
        PasswordStrength.STRONG -> stringResource(R.string.strength_strong)
        PasswordStrength.VERY_STRONG -> stringResource(R.string.strength_very_strong)
    }
