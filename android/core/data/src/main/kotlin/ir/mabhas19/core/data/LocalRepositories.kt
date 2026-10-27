package ir.mabhas19.core.data

import androidx.room.withTransaction
import ir.mabhas19.core.database.ContextEntity
import ir.mabhas19.core.database.DraftEntity
import ir.mabhas19.core.database.MabhasDatabase
import ir.mabhas19.core.database.ProjectEntity
import ir.mabhas19.core.domain.AppClock
import ir.mabhas19.core.domain.DraftConflictException
import ir.mabhas19.core.domain.DraftRepository
import ir.mabhas19.core.domain.IdGenerator
import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.model.Edition
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectContext
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.SectionDraft
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class LocalProjectRepository @Inject constructor(
    private val database: MabhasDatabase,
    private val clock: AppClock,
    private val ids: IdGenerator
) : ProjectRepository {
    private val dao get() = database.projects()
    override fun observeProjects() = dao.observeProjects().map { rows -> rows.map { it.toModel() } }
    override fun observeProject(id: ProjectId) = dao.observeProject(id.value).map { it?.toModel() }
    override fun observeContext(id: ProjectId) = dao.observeContext(id.value).map { row ->
        row?.let {
            ProjectContext(id, it.cityId, it.occupancyId, it.conditionedAreaM2, it.floorCount)
        }
    }

    override suspend fun create(title: String): ProjectId {
        val now = clock.nowMillis()
        val project =
            Project(
                ProjectId(ids.nextId()),
                title.trim(),
                createdAtMillis = now,
                updatedAtMillis = now
            )
        dao.upsertProject(
            ProjectEntity(project.id.value, project.title, project.edition.wireValue, now, now)
        )
        return project.id
    }

    override suspend fun saveContext(context: ProjectContext) {
        dao.upsertContext(
            ContextEntity(
                context.projectId.value,
                context.cityId,
                context.occupancyId,
                context.conditionedAreaM2,
                context.floorCount
            )
        )
    }
}

class LocalDraftRepository @Inject constructor(private val database: MabhasDatabase) :
    DraftRepository {
    override fun observeDraft(projectId: ProjectId, section: String) =
        database.projects().observeDraft(
            projectId.value,
            Edition.FOURTH.wireValue,
            section
        ).map { row ->
            row?.let { SectionDraft(projectId, it.section, it.payloadJson, it.localRevision) }
        }

    override suspend fun saveDraft(draft: SectionDraft): SectionDraft = database.withTransaction {
        val dao = database.projects()
        val current = dao.getDraft(draft.projectId.value, draft.edition.wireValue, draft.section)
        if ((current?.localRevision ?: 0) != draft.localRevision) throw DraftConflictException()
        val saved = draft.copy(localRevision = draft.localRevision + 1)
        dao.upsertDraft(
            DraftEntity(
                saved.projectId.value,
                saved.edition.wireValue,
                saved.section,
                saved.payloadJson,
                saved.localRevision
            )
        )
        saved
    }
}

private fun ProjectEntity.toModel(): Project {
    require(edition == Edition.FOURTH.wireValue) { "Unsupported project edition" }
    return Project(ProjectId(id), title, Edition.FOURTH, createdAtMillis, updatedAtMillis)
}
