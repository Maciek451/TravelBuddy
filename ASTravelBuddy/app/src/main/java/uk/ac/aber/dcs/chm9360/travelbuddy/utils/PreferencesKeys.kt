package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")
val Context.themePreferenceFlow get() = dataStore.data.map { preferences ->
    preferences[PreferencesKeys.THEME_KEY] ?: 2
}
val Context.languagePreferenceFlow get() = dataStore.data.map { preferences ->
    preferences[PreferencesKeys.LANGUAGE_KEY] ?: 0
}

object PreferencesKeys {
    val THEME_KEY = intPreferencesKey("theme")
    val LANGUAGE_KEY = intPreferencesKey("language")
}

suspend fun saveThemePreference(context: Context, theme: Int) {
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.THEME_KEY] = theme
    }
}

suspend fun saveLanguagePreference(context: Context, language: Int) {
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.LANGUAGE_KEY] = language
    }
}

fun getLanguagePreference(context: Context): Flow<Int> {
    return context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_KEY] ?: 0
        }
}