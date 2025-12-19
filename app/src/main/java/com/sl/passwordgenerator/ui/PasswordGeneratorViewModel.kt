package com.sl.passwordgenerator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sl.passwordgenerator.data.SettingsRepository
import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationConfig
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import com.sl.passwordgenerator.domain.usecase.PasswordGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val passwordGenerator: PasswordGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordGeneratorUiState())
    val uiState: StateFlow<PasswordGeneratorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PasswordGeneratorUiEvent>()
    val events: SharedFlow<PasswordGeneratorUiEvent> = _events.asSharedFlow()

    private var isInitialized = false

    init {
        viewModelScope.launch {
            settingsRepository.preferencesFlow
                .first()
                .also { preferences ->
                    _uiState.value = preferences.toUiState().withStrength()
                    isInitialized = true

                    if (preferences.password.isEmpty()) {
                        generatePassword()
                    }
                }
        }
    }

    fun onPasswordChanged(value: String) {
        updateState { it.copy(password = value) }
    }

    fun onLengthChanged(value: Float) {
        updateState {
            it.copy(
                length = value.coerceIn(
                    PasswordConstants.MIN_LENGTH.toFloat(),
                    PasswordConstants.MAX_LENGTH.toFloat()
                )
            )
        }
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
        val config = _uiState.value.toGenerationConfig()
        when (val result = passwordGenerator.generate(config)) {
            is PasswordGenerationResult.Success ->
                updateState { it.copy(password = result.password) }
            is PasswordGenerationResult.Error ->
                emitEvent(PasswordGeneratorUiEvent.Error(result.reason))
        }
    }

    private fun updateState(
        persist: Boolean = true,
        transform: (PasswordGeneratorUiState) -> PasswordGeneratorUiState
    ) {
        val newState = transform(_uiState.value).withStrength()
        _uiState.value = newState

        if (isInitialized && persist) {
            viewModelScope.launch {
                settingsRepository.savePreferences(newState.toPreferences())
            }
        }
    }

    private fun emitEvent(event: PasswordGeneratorUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun PasswordGeneratorUiState.withStrength(): PasswordGeneratorUiState {
        return copy(strengthScore = passwordGenerator.estimatePasswordScore(password))
    }
}

// Extension functions для маппинга
private fun GeneratorPreferences.toUiState() = PasswordGeneratorUiState(
    password = password,
    length = length,
    useLowercase = useLowercase,
    useUppercase = useUppercase,
    useDigits = useDigits,
    useSymbols = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar = excludeSimilar,
    isLoading = false
)

private fun PasswordGeneratorUiState.toGenerationConfig() = PasswordGenerationConfig(
    length = length.toInt(),
    useLowercase = useLowercase,
    useUppercase = useUppercase,
    useDigits = useDigits,
    useSymbols = useSymbols,
    excludeSimilar = excludeSimilar,
    excludeDuplicates = excludeDuplicates
)

private fun PasswordGeneratorUiState.toPreferences() = GeneratorPreferences(
    password = password,
    length = length,
    useLowercase = useLowercase,
    useUppercase = useUppercase,
    useDigits = useDigits,
    useSymbols = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar = excludeSimilar
)