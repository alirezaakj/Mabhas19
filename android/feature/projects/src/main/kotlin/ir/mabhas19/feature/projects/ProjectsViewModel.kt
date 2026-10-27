package ir.mabhas19.feature.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProjectError { INVALID_TITLE, STORAGE }

data class EditorState(
    val saving: Boolean = false,
    val error: ProjectError? = null,
    val createdId: ProjectId? = null
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val mutableEditor = MutableStateFlow(EditorState())
    val editor = mutableEditor.asStateFlow()
    val title: StateFlow<String> = savedState.getStateFlow("projectTitle", "")
    private val mutableLoadFailed = MutableStateFlow(false)
    val loadFailed = mutableLoadFailed.asStateFlow()
    val projects = repository.observeProjects()
        .catch { mutableLoadFailed.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeProject(id: ProjectId) = repository.observeProject(id)

    fun updateTitle(value: String) {
        savedState["projectTitle"] = value
        mutableEditor.value = mutableEditor.value.copy(error = null)
    }

    fun acknowledgeNavigation() {
        savedState["projectTitle"] = ""
        mutableEditor.value = EditorState()
    }

    fun createProject() {
        if (mutableEditor.value.saving || mutableEditor.value.createdId != null) return
        val name = title.value.trim()
        if (name.isEmpty() || name.length > Project.MAX_TITLE_LENGTH) {
            mutableEditor.value = EditorState(error = ProjectError.INVALID_TITLE)
            return
        }
        mutableEditor.value = EditorState(saving = true)
        viewModelScope.launch {
            try {
                mutableEditor.value = EditorState(createdId = repository.create(name))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableEditor.value = EditorState(error = ProjectError.STORAGE)
            }
        }
    }
}
