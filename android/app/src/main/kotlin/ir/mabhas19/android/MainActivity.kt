package ir.mabhas19.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.mabhas19.core.designsystem.MabhasTheme
import ir.mabhas19.core.model.ThemePreference
import ir.mabhas19.feature.projects.ProjectsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val app: AppViewModel by viewModels()
    private val projects: ProjectsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by app.theme.collectAsStateWithLifecycle()
            val error by app.themeError.collectAsStateWithLifecycle()
            val dark = when (theme) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            MabhasTheme(darkTheme = dark) {
                MabhasNavigation(projects, theme, error, app::cycleTheme)
            }
        }
    }
}
