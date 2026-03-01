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
    // FIX #3: isLoading удалён — он объявлялся, но нигде не использовался в UI,
    // что приводило к артефакту: экран мгновенно показывал дефолтные значения
    // (все чекбоксы true), пока DataStore не ответил.
    // Вместо этого ViewModel стартует с isInitialized = false и не принимает
    // действия пользователя до завершения загрузки настроек.
    // FIX prev: isGenerating живёт здесь, а не в локальном remember{}
    val isGenerating: Boolean = false
)

sealed class PasswordGeneratorUiEvent {
    data class Error(val reason: PasswordGenerationError) : PasswordGeneratorUiEvent()
}