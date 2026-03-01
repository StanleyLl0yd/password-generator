package com.sl.passwordgenerator.ui

import com.sl.passwordgenerator.domain.model.PasswordGenerationError

data class PasswordGeneratorUiState(
    val password: String = "",
    val length: Float = 16f,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true,
    val strengthScore: Int = 0,
    val isLoading: Boolean = true,
    // FIX #3: перенесён из локального состояния Composable в UiState,
    // чтобы флаг переживал рекомпозицию и управлялся только из ViewModel
    val isGenerating: Boolean = false
)

sealed class PasswordGeneratorUiEvent {
    data class Error(val reason: PasswordGenerationError) : PasswordGeneratorUiEvent()
}