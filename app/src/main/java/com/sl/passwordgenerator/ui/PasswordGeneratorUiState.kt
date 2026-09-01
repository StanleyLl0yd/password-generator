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
    val isGenerating: Boolean = false
) {
    override fun toString(): String =
        "PasswordGeneratorUiState(" +
            "password=<redacted>, " +
            "length=$length, " +
            "useLowercase=$useLowercase, " +
            "useUppercase=$useUppercase, " +
            "useDigits=$useDigits, " +
            "useSymbols=$useSymbols, " +
            "excludeDuplicates=$excludeDuplicates, " +
            "excludeSimilar=$excludeSimilar, " +
            "strengthScore=$strengthScore, " +
            "isGenerating=$isGenerating)"
}

sealed class PasswordGeneratorUiEvent {
    data class Error(val reason: PasswordGenerationError) : PasswordGeneratorUiEvent()
    data object SettingsSaveError : PasswordGeneratorUiEvent()
}
