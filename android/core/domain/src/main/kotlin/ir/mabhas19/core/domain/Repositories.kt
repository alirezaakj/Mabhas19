package ir.mabhas19.core.domain

import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectContext
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.SectionDraft
import ir.mabhas19.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeProjects(): Flow<List<Project>>
    fun observeProject(id: ProjectId): Flow<Project?>
    suspend fun create(title: String): ProjectId
    suspend fun saveContext(context: ProjectContext)
    fun observeContext(id: ProjectId): Flow<ProjectContext?>
}

interface DraftRepository {
    fun observeDraft(projectId: ProjectId, section: String): Flow<SectionDraft?>

    /** Saves locally; a mismatched revision must preserve the existing draft and fail. */
    suspend fun saveDraft(draft: SectionDraft): SectionDraft
}

interface PreferencesRepository {
    val theme: Flow<ThemePreference>
    suspend fun setTheme(theme: ThemePreference)
}

fun interface AppClock {
    fun nowMillis(): Long
}
fun interface IdGenerator {
    fun nextId(): String
}

class DraftConflictException : IllegalStateException("Local draft revision changed")
