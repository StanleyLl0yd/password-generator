package com.sl.passwordgenerator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sl.passwordgenerator.data.SettingsRepository
import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import com.sl.passwordgenerator.domain.usecase.PasswordGenerator
import java.io.IOException
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

class PasswordGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val passwordGenerator = PasswordGenerator()

    private val _uiState = MutableStateFlow(PasswordGeneratorUiState())
    val uiState: StateFlow<PasswordGeneratorUiState> = _uiState.asStateFlow()

    private val _events = Channel<PasswordGeneratorUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var saveJob: Job? = null
    private var isInitialized = false

    init {
        viewModelScope.launch {
            val preferences = settingsRepository.preferencesFlow.first()
            _uiState.value = PasswordGeneratorUiState(preferences = preferences)
            isInitialized = true
            generatePassword()
        }
    }

    fun onLengthChanged(value: Int) {
        updatePreferences(debounced = true) {
            it.copy(
                length = value.coerceIn(
                    PasswordConstants.MIN_LENGTH,
                    PasswordConstants.MAX_LENGTH
                )
            )
        }
    }

    fun onLengthChangeFinished() {
        if (!isInitialized) return
        persistPreferences(_uiState.value.preferences, delayMillis = 0)
    }

    fun onLowercaseChanged(enabled: Boolean) {
        updatePreferences { it.copy(useLowercase = enabled) }
    }

    fun onUppercaseChanged(enabled: Boolean) {
        updatePreferences { it.copy(useUppercase = enabled) }
    }

    fun onDigitsChanged(enabled: Boolean) {
        updatePreferences { it.copy(useDigits = enabled) }
    }

    fun onSymbolsChanged(enabled: Boolean) {
        updatePreferences { it.copy(useSymbols = enabled) }
    }

    fun onExcludeDuplicatesChanged(enabled: Boolean) {
        updatePreferences { it.copy(excludeDuplicates = enabled) }
    }

    fun onExcludeSimilarChanged(enabled: Boolean) {
        updatePreferences { it.copy(excludeSimilar = enabled) }
    }

    fun generatePassword() {
        if (!isInitialized || _uiState.value.isGenerating) return

        val preferences = _uiState.value.preferences
        _uiState.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                passwordGenerator.generate(preferences)
            }

            when (result) {
                is PasswordGenerationResult.Success -> {
                    val strengthScore = passwordGenerator.estimatePasswordScore(result.password)
                    _uiState.update {
                        it.copy(
                            password = result.password,
                            strengthScore = strengthScore,
                            isGenerating = false
                        )
                    }
                }

                is PasswordGenerationResult.Error -> {
                    _uiState.update { it.copy(isGenerating = false) }
                    _events.send(PasswordGeneratorUiEvent.Error(result.reason))
                }
            }
        }
    }

    private fun updatePreferences(
        debounced: Boolean = false,
        transform: (GeneratorPreferences) -> GeneratorPreferences
    ) {
        if (!isInitialized) return

        val preferences = transform(_uiState.value.preferences)
        _uiState.update { it.copy(preferences = preferences) }
        persistPreferences(preferences, delayMillis = if (debounced) 300 else 0)
    }

    private fun persistPreferences(
        preferences: GeneratorPreferences,
        delayMillis: Long
    ) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (delayMillis > 0) delay(delayMillis.milliseconds)
            try {
                settingsRepository.savePreferences(preferences)
            } catch (_: IOException) {
                _events.send(PasswordGeneratorUiEvent.SettingsSaveError)
            }
        }
    }
}
