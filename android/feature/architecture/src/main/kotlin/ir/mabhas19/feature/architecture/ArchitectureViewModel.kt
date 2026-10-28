package ir.mabhas19.feature.architecture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mabhas19.core.domain.DraftConflictException
import ir.mabhas19.core.domain.DraftRepository
import ir.mabhas19.core.domain.ProjectRepository
import ir.mabhas19.core.model.ProjectContext
import ir.mabhas19.core.model.ProjectId
import ir.mabhas19.core.model.SectionDraft
import ir.mabhas19.core.network.ArchitectureEvaluationApi
import ir.mabhas19.core.network.ArchitectureEvaluationApiFactory
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

enum class ArchitectureError { STORAGE, CONFLICT, UNSUPPORTED_DRAFT, INVALID_TITLE }

data class ArchitectureState(
    val projectId: ProjectId? = null,
    val projectTitle: String = "",
    val draft: ArchitectureDraft = ArchitectureDraft(),
    val revision: Long = 0,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: ArchitectureError? = null,
    val editable: Boolean = false
)

/** Native local workspace schema, intentionally distinct from the server's wire payload. */
@OptIn(FlowPreview::class)
@HiltViewModel
class ArchitectureViewModel @Inject constructor(
    private val projects: ProjectRepository,
    private val drafts: DraftRepository,
    private val savedState: SavedStateHandle,
    private val api: ArchitectureEvaluationApi = ArchitectureEvaluationApiFactory.create()
) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow(ArchitectureState())
    val state = mutableState.asStateFlow()
    private val mutableEvaluation = MutableStateFlow(ArchitectureEvaluation())
    val evaluation = mutableEvaluation.asStateFlow()
    private val mutex = Mutex()
    private var transitioning = false
    private var draftLoaded = false
    private var evaluationGeneration = 0L
    private val edits = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        viewModelScope.launch { edits.debounce(600).collect { persist() } }
        savedState.get<String>(PROJECT_KEY)?.let { selectProject(ProjectId(it)) }
    }

    fun selectProject(id: ProjectId) {
        if (mutableState.value.projectId == id && draftLoaded) return
        transition {
            if (persistLocked()) loadLocked(id)
        }
    }

    fun createProject(title: String, sample: Boolean = false) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank() || cleanTitle.length > 120) {
            mutableState.value = mutableState.value.copy(error = ArchitectureError.INVALID_TITLE)
            return
        }
        transition {
            if (!persistLocked()) return@transition
            try {
                val id = projects.create(cleanTitle)
                mutableState.value = ArchitectureState(
                    projectId = id,
                    projectTitle = cleanTitle,
                    draft = if (sample) educationalSample() else ArchitectureDraft(),
                    editable = true
                )
                draftLoaded = true
                savedState[PROJECT_KEY] = id.value
                persistLocked()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value =
                    mutableState.value.copy(loading = false, error = ArchitectureError.STORAGE)
            }
        }
    }

    /** Freeze input before waiting for an existing save, so switching cannot drop a newer edit. */
    private fun transition(action: suspend () -> Unit) {
        if (transitioning) return
        transitioning = true
        mutableState.value = mutableState.value.copy(loading = true)
        evaluationGeneration++
        mutableEvaluation.value = ArchitectureEvaluation()
        viewModelScope.launch {
            mutex.withLock {
                try {
                    action()
                } finally {
                    transitioning = false
                    mutableState.value = mutableState.value.copy(loading = false)
                }
            }
        }
    }

    fun update(draft: ArchitectureDraft) {
        val current = mutableState.value
        if (!current.editable || transitioning || current.loading) return
        val projectId = current.projectId ?: return
        val next = ArchitectureRules.normalize(draft)
        mutableState.value = current.copy(draft = next, saved = false)
        mutableEvaluation.value = mutableEvaluation.value.copy(stale = true)
        savedState[RECOVERY_PROJECT_KEY] = projectId.value
        savedState[RECOVERY_PAYLOAD_KEY] = json.encodeToString(next)
        savedState[RECOVERY_REVISION_KEY] = current.revision
        edits.tryEmit(Unit)
    }

    fun save() {
        viewModelScope.launch { persist() }
    }

    @Suppress("ReturnCount") // Early guards keep invalid or unbound inputs away from the service.
    fun evaluate(section: String) {
        val current = mutableState.value
        if (!current.editable || transitioning || current.loading) return
        if (current.projectId == null || mutableEvaluation.value.calculating) return
        val generation = ++evaluationGeneration
        val body = ArchitecturePayloads.forSection(section, current.draft)
        if (body == null) {
            mutableEvaluation.value = ArchitectureEvaluation(
                section = section,
                error = EvaluationError.INPUT,
                validation = ArchitecturePayloads.validation(section, current.draft)
            )
            return
        }
        mutableEvaluation.value = ArchitectureEvaluation(section = section, calculating = true)
        viewModelScope.launch {
            val result = try {
                val response = api.evaluateSection(section, body)
                if ((response["ok"] as? JsonPrimitive)?.booleanOrNull == true) {
                    ArchitectureEvaluation(section = section, result = response)
                } else {
                    ArchitectureEvaluation(section = section, error = EvaluationError.SERVICE)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                ArchitectureEvaluation(section = section, error = EvaluationError.NETWORK)
            }
            // A delayed reply can never attach to another project or silently become current.
            if (generation == evaluationGeneration &&
                mutableState.value.projectId == current.projectId
            ) {
                mutableEvaluation.value =
                    result.copy(stale = mutableState.value.draft != current.draft)
            }
        }
    }

    private suspend fun persist() = mutex.withLock { persistLocked() }

    @Suppress("ReturnCount") // Preserve distinct no-op, read-only and failed-save outcomes.
    private suspend fun persistLocked(): Boolean {
        val current = mutableState.value
        val id = current.projectId
        if (id == null || !draftLoaded || current.saved) return true
        val readonly = current.error in setOf(
            ArchitectureError.UNSUPPORTED_DRAFT,
            ArchitectureError.CONFLICT
        )
        if (readonly || (current.loading && !transitioning)) {
            return false
        }
        mutableState.value = current.copy(saving = true)
        return try {
            val stored = drafts.saveDraft(
                SectionDraft(id, SECTION, json.encodeToString(current.draft), current.revision)
            )
            mutableState.value = mutableState.value.copy(revision = stored.localRevision)
            if (savedState.get<String>(RECOVERY_PROJECT_KEY) == id.value) {
                savedState[RECOVERY_REVISION_KEY] = stored.localRevision
            }
            projects.saveArchitectureContext(id, current.draft)
            val latest = mutableState.value
            mutableState.value = latest.copy(
                revision = stored.localRevision,
                saving = false,
                saved = latest.draft == current.draft,
                error = null
            )
            if (latest.draft == current.draft &&
                savedState.get<String>(RECOVERY_PROJECT_KEY) == id.value
            ) {
                savedState.remove<String>(RECOVERY_PROJECT_KEY)
                savedState.remove<String>(RECOVERY_PAYLOAD_KEY)
                savedState.remove<Long>(RECOVERY_REVISION_KEY)
            }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (_: DraftConflictException) {
            mutableState.value =
                mutableState.value.copy(saving = false, error = ArchitectureError.CONFLICT)
            false
        } catch (_: Exception) {
            // Keep the editor and revision; the user can retry without losing input.
            mutableState.value =
                mutableState.value.copy(saving = false, error = ArchitectureError.STORAGE)
            false
        }
    }

    private suspend fun loadLocked(id: ProjectId) {
        mutableEvaluation.value = ArchitectureEvaluation()
        mutableState.value = ArchitectureState(loading = true)
        draftLoaded = false
        try {
            val project = projects.observeProject(id).first() ?: error("Missing project")
            val stored = drafts.observeDraft(id, SECTION).first()
            // Validate persisted schema first: an older recovery must never hide a future schema.
            val storedDraft = stored?.payloadJson?.let(::decodeDraft)
            val recovery = if (savedState.get<String>(RECOVERY_PROJECT_KEY) == id.value) {
                savedState.get<String>(RECOVERY_PAYLOAD_KEY)
            } else {
                null
            }
            val revision = stored?.localRevision ?: 0
            val recoveryRevision = savedState.get<Long>(RECOVERY_REVISION_KEY)
            val conflict = recovery != null && recoveryRevision != revision
            val draft = recovery?.let(::decodeDraft) ?: storedDraft ?: ArchitectureDraft()
            mutableState.value = ArchitectureState(
                id,
                project.title,
                draft,
                if (conflict) recoveryRevision ?: 0 else revision,
                saved = recovery == null,
                error = if (conflict) ArchitectureError.CONFLICT else null,
                editable = true
            )
            draftLoaded = true
            savedState[PROJECT_KEY] = id.value
        } catch (error: CancellationException) {
            throw error
        } catch (_: SerializationException) {
            mutableState.value =
                ArchitectureState(projectId = id, error = ArchitectureError.UNSUPPORTED_DRAFT)
        } catch (_: UnsupportedDraftException) {
            mutableState.value =
                ArchitectureState(projectId = id, error = ArchitectureError.UNSUPPORTED_DRAFT)
        } catch (_: Exception) {
            mutableState.value =
                ArchitectureState(projectId = id, error = ArchitectureError.STORAGE)
        }
    }

    private fun decodeDraft(payload: String): ArchitectureDraft {
        val draft = json.decodeFromString<ArchitectureDraft>(payload)
        if (draft.schemaVersion != 1 ||
            draft.edition != "1399-v4"
        ) {
            throw UnsupportedDraftException()
        }
        return draft
    }

    private class UnsupportedDraftException : IllegalArgumentException()

    companion object {
        const val SECTION = "android_architecture_v1"
        private const val PROJECT_KEY = "architectureProjectId"
        private const val RECOVERY_PROJECT_KEY = "architectureRecoveryProject"
        private const val RECOVERY_PAYLOAD_KEY = "architectureRecoveryPayload"
        private const val RECOVERY_REVISION_KEY = "architectureRecoveryRevision"
    }
}

/** Explicit synthetic entries only. Evidence remains unconfirmed and numerical results unapproved. */
private suspend fun ProjectRepository.saveArchitectureContext(
    id: ProjectId,
    draft: ArchitectureDraft
) {
    val area = ArchitectureRules.number(draft.area)?.takeIf { it > 0 }
    val floors = ArchitectureRules.number(draft.floors)
        ?.takeIf { it >= 1 && it <= Int.MAX_VALUE && it % 1.0 == 0.0 }?.toInt()
    saveContext(
        ProjectContext(
            id,
            draft.cityId.takeIf { it.isNotBlank() },
            draft.occupancyId.takeIf { it.isNotBlank() },
            area,
            floors
        )
    )
}

private fun educationalSample(): ArchitectureDraft = ArchitectureDraft.sample().copy(
    province = "البرز",
    method = "prescriptive",
    grade = "EC",
    solarArea = "160",
    obstacleAngle = "18",
    solar = "high",
    inertia = "medium",
    facades = mapOf(
        "north" to FacadeDraft("240", "48"),
        "south" to FacadeDraft("240", "72"),
        "east" to FacadeDraft("180", "36"),
        "west" to FacadeDraft("180", "36")
    ),
    sections = listOf(
        "occupancy", "walls", "roof_floor", "glazing", "shading", "daylight", "airtightness",
        "thermal_bridges", "boundaries"
    ).associateWith(::envelopeSample)
)
