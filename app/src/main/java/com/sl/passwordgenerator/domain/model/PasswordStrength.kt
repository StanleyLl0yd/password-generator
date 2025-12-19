package com.sl.passwordgenerator.domain.model

enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG;

    companion object {
        fun fromScore(score: Int): PasswordStrength = when {
            score < 20 -> VERY_WEAK
            score < 40 -> WEAK
            score < 60 -> MEDIUM
            score < 80 -> STRONG
            else -> VERY_STRONG
        }
    }
}