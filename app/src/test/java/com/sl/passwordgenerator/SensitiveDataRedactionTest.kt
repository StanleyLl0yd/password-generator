package com.sl.passwordgenerator

import com.sl.passwordgenerator.domain.model.PasswordGenerationResult
import com.sl.passwordgenerator.ui.PasswordGeneratorUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveDataRedactionTest {
    @Test
    fun generationResultDoesNotExposePassword() {
        val password = "secret-value-123"
        val rendered = PasswordGenerationResult.Success(password).toString()

        assertFalse(rendered.contains(password))
        assertTrue(rendered.contains("<redacted>"))
    }

    @Test
    fun uiStateDoesNotExposePassword() {
        val password = "secret-value-456"
        val rendered = PasswordGeneratorUiState(password = password).toString()

        assertFalse(rendered.contains(password))
        assertTrue(rendered.contains("<redacted>"))
    }
}
