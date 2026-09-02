package com.sl.passwordgenerator.ui

import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationError

data class PasswordGeneratorUiState(
    val preferences: GeneratorPreferences = GeneratorPreferences(),
    val password: String = "",
    val strengthScore: Int = 0,
    val isGenerating: Boolean = false
) {
    override fun toString(): String =
        "PasswordGeneratorUiState(" +
            "preferences=$preferences, " +
            "password=<redacted>, " +
            "strengthScore=$strengthScore, " +
            "isGenerating=$isGenerating)"
}

sealed class PasswordGeneratorUiEvent {
    data class Error(val reason: PasswordGenerationError) : PasswordGeneratorUiEvent()
    data object SettingsSaveError : PasswordGeneratorUiEvent()
}
