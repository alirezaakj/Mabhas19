package ir.mabhas19.feature.architecture

import ir.mabhas19.core.network.ArchitectureEvaluationApi
import kotlinx.serialization.json.JsonObject

enum class EvaluationError { INPUT, NETWORK, SERVICE }

data class ArchitectureEvaluation(
    val section: String = "",
    val calculating: Boolean = false,
    val result: JsonObject? = null,
    val error: EvaluationError? = null,
    val validation: List<String> = emptyList(),
    val stale: Boolean = false
)

internal suspend fun ArchitectureEvaluationApi.evaluateSection(
    section: String,
    body: JsonObject
): JsonObject = when (section) {
    "walls" -> walls(body)
    "roof_floor" -> roofFloor(body)
    "glazing" -> glazing(body)
    "shading" -> shading(body)
    "daylight" -> daylight(body)
    "airtightness" -> airtightness(body)
    "thermal_bridges" -> thermalBridges(body)
    "boundaries" -> boundaries(body)
    else -> error("Unsupported section")
}
