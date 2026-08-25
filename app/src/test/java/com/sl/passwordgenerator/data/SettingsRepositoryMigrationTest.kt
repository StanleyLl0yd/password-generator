package com.sl.passwordgenerator.data

import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsRepositoryMigrationTest {

    @Test
    fun removeLegacyPassword_deletesPersistedPasswordValue() {
        val preferences = mutablePreferencesOf(LEGACY_PASSWORD_KEY to "old-generated-password")

        preferences.removeLegacyPassword()

        assertNull(preferences[LEGACY_PASSWORD_KEY])
    }
}
