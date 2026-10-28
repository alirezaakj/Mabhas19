package ir.mabhas19.feature.architecture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun CalculationPanel(
    section: String,
    evaluation: ArchitectureEvaluation,
    onCalculate: () -> Unit
) {
    ArchitecturePanel(stringResource(R.string.architecture_calculation_title)) {
        Text(
            stringResource(R.string.architecture_calculation_help),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = onCalculate,
            enabled = !evaluation.calculating,
            modifier = Modifier.fillMaxWidth().testTag("architecture_calculate_$section")
        ) {
            Text(
                stringResource(
                    if (evaluation.calculating) {
                        R.string.architecture_calculating
                    } else {
                        R.string.architecture_calculate
                    }
                )
            )
        }
        if (evaluation.section == section) {
            if (evaluation.stale) {
                Text(
                    stringResource(R.string.architecture_calculation_stale),
                    color = MaterialTheme.colorScheme.error
                )
            }
            val errorMessage = evaluationErrorMessage(evaluation)
            if (errorMessage !=
                null
            ) {
                Text(stringResource(errorMessage), color = MaterialTheme.colorScheme.error)
            }
            evaluation.result?.let { result ->
                Text(stringResource(R.string.architecture_calculation_received))
                resultLines(result["result"] ?: result).take(60).forEach { line ->
                    ResultLine(line)
                }
                Text(
                    stringResource(R.string.architecture_not_certificate),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private data class CalculationLine(val key: String, val value: String, val context: String = "")

/** Selected user-facing results only; sessions, raw payloads and unknown flags are never displayed. */
private fun resultLines(
    element: JsonElement,
    context: String = "",
    depth: Int = 0
): List<CalculationLine> {
    if (depth > 6) return emptyList()
    return when (element) {
        is JsonArray -> element.take(30).flatMap { resultLines(it, context, depth + 1) }.take(60)
        is JsonObject -> objectResultLines(element, context, depth)
        else -> emptyList()
    }
}

private fun objectResultLines(
    element: JsonObject,
    context: String,
    depth: Int
): List<CalculationLine> {
    val name = (element["name"] as? JsonPrimitive)?.content
        ?: (element["label"] as? JsonPrimitive)?.content ?: context
    return element.flatMap { (key, value) ->
        when {
            value is JsonNull -> emptyList()
            value is JsonPrimitive && key in resultKeys ->
                listOf(CalculationLine(key, value.content.take(2000), name))
            key in messageKeys && value is JsonArray -> value.mapNotNull {
                (it as? JsonPrimitive)?.content?.let { message ->
                    CalculationLine("message", message.take(2000), name)
                }
            }
            value is JsonObject || value is JsonArray -> resultLines(value, name, depth + 1)
            else -> emptyList()
        }
    }.take(60)
}

private fun evaluationErrorMessage(evaluation: ArchitectureEvaluation): Int? =
    when (evaluation.error) {
        EvaluationError.INPUT -> when (evaluation.validation.firstOrNull()?.substringBefore(':')) {
            "context_required" -> R.string.architecture_calculation_context
            "rows_required", "duplicate_id" -> R.string.architecture_calculation_rows
            "single_roof_required" -> R.string.architecture_calculation_roof
            else -> R.string.architecture_calculation_input
        }
        EvaluationError.NETWORK -> R.string.architecture_calculation_network
        EvaluationError.SERVICE -> R.string.architecture_calculation_service
        null -> null
    }

private val messageKeys = setOf("issues", "notes", "warnings")
private val resultKeys = setOf(
    "status", "overall", "actual_r", "calculated_r_for_review", "required_r", "R_total", "R_min",
    "U", "U_wm2k", "U_W_m2K", "U_glass_W_m2K", "U_frame_W_m2K", "actual", "limit",
    "calculated_value", "table", "message", "scope", "selected_metric",
    "minimum_R", "compared_R", "R_layers", "R_insulation_only", "U_inverse_layers",
    "required_percent", "coverage_percent", "daylit_area_m2", "numeric_complete",
    "numeric_threshold_met", "linear_h_w_k", "point_h_w_k", "surface_h_w_k",
    "combined_h_w_k", "indicative_equivalent_u_w_m2k", "basis", "comparison_basis",
    "explicit_bridge_calculation_required"
)

private val resultLabelIds = mapOf(
    R.string.architecture_result_status to listOf("status", "overall"),
    R.string.architecture_result_r to listOf(
        "actual_r",
        "calculated_r_for_review",
        "R_total",
        "R_layers",
        "compared_R"
    ),
    R.string.architecture_result_r_min to listOf("required_r", "R_min", "minimum_R"),
    R.string.architecture_result_insulation_r to listOf("R_insulation_only"),
    R.string.architecture_result_u to listOf(
        "U",
        "U_wm2k",
        "U_W_m2K",
        "U_glass_W_m2K",
        "U_frame_W_m2K",
        "U_inverse_layers"
    ),
    R.string.architecture_result_actual to listOf("actual", "calculated_value"),
    R.string.architecture_result_limit to listOf("limit"),
    R.string.architecture_result_table to listOf("table"),
    R.string.architecture_result_coverage to listOf("coverage_percent"),
    R.string.architecture_result_required_percent to listOf("required_percent"),
    R.string.architecture_result_daylit to listOf("daylit_area_m2"),
    R.string.architecture_result_numeric_complete to listOf("numeric_complete"),
    R.string.architecture_result_threshold to listOf("numeric_threshold_met"),
    R.string.architecture_result_linear_h to listOf("linear_h_w_k"),
    R.string.architecture_result_point_h to listOf("point_h_w_k"),
    R.string.architecture_result_surface_h to listOf("surface_h_w_k"),
    R.string.architecture_result_combined_h to listOf("combined_h_w_k"),
    R.string.architecture_result_equivalent_u to listOf("indicative_equivalent_u_w_m2k"),
    R.string.architecture_result_basis to listOf("basis", "comparison_basis"),
    R.string.architecture_result_bridge_required to listOf("explicit_bridge_calculation_required")
).flatMap { (label, keys) -> keys.map { it to label } }.toMap()

private val statusLabelIds = mapOf(
    R.string.architecture_result_pass to listOf(
        "pass",
        "component_pass",
        "numeric_requirements_met",
        "preliminary_eligible"
    ),
    R.string.architecture_result_fail to listOf(
        "fail",
        "forbidden",
        "component_fail",
        "numeric_failure",
        "not_eligible"
    ),
    R.string.architecture_result_na to listOf("not_applicable", "not_applicable_claimed")
).flatMap { (label, keys) -> keys.map { it to label } }.toMap()

@Composable
private fun ResultLine(line: CalculationLine) {
    val label = resultLabelIds[line.key]
    val value = when {
        line.key in setOf("status", "overall") -> stringResource(
            statusLabelIds[line.value] ?: R.string.architecture_result_review
        )
        line.value == "true" -> stringResource(R.string.architecture_result_yes)
        line.value == "false" -> stringResource(R.string.architecture_result_no)
        else -> line.value
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (line.context.isNotBlank()) {
            Text(line.context, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            if (label == null) value else "${stringResource(label)}: $value",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
