package ir.mabhas19.feature.architecture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun AssemblyList(
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit,
    roof: Boolean
) {
    val kind = if (roof) "roof" else "wall"
    val title = if (roof) R.string.envelope_roof_title else R.string.envelope_wall_title
    val rows = envelopeRows(values, kind)
    EnvelopeCard(title) {
        Text(stringResource(R.string.envelope_layers_order))
        Button(
            onClick = { onChange(envelopeAddRow(values, kind)) },
            modifier = Modifier.testTag(if (roof) "envelope_add_roof" else "envelope_add_wall")
        ) {
            Text(
                stringResource(if (roof) R.string.envelope_add_roof else R.string.envelope_add_wall)
            )
        }
        OutlinedButton(onClick = { onChange(envelopeSampleAssembly(values, roof)) }) {
            Text(
                stringResource(
                    if (roof) R.string.envelope_sample_roof else R.string.envelope_sample_wall
                )
            )
        }
        if (rows.isEmpty()) Text(stringResource(R.string.envelope_empty))
        if (!roof) {
            EnvelopeCheck(
                "group_documented",
                R.string.envelope_group_documented,
                values,
                onChange
            )
        }
    }
    rows.forEach { index ->
        val prefix = "$kind.$index"
        EnvelopeCard(title) {
            Text(values["$prefix.id"].orEmpty(), style = MaterialTheme.typography.titleSmall)
            EnvelopeFields(
                values,
                prefix,
                listOf(
                    field("id", R.string.envelope_code),
                    field("name", R.string.envelope_name),
                    choice("adjacency", R.string.envelope_adjacency, EnvelopeChoices.adjacency),
                    choice(
                        "insulation",
                        R.string.envelope_insulation,
                        if (roof) EnvelopeChoices.roofInsulation else EnvelopeChoices.insulation
                    )
                ),
                onChange
            )
            if (roof) RoofGeometry(prefix, values, onChange)
            Text(
                stringResource(R.string.envelope_layers_order),
                style = MaterialTheme.typography.titleMedium
            )
            envelopeRows(values, "$prefix.layer").forEach { layer ->
                LayerEditor("$prefix.layer.$layer", values, onChange)
            }
            OutlinedButton(
                onClick = { onChange(envelopeAddRow(values, "$prefix.layer")) },
                modifier = Modifier.testTag("envelope_add_layer")
            ) {
                Text(stringResource(R.string.envelope_add_layer))
            }
            Text(
                stringResource(
                    R.string.envelope_layer_sum,
                    EnvelopeNumbers.format(EnvelopeNumbers.layersResistance(values, prefix))
                ),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                stringResource(R.string.envelope_layer_sum_note),
                style = MaterialTheme.typography.bodySmall
            )
            EnvelopeCheck(
                "$prefix.bridges_checked",
                R.string.envelope_bridge_check,
                values,
                onChange
            )
            EnvelopeCheck("$prefix.parallel_paths", R.string.envelope_parallel, values, onChange)
            EnvelopeCheck(
                "$prefix.assembly_verified",
                R.string.envelope_assembly_check,
                values,
                onChange
            )
            RemoveRow(prefix, values, onChange)
        }
    }
    if (!roof) WallChecklist(values, onChange)
}

@Composable
private fun WallChecklist(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_wall_checklist_title) {
        val labels = listOf(
            R.string.envelope_wall_check_1,
            R.string.envelope_wall_check_2,
            R.string.envelope_wall_check_3,
            R.string.envelope_wall_check_4,
            R.string.envelope_wall_check_5,
            R.string.envelope_wall_check_6
        )
        labels.forEachIndexed { index, label ->
            val number = index + 1
            val options = EnvelopeChoices.wallChecklist + if (number == 4) {
                listOf("not_applicable_claimed" to R.string.envelope_group4_na)
            } else {
                emptyList()
            }
            Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
            EnvelopeFields(
                values,
                "checklist.$number",
                listOf(
                    choice("state", R.string.envelope_status, options),
                    field("doc", R.string.envelope_drawing)
                ),
                onChange
            )
        }
    }
}

@Composable
private fun RoofGeometry(
    prefix: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    EnvelopeFields(
        values,
        prefix,
        listOf(
            choice("surface", R.string.envelope_surface, EnvelopeChoices.surface),
            choice(
                "wall_insulation",
                R.string.envelope_wall_insulation,
                EnvelopeChoices.insulation
            ),
            choice("soil_insulation_scheme", R.string.envelope_soil_scheme, EnvelopeChoices.soil),
            number("height_above_grade_cm", R.string.envelope_height_grade),
            number("perimeter_width_cm", R.string.envelope_perimeter_width),
            field("design_source", R.string.envelope_drawing)
        ),
        onChange
    )
}

@Composable
private fun LayerEditor(
    prefix: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            var detailsExpanded by remember(prefix) { mutableStateOf(false) }
            val directResistance = values["$prefix.type"] == "resistance"
            val thermalFields = if (directResistance) {
                listOf(number("resistance_m2kw", R.string.envelope_resistance))
            } else {
                listOf(number("lambda_w_mk", R.string.envelope_lambda))
            }
            EnvelopeFields(
                values,
                prefix,
                listOf(
                    field("material", R.string.envelope_material),
                    choice("type", R.string.envelope_coefficient_type, EnvelopeChoices.coefficient),
                    number("thickness_mm", R.string.envelope_thickness)
                ) + thermalFields + listOf(field("source", R.string.envelope_source)),
                onChange
            )
            Text(
                stringResource(
                    R.string.envelope_layer_preview,
                    EnvelopeNumbers.format(EnvelopeNumbers.layerResistance(values, prefix))
                ),
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = { detailsExpanded = !detailsExpanded }) {
                Text(stringResource(R.string.envelope_layer_details))
            }
            if (detailsExpanded) {
                EnvelopeFields(
                    values,
                    prefix,
                    listOf(
                        field("geometry_drawing_ref", R.string.envelope_geometry_drawing),
                        field("geometry_notes", R.string.envelope_geometry_notes),
                        field("manufacturer", R.string.envelope_manufacturer),
                        field("license_no", R.string.envelope_license),
                        field("product_standard", R.string.envelope_standard),
                        choice(
                            "thermal_certificate",
                            R.string.envelope_certificate,
                            EnvelopeChoices.certificate
                        ),
                        field("certificate_ref", R.string.envelope_certificate_ref)
                    ),
                    onChange
                )
                EnvelopeCheck(
                    "$prefix.is_insulation",
                    R.string.envelope_is_insulation,
                    values,
                    onChange
                )
                EnvelopeCheck(
                    "$prefix.geometry_match_documented",
                    R.string.envelope_geometry_check,
                    values,
                    onChange
                )
            }
            RemoveRow(prefix, values, onChange)
        }
    }
}

@Composable
internal fun GlazingList(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_glazing_title) {
        Button(onClick = { onChange(envelopeAddRow(values, "opening")) }) {
            Text(stringResource(R.string.envelope_add_opening))
        }
        OutlinedButton(onClick = { onChange(envelopeSampleGlazing(values)) }) {
            Text(stringResource(R.string.envelope_sample_opening))
        }
        if (envelopeRows(values, "opening").isEmpty()) Text(stringResource(R.string.envelope_empty))
    }
    RepeatedCards("opening", R.string.envelope_opening_title, values, onChange) { prefix ->
        val fields = glazingFields.map { field ->
            if (field.key == "frame_u" && values["$prefix.frame_type"] == "wood") {
                field.copy(label = R.string.envelope_wood_lambda)
            } else {
                field
            }
        }
        EnvelopeFields(values, prefix, fields, onChange)
        EnvelopeCheck(
            "$prefix.shading_requested",
            R.string.envelope_shading_request,
            values,
            onChange
        )
        EnvelopeFields(
            values,
            prefix,
            listOf(field("shading_source", R.string.envelope_drawing)),
            onChange
        )
    }
    EnvelopeCard(R.string.envelope_technical_checks) {
        EnvelopeCheck("wwr_checked", R.string.envelope_wwr_check, values, onChange)
        EnvelopeCheck("mandatory_checked", R.string.envelope_window_check, values, onChange)
        EnvelopeCheck("shading_checked", R.string.envelope_shade_check, values, onChange)
    }
}

private val glazingFields = listOf(
    field("id", R.string.envelope_opening_id),
    field("parent_wall_id", R.string.envelope_parent_wall),
    field("name", R.string.envelope_name),
    choice("orientation", R.string.envelope_orientation, EnvelopeChoices.orientation),
    choice("adjacency", R.string.envelope_adjacency, EnvelopeChoices.adjacency),
    number("U_wm2k", R.string.envelope_window_u),
    choice("frame_type", R.string.envelope_frame_type, EnvelopeChoices.frame),
    field("opening_layout", R.string.envelope_opening_layout),
    number("frame_u", R.string.envelope_frame_u),
    number("glass_u", R.string.envelope_glass_u),
    choice("glass_plane", R.string.envelope_glass_plane, EnvelopeChoices.plane),
    choice("glass_table", R.string.envelope_glass_table, EnvelopeChoices.glassTable),
    number("gas_gap_mm", R.string.envelope_gas_gap),
    field("emissivity", R.string.envelope_emissivity),
    number("SHGC", R.string.envelope_shgc),
    number("Tv", R.string.envelope_tv),
    field("profile_manufacturer", R.string.envelope_profile_manufacturer),
    field("glass_manufacturer", R.string.envelope_glass_manufacturer),
    field("product_source", R.string.envelope_window_source)
)
