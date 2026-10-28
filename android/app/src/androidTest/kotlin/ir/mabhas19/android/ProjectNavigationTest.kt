package ir.mabhas19.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.mabhas19.core.designsystem.MabhasTheme
import ir.mabhas19.core.domain.DraftConflictException
import ir.mabhas19.core.domain.DraftRepository
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.SectionDraft
import ir.mabhas19.core.model.ThemePreference
import ir.mabhas19.core.testing.FakeProjectRepository
import ir.mabhas19.feature.architecture.ArchitectureViewModel
import ir.mabhas19.feature.projects.ProjectsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectNavigationTest {
    @get:Rule val compose = createComposeRule()

    private fun setContent(repository: FakeProjectRepository): ProjectsViewModel {
        val viewModel = ProjectsViewModel(repository, SavedStateHandle())
        val architecture = ArchitectureViewModel(repository, NavigationDrafts(), SavedStateHandle())
        compose.setContent {
            MabhasTheme {
                MabhasNavigation(viewModel, ThemePreference.SYSTEM, false, {}, architecture)
            }
        }
        return viewModel
    }

    @Test fun homeStartsTheAppAndCreatingProjectOpensDetail() {
        val repository = FakeProjectRepository()
        setContent(repository)
        compose.onNodeWithTag("home_screen").assertIsDisplayed()
        compose.onNodeWithTag("home_new_project").performClick()
        compose.onNodeWithTag("project_title").performTextInput("پرونده آزمایشی")
        compose.onNodeWithTag("save_project").performClick()
        // Physical devices can take longer than the default CI emulator to settle after IME input.
        compose.waitUntil(15_000) { repository.projects.value.size == 1 }
        compose.onNodeWithTag("project_detail").assertIsDisplayed()
        compose.onNodeWithText("پرونده آزمایشی").assertIsDisplayed()
        compose.onNodeWithTag("navigate_back").performClick()
        compose.onNodeWithTag("home_screen").assertIsDisplayed()
        compose.onNodeWithTag("home_screen").performScrollToNode(hasTestTag("home_open_projects"))
        compose.onNodeWithTag("home_open_projects").performClick()
        compose.onNodeWithTag("projects_screen").assertIsDisplayed()
        compose.onNodeWithTag("project_test-1").assertIsDisplayed()
    }

    @Test fun architectureOpensNativeProjectChooser() {
        setContent(FakeProjectRepository())
        compose.onNodeWithTag("home_screen")
            .performScrollToNode(hasTestTag("home_section_architecture"))
        compose.onNodeWithTag("home_section_architecture").performClick()
        compose.onNodeWithTag("architecture_screen").assertIsDisplayed()
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("architecture_create_sample"))
        compose.onNodeWithTag("architecture_create_sample").assertIsDisplayed()
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("architecture_back"))
        compose.onNodeWithTag("architecture_back").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test fun architectureSampleBindsProjectAndOpensEditableWalls() {
        val repository = FakeProjectRepository()
        setContent(repository)
        compose.onNodeWithTag("home_screen")
            .performScrollToNode(hasTestTag("home_section_architecture"))
        compose.onNodeWithTag("home_section_architecture").performClick()
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("architecture_create_sample"))
        compose.onNodeWithTag("architecture_create_sample").performClick()
        compose.waitUntil(15_000) { repository.projects.value.size == 1 }
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("architecture_area"))
        compose.onNodeWithTag("architecture_area").assertIsDisplayed()
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("architecture_step_1"))
        compose.onNodeWithTag("architecture_step_1").performClick()
        compose.onNodeWithTag("architecture_screen")
            .performScrollToNode(hasTestTag("envelope_walls_screen"))
        compose.onNodeWithTag("envelope_walls_screen").assertIsDisplayed()
    }
}

private class NavigationDrafts : DraftRepository {
    private val data = MutableStateFlow<Map<Pair<ProjectId, String>, SectionDraft>>(emptyMap())
    override fun observeDraft(projectId: ProjectId, section: String) = data.map {
        it[
            projectId to
                section
        ]
    }
    override suspend fun saveDraft(draft: SectionDraft): SectionDraft {
        val key = draft.projectId to draft.section
        if ((data.value[key]?.localRevision ?: 0) !=
            draft.localRevision
        ) {
            throw DraftConflictException()
        }
        val saved = draft.copy(localRevision = draft.localRevision + 1)
        data.value += key to saved
        return saved
    }
}
