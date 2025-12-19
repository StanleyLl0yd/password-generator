package com.sl.passwordgenerator.domain.usecase

import com.sl.passwordgenerator.domain.model.PasswordStrength
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EstimatePasswordStrengthUseCase @Inject constructor(
    private val passwordGenerator: PasswordGenerator
) {
    operator fun invoke(password: String): PasswordStrength {
        val score = passwordGenerator.estimatePasswordScore(password)
        return PasswordStrength.fromScore(score)
    }
}