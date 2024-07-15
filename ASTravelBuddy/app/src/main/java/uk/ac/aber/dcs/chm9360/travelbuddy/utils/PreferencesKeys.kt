package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")
val Context.themePreferenceFlow get() = dataStore.data.map { preferences ->
    preferences[PreferencesKeys.THEME_KEY] ?: 0
}

object PreferencesKeys {
    val THEME_KEY = intPreferencesKey("theme")
}

suspend fun saveThemePreference(context: Context, theme: Int) {
    context.dataStore.edit { preferences ->
        preferences[PreferencesKeys.THEME_KEY] = theme
    }
}