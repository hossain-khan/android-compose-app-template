package dev.hossain.compose.example.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Repository interface for managing user preferences via Jetpack DataStore.
 *
 * Demonstrates local persistence of key-value user settings.
 */
interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val showUnreadOnly: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setShowUnreadOnly(unreadOnly: Boolean)
}

/**
 * Production implementation of [UserPreferencesRepository] backed by [DataStore].
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHOW_UNREAD_ONLY = booleanPreferencesKey("show_unread_only")
    }

    override val themeMode: Flow<ThemeMode> =
        dataStore.data.map { preferences ->
            when (preferences[Keys.THEME_MODE]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    override val showUnreadOnly: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[Keys.SHOW_UNREAD_ONLY] ?: false
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setShowUnreadOnly(unreadOnly: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.SHOW_UNREAD_ONLY] = unreadOnly
        }
    }
}
