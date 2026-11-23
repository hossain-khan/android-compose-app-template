package dev.hossain.compose.example.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserPreferencesRepositoryImplTest {
    @Test
    fun `default preferences emit SYSTEM theme and false showUnreadOnly`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakePreferencesDataStore())

            assertThat(repository.themeMode.first()).isEqualTo(ThemeMode.SYSTEM)
            assertThat(repository.showUnreadOnly.first()).isFalse()
        }

    @Test
    fun `setThemeMode updates themeMode flow`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakePreferencesDataStore())

            repository.setThemeMode(ThemeMode.DARK)
            assertThat(repository.themeMode.first()).isEqualTo(ThemeMode.DARK)

            repository.setThemeMode(ThemeMode.LIGHT)
            assertThat(repository.themeMode.first()).isEqualTo(ThemeMode.LIGHT)

            repository.setThemeMode(ThemeMode.SYSTEM)
            assertThat(repository.themeMode.first()).isEqualTo(ThemeMode.SYSTEM)
        }

    @Test
    fun `setShowUnreadOnly updates showUnreadOnly flow`() =
        runTest {
            val repository = UserPreferencesRepositoryImpl(FakePreferencesDataStore())

            repository.setShowUnreadOnly(true)
            assertThat(repository.showUnreadOnly.first()).isTrue()

            repository.setShowUnreadOnly(false)
            assertThat(repository.showUnreadOnly.first()).isFalse()
        }
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
