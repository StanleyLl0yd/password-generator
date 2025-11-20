package com.sl.passwordgenerator.domain.model

sealed class PasswordGenerationResult {
    data class Success(val password: String) : PasswordGenerationResult()
    data class Error(val reason: PasswordGenerationError) : PasswordGenerationResult()
}

enum class PasswordGenerationError {
    NO_CHARSETS,
    NOT_ENOUGH_UNIQUE_CHARS
}
