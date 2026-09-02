package com.sl.passwordgenerator.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sl.passwordgenerator.domain.PasswordConstants
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal val LEGACY_PASSWORD_KEY = stringPreferencesKey("password")

internal fun MutablePreferences.removeLegacyPassword() {
    remove(LEGACY_PASSWORD_KEY)
}

private val Context.dataStore by preferencesDataStore(
    name = "generator_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    val preferencesFlow: Flow<GeneratorPreferences> = flow {
        // v1.4.1 and older persisted the generated password. Remove it before exposing data.
        try {
            dataStore.edit { prefs -> prefs.removeLegacyPassword() }
        } catch (_: IOException) {
            // Do not expose data if the privacy migration could not be completed.
            emit(GeneratorPreferences())
            return@flow
        }

        emitAll(
            dataStore.data
                .catch { error ->
                    if (error is IOException) emit(emptyPreferences()) else throw error
                }
                .map { prefs ->
                    GeneratorPreferences(
                        length = (prefs[Keys.LENGTH] ?: PasswordConstants.DEFAULT_LENGTH).coerceIn(
                            PasswordConstants.MIN_LENGTH,
                            PasswordConstants.MAX_LENGTH
                        ),
                        useLowercase = prefs[Keys.USE_LOWERCASE] ?: true,
                        useUppercase = prefs[Keys.USE_UPPERCASE] ?: true,
                        useDigits = prefs[Keys.USE_DIGITS] ?: true,
                        useSymbols = prefs[Keys.USE_SYMBOLS] ?: true,
                        excludeDuplicates = prefs[Keys.EXCLUDE_DUPLICATES] ?: true,
                        excludeSimilar = prefs[Keys.EXCLUDE_SIMILAR] ?: true
                    )
                }
        )
    }

    suspend fun savePreferences(preferences: GeneratorPreferences) {
        dataStore.edit { prefs ->
            // Defensive cleanup in case an old backup or app downgrade restores the key.
            prefs.removeLegacyPassword()
            prefs[Keys.LENGTH] = preferences.length
            prefs[Keys.USE_LOWERCASE] = preferences.useLowercase
            prefs[Keys.USE_UPPERCASE] = preferences.useUppercase
            prefs[Keys.USE_DIGITS] = preferences.useDigits
            prefs[Keys.USE_SYMBOLS] = preferences.useSymbols
            prefs[Keys.EXCLUDE_DUPLICATES] = preferences.excludeDuplicates
            prefs[Keys.EXCLUDE_SIMILAR] = preferences.excludeSimilar
        }
    }

    private object Keys {
        val LENGTH = intPreferencesKey("length")
        val USE_LOWERCASE = booleanPreferencesKey("use_lowercase")
        val USE_UPPERCASE = booleanPreferencesKey("use_uppercase")
        val USE_DIGITS = booleanPreferencesKey("use_digits")
        val USE_SYMBOLS = booleanPreferencesKey("use_symbols")
        val EXCLUDE_DUPLICATES = booleanPreferencesKey("exclude_duplicates")
        val EXCLUDE_SIMILAR = booleanPreferencesKey("exclude_similar")
    }
}
