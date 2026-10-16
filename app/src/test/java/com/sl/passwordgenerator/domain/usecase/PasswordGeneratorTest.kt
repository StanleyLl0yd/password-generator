package com.sl.passwordgenerator.domain.usecase

import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import com.sl.passwordgenerator.domain.model.PasswordGenerationError
import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PasswordGeneratorTest {

    private val generator = PasswordGenerator()

    @Test
    fun generate_returnsRequestedLengthAndEverySelectedGroup() {
        val password = generatePassword(
            config(
                length = 32,
                useLowercase = true,
                useUppercase = true,
                useDigits = true,
                useSymbols = true
            )
        )

        assertEquals(32, password.length)
        assertTrue(password.any { it in PasswordConstants.LOWERCASE_CHARS })
        assertTrue(password.any { it in PasswordConstants.UPPERCASE_CHARS })
        assertTrue(password.any { it in PasswordConstants.DIGIT_CHARS })
        assertTrue(password.any { it in PasswordConstants.SYMBOL_CHARS })
    }

    @Test
    fun generate_includesEverySelectedGroupAtMinimumLength() {
        val password = generatePassword(
            config(
                length = PasswordConstants.MIN_LENGTH,
                useLowercase = true,
                useUppercase = true,
                useDigits = true,
                useSymbols = true
            )
        )

        assertEquals(PasswordConstants.MIN_LENGTH, password.length)
        assertTrue(password.any { it in PasswordConstants.LOWERCASE_CHARS })
        assertTrue(password.any { it in PasswordConstants.UPPERCASE_CHARS })
        assertTrue(password.any { it in PasswordConstants.DIGIT_CHARS })
        assertTrue(password.any { it in PasswordConstants.SYMBOL_CHARS })
    }

    @Test
    fun generate_excludesSimilarAndDuplicateCharacters() {
        val password = generatePassword(
            config(
                length = 64,
                useLowercase = true,
                useUppercase = true,
                useDigits = true,
                useSymbols = true,
                excludeSimilar = true,
                excludeDuplicates = true
            )
        )

        assertTrue(password.none { it in PasswordConstants.SIMILAR_CHARS })
        assertEquals(password.length, password.toSet().size)
    }

    @Test
    fun generate_returnsNoCharsetsErrorWhenNothingSelected() {
        val result = generator.generate(
            config(
                useLowercase = false,
                useUppercase = false,
                useDigits = false,
                useSymbols = false
            )
        )

        assertEquals(
            PasswordGenerationResult.Error(PasswordGenerationError.NO_CHARSETS),
            result
        )
    }

    @Test
    fun generate_rejectsLengthsOutsideDomainBounds() {
        listOf(PasswordConstants.MIN_LENGTH - 1, PasswordConstants.MAX_LENGTH + 1)
            .forEach { invalidLength ->
                val result = generator.generate(config(length = invalidLength))

                assertEquals(
                    PasswordGenerationResult.Error(PasswordGenerationError.INVALID_LENGTH),
                    result
                )
            }
    }

    @Test
    fun generate_returnsUniqueCharsErrorWhenFilteredPoolIsTooSmall() {
        val result = generator.generate(
            config(
                length = 5,
                useLowercase = false,
                useUppercase = false,
                useDigits = true,
                useSymbols = false,
                excludeSimilar = true,
                excludeDuplicates = true
            )
        )

        assertEquals(
            PasswordGenerationResult.Error(PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS),
            result
        )
    }

    @Test
    fun estimatePasswordScore_rejectsLongRepeatedContent() {
        assertTrue(generator.estimatePasswordScore("a".repeat(32)) < 20)
        assertTrue(generator.estimatePasswordScore("password".repeat(4)) < 20)
    }

    @Test
    fun estimatePasswordScore_acceptsRandomLookingGeneratorOutput() {
        assertTrue(generator.estimatePasswordScore("fK7!pQ3@vN9#xR4%") >= 80)
    }

    @Test
    fun generationContractHoldsAcrossAllOptionsAndBoundaryLengths() {
        val groups = listOf(
            PasswordConstants.LOWERCASE_CHARS, PasswordConstants.UPPERCASE_CHARS,
            PasswordConstants.DIGIT_CHARS, PasswordConstants.SYMBOL_CHARS
        )
        for (mask in 0..15) for (similar in listOf(false, true)) {
            val enabled = groups.filterIndexed { index, _ -> mask and (1 shl index) != 0 }
                .map { group -> if (similar) group.filterNot { it in PasswordConstants.SIMILAR_CHARS } else group }
            val pool = enabled.joinToString("")
            for (duplicates in listOf(false, true)) for (length in listOf(4, 16, 64)) {
                val preferences = config(
                    length, mask and 1 != 0, mask and 2 != 0, mask and 4 != 0,
                    mask and 8 != 0, similar, duplicates
                )
                val result = generator.generate(preferences)
                when {
                    pool.isEmpty() -> assertEquals(
                        PasswordGenerationResult.Error(PasswordGenerationError.NO_CHARSETS), result
                    )
                    duplicates && length > pool.length -> assertEquals(
                        PasswordGenerationResult.Error(PasswordGenerationError.NOT_ENOUGH_UNIQUE_CHARS), result
                    )
                    else -> {
                        val password = (result as PasswordGenerationResult.Success).password
                        assertEquals(length, password.length)
                        assertTrue(password.all { it in pool })
                        assertTrue(enabled.all { group -> password.any { it in group } })
                        if (duplicates) assertEquals(length, password.toSet().size)
                    }
                }
            }
        }
    }

    private fun generatePassword(config: GeneratorPreferences): String =
        when (val result = generator.generate(config)) {
            is PasswordGenerationResult.Success -> result.password
            is PasswordGenerationResult.Error -> {
                fail("Expected generated password, got ${result.reason}")
                error("Unreachable")
            }
        }

    private fun config(
        length: Int = 16,
        useLowercase: Boolean = true,
        useUppercase: Boolean = true,
        useDigits: Boolean = true,
        useSymbols: Boolean = true,
        excludeSimilar: Boolean = false,
        excludeDuplicates: Boolean = false
    ) = GeneratorPreferences(
        length = length,
        useLowercase = useLowercase,
        useUppercase = useUppercase,
        useDigits = useDigits,
        useSymbols = useSymbols,
        excludeSimilar = excludeSimilar,
        excludeDuplicates = excludeDuplicates
    )
}
