package com.sl.passwordgenerator.domain.model

// #1: поле password удалено — пароль больше не хранится на диске.
// Сохраняются только пользовательские настройки генератора.
data class GeneratorPreferences(
    val length: Float = 16f,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true
)