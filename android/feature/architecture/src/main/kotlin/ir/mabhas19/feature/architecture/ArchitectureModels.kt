package ir.mabhas19.feature.architecture

import kotlinx.serialization.Serializable

@Serializable
data class FacadeDraft(val area: String = "", val glazing: String = "", val absent: Boolean = false)

/** F03/F20 local input. Strings retain incomplete edits; only canonical parsed values enter rules. */
@Serializable
data class ArchitectureDraft(
    val schemaVersion: Int = 1,
    val edition: String = "1399-v4",
    val cityId: String = "",
    val province: String = "",
    val occupancyId: String = "",
    val area: String = "",
    val floors: String = "",
    val method: String = "",
    val year: String = "1403",
    val minGrade: String = "EC",
    val grade: String = "",
    val solarArea: String = "",
    val obstacleAngle: String = "",
    val solar: String = "low_or_none",
    val continuity: String = "continuous",
    val inertia: String = "",
    val inertiaDocumented: Boolean = false,
    val ministryChecked: Boolean = false,
    val facades: Map<String, FacadeDraft> = listOf("north", "south", "east", "west")
        .associateWith { FacadeDraft() },
    val sections: Map<String, Map<String, String>> = emptyMap()
) {
    companion object {
        /** Explicit synthetic context seen in the reviewed architecture screenshot; no approvals. */
        fun sample() = ArchitectureDraft(
            cityId = "c181",
            occupancyId = "o01",
            area = "1000",
            floors = "4"
        )
    }
}

/** Client-source screening only; this is never a compliance certificate or full calculation. */
data class Screening(
    val pending: List<String>,
    val issues: List<String>,
    val group: Int?,
    val solarEligible: Boolean?,
    val methodAllowed: Boolean?
)
