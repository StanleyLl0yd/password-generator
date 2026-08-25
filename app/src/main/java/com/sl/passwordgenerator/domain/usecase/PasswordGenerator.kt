package com.sl.passwordgenerator.domain.usecase

import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.PasswordGenerationConfig
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln
import kotlin.random.asKotlinRandom

private data class CharPool(
    val groups: List<String>,
    val allChars: String
)

@Singleton
class PasswordGenerator @Inject constructor() {

    // SecureRandom — thread-safe синглтон, намеренно не создаётся локально
    private val secureRandom = SecureRandom()

    // Явное преобразование SecureRandom → kotlin.random.Random
    private val kotlinRandom = secureRandom.asKotlinRandom()

    fun clampLength(value: Float): Float =
        value.coerceIn(
            PasswordConstants.MIN_LENGTH.toFloat(),
            PasswordConstants.MAX_LENGTH.toFloat()
        )

    fun generate(config: PasswordGenerationConfig): PasswordGenerationResult {
        if (config.length !in PasswordConstants.MIN_LENGTH..PasswordConstants.MAX_LENGTH) {
            return PasswordGenerationResult.Error(PasswordGenerationError.INVALID_LENGTH)
        }

        val pool = buildCharPool(
            useLowercase  = config.useLowercase,
            useUppercase  = config.useUppercase,
            useDigits     = config.useDigits,
            useSymbols    = config.useSymbols,
            excludeSimilar = config.excludeSimilar
        )

        if (pool.allChars.isEmpty() || pool.groups.isEmpty()) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NO_CHARSETS)
        }

        val distinctCount = pool.allChars.toSet().size
        if (config.excludeDuplicates && config.length > distinctCount) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS)
        }

        val password = generatePassword(
            length           = config.length,
            pool             = pool,
            excludeDuplicates = config.excludeDuplicates
        )

        return PasswordGenerationResult.Success(password)
    }

    fun estimatePasswordScore(password: String): Int {
        if (password.isEmpty()) return 0
        val effectiveLength = calculateEffectiveLength(password)
        val charSpace        = calculateCharSpace(password)
        val entropyScore     = calculateEntropyScore(effectiveLength, charSpace)
        val penalty          = calculatePasswordPenalty(password, effectiveLength)
        return (entropyScore + penalty).coerceIn(0, 100)
    }

    private fun buildCharPool(
        useLowercase:  Boolean,
        useUppercase:  Boolean,
        useDigits:     Boolean,
        useSymbols:    Boolean,
        excludeSimilar: Boolean
    ): CharPool {
        fun String.filterSimilar(): String =
            if (excludeSimilar) filterNot { it in PasswordConstants.SIMILAR_CHARS } else this

        val groups = buildList {
            if (useLowercase) add(PasswordConstants.LOWERCASE_CHARS.filterSimilar())
            if (useUppercase) add(PasswordConstants.UPPERCASE_CHARS.filterSimilar())
            if (useDigits)    add(PasswordConstants.DIGIT_CHARS.filterSimilar())
            if (useSymbols)   add(PasswordConstants.SYMBOL_CHARS.filterSimilar())
        }.filter { it.isNotEmpty() }

        return CharPool(
            groups   = groups,
            allChars = groups.joinToString("")
        )
    }

    private fun generatePassword(
        length:           Int,
        pool:             CharPool,
        excludeDuplicates: Boolean
    ): String {
        if (pool.groups.isEmpty() || pool.allChars.isEmpty() || length <= 0) return ""

        val result    = StringBuilder(length)
        val usedChars = if (excludeDuplicates) mutableSetOf<Char>() else null

        // Гарантируем хотя бы по одному символу из каждой группы
        for (group in pool.groups) {
            if (result.length >= length) break
            val available = if (excludeDuplicates) group.filterNot { it in usedChars!! } else group
            if (available.isEmpty()) continue
            val ch = available[secureRandom.nextInt(available.length)]
            result.append(ch)
            usedChars?.add(ch)
        }

        // Добираем оставшиеся символы
        while (result.length < length) {
            val available = if (excludeDuplicates) {
                val a = pool.allChars.filterNot { it in usedChars!! }
                check(a.isNotEmpty()) {
                    "No unique chars left — NOT_ENOUGH_UNIQUE_CHARS check inconsistent with pool."
                }
                a
            } else {
                pool.allChars
            }
            val ch = available[secureRandom.nextInt(available.length)]
            result.append(ch)
            usedChars?.add(ch)
        }

        return result.toList().shuffled(kotlinRandom).joinToString("")
    }

    private fun calculateCharSpace(password: String): Int {
        var space = 0
        if (password.any { it.isLowerCase() })       space += 26
        if (password.any { it.isUpperCase() })       space += 26
        if (password.any { it.isDigit() })           space += 10
        if (password.any { !it.isLetterOrDigit() })  space += PasswordConstants.SYMBOL_CHARS.length
        return if (space == 0) 1 else space
    }

    /**
     * Repeating a short block does not add the entropy implied by the displayed length.
     * For example, "password" repeated four times is still based on an eight-character unit.
     */
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
        val maxEntropy  = PasswordConstants.REF_LENGTH_FOR_MAX_SCORE *
                (ln(PasswordConstants.FULL_CHARSPACE) / ln(2.0))
        return (entropyBits * 100.0 / maxEntropy).toInt()
    }

    private fun calculatePasswordPenalty(password: String, effectiveLength: Int): Int {
        var adj = 0

        when {
            effectiveLength < 6 -> adj -= 35
            effectiveLength < 8 -> adj -= 25
        }

        val hasDigit  = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (effectiveLength < 10 && hasDigit
            && !password.any { it.isLowerCase() }
            && !password.any { it.isUpperCase() }
            && !hasSymbol
        ) {
            adj -= 15
        }

        if (containsSequentialSubstring(password, minLength = 4)) adj -= 20
        if (hasManyRepeats(password))                              adj -= 10
        if (password.toSet().size == 1 && password.length >= 3)   adj -= 10
        if (containsCommonPattern(password))                       adj -= 30

        return adj
    }

    // Ищет любую последовательную подстроку заданной длины (ascending или descending)
    private fun containsSequentialSubstring(password: String, minLength: Int): Boolean {
        if (password.length < minLength) return false
        var ascLen  = 1
        var descLen = 1
        for (i in 1 until password.length) {
            val diff = password[i] - password[i - 1]
            if (diff ==  1) { if (++ascLen  >= minLength) return true } else ascLen  = 1
            if (diff == -1) { if (++descLen >= minLength) return true } else descLen = 1
        }
        return false
    }

    private fun hasManyRepeats(password: String): Boolean {
        if (password.length < 4) return false
        return password.toSet().size.toDouble() / password.length < 0.5
    }

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
