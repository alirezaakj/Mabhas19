package ir.mabhas19.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import ir.mabhas19.core.domain.PreferencesRepository
import ir.mabhas19.core.model.ThemePreference
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class LocalPreferences @Inject constructor(private val store: DataStore<Preferences>) :
    PreferencesRepository {
    override val theme = store.data.catch { error ->
        if (error is IOException) {
            emit(
                androidx.datastore.preferences.core.emptyPreferences()
            )
        } else {
            throw error
        }
    }.map { values ->
        ThemePreference.entries.find { it.name == values[THEME] } ?: ThemePreference.SYSTEM
    }

    override suspend fun setTheme(theme: ThemePreference) {
        store.edit { it[THEME] = theme.name }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
    }
}
