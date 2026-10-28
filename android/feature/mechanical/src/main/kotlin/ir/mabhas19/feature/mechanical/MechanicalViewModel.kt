package ir.mabhas19.feature.mechanical

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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

enum class MechanicalError { STORAGE, CONFLICT, UNSUPPORTED_DRAFT, INVALID_TITLE }

private const val RECOVERY_PROJECT_KEY = "mechanicalRecoveryProject"
private const val RECOVERY_PAYLOAD_KEY = "mechanicalRecoveryPayload"

data class MechanicalState(
    val projectId: ProjectId? = null,
    val projectTitle: String = "",
    val context: ProjectContext? = null,
    val draft: MechanicalDraft = MechanicalDraft(),
    val revision: Long = 0,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: MechanicalError? = null,
    val editable: Boolean = false
)

/** Stores a mechanical projection inside the shared local arch_chapter45 draft (F15). */
@OptIn(FlowPreview::class)
@HiltViewModel
class MechanicalViewModel @Inject constructor(
    private val projects: ProjectRepository,
    private val drafts: DraftRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutableState = MutableStateFlow(MechanicalState())
    val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private var transitioning = false
    private var draftLoaded = false
    private var envelope = emptyEnvelope()
    private val edits = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        viewModelScope.launch {
            edits.debounce(600).collect { mutex.withLock { persistLocked() } }
        }
        savedState.get<String>(PROJECT_KEY)?.let { selectProject(ProjectId(it)) }
    }

    fun selectProject(id: ProjectId) {
        if (mutableState.value.projectId == id && draftLoaded) return
        transition {
            if (persistLocked()) loadLocked(id)
        }
    }

    fun createProject(title: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank() || cleanTitle.length > 120) {
            mutableState.value = mutableState.value.copy(error = MechanicalError.INVALID_TITLE)
            return
        }
        transition {
            if (!persistLocked()) return@transition
            try {
                val id = projects.create(cleanTitle)
                envelope = emptyEnvelope()
                mutableState.value = MechanicalState(
                    projectId = id,
                    projectTitle = cleanTitle,
                    editable = true
                )
                draftLoaded = true
                savedState[PROJECT_KEY] = id.value
                persistLocked()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    error = MechanicalError.STORAGE
                )
            }
        }
    }

    private fun transition(action: suspend () -> Unit) {
        if (transitioning) return
        transitioning = true
        mutableState.value = mutableState.value.copy(loading = true)
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

    fun update(draft: MechanicalDraft) {
        val current = mutableState.value
        val projectId = current.projectId ?: return
        val isBusy = transitioning || current.loading
        val hasSupportedSchema = draft.schemaVersion == 1 && draft.edition == EDITION
        if (!current.editable || isBusy || !hasSupportedSchema) return
        mutableState.value = current.copy(draft = draft, saved = false)
        savedState[RECOVERY_PROJECT_KEY] = projectId.value
        savedState[RECOVERY_PAYLOAD_KEY] = json.encodeToString(draft)
        savedState[RECOVERY_REVISION_KEY] = current.revision
        edits.tryEmit(Unit)
    }

    fun save() {
        viewModelScope.launch { mutex.withLock { persistLocked() } }
    }

    private suspend fun persistLocked(): Boolean {
        val current = mutableState.value
        val id = current.projectId
        if (id == null || !draftLoaded || current.saved) return true
        return if (
            current.error in setOf(MechanicalError.UNSUPPORTED_DRAFT, MechanicalError.CONFLICT)
        ) {
            false
        } else {
            mutableState.value = current.copy(saving = true)
            try {
                val stored = drafts.saveDraft(
                    SectionDraft(
                        id,
                        SECTION,
                        encodeEnvelope(envelope, current.draft),
                        current.revision
                    )
                )
                mutableState.value = mutableState.value.copy(
                    revision = stored.localRevision,
                    saving = false,
                    saved = true,
                    error = null
                )
                savedState.remove<String>(RECOVERY_PROJECT_KEY)
                savedState.remove<String>(RECOVERY_PAYLOAD_KEY)
                savedState.remove<Long>(RECOVERY_REVISION_KEY)
                true
            } catch (error: CancellationException) {
                throw error
            } catch (_: DraftConflictException) {
                mutableState.value = mutableState.value.copy(
                    saving = false,
                    error = MechanicalError.CONFLICT
                )
                false
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    saving = false,
                    error = MechanicalError.STORAGE
                )
                false
            }
        }
    }

    private suspend fun loadLocked(id: ProjectId) {
        mutableState.value = MechanicalState(loading = true)
        draftLoaded = false
        try {
            val project = projects.observeProject(id).first() ?: error("Missing project")
            val context = projects.observeContext(id).first()
            val stored = drafts.observeDraft(id, SECTION).first()
            val loaded = stored?.payloadJson?.let(::decodeEnvelope)
            envelope = loaded?.first ?: emptyEnvelope()
            val recovery = recoveryPayload(savedState, id)
            val storedRevision = stored?.localRevision ?: 0L
            val recoveryRevision = savedState.get<Long>(RECOVERY_REVISION_KEY)
            val conflict = recovery != null && recoveryRevision != storedRevision
            val draft = recovery?.let(::decodeDraft)
                ?: loaded?.second
                ?: MechanicalDraft()
            mutableState.value = MechanicalState(
                projectId = id,
                projectTitle = project.title,
                context = context,
                draft = draft,
                revision = if (conflict) recoveryRevision ?: 0L else storedRevision,
                saved = recovery == null,
                error = if (conflict) MechanicalError.CONFLICT else null,
                editable = true
            )
            draftLoaded = true
            savedState[PROJECT_KEY] = id.value
        } catch (error: CancellationException) {
            throw error
        } catch (_: SerializationException) {
            mutableState.value = MechanicalState(
                projectId = id,
                error = MechanicalError.UNSUPPORTED_DRAFT
            )
        } catch (_: UnsupportedMechanicalDraft) {
            mutableState.value = MechanicalState(
                projectId = id,
                error = MechanicalError.UNSUPPORTED_DRAFT
            )
        } catch (_: Exception) {
            mutableState.value = MechanicalState(projectId = id, error = MechanicalError.STORAGE)
        }
    }

    private fun decodeEnvelope(payload: String): Pair<JsonObject, MechanicalDraft> {
        val root = json.decodeFromString<JsonObject>(payload)
        if (root["nativeWorkspaceSchema"]?.toString() != "1" ||
            root["edition"]?.toString()?.trim('"') != EDITION
        ) {
            throw UnsupportedMechanicalDraft()
        }
        val projections = root["projections"] as? JsonObject ?: JsonObject(emptyMap())
        val draft = projections[PROJECTION_KEY]?.let {
            json.decodeFromJsonElement<MechanicalDraft>(it)
        } ?: MechanicalDraft()
        if (draft.schemaVersion != 1 || draft.edition != EDITION) {
            throw UnsupportedMechanicalDraft()
        }
        return root to draft
    }

    private fun decodeDraft(payload: String): MechanicalDraft {
        val draft = json.decodeFromString<MechanicalDraft>(payload)
        if (draft.schemaVersion != 1 || draft.edition != EDITION) {
            throw UnsupportedMechanicalDraft()
        }
        return draft
    }

    private fun encodeEnvelope(root: JsonObject, draft: MechanicalDraft): String {
        val oldProjections = root["projections"] as? JsonObject ?: JsonObject(emptyMap())
        val updated = buildJsonObject {
            root.forEach { (key, value) ->
                if (key != "projections") put(key, value)
            }
            put("nativeWorkspaceSchema", JsonPrimitive(1))
            put("edition", JsonPrimitive(EDITION))
            put(
                "projections",
                buildJsonObject {
                    oldProjections.forEach { (key, value) ->
                        if (key != PROJECTION_KEY) put(key, value)
                    }
                    put(PROJECTION_KEY, json.encodeToJsonElement(draft))
                }
            )
        }
        return json.encodeToString(updated)
    }

    private class UnsupportedMechanicalDraft : IllegalArgumentException()

    companion object {
        const val SECTION = "arch_chapter45"
        private const val EDITION = "1399-v4"
        private const val PROJECTION_KEY = "mechanical"
        private const val PROJECT_KEY = "mechanicalProjectId"
        private const val RECOVERY_REVISION_KEY = "mechanicalRecoveryRevision"

        private fun emptyEnvelope() = buildJsonObject {
            put("nativeWorkspaceSchema", JsonPrimitive(1))
            put("edition", JsonPrimitive(EDITION))
            put("projections", buildJsonObject {})
        }
    }
}

private fun recoveryPayload(savedState: SavedStateHandle, id: ProjectId): String? =
    if (savedState.get<String>(RECOVERY_PROJECT_KEY) == id.value) {
        savedState.get<String>(RECOVERY_PAYLOAD_KEY)
    } else {
        null
    }
