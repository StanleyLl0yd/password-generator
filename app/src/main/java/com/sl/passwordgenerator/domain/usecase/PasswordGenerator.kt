package com.sl.passwordgenerator.domain.usecase

import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import java.security.SecureRandom
import java.util.Collections
import kotlin.math.ln

private data class CharPool(
    val groups: List<String>,
    val allChars: String
)

class PasswordGenerator {

    private val secureRandom = SecureRandom()

    fun generate(config: GeneratorPreferences): PasswordGenerationResult {
        if (config.length !in PasswordConstants.MIN_LENGTH..PasswordConstants.MAX_LENGTH) {
            return PasswordGenerationResult.Error(PasswordGenerationError.INVALID_LENGTH)
        }

        val pool = buildCharPool(
            useLowercase = config.useLowercase,
            useUppercase = config.useUppercase,
            useDigits = config.useDigits,
            useSymbols = config.useSymbols,
            excludeSimilar = config.excludeSimilar
        )

        if (pool.allChars.isEmpty()) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NO_CHARSETS)
        }

        if (config.excludeDuplicates && config.length > pool.allChars.length) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS)
        }

        return PasswordGenerationResult.Success(
            generatePassword(
                length = config.length,
                pool = pool,
                excludeDuplicates = config.excludeDuplicates
            )
        )
    }

    fun estimatePasswordScore(password: String): Int {
        if (password.isEmpty()) return 0
        val effectiveLength = calculateEffectiveLength(password)
        val charSpace = calculateCharSpace(password)
        val entropyScore = calculateEntropyScore(effectiveLength, charSpace)
        val penalty = calculatePasswordPenalty(password, effectiveLength)
        return (entropyScore + penalty).coerceIn(0, 100)
    }

    private fun buildCharPool(
        useLowercase: Boolean,
        useUppercase: Boolean,
        useDigits: Boolean,
        useSymbols: Boolean,
        excludeSimilar: Boolean
    ): CharPool {
        fun String.filterSimilar(): String =
            if (excludeSimilar) filterNot { it in PasswordConstants.SIMILAR_CHARS } else this

        val groups = buildList {
            if (useLowercase) add(PasswordConstants.LOWERCASE_CHARS.filterSimilar())
            if (useUppercase) add(PasswordConstants.UPPERCASE_CHARS.filterSimilar())
            if (useDigits) add(PasswordConstants.DIGIT_CHARS.filterSimilar())
            if (useSymbols) add(PasswordConstants.SYMBOL_CHARS.filterSimilar())
        }.filter { it.isNotEmpty() }

        return CharPool(
            groups = groups,
            allChars = groups.joinToString("")
        )
    }

    private fun generatePassword(
        length: Int,
        pool: CharPool,
        excludeDuplicates: Boolean
    ): String {
        val result = StringBuilder(length)
        val availableChars = if (excludeDuplicates) pool.allChars.toMutableList() else null

        for (group in pool.groups) {
            val char = group[secureRandom.nextInt(group.length)]
            result.append(char)
            availableChars?.remove(char)
        }

        while (result.length < length) {
            val char = if (availableChars == null) {
                pool.allChars[secureRandom.nextInt(pool.allChars.length)]
            } else {
                availableChars.removeAt(secureRandom.nextInt(availableChars.size))
            }
            result.append(char)
        }

        val shuffled = result.toString().toMutableList()
        Collections.shuffle(shuffled, secureRandom)
        return shuffled.joinToString("")
    }

    private fun calculateCharSpace(password: String): Int {
        var space = 0
        if (password.any { it.isLowerCase() }) space += PasswordConstants.LOWERCASE_CHARS.length
        if (password.any { it.isUpperCase() }) space += PasswordConstants.UPPERCASE_CHARS.length
        if (password.any { it.isDigit() }) space += PasswordConstants.DIGIT_CHARS.length
        if (password.any { !it.isLetterOrDigit() }) space += PasswordConstants.SYMBOL_CHARS.length
        return space.coerceAtLeast(1)
    }

    // Exact repeated blocks count as their shortest repeating unit.
    private fun calculateEffectiveLength(password: String): Int {
        for (unitLength in 1..password.length / 2) {
            if (password.length % unitLength != 0) continue
            if (password.indices.all { index ->
                    password[index] == password[index % unitLength]
                }
            ) {
                return unitLength
            }
        }
        return password.length
    }

    private fun calculateEntropyScore(length: Int, charSpace: Int): Int {
        val entropyBits = length * (ln(charSpace.toDouble()) / ln(2.0))
        val maxEntropy = PasswordConstants.REF_LENGTH_FOR_MAX_SCORE *
            (ln(PasswordConstants.FULL_CHARSPACE.toDouble()) / ln(2.0))
        return (entropyBits * 100.0 / maxEntropy).toInt()
    }

    private fun calculatePasswordPenalty(password: String, effectiveLength: Int): Int {
        var adjustment = 0

        when {
            effectiveLength < 6 -> adjustment -= 35
            effectiveLength < 8 -> adjustment -= 25
        }

        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (
            effectiveLength < 10 && hasDigit &&
            !password.any { it.isLowerCase() } &&
            !password.any { it.isUpperCase() } &&
            !hasSymbol
        ) {
            adjustment -= 15
        }

        if (containsSequentialSubstring(password, minLength = 4)) adjustment -= 20
        if (hasManyRepeats(password)) adjustment -= 10
        if (password.toSet().size == 1 && password.length >= 3) adjustment -= 10
        if (containsCommonPattern(password)) adjustment -= 30

        return adjustment
    }

    private fun containsSequentialSubstring(password: String, minLength: Int): Boolean {
        if (password.length < minLength) return false
        var ascendingLength = 1
        var descendingLength = 1
        for (index in 1 until password.length) {
            val difference = password[index] - password[index - 1]
            if (difference == 1) {
                if (++ascendingLength >= minLength) return true
            } else {
                ascendingLength = 1
            }
            if (difference == -1) {
                if (++descendingLength >= minLength) return true
            } else {
                descendingLength = 1
            }
        }
        return false
    }

    private fun hasManyRepeats(password: String): Boolean =
        password.length >= 4 && password.toSet().size.toDouble() / password.length < 0.5

    private fun containsCommonPattern(password: String): Boolean {
        val normalized = password.lowercase()
        return COMMON_PATTERNS.any { it in normalized }
    }

    private companion object {
        val COMMON_PATTERNS = listOf(
            "password",
            "qwerty",
            "asdf",
            "zxcv",
            "letmein",
            "admin",
            "welcome"
        )
    }
}
