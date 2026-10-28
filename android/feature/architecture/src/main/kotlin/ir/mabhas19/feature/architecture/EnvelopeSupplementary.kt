package ir.mabhas19.feature.architecture

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun OccupancyEditor(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_occupancy_title) {
        Text(stringResource(R.string.envelope_occupancy_note))
        EnvelopeFields(
            values,
            "main",
            listOf(
                number("total_area_m2", R.string.envelope_total_area),
                number("area_m2", R.string.envelope_main_area),
                number("floors", R.string.envelope_floors),
                number("total_floors", R.string.envelope_total_floors)
            ),
            onChange
        )
        AddRow("part", R.string.envelope_add_part, values, onChange)
    }
    RepeatedCards("part", R.string.envelope_part_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("id", R.string.envelope_part_id),
                field("name", R.string.envelope_name),
                number("area_m2", R.string.envelope_part_area),
                number("floors", R.string.envelope_floors)
            ),
            onChange
        )
        ArchitecturePicker(
            label = stringResource(R.string.envelope_occupancy_id),
            value = values["$prefix.occupancy_id"].orEmpty(),
            options = ArchitectureCatalog.occupancies.map { it.id to it.label },
            onSelect = { onChange(values + ("$prefix.occupancy_id" to it)) },
            tag = "$prefix.occupancy_id"
        )
    }
    EnvelopeCard(R.string.envelope_assignments) {
        Text(stringResource(R.string.envelope_assignment_note))
        AddRow("assignment", R.string.envelope_add_assignment, values, onChange)
    }
    RepeatedCards("assignment", R.string.envelope_assignment_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                choice("kind", R.string.envelope_assembly_kind, EnvelopeChoices.assembly),
                field("assembly_id", R.string.envelope_assembly_id),
                field("part_id", R.string.envelope_part_id),
                field("drawing", R.string.envelope_drawing)
            ),
            onChange
        )
    }
}

@Composable
internal fun ShadingEditor(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_shading_title) {
        Text(stringResource(R.string.envelope_shading_note))
        AddRow("shade", R.string.envelope_add_shade, values, onChange)
    }
    RepeatedCards("shade", R.string.envelope_shade_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("opening_id", R.string.envelope_opening_id),
                choice("orientation", R.string.envelope_orientation, EnvelopeChoices.orientation),
                number("win_u", R.string.envelope_window_u),
                number("win_shgc", R.string.envelope_shgc),
                number("win_tv", R.string.envelope_tv),
                choice("type", R.string.envelope_shade_type, EnvelopeChoices.shade),
                choice(
                    "mandatory_for_window",
                    R.string.envelope_shade_mandatory,
                    EnvelopeChoices.required
                ),
                choice("glare_status", R.string.envelope_glare_status, EnvelopeChoices.glare),
                choice("control_day", R.string.envelope_day, EnvelopeChoices.controlDay),
                choice("control_hour", R.string.envelope_solar_hour, EnvelopeChoices.controlHour),
                number("window_height_m", R.string.envelope_window_height),
                number("depth_m", R.string.envelope_shade_depth),
                number("gap_m", R.string.envelope_shade_gap),
                number("profile_angle_deg", R.string.envelope_profile_angle),
                field("drawing", R.string.envelope_drawing),
                field("solar_basis", R.string.envelope_solar_basis),
                field("notes", R.string.envelope_notes)
            ),
            onChange
        )
        EnvelopeCheck(
            "$prefix.geometry_checked",
            R.string.envelope_shade_geometry_check,
            values,
            onChange
        )
        EnvelopeCheck("$prefix.glare_checked", R.string.envelope_glare_check, values, onChange)
    }
}

@Composable
internal fun DaylightEditor(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_daylight_title) {
        EnvelopeFields(
            values,
            "daylight",
            listOf(number("area_m2", R.string.envelope_total_area)),
            onChange
        )
        EnvelopeCheck("all_rooms_recorded", R.string.envelope_all_rooms, values, onChange)
        AddRow("room", R.string.envelope_add_room, values, onChange)
    }
    RepeatedCards("room", R.string.envelope_room_title, values, onChange) { prefix ->
        EnvelopeFields(values, prefix, daylightFields, onChange)
        EnvelopeCheck("$prefix.plan_checked", R.string.envelope_plan_check, values, onChange)
        EnvelopeCheck("$prefix.all_facade_windows", R.string.envelope_all_windows, values, onChange)
        EnvelopeFields(
            values,
            prefix,
            listOf(
                choice(
                    "multi_window_geometry",
                    R.string.envelope_multi_geometry,
                    EnvelopeChoices.multiGeometry
                ),
                choice(
                    "lateral_obstructions_clear",
                    R.string.envelope_lateral_clear,
                    EnvelopeChoices.yesNo
                )
            ),
            onChange
        )
        AddRow("$prefix.window", R.string.envelope_add_room_window, values, onChange)
        RepeatedCards(
            "$prefix.window",
            R.string.envelope_room_window,
            values,
            onChange
        ) { windowPrefix ->
            EnvelopeFields(
                values,
                windowPrefix,
                listOf(
                    number("width_m", R.string.envelope_window_width),
                    number("left_m", R.string.envelope_window_left),
                    number("head_m", R.string.envelope_window_head),
                    choice("shaded", R.string.envelope_has_shade, EnvelopeChoices.yesNo),
                    number("obstacle_angle_deg", R.string.envelope_obstruction_angle)
                ),
                onChange
            )
        }
    }
    EnvelopeCard(R.string.envelope_work_zone_title) {
        AddRow("zone", R.string.envelope_add_zone, values, onChange)
        EnvelopeCheck("all_zones_recorded", R.string.envelope_all_zones, values, onChange)
    }
    RepeatedCards("zone", R.string.envelope_work_zone_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("id", R.string.envelope_zone_id),
                field("room_id", R.string.envelope_room_id),
                number("target_lux", R.string.envelope_target_lux),
                number("minimum_lux", R.string.envelope_min_lux),
                number("average_lux", R.string.envelope_average_lux),
                number("adjacent_lux", R.string.envelope_adjacent_lux),
                number("background_lux", R.string.envelope_background_lux),
                field("reference", R.string.envelope_lux_source)
            ),
            onChange
        )
    }
}

private val daylightFields = listOf(
    field("name", R.string.envelope_room_name),
    number("window_count", R.string.envelope_window_count),
    number("width_m", R.string.envelope_room_width),
    number("depth_m", R.string.envelope_room_depth),
    choice("orientation", R.string.envelope_orientation, EnvelopeChoices.orientation),
    choice("lux_band", R.string.envelope_lux_band, EnvelopeChoices.luxBand),
    number("window_width_m", R.string.envelope_window_width),
    number("window_left_m", R.string.envelope_window_left),
    number("window_head_m", R.string.envelope_window_head),
    number("reflectance", R.string.envelope_reflectance),
    choice("shaded", R.string.envelope_has_shade, EnvelopeChoices.yesNo),
    number("obstruction_angle_deg", R.string.envelope_obstruction_angle),
    choice("work_type", R.string.envelope_work_type, EnvelopeChoices.workType),
    number("work_height_m", R.string.envelope_work_height),
    choice("adjacent_opening", R.string.envelope_adjacent_opening, EnvelopeChoices.yesNo),
    field("lux_reference", R.string.envelope_lux_source)
)

@Composable
internal fun AirSealingEditor(
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    airSealingPoints.forEach { (id, label) ->
        EnvelopeCard(label) {
            EnvelopeFields(
                values,
                "air.$id",
                listOf(
                    choice("status", R.string.envelope_status, EnvelopeChoices.evidenceStatus),
                    field("evidence", R.string.envelope_drawing),
                    field("comment", R.string.envelope_na_reason)
                ),
                onChange
            )
        }
    }
}

internal val airSealingPoints = listOf(
    "window_perimeter" to R.string.envelope_air_window,
    "operable_seals" to R.string.envelope_air_seals,
    "wall_roof_joint" to R.string.envelope_air_roof,
    "wall_floor_joint" to R.string.envelope_air_floor,
    "wall_corner" to R.string.envelope_air_corner,
    "penetrations" to R.string.envelope_air_penetrations,
    "shafts" to R.string.envelope_air_shafts,
    "hatches" to R.string.envelope_air_hatches,
    "curtain_wall" to R.string.envelope_air_curtain,
    "air_barrier" to R.string.envelope_air_barrier,
    "inspection" to R.string.envelope_air_inspection
)

@Composable
internal fun BridgeEditor(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_bridges_title) {
        EnvelopeFields(
            values,
            "bridge_context",
            listOf(
                choice("method", R.string.envelope_method, EnvelopeChoices.method),
                choice(
                    "dimension_basis",
                    R.string.envelope_dimension_basis,
                    EnvelopeChoices.dimension
                ),
                number("envelope_area_m2", R.string.envelope_envelope_area),
                number("base_u", R.string.envelope_base_u)
            ),
            onChange
        )
        EnvelopeCheck("discontinuous_insulation", R.string.envelope_discontinuous, values, onChange)
        EnvelopeCheck(
            "balance_exterior_reference",
            R.string.envelope_balance_reference,
            values,
            onChange
        )
        AddRow("bridge", R.string.envelope_add_bridge, values, onChange)
    }
    RepeatedCards("bridge", R.string.envelope_bridge_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("type", R.string.envelope_bridge_type),
                field("label", R.string.envelope_code),
                number("count", R.string.envelope_count),
                number("length_m", R.string.envelope_length),
                number("psi_w_mk", R.string.envelope_psi),
                number("chi_w_k", R.string.envelope_chi),
                choice("source_type", R.string.envelope_source_type, EnvelopeChoices.bridgeSource),
                field("reference", R.string.envelope_source),
                field("drawing", R.string.envelope_drawing),
                field("notes", R.string.envelope_notes)
            ),
            onChange
        )
    }
}

@Composable
internal fun BoundaryEditor(values: Map<String, String>, onChange: (Map<String, String>) -> Unit) {
    EnvelopeCard(R.string.envelope_boundaries_title) {
        AddRow("adjacent", R.string.envelope_add_adjacent, values, onChange)
        AddRow("connection", R.string.envelope_add_connection, values, onChange)
    }
    RepeatedCards("adjacent", R.string.envelope_adjacent_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("id", R.string.envelope_code),
                field("wall_ref", R.string.envelope_parent_wall),
                number("area_m2", R.string.envelope_envelope_area),
                choice("interface", R.string.envelope_interface, EnvelopeChoices.wallInterface),
                choice("gap", R.string.envelope_gap, EnvelopeChoices.gap),
                choice(
                    "neighbor_condition",
                    R.string.envelope_neighbor_conditioned,
                    EnvelopeChoices.neighbor
                ),
                field("neighbor_evidence", R.string.envelope_neighbor_evidence),
                field("drawing_ref", R.string.envelope_drawing)
            ),
            onChange
        )
    }
    RepeatedCards("connection", R.string.envelope_connection_title, values, onChange) { prefix ->
        EnvelopeFields(
            values,
            prefix,
            listOf(
                field("id", R.string.envelope_code),
                choice("destination", R.string.envelope_to_space, EnvelopeChoices.destination),
                choice("separation", R.string.envelope_separation, EnvelopeChoices.yesNo),
                choice("door", R.string.envelope_door_kind, EnvelopeChoices.door),
                field("drawing_ref", R.string.envelope_drawing)
            ),
            onChange
        )
        EnvelopeCheck(
            "$prefix.frequent_external",
            R.string.envelope_frequent_external,
            values,
            onChange
        )
    }
}
