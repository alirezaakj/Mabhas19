package ir.mabhas19.feature.architecture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * F04–F14/F18 native draft editors, based on the captured fourth-edition public clients.
 * The caller owns persistence and scopes the supplied raw string map to its project and edition.
 * No result here asserts A04–A14 server execution or regulatory approval.
 */
@Composable
fun EnvelopeEditor(
    section: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    Column(
        modifier = Modifier.testTag("envelope_${section}_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (section) {
            "walls" -> AssemblyList(values, onChange, roof = false)
            "roof_floor" -> AssemblyList(values, onChange, roof = true)
            "glazing" -> GlazingList(values, onChange)
            "occupancy" -> OccupancyEditor(values, onChange)
            "shading" -> ShadingEditor(values, onChange)
            "daylight" -> DaylightEditor(values, onChange)
            "airtightness" -> AirSealingEditor(values, onChange)
            "thermal_bridges" -> BridgeEditor(values, onChange)
            "boundaries" -> BoundaryEditor(values, onChange)
            "dossier" -> EnvelopeSummary(values)
        }
        EnvelopeCard(R.string.envelope_review_title) {
            Text(
                stringResource(R.string.envelope_review_note),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EnvelopeSummary(values: Map<String, String>) {
    EnvelopeCard(R.string.envelope_dossier_title) {
        Text(stringResource(R.string.envelope_dossier_note))
        listOf(
            "wall" to R.string.envelope_wall_title,
            "roof" to R.string.envelope_roof_title,
            "opening" to R.string.envelope_glazing_title,
            "part" to R.string.envelope_part_title,
            "room" to R.string.envelope_room_title,
            "bridge" to R.string.envelope_bridge_title
        ).forEach { (prefix, title) ->
            Text(
                stringResource(
                    R.string.envelope_summary_count,
                    stringResource(title),
                    envelopeRows(values, prefix).size
                )
            )
            envelopeRows(values, prefix).forEach { index ->
                val name = values["$prefix.$index.name"].orEmpty()
                val id = values["$prefix.$index.id"].orEmpty()
                if (name.isNotBlank() || id.isNotBlank()) Text("$id  $name")
            }
        }
    }
}
