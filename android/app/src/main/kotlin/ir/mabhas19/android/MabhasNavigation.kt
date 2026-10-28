package ir.mabhas19.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.ThemePreference
import ir.mabhas19.feature.architecture.ArchitectureScreen
import ir.mabhas19.feature.architecture.ArchitectureViewModel
import ir.mabhas19.feature.home.HomeScreen
import ir.mabhas19.feature.home.HomeSection
import ir.mabhas19.feature.home.SectionPlaceholderScreen
import ir.mabhas19.feature.mechanical.MechanicalScreen
import ir.mabhas19.feature.mechanical.MechanicalViewModel
import ir.mabhas19.feature.projects.NewProjectScreen
import ir.mabhas19.feature.projects.ProjectDetailScreen
import ir.mabhas19.feature.projects.ProjectsScreen
import ir.mabhas19.feature.projects.ProjectsViewModel
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object ProjectList : NavKey

@Serializable data object NewProject : NavKey

@Serializable data class ProjectDetail(val id: String) : NavKey

@Serializable data class Section(val key: String) : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MabhasNavigation(
    viewModel: ProjectsViewModel,
    theme: ThemePreference,
    themeError: Boolean,
    onThemeChange: () -> Unit,
    architectureViewModel: ArchitectureViewModel,
    openArchitecture: Boolean = false,
    mechanicalViewModel: MechanicalViewModel,
    openMechanical: Boolean = false
) {
    val backStack = rememberNavBackStack(Home)
    ArchitectureIntent(openArchitecture) {
        if ((backStack.lastOrNull() as? Section)?.key != "architecture") {
            backStack.add(Section("architecture"))
        }
    }
    MechanicalIntent(openMechanical) {
        if ((backStack.lastOrNull() as? Section)?.key != "mechanical") {
            backStack.add(Section("mechanical"))
        }
    }
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val architectureState by architectureViewModel.state.collectAsStateWithLifecycle()
    val architectureEvaluation by architectureViewModel.evaluation.collectAsStateWithLifecycle()
    val mechanicalState by mechanicalViewModel.state.collectAsStateWithLifecycle()
    val fullScreenSection = (backStack.lastOrNull() as? Section)?.key in
        setOf("architecture", "mechanical")
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
            if (!fullScreenSection) {
                TopAppBar(
                    title = {
                        if (backStack.lastOrNull() == Home) {
                            Column {
                                Text(
                                    stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.app_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(stringResource(R.string.app_name))
                        }
                    },
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
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (themeError) Text(stringResource(R.string.theme_error))
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeScreen(
                            projects = projects,
                            loadFailed = loadFailed,
                            onCreateProject = { backStack.add(NewProject) },
                            onOpenProjects = { backStack.add(ProjectList) },
                            onOpenProject = { backStack.add(ProjectDetail(it.value)) },
                            onOpenSection = { backStack.add(Section(it.key)) }
                        )
                    }
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
                    entry<Section> { key ->
                        if (key.key == "architecture") {
                            ArchitectureScreen(
                                state = architectureState,
                                evaluation = architectureEvaluation,
                                projects = projects,
                                onChange = architectureViewModel::update,
                                onChooseProject = architectureViewModel::selectProject,
                                onCreateProject = architectureViewModel::createProject,
                                onSave = architectureViewModel::save,
                                onEvaluate = architectureViewModel::evaluate,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        } else if (key.key == "mechanical") {
                            MechanicalScreen(
                                state = mechanicalState,
                                projects = projects,
                                onChooseProject = mechanicalViewModel::selectProject,
                                onCreateProject = mechanicalViewModel::createProject,
                                onChange = mechanicalViewModel::update,
                                onSave = mechanicalViewModel::save,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        } else {
                            SectionPlaceholderScreen(HomeSection.fromKey(key.key))
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MechanicalIntent(openMechanical: Boolean, onOpen: () -> Unit) {
    var handled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(openMechanical) {
        if (openMechanical && !handled) {
            onOpen()
            handled = true
        }
    }
}

@Composable
private fun ArchitectureIntent(openArchitecture: Boolean, onOpen: () -> Unit) {
    var handled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(openArchitecture) {
        if (openArchitecture && !handled) {
            onOpen()
            handled = true
        }
    }
}
