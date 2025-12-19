package com.sl.passwordgenerator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    @ApplicationContext
    private val context: Context
){

    val preferencesFlow: Flow<GeneratorPreferences> = context.dataStore.data.map { preferences ->
        GeneratorPreferences(
            password = preferences[Keys.PASSWORD].orEmpty(),
            length = (preferences[Keys.LENGTH] ?: 16).toFloat().coerceIn(
                PasswordConstants.MIN_LENGTH.toFloat(),
                PasswordConstants.MAX_LENGTH.toFloat()
            ),
            useLowercase = preferences[Keys.USE_LOWERCASE] ?: true,
            useUppercase = preferences[Keys.USE_UPPERCASE] ?: true,
            useDigits = preferences[Keys.USE_DIGITS] ?: true,
            useSymbols = preferences[Keys.USE_SYMBOLS] ?: true,
            excludeDuplicates = preferences[Keys.EXCLUDE_DUPLICATES] ?: true,
            excludeSimilar = preferences[Keys.EXCLUDE_SIMILAR] ?: true
        )
    }

    suspend fun savePreferences(preferences: GeneratorPreferences) {
        context.dataStore.edit { mutablePreferences ->
            mutablePreferences[Keys.PASSWORD] = preferences.password
            mutablePreferences[Keys.LENGTH] = preferences.length.toInt()
            mutablePreferences[Keys.USE_LOWERCASE] = preferences.useLowercase
            mutablePreferences[Keys.USE_UPPERCASE] = preferences.useUppercase
            mutablePreferences[Keys.USE_DIGITS] = preferences.useDigits
            mutablePreferences[Keys.USE_SYMBOLS] = preferences.useSymbols
            mutablePreferences[Keys.EXCLUDE_DUPLICATES] = preferences.excludeDuplicates
            mutablePreferences[Keys.EXCLUDE_SIMILAR] = preferences.excludeSimilar
        }
    }

    private object Keys {
        val PASSWORD = stringPreferencesKey("password")
        val LENGTH = intPreferencesKey("length")
        val USE_LOWERCASE = booleanPreferencesKey("use_lowercase")
        val USE_UPPERCASE = booleanPreferencesKey("use_uppercase")
        val USE_DIGITS = booleanPreferencesKey("use_digits")
        val USE_SYMBOLS = booleanPreferencesKey("use_symbols")
        val EXCLUDE_DUPLICATES = booleanPreferencesKey("exclude_duplicates")
        val EXCLUDE_SIMILAR = booleanPreferencesKey("exclude_similar")
    }
}