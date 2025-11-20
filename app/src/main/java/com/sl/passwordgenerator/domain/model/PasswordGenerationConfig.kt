package com.sl.passwordgenerator.domain.model

data class PasswordGenerationConfig(
    val length: Int,
    val useLowercase: Boolean,
    val useUppercase: Boolean,
    val useDigits: Boolean,
    val useSymbols: Boolean,
    val excludeSimilar: Boolean,
    val excludeDuplicates: Boolean
)
