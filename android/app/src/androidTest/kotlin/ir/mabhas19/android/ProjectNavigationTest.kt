package ir.mabhas19.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.mabhas19.core.designsystem.MabhasTheme
import ir.mabhas19.core.model.ThemePreference
import ir.mabhas19.core.testing.FakeProjectRepository
import ir.mabhas19.feature.projects.ProjectsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun createProjectNavigatesToDetailAndBackToPersistedList() {
        val repository = FakeProjectRepository()
        val viewModel = ProjectsViewModel(repository, SavedStateHandle())
        compose.setContent {
            MabhasTheme {
                MabhasNavigation(viewModel, ThemePreference.SYSTEM, false, {})
            }
        }
        compose.onNodeWithTag("new_project").performClick()
        compose.onNodeWithTag("project_title").performTextInput("پرونده آزمایشی")
        compose.onNodeWithTag("save_project").performClick()
        // Physical devices can take longer than the default CI emulator to settle after IME input.
        compose.waitUntil(15_000) { repository.projects.value.size == 1 }
        compose.onNodeWithTag("project_detail").assertIsDisplayed()
        compose.onNodeWithText("پرونده آزمایشی").assertIsDisplayed()
        compose.onNodeWithTag("navigate_back").performClick()
        compose.onNodeWithTag("project_test-1").assertIsDisplayed()
    }
}
