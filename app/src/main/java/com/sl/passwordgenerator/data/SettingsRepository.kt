package com.sl.passwordgenerator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "generator_preferences")

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

    val preferencesFlow: Flow<GeneratorPreferences> = context.dataStore.data.map { prefs ->
        GeneratorPreferences(
            // FIX #1: Keys.PASSWORD удалён полностью.
            // Старый ключ "password" на диске (если остался от предыдущих версий)
            // просто игнорируется — DataStore не трогает неизвестные ключи при чтении,
            // а при следующей записи они вытесняются через edit{}.
            length = (prefs[Keys.LENGTH] ?: 16).toFloat().coerceIn(
                PasswordConstants.MIN_LENGTH.toFloat(),
                PasswordConstants.MAX_LENGTH.toFloat()
            ),
            useLowercase   = prefs[Keys.USE_LOWERCASE]      ?: true,
            useUppercase   = prefs[Keys.USE_UPPERCASE]      ?: true,
            useDigits      = prefs[Keys.USE_DIGITS]         ?: true,
            useSymbols     = prefs[Keys.USE_SYMBOLS]        ?: true,
            excludeDuplicates = prefs[Keys.EXCLUDE_DUPLICATES] ?: true,
            excludeSimilar    = prefs[Keys.EXCLUDE_SIMILAR]    ?: true
        )
    }

    suspend fun savePreferences(preferences: GeneratorPreferences) {
        context.dataStore.edit { prefs ->
            // FIX #1: PASSWORD ключ не записывается
            prefs[Keys.LENGTH]             = preferences.length.toInt()
            prefs[Keys.USE_LOWERCASE]      = preferences.useLowercase
            prefs[Keys.USE_UPPERCASE]      = preferences.useUppercase
            prefs[Keys.USE_DIGITS]         = preferences.useDigits
            prefs[Keys.USE_SYMBOLS]        = preferences.useSymbols
            prefs[Keys.EXCLUDE_DUPLICATES] = preferences.excludeDuplicates
            prefs[Keys.EXCLUDE_SIMILAR]    = preferences.excludeSimilar
        }
    }

    private object Keys {
        // FIX #1: val PASSWORD удалён
        val LENGTH             = intPreferencesKey("length")
        val USE_LOWERCASE      = booleanPreferencesKey("use_lowercase")
        val USE_UPPERCASE      = booleanPreferencesKey("use_uppercase")
        val USE_DIGITS         = booleanPreferencesKey("use_digits")
        val USE_SYMBOLS        = booleanPreferencesKey("use_symbols")
        val EXCLUDE_DUPLICATES = booleanPreferencesKey("exclude_duplicates")
        val EXCLUDE_SIMILAR    = booleanPreferencesKey("exclude_similar")
    }
}