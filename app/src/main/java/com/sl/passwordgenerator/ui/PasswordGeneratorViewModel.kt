package com.sl.passwordgenerator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sl.passwordgenerator.data.SettingsRepository
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationConfig
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import com.sl.passwordgenerator.domain.usecase.PasswordGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val passwordGenerator: PasswordGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordGeneratorUiState())
    val uiState: StateFlow<PasswordGeneratorUiState> = _uiState.asStateFlow()

    private val _events = Channel<PasswordGeneratorUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var saveJob: Job? = null
    private var isInitialized = false

    init {
        viewModelScope.launch {
            val preferences = settingsRepository.preferencesFlow.first()
            _uiState.value = preferences.toUiState().withStrength()
            isInitialized = true

            if (_uiState.value.password.isEmpty()) {
                generatePassword()
            }
        }
    }

    fun onLengthChanged(value: Float) {
        updateState(saveMode = SaveMode.DEBOUNCED) {
            it.copy(length = passwordGenerator.clampLength(value))
        }
    }

    fun onLengthChangeFinished() {
        if (!isInitialized) return
        persistPreferences(_uiState.value, delayMillis = 0)
    }

    fun onLowercaseChanged(enabled: Boolean) {
        updateState { it.copy(useLowercase = enabled) }
    }

    fun onUppercaseChanged(enabled: Boolean) {
        updateState { it.copy(useUppercase = enabled) }
    }

    fun onDigitsChanged(enabled: Boolean) {
        updateState { it.copy(useDigits = enabled) }
    }

    fun onSymbolsChanged(enabled: Boolean) {
        updateState { it.copy(useSymbols = enabled) }
    }

    fun onExcludeDuplicatesChanged(enabled: Boolean) {
        updateState { it.copy(excludeDuplicates = enabled) }
    }

    fun onExcludeSimilarChanged(enabled: Boolean) {
        updateState { it.copy(excludeSimilar = enabled) }
    }

    fun generatePassword() {
        if (!isInitialized) return
        if (_uiState.value.isGenerating) return

        _uiState.update { it.copy(isGenerating = true) }

        val config = _uiState.value.toGenerationConfig()

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                passwordGenerator.generate(config)
            }

            when (result) {
                is PasswordGenerationResult.Success ->
                    updateState(saveMode = SaveMode.NONE) {
                        it.copy(password = result.password, isGenerating = false)
                    }

                is PasswordGenerationResult.Error -> {
                    _uiState.update { it.copy(isGenerating = false) }
                    _events.send(PasswordGeneratorUiEvent.Error(result.reason))
                }
            }
        }
    }

    private fun updateState(
        saveMode: SaveMode = SaveMode.IMMEDIATE,
        transform: (PasswordGeneratorUiState) -> PasswordGeneratorUiState
    ) {
        if (!isInitialized) return

        val newState = transform(_uiState.value).withStrength()
        _uiState.value = newState

        when (saveMode) {
            SaveMode.NONE -> Unit
            SaveMode.IMMEDIATE -> persistPreferences(newState, delayMillis = 0)
            SaveMode.DEBOUNCED -> persistPreferences(newState, delayMillis = 300)
        }
    }

    private fun persistPreferences(
        state: PasswordGeneratorUiState,
        delayMillis: Long
    ) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (delayMillis > 0) delay(delayMillis.milliseconds)
            try {
                settingsRepository.savePreferences(state.toPreferences())
            } catch (_: IOException) {
                _events.send(PasswordGeneratorUiEvent.SettingsSaveError)
            }
        }
    }

    private fun PasswordGeneratorUiState.withStrength(): PasswordGeneratorUiState =
        copy(strengthScore = passwordGenerator.estimatePasswordScore(password))

    private enum class SaveMode {
        NONE,
        IMMEDIATE,
        DEBOUNCED
    }
}

private fun GeneratorPreferences.toUiState() = PasswordGeneratorUiState(
    password          = "",
    length            = length,
    useLowercase      = useLowercase,
    useUppercase      = useUppercase,
    useDigits         = useDigits,
    useSymbols        = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar    = excludeSimilar
)

private fun PasswordGeneratorUiState.toGenerationConfig() = PasswordGenerationConfig(
    length            = length.toInt(),
    useLowercase      = useLowercase,
    useUppercase      = useUppercase,
    useDigits         = useDigits,
    useSymbols        = useSymbols,
    excludeSimilar    = excludeSimilar,
    excludeDuplicates = excludeDuplicates
)

private fun PasswordGeneratorUiState.toPreferences() = GeneratorPreferences(
    length            = length,
    useLowercase      = useLowercase,
    useUppercase      = useUppercase,
    useDigits         = useDigits,
    useSymbols        = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar    = excludeSimilar
)
