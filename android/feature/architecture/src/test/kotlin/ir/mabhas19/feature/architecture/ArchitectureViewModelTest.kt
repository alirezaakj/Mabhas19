package ir.mabhas19.feature.architecture

import androidx.lifecycle.SavedStateHandle
import ir.mabhas19.core.domain.DraftConflictException
import ir.mabhas19.core.domain.DraftRepository
import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.model.ProjectContext
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.SectionDraft
import ir.mabhas19.core.network.ArchitectureEvaluationApi
import ir.mabhas19.core.testing.FakeProjectRepository
import ir.mabhas19.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class ArchitectureViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun savedProjectRestoresItsOwnSectionsAndCanonicalContext() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.createProject("first")
        advanceUntilIdle()
        val first = requireNotNull(model.state.value.projectId)
        model.update(
            model.state.value.draft.copy(
                cityId = "c181",
                occupancyId = "o01",
                area = "۱۲۰۰٫۵",
                floors = "۴",
                sections = mapOf("walls" to mapOf("wall.0.name" to "A"))
            )
        )
        advanceUntilIdle()
        assertTrue(model.state.value.saved)
        assertEquals(1200.5, projects.observeContext(first).first()?.conditionedAreaM2)
        model.createProject("second")
        advanceUntilIdle()
        assertNotEquals(first, model.state.value.projectId)
        assertTrue(model.state.value.draft.sections.isEmpty())
        model.selectProject(first)
        advanceUntilIdle()
        assertEquals("A", model.state.value.draft.sections["walls"]?.get("wall.0.name"))
        assertEquals("۱۲۰۰٫۵", model.state.value.draft.area)
    }

    @Test fun failedSavePreservesInputAndPreventsProjectSwitch() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.createProject("first")
        advanceUntilIdle()
        val first = model.state.value.projectId
        val second = projects.create("second")
        drafts.failWrites = true
        model.update(model.state.value.draft.copy(area = "۱۲۰۰"))
        model.selectProject(second)
        advanceUntilIdle()
        assertEquals(first, model.state.value.projectId)
        assertEquals("۱۲۰۰", model.state.value.draft.area)
        assertFalse(model.state.value.saved)
        assertEquals(ArchitectureError.STORAGE, model.state.value.error)
    }

    @Test fun conflictDoesNotOverwriteAnotherDraftOrDiscardEditor() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.createProject("first")
        advanceUntilIdle()
        val id = requireNotNull(model.state.value.projectId)
        val stored = requireNotNull(drafts.observeDraft(id, ArchitectureViewModel.SECTION).first())
        drafts.saveDraft(stored.copy(payloadJson = "{\"area\":\"200\"}"))
        model.update(model.state.value.draft.copy(area = "300"))
        model.save()
        advanceUntilIdle()
        assertEquals(ArchitectureError.CONFLICT, model.state.value.error)
        assertEquals("300", model.state.value.draft.area)
        val conflicted = drafts.observeDraft(id, ArchitectureViewModel.SECTION).first()!!
        assertEquals("200", Json.decodeFromString<ArchitectureDraft>(conflicted.payloadJson).area)
    }

    @Test fun futureSchemaIsNeverOverwritten() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val id = projects.create("future")
        drafts.saveDraft(SectionDraft(id, ArchitectureViewModel.SECTION, "{\"schemaVersion\":2}"))
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.selectProject(id)
        advanceUntilIdle()
        assertEquals(ArchitectureError.UNSUPPORTED_DRAFT, model.state.value.error)
        model.update(ArchitectureDraft.sample())
        model.save()
        advanceUntilIdle()
        assertEquals(
            "{\"schemaVersion\":2}",
            drafts.observeDraft(id, ArchitectureViewModel.SECTION).first()?.payloadJson
        )
        val healthy = projects.create("healthy")
        model.selectProject(healthy)
        advanceUntilIdle()
        assertEquals(healthy, model.state.value.projectId)
        assertNull(model.state.value.error)
    }

    @Test fun switchingFlushesEditsMadeDuringBackgroundSaveAndFreezesTransitionInput() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.createProject("first")
        advanceUntilIdle()
        val first = requireNotNull(model.state.value.projectId)
        val second = projects.create("second")
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var held = false
        drafts.beforeWrite = {
            if (!held) {
                held = true
                started.complete(Unit)
                release.await()
            }
        }
        model.update(model.state.value.draft.copy(area = "100"))
        model.save()
        runCurrent()
        assertTrue(started.isCompleted)
        model.update(model.state.value.draft.copy(area = "200"))
        model.selectProject(second)
        assertTrue(model.state.value.loading)
        model.update(model.state.value.draft.copy(area = "300"))
        assertEquals("200", model.state.value.draft.area)
        release.complete(Unit)
        advanceUntilIdle()
        assertEquals(second, model.state.value.projectId)
        val stored = drafts.observeDraft(first, ArchitectureViewModel.SECTION).first()!!
        assertEquals("200", Json.decodeFromString<ArchitectureDraft>(stored.payloadJson).area)
    }

    @Test fun recoveredEditsKeepTheirBaseRevisionAndCannotOverwriteNewerStorage() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val id = projects.create("first")
        val base = drafts.saveDraft(
            SectionDraft(id, ArchitectureViewModel.SECTION, "{\"area\":\"100\"}")
        )
        drafts.saveDraft(base.copy(payloadJson = "{\"area\":\"200\"}"))
        val handle = recoveryHandle(id, base.localRevision, ArchitectureDraft(area = "300"))
        val model = ArchitectureViewModel(projects, drafts, handle, FakeEvaluationApi())
        advanceUntilIdle()
        assertEquals(ArchitectureError.CONFLICT, model.state.value.error)
        assertEquals("300", model.state.value.draft.area)
        model.save()
        advanceUntilIdle()
        val stored = drafts.observeDraft(id, ArchitectureViewModel.SECTION).first()!!
        assertEquals("200", Json.decodeFromString<ArchitectureDraft>(stored.payloadJson).area)
        assertEquals(2L, stored.localRevision)
    }

    @Test fun exactRecoveredSnapshotSaveClearsRecoveryAndAdvancesRevision() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val id = projects.create("first")
        val base = drafts.saveDraft(SectionDraft(id, ArchitectureViewModel.SECTION, "{}"))
        val handle = recoveryHandle(id, base.localRevision, ArchitectureDraft(area = "300"))
        val model = ArchitectureViewModel(projects, drafts, handle, FakeEvaluationApi())
        advanceUntilIdle()
        assertFalse(model.state.value.saved)
        model.save()
        advanceUntilIdle()
        assertTrue(model.state.value.saved)
        assertEquals(2L, model.state.value.revision)
        assertNull(handle.get<String>("architectureRecoveryPayload"))
        assertNull(handle.get<Long>("architectureRecoveryRevision"))
    }

    @Test fun recoveryCannotMaskFutureStoredSchema() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val id = projects.create("future")
        val stored = drafts.saveDraft(
            SectionDraft(id, ArchitectureViewModel.SECTION, "{\"schemaVersion\":2}")
        )
        val handle = recoveryHandle(id, stored.localRevision, ArchitectureDraft(area = "300"))
        val model = ArchitectureViewModel(projects, drafts, handle, FakeEvaluationApi())
        advanceUntilIdle()
        assertEquals(ArchitectureError.UNSUPPORTED_DRAFT, model.state.value.error)
        model.save()
        advanceUntilIdle()
        assertEquals(stored, drafts.observeDraft(id, ArchitectureViewModel.SECTION).first())
    }

    @Test fun temporaryReadFailureCanBeRetriedWithoutWritingAnEmptyDraft() = runTest {
        val projects = FakeProjectRepository()
        val drafts = MemoryDrafts()
        val id = projects.create("first")
        val stored = drafts.saveDraft(
            SectionDraft(id, ArchitectureViewModel.SECTION, "{\"area\":\"100\"}")
        )
        drafts.failReads = true
        val model = ArchitectureViewModel(projects, drafts, SavedStateHandle(), FakeEvaluationApi())
        model.selectProject(id)
        advanceUntilIdle()
        assertEquals(ArchitectureError.STORAGE, model.state.value.error)
        model.update(ArchitectureDraft(area = "300"))
        model.save()
        advanceUntilIdle()
        drafts.failReads = false
        assertEquals(stored, drafts.observeDraft(id, ArchitectureViewModel.SECTION).first())
        model.selectProject(id)
        advanceUntilIdle()
        assertEquals("100", model.state.value.draft.area)
        assertNull(model.state.value.error)
    }

    @Test fun contextWriteFailureRetainsAlreadyCommittedDraftRevisionForRetry() = runTest {
        val base = FakeProjectRepository()
        var failContext = true
        val projects = object : ProjectRepository by base {
            override suspend fun saveContext(context: ProjectContext) {
                check(!failContext)
                base.saveContext(context)
            }
        }
        val model =
            ArchitectureViewModel(projects, MemoryDrafts(), SavedStateHandle(), FakeEvaluationApi())
        model.createProject("first")
        advanceUntilIdle()
        assertEquals(ArchitectureError.STORAGE, model.state.value.error)
        assertEquals(1L, model.state.value.revision)
        failContext = false
        model.save()
        advanceUntilIdle()
        assertTrue(model.state.value.saved)
        assertEquals(2L, model.state.value.revision)
        assertNull(model.state.value.error)
    }

    @Test fun oldReplyCannotReattachAfterSwitchAwayAndBackOrOverwriteNewRequest() = runTest {
        val projects = FakeProjectRepository()
        val api = FakeEvaluationApi()
        val oldReply = CompletableDeferred<JsonObject>()
        val newReply = CompletableDeferred<JsonObject>()
        api.replies += oldReply
        api.replies += newReply
        val model = ArchitectureViewModel(projects, MemoryDrafts(), SavedStateHandle(), api)
        model.createProject("first")
        advanceUntilIdle()
        val first = requireNotNull(model.state.value.projectId)
        model.update(
            model.state.value.draft.copy(
                sections = mapOf("airtightness" to envelopeSample("airtightness"))
            )
        )
        model.evaluate("airtightness")
        runCurrent()
        assertTrue(model.evaluation.value.calculating)
        model.createProject("second")
        advanceUntilIdle()
        assertNull(model.evaluation.value.result)
        assertFalse(model.evaluation.value.calculating)
        model.selectProject(first)
        advanceUntilIdle()
        model.evaluate("airtightness")
        runCurrent()
        val fresh = JsonObject(
            mapOf(
                "ok" to JsonPrimitive(true),
                "message" to JsonPrimitive("fresh")
            )
        )
        newReply.complete(fresh)
        advanceUntilIdle()
        assertEquals(fresh, model.evaluation.value.result)
        oldReply.complete(
            JsonObject(
                mapOf(
                    "ok" to JsonPrimitive(true),
                    "message" to JsonPrimitive("old")
                )
            )
        )
        advanceUntilIdle()
        assertEquals(fresh, model.evaluation.value.result)
    }

    private fun recoveryHandle(id: ProjectId, revision: Long, draft: ArchitectureDraft) =
        SavedStateHandle(
            mapOf(
                "architectureProjectId" to id.value,
                "architectureRecoveryProject" to id.value,
                "architectureRecoveryPayload" to Json.encodeToString(draft),
                "architectureRecoveryRevision" to revision
            )
        )
}

internal class MemoryDrafts : DraftRepository {
    val rows = MutableStateFlow<Map<Pair<ProjectId, String>, SectionDraft>>(emptyMap())
    var failWrites = false
    var failReads = false
    var beforeWrite: suspend (SectionDraft) -> Unit = {}
    override fun observeDraft(projectId: ProjectId, section: String) = rows.map {
        check(!failReads)
        it[projectId to section]
    }
    override suspend fun saveDraft(draft: SectionDraft): SectionDraft {
        beforeWrite(draft)
        check(!failWrites)
        val key = draft.projectId to draft.section
        if ((rows.value[key]?.localRevision ?: 0) !=
            draft.localRevision
        ) {
            throw DraftConflictException()
        }
        val saved = draft.copy(localRevision = draft.localRevision + 1)
        rows.value = rows.value + (key to saved)
        return saved
    }
}

private class FakeEvaluationApi : ArchitectureEvaluationApi {
    val replies = ArrayDeque<CompletableDeferred<JsonObject>>()
    private suspend fun reply(): JsonObject = replies.removeFirst().await()
    override suspend fun walls(body: JsonObject) = reply()
    override suspend fun roofFloor(body: JsonObject) = reply()
    override suspend fun glazing(body: JsonObject) = reply()
    override suspend fun shading(body: JsonObject) = reply()
    override suspend fun daylight(body: JsonObject) = reply()
    override suspend fun airtightness(body: JsonObject) = reply()
    override suspend fun thermalBridges(body: JsonObject) = reply()
    override suspend fun boundaries(body: JsonObject) = reply()
}
