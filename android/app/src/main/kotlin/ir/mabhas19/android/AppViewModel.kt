package ir.mabhas19.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mabhas19.core.domain.PreferencesRepository
import ir.mabhas19.core.model.ThemePreference
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(private val preferences: PreferencesRepository) :
    ViewModel() {
    val theme = preferences.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ThemePreference.SYSTEM
    )
    private val mutableThemeError = MutableStateFlow(false)
    val themeError = mutableThemeError.asStateFlow()

    fun cycleTheme() {
        viewModelScope.launch {
            try {
                preferences.setTheme(
                    ThemePreference.entries[
                        (theme.value.ordinal + 1) %
                            ThemePreference.entries.size
                    ]
                )
                mutableThemeError.value = false
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableThemeError.value = true
            }
        }
    }
}
