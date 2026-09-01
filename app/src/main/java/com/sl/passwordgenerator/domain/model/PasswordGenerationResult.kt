package com.sl.passwordgenerator.domain.model

sealed class PasswordGenerationResult {
    data class Success(val password: String) : PasswordGenerationResult() {
        override fun toString(): String = "Success(password=<redacted>)"
    }

    data class Error(val reason: PasswordGenerationError) : PasswordGenerationResult()
}

enum class PasswordGenerationError {
    INVALID_LENGTH,
    NO_CHARSETS,
    NOT_ENOUGH_UNIQUE_CHARS
}
