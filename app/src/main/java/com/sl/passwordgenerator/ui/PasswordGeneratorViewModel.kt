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
            val preferences = settingsRepository.preferencesFlow.first()

            // FIX #4: withStrength() вызывается только один раз здесь.
            // Далее isInitialized выставляется ДО generatePassword(),
            // чтобы первый пароль тоже сохранялся в DataStore.
            _uiState.value = preferences.toUiState().let { state ->
                state.copy(
                    strengthScore = passwordGenerator.estimatePasswordScore(state.password),
                    isLoading = false
                )
            }
            isInitialized = true

            if (preferences.password.isEmpty()) {
                generatePassword()
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
        // FIX #3: защита от повторного вызова пока идёт генерация
        if (_uiState.value.isGenerating) return

        // FIX #3: isGenerating управляется из ViewModel, переживает рекомпозицию
        _uiState.update { it.copy(isGenerating = true) }

        val config = _uiState.value.toGenerationConfig()
        when (val result = passwordGenerator.generate(config)) {
            is PasswordGenerationResult.Success ->
                // FIX #3: сбрасываем isGenerating вместе с новым паролем атомарно
                updateState { it.copy(password = result.password, isGenerating = false) }
            is PasswordGenerationResult.Error -> {
                _uiState.update { it.copy(isGenerating = false) }
                emitEvent(PasswordGeneratorUiEvent.Error(result.reason))
            }
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
                // FIX #5: пароль НЕ сохраняется в DataStore — только настройки.
                // README заявляет "passwords generated in memory", поэтому
                // передаём пустую строку вместо актуального пароля.
                settingsRepository.savePreferences(newState.toPreferences(savePassword = false))
            }
        }
    }

    private fun emitEvent(event: PasswordGeneratorUiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    // FIX #4: withStrength() вынесен в приватный метод, вызывается только из updateState
    private fun PasswordGeneratorUiState.withStrength(): PasswordGeneratorUiState =
        copy(strengthScore = passwordGenerator.estimatePasswordScore(password))
}

// Маппинг
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

// FIX #5: параметр savePassword позволяет явно не сохранять пароль на диск
private fun PasswordGeneratorUiState.toPreferences(savePassword: Boolean = false) = GeneratorPreferences(
    password = if (savePassword) password else "",
    length = length,
    useLowercase = useLowercase,
    useUppercase = useUppercase,
    useDigits = useDigits,
    useSymbols = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar = excludeSimilar
)