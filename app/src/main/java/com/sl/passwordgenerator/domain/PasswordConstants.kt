package com.sl.passwordgenerator.domain

object PasswordConstants {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 64
    const val REF_LENGTH_FOR_MAX_SCORE = 20.0

    const val SIMILAR_CHARS = "iIl1oO0B8G6S5Z2"

    const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
    const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val DIGIT_CHARS = "0123456789"
    const val SYMBOL_CHARS = "!@#$%^&*()-_=+[]{};:,.<>?/|"

    // Keep strength normalization aligned with the generator's actual character pool.
    const val FULL_CHARSPACE = 89.0
}
