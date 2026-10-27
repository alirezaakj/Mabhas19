package ir.mabhas19.core.model

@JvmInline
value class ProjectId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

enum class Edition(val wireValue: String) {
    FOURTH("1399-v4")
}

data class Project(
    val id: ProjectId,
    val title: String,
    val edition: Edition = Edition.FOURTH,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    init {
        require(title.isNotBlank() && title.length <= MAX_TITLE_LENGTH)
    }

    companion object {
        const val MAX_TITLE_LENGTH = 120
    }
}

data class ProjectContext(
    val projectId: ProjectId,
    val cityId: String? = null,
    val occupancyId: String? = null,
    val conditionedAreaM2: Double? = null,
    val floorCount: Int? = null
) {
    init {
        require(conditionedAreaM2 == null || conditionedAreaM2.isFinite() && conditionedAreaM2 > 0)
        require(floorCount == null || floorCount > 0)
    }
}

data class SectionDraft(
    val projectId: ProjectId,
    val section: String,
    val payloadJson: String,
    val localRevision: Long = 0,
    val edition: Edition = Edition.FOURTH
) {
    init {
        require(section.isNotBlank())
        require(localRevision >= 0)
    }
}

enum class ThemePreference { SYSTEM, LIGHT, DARK }
