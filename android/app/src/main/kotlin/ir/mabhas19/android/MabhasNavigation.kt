package ir.mabhas19.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.ThemePreference
import ir.mabhas19.feature.projects.NewProjectScreen
import ir.mabhas19.feature.projects.ProjectDetailScreen
import ir.mabhas19.feature.projects.ProjectsScreen
import ir.mabhas19.feature.projects.ProjectsViewModel
import kotlinx.serialization.Serializable

@Serializable data object ProjectList : NavKey

@Serializable data object NewProject : NavKey

@Serializable data class ProjectDetail(val id: String) : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MabhasNavigation(
    viewModel: ProjectsViewModel,
    theme: ThemePreference,
    themeError: Boolean,
    onThemeChange: () -> Unit
) {
    val backStack = rememberNavBackStack(ProjectList)
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val themeLabel = when (theme) {
        ThemePreference.SYSTEM -> R.string.theme_system
        ThemePreference.LIGHT -> R.string.theme_light
        ThemePreference.DARK -> R.string.theme_dark
    }
    LaunchedEffect(editor.createdId) {
        editor.createdId?.let { id ->
            if (backStack.lastOrNull() == NewProject) backStack.removeLastOrNull()
            backStack.add(ProjectDetail(id.value))
            viewModel.acknowledgeNavigation()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    if (backStack.size > 1) {
                        TextButton(onClick = {
                            backStack.removeLastOrNull()
                        }, modifier = Modifier.testTag("navigate_back")) {
                            Text(stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onThemeChange) { Text(stringResource(themeLabel)) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (themeError) Text(stringResource(R.string.theme_error))
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<ProjectList> {
                        ProjectsScreen(projects, loadFailed, {
                            backStack.add(NewProject)
                        }, { backStack.add(ProjectDetail(it.value)) })
                    }
                    entry<NewProject> {
                        NewProjectScreen(
                            title,
                            editor,
                            viewModel::updateTitle,
                            viewModel::createProject
                        )
                    }
                    entry<ProjectDetail> { key ->
                        val projectFlow =
                            remember(key.id) { viewModel.observeProject(ProjectId(key.id)) }
                        val project by projectFlow.collectAsStateWithLifecycle(initialValue = null)
                        ProjectDetailScreen(project)
                    }
                }
            )
        }
    }
}
