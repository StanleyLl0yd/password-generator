package com.sl.passwordgenerator.domain.model

import com.sl.passwordgenerator.domain.PasswordConstants

data class GeneratorPreferences(
    val length: Int = PasswordConstants.DEFAULT_LENGTH,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true
)
