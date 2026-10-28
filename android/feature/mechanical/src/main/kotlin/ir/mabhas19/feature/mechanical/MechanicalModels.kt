package ir.mabhas19.feature.mechanical

import kotlinx.serialization.Serializable

@Serializable
data class MechanicalAnswer(
    val status: String = STATUS_UNKNOWN,
    val evidence: String = "",
    val note: String = ""
) {
    companion object {
        const val STATUS_UNKNOWN = "unknown"
        const val STATUS_COMPLIANT = "compliant"
        const val STATUS_NONCOMPLIANT = "noncompliant"
        const val STATUS_NOT_APPLICABLE = "not_applicable"
        val statuses = setOf(
            STATUS_UNKNOWN,
            STATUS_COMPLIANT,
            STATUS_NONCOMPLIANT,
            STATUS_NOT_APPLICABLE
        )
    }
}

/** F15 Android projection. Values stay local and project-scoped; they are not A15 wire payloads. */
@Serializable
data class MechanicalDraft(
    val schemaVersion: Int = 1,
    val edition: String = "1399-v4",
    val answers: Map<String, MechanicalAnswer> = emptyMap(),
    val technicalValues: Map<String, String> = emptyMap()
)

@Serializable
data class MechanicalCatalog(
    val edition: String,
    val sourcePage: String,
    val sourceFiles: List<String>,
    val topics: List<MechanicalTopic>,
    val clauses: List<MechanicalClause>,
    val technicalFields: List<MechanicalTechnicalField>
)

@Serializable
data class MechanicalTopic(
    val id: String,
    val order: Int,
    val title: String,
    val desc: String,
    val headings: List<String>
)

@Serializable
data class MechanicalClause(
    val heading: String,
    val id: String,
    val code: String,
    val title: String,
    val summary: String,
    val topic: String,
    val subclauses: List<MechanicalSubclause>
)

@Serializable
data class MechanicalSubclause(val id: String, val text: String)

@Serializable
data class MechanicalTechnicalField(
    val id: String,
    val label: String,
    val type: String,
    val topic: String,
    val heading: String,
    val options: List<MechanicalOption> = emptyList()
)

@Serializable
data class MechanicalOption(val value: String, val label: String)
