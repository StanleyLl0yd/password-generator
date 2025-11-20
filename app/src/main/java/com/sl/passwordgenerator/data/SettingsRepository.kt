package com.sl.passwordgenerator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sl.passwordgenerator.domain.model.GeneratorPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "generator_preferences")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

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

    private companion object {
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
