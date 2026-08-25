package com.sl.passwordgenerator.domain

object PasswordConstants {
    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 64
    const val REF_LENGTH_FOR_MAX_SCORE = 20.0

    // Visually ambiguous pairs are excluded together for easier manual entry.
    const val SIMILAR_CHARS = "iIl1oO0B8G6S5Z2"

    const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
    const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val DIGIT_CHARS = "0123456789"
    const val SYMBOL_CHARS = "!@#$%^&*()-_=+[]{};:,.<>?/|"

    // The generator intentionally uses a compatibility-focused subset of printable ASCII.
    // Keep strength normalization aligned with the actual pool instead of assuming 95 chars.
    const val FULL_CHARSPACE = 89.0
}
