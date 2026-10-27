package ir.mabhas19.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAtMillis DESC, id ASC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeProject(id: String): Flow<ProjectEntity?>

    @Upsert suspend fun upsertProject(project: ProjectEntity)

    @Upsert suspend fun upsertContext(context: ContextEntity)

    @Query("SELECT * FROM project_contexts WHERE projectId = :id")
    fun observeContext(id: String): Flow<ContextEntity?>

    @Query(
        "SELECT * FROM section_drafts WHERE projectId = :id AND edition = :edition AND section = :section"
    )
    fun observeDraft(id: String, edition: String, section: String): Flow<DraftEntity?>

    @Query(
        "SELECT * FROM section_drafts WHERE projectId = :id AND edition = :edition AND section = :section"
    )
    suspend fun getDraft(id: String, edition: String, section: String): DraftEntity?

    @Upsert suspend fun upsertDraft(draft: DraftEntity)
}
