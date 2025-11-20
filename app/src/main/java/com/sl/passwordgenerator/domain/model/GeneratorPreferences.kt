package com.sl.passwordgenerator.domain.model

data class GeneratorPreferences(
    val password: String = "",
    val length: Float = 16f,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true
)
