package ir.mabhas19.feature.projects

import androidx.lifecycle.SavedStateHandle
import ir.mabhas19.core.testing.FakeProjectRepository
import ir.mabhas19.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsViewModelTest {
    @get:Rule val dispatchers = MainDispatcherRule()

    @Test fun `invalid title never writes a project`() = runTest {
        val repository = FakeProjectRepository()
        val vm = ProjectsViewModel(repository, SavedStateHandle())
        vm.createProject()
        advanceUntilIdle()
        assertEquals(ProjectError.INVALID_TITLE, vm.editor.value.error)
        assertEquals(0, repository.projects.value.size)
    }

    @Test fun `save completes once and restores input on storage failure`() = runTest {
        val repository = FakeProjectRepository()
        val state = SavedStateHandle()
        val vm = ProjectsViewModel(repository, state)
        vm.updateTitle("  پروژه آزمایشی  ")
        repository.failWrites = true
        vm.createProject()
        advanceUntilIdle()
        assertEquals(ProjectError.STORAGE, vm.editor.value.error)
        assertEquals("  پروژه آزمایشی  ", vm.title.value)
        assertNull(vm.editor.value.createdId)
        repository.failWrites = false
        vm.createProject()
        vm.createProject()
        advanceUntilIdle()
        assertEquals(1, repository.projects.value.size)
        assertEquals("پروژه آزمایشی", repository.projects.value.single().title)
        assertNotNull(vm.editor.value.createdId)
        vm.acknowledgeNavigation()
        assertEquals("", vm.title.value)
    }
}
