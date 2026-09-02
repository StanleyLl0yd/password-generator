package com.sl.passwordgenerator.ui

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WindowInsets
import androidx.compose.material3.navigationBars
import androidx.compose.material3.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sl.passwordgenerator.R
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.ui.components.CheckboxRow
import com.sl.passwordgenerator.ui.components.LengthControl
import com.sl.passwordgenerator.ui.components.PasswordField
import com.sl.passwordgenerator.ui.components.StrengthIndicator
import com.sl.passwordgenerator.util.HapticFeedback
import com.sl.passwordgenerator.util.SecureClipboard
import kotlinx.coroutines.launch

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordGeneratorViewModel = viewModel()
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
                    snackbarHostState.showSnackbar(resources.getString(R.string.error_settings_save))
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
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
                .padding(start = 16.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Button(
                onClick = onGenerateClick,
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonText, style = MaterialTheme.typography.labelLarge)
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
    val preferences = state.preferences

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 560.dp)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PasswordCard(
            password = state.password,
            strengthScore = state.strengthScore,
            strengthLabel = state.strengthScore.toStrengthLabel(),
            isGenerating = state.isGenerating,
            onCopyClick = onCopyClick
        )

        LengthControl(
            length = preferences.length,
            onLengthChange = viewModel::onLengthChanged,
            onLengthChangeFinished = viewModel::onLengthChangeFinished,
            label = stringResource(R.string.length_label),
            decreaseContentDescription = stringResource(R.string.decrease_length),
            increaseContentDescription = stringResource(R.string.increase_length)
        )

        CompactSection(title = stringResource(R.string.character_sets_title)) {
            CharacterSetGrid(
                lowercase = preferences.useLowercase,
                uppercase = preferences.useUppercase,
                digits = preferences.useDigits,
                symbols = preferences.useSymbols,
                onLowercaseChanged = viewModel::onLowercaseChanged,
                onUppercaseChanged = viewModel::onUppercaseChanged,
                onDigitsChanged = viewModel::onDigitsChanged,
                onSymbolsChanged = viewModel::onSymbolsChanged
            )
        }

        CompactSection(title = stringResource(R.string.advanced_options_title)) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    CheckboxRow(
                        checked = preferences.excludeSimilar,
                        onCheckedChange = viewModel::onExcludeSimilarChanged,
                        text = stringResource(R.string.exclude_similar_label)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                    CheckboxRow(
                        checked = preferences.excludeDuplicates,
                        onCheckedChange = viewModel::onExcludeDuplicatesChanged,
                        text = stringResource(R.string.exclude_duplicates_label)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
            StrengthIndicator(
                strengthScore = strengthScore,
                strengthLabel = strengthLabel,
                title = stringResource(R.string.strength_title)
            )
        }
    }
}

@Composable
private fun CompactSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        content()
    }
}

@Composable
private fun CharacterSetGrid(
    lowercase: Boolean,
    uppercase: Boolean,
    digits: Boolean,
    symbols: Boolean,
    onLowercaseChanged: (Boolean) -> Unit,
    onUppercaseChanged: (Boolean) -> Unit,
    onDigitsChanged: (Boolean) -> Unit,
    onSymbolsChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CharacterSetChip(
                lowercase,
                stringResource(R.string.lowercase_compact),
                onLowercaseChanged,
                Modifier.weight(1f)
            )
            CharacterSetChip(
                uppercase,
                stringResource(R.string.uppercase_compact),
                onUppercaseChanged,
                Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CharacterSetChip(
                digits,
                stringResource(R.string.digits_compact),
                onDigitsChanged,
                Modifier.weight(1f)
            )
            CharacterSetChip(
                symbols,
                stringResource(R.string.symbols_compact),
                onSymbolsChanged,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CharacterSetChip(
    selected: Boolean,
    label: String,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = {
            Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = modifier.height(40.dp)
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
private fun Int.toStrengthLabel(): String = stringResource(
    when {
        this < 20 -> R.string.strength_very_weak
        this < 40 -> R.string.strength_weak
        this < 60 -> R.string.strength_medium
        this < 80 -> R.string.strength_strong
        else -> R.string.strength_very_strong
    }
)
