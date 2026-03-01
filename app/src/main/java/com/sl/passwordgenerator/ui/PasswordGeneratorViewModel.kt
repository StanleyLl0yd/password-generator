package com.sl.passwordgenerator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sl.passwordgenerator.data.SettingsRepository
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationConfig
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import com.sl.passwordgenerator.domain.usecase.PasswordGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val passwordGenerator: PasswordGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordGeneratorUiState())
    val uiState: StateFlow<PasswordGeneratorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PasswordGeneratorUiEvent>()
    val events: SharedFlow<PasswordGeneratorUiEvent> = _events.asSharedFlow()

    // FIX #9: одна Job для debounce сохранения — предыдущая отменяется при каждом изменении
    private var saveJob: Job? = null

    // Блокирует сохранение и действия пользователя до завершения первичной загрузки
    private var isInitialized = false

    init {
        viewModelScope.launch {
            val preferences = settingsRepository.preferencesFlow.first()
            _uiState.value = preferences.toUiState().withStrength()
            isInitialized = true

            // Первый запуск — нет сохранённого пароля, генерируем сразу
            if (_uiState.value.password.isEmpty()) {
                generatePassword()
            }
        }
    }

    fun onPasswordChanged(value: String) {
        updateState { it.copy(password = value) }
    }

    fun onLengthChanged(value: Float) {
        // FIX #8: clamp вынесен в PasswordGenerator.clampLength() — бизнес-правило в domain
        updateState { it.copy(length = passwordGenerator.clampLength(value)) }
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

    // FIX #10 + #2: generatePassword запускается в корутине на Dispatchers.Default.
    // Это решает три проблемы разом:
    //   - SecureRandom + строковые операции не блокируют Main-поток (#2)
    //   - При onCleared() корутина отменяется автоматически (#10)
    //   - Повторный вызов во время генерации игнорируется через isGenerating
    fun generatePassword() {
        if (!isInitialized) return
        if (_uiState.value.isGenerating) return

        _uiState.update { it.copy(isGenerating = true) }

        val config = _uiState.value.toGenerationConfig()

        viewModelScope.launch {
            // FIX #2: тяжёлая работа (SecureRandom, string ops) — на Default dispatcher
            val result = withContext(Dispatchers.Default) {
                passwordGenerator.generate(config)
            }

            when (result) {
                is PasswordGenerationResult.Success ->
                    // updateState вернётся на Main через StateFlow — это безопасно
                    updateState { it.copy(password = result.password, isGenerating = false) }

                is PasswordGenerationResult.Error -> {
                    _uiState.update { it.copy(isGenerating = false) }
                    _events.emit(PasswordGeneratorUiEvent.Error(result.reason))
                }
            }
        }
    }

    private fun updateState(transform: (PasswordGeneratorUiState) -> PasswordGeneratorUiState) {
        val newState = transform(_uiState.value).withStrength()
        _uiState.value = newState

        if (!isInitialized) return

        // FIX #9: debounce 300ms — при быстром движении слайдера отменяем предыдущий save,
        // записываем в DataStore только финальное значение
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300)
            settingsRepository.savePreferences(newState.toPreferences())
        }
    }

    private fun PasswordGeneratorUiState.withStrength(): PasswordGeneratorUiState =
        copy(strengthScore = passwordGenerator.estimatePasswordScore(password))
}

// ── Mapping functions ────────────────────────────────────────────────────────

private fun GeneratorPreferences.toUiState() = PasswordGeneratorUiState(
    // FIX #1: поле password в GeneratorPreferences удалено — стартуем с пустым паролем,
    // generatePassword() вызовется в init и заполнит его
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

// FIX #1: пароль не сохраняется — GeneratorPreferences больше не имеет поля password
private fun PasswordGeneratorUiState.toPreferences() = GeneratorPreferences(
    length            = length,
    useLowercase      = useLowercase,
    useUppercase      = useUppercase,
    useDigits         = useDigits,
    useSymbols        = useSymbols,
    excludeDuplicates = excludeDuplicates,
    excludeSimilar    = excludeSimilar
)