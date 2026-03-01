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

    // FIX #6: явное преобразование SecureRandom → kotlin.random.Random
    // чтобы не полагаться на неявный адаптер из stdlib
    private val kotlinRandom = secureRandom.asKotlinRandom()

    fun generate(config: PasswordGenerationConfig): PasswordGenerationResult {
        val pool = buildCharPool(
            useLowercase = config.useLowercase,
            useUppercase = config.useUppercase,
            useDigits = config.useDigits,
            useSymbols = config.useSymbols,
            excludeSimilar = config.excludeSimilar
        )

        if (pool.allChars.isEmpty() || pool.groups.isEmpty()) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NO_CHARSETS)
        }

        // FIX #2: проверка на уникальность считается по distinct-символам пула,
        // а не по длине строки allChars (которая может содержать дубли между группами)
        val distinctCount = pool.allChars.toSet().size
        if (config.excludeDuplicates && config.length > distinctCount) {
            return PasswordGenerationResult.Error(PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS)
        }

        val password = generatePassword(
            length = config.length,
            pool = pool,
            excludeDuplicates = config.excludeDuplicates
        )

        return PasswordGenerationResult.Success(password)
    }

    fun estimatePasswordScore(password: String): Int {
        if (password.isEmpty()) return 0

        val length = password.length
        val charSpace = calculateCharSpace(password)
        val entropyScore = calculateEntropyScore(length, charSpace)
        val penalty = calculatePasswordPenalty(password, length)

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
        if (pool.groups.isEmpty() || pool.allChars.isEmpty() || length <= 0) return ""

        val result = StringBuilder(length)
        val usedChars = if (excludeDuplicates) mutableSetOf<Char>() else null

        // Гарантируем хотя бы по одному символу из каждой группы
        for (group in pool.groups) {
            if (result.length >= length) break

            val availableChars = if (excludeDuplicates) {
                group.filterNot { it in usedChars!! }
            } else {
                group
            }

            if (availableChars.isEmpty()) continue

            val ch = availableChars[secureRandom.nextInt(availableChars.length)]
            result.append(ch)
            usedChars?.add(ch)
        }

        // Добираем оставшиеся символы
        while (result.length < length) {
            val availableChars = if (excludeDuplicates) {
                // FIX #2: убран фоллбэк `.ifEmpty { pool.allChars }`.
                // Если уникальных символов не осталось — это ошибка данных,
                // которая должна была быть поймана проверкой выше (NOT_ENOUGH_UNIQUE_CHARS).
                // Здесь она означает баг в логике — бросаем понятное исключение.
                val available = pool.allChars.filterNot { it in usedChars!! }
                check(available.isNotEmpty()) {
                    "No unique chars left but NOT_ENOUGH_UNIQUE_CHARS was not triggered. " +
                            "distinctCount check may be inconsistent with pool composition."
                }
                available
            } else {
                pool.allChars
            }

            val ch = availableChars[secureRandom.nextInt(availableChars.length)]
            result.append(ch)
            usedChars?.add(ch)
        }

        // FIX #6: явно используем kotlinRandom вместо неявного приведения SecureRandom
        return result.toList().shuffled(kotlinRandom).joinToString("")
    }

    private fun calculateCharSpace(password: String): Int {
        var charSpace = 0
        if (password.any { it.isLowerCase() }) charSpace += 26
        if (password.any { it.isUpperCase() }) charSpace += 26
        if (password.any { it.isDigit() }) charSpace += 10
        if (password.any { !it.isLetterOrDigit() }) charSpace += 33
        return if (charSpace == 0) 1 else charSpace
    }

    private fun calculateEntropyScore(length: Int, charSpace: Int): Int {
        val entropyBits = length * (ln(charSpace.toDouble()) / ln(2.0))
        val maxEntropy = PasswordConstants.REF_LENGTH_FOR_MAX_SCORE *
                (ln(PasswordConstants.FULL_CHARSPACE) / ln(2.0))
        return (entropyBits * 100.0 / maxEntropy).toInt()
    }

    private fun calculatePasswordPenalty(password: String, length: Int): Int {
        var scoreAdjustment = 0

        when {
            length < 6 -> scoreAdjustment -= 35
            length < 8 -> scoreAdjustment -= 25
        }

        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        if (length < 10 && hasDigit && !hasLower && !hasUpper && !hasSymbol) {
            scoreAdjustment -= 15
        }

        // FIX #8: ищем последовательности внутри пароля, а не только если весь пароль — последовательность
        if (containsSequentialSubstring(password, minLength = 4)) scoreAdjustment -= 20
        if (hasManyRepeats(password)) scoreAdjustment -= 10
        if (password.toSet().size == 1 && length >= 3) scoreAdjustment -= 10

        return scoreAdjustment
    }

    // FIX #8: заменён isSequential — теперь ищет любую последовательную подстроку
    // заданной минимальной длины (ascending или descending)
    private fun containsSequentialSubstring(password: String, minLength: Int): Boolean {
        if (password.length < minLength) return false

        var ascLen = 1
        var descLen = 1

        for (i in 1 until password.length) {
            val diff = password[i] - password[i - 1]

            if (diff == 1) {
                ascLen++
                if (ascLen >= minLength) return true
            } else {
                ascLen = 1
            }

            if (diff == -1) {
                descLen++
                if (descLen >= minLength) return true
            } else {
                descLen = 1
            }
        }

        return false
    }

    private fun hasManyRepeats(password: String): Boolean {
        if (password.length < 4) return false
        val ratio = password.toSet().size.toDouble() / password.length
        return ratio < 0.5
    }
}