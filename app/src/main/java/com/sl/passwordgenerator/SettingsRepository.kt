package com.sl.passwordgenerator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "generator_preferences")

data class GeneratorPreferences(
    val password: String = "",
    val length: Float = 16f,
    val useLowercase: Boolean = true,
    val useUppercase: Boolean = true,
    val useDigits: Boolean = true,
    val useSymbols: Boolean = true,
    val excludeDuplicates: Boolean = true,
    val excludeSimilar: Boolean = true
)

class SettingsRepository(private val context: Context) {

    val preferencesFlow: Flow<GeneratorPreferences> = context.dataStore.data.map { preferences ->
        GeneratorPreferences(
            password = preferences[PASSWORD] ?: "",
            length = (preferences[LENGTH] ?: 16).toFloat(),
            useLowercase = preferences[USE_LOWERCASE] ?: true,
            useUppercase = preferences[USE_UPPERCASE] ?: true,
            useDigits = preferences[USE_DIGITS] ?: true,
            useSymbols = preferences[USE_SYMBOLS] ?: true,
            excludeDuplicates = preferences[EXCLUDE_DUPLICATES] ?: true,
            excludeSimilar = preferences[EXCLUDE_SIMILAR] ?: true
        )
    }

    suspend fun savePreferences(preferences: GeneratorPreferences) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[PASSWORD] = preferences.password
            mutablePreferences[LENGTH] = preferences.length.toInt()
            mutablePreferences[USE_LOWERCASE] = preferences.useLowercase
            mutablePreferences[USE_UPPERCASE] = preferences.useUppercase
            mutablePreferences[USE_DIGITS] = preferences.useDigits
            mutablePreferences[USE_SYMBOLS] = preferences.useSymbols
            mutablePreferences[EXCLUDE_DUPLICATES] = preferences.excludeDuplicates
            mutablePreferences[EXCLUDE_SIMILAR] = preferences.excludeSimilar
        }
    }

    companion object {
        private val PASSWORD = stringPreferencesKey("password")
        private val LENGTH = intPreferencesKey("length")
        private val USE_LOWERCASE = booleanPreferencesKey("use_lowercase")
        private val USE_UPPERCASE = booleanPreferencesKey("use_uppercase")
        private val USE_DIGITS = booleanPreferencesKey("use_digits")
        private val USE_SYMBOLS = booleanPreferencesKey("use_symbols")
        private val EXCLUDE_DUPLICATES = booleanPreferencesKey("exclude_duplicates")
        private val EXCLUDE_SIMILAR = booleanPreferencesKey("exclude_similar")
    }
}
