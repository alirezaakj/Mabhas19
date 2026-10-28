package ir.mabhas19.feature.architecture

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * F05–F14/A04–A14 request mapping from the served public clients captured 2026-10-27.
 * Produces calculation requests only. No project ID, CSRF, save action, or approval is manufactured.
 */
@Suppress("TooManyFunctions")
object ArchitecturePayloads {
    val supportedSections: Set<String> = setOf(
        "walls",
        "roof_floor",
        "glazing",
        "shading",
        "daylight",
        "airtightness",
        "thermal_bridges",
        "boundaries"
    )

    fun forSection(section: String, draft: ArchitectureDraft): JsonObject? = try {
        build(section, draft)
    } catch (_: PayloadProblem) {
        null
    }

    /** Stable code followed by a field path; missing data and malformed numbers remain distinct. */
    fun validation(section: String, draft: ArchitectureDraft): List<String> = try {
        build(section, draft)
        emptyList()
    } catch (problem: PayloadProblem) {
        listOf(problem.message.orEmpty())
    }

    private fun build(section: String, draft: ArchitectureDraft): JsonObject {
        checkInput(draft.edition == "1399-v4", "unsupported_edition")
        checkInput(draft.schemaVersion == 1, "unsupported_schema")
        checkInput(section in supportedSections, "section_not_supported:$section")
        val input = Input(draft.sections[section].orEmpty())
        return when (section) {
            "walls" -> walls(input, draft)
            "roof_floor" -> roofFloor(input, draft)
            "glazing" -> glazing(input, draft)
            "shading" -> shading(input)
            "daylight" -> daylight(input, draft)
            "airtightness" -> airtightness(input)
            "thermal_bridges" -> bridges(input)
            "boundaries" -> boundaries(input)
            else -> throw PayloadProblem("section_not_supported:$section")
        }
    }

    private fun context(draft: ArchitectureDraft): JsonObject {
        checkInput(
            ArchitectureCatalog.cities.any {
                it.id == draft.cityId
            },
            "context_required:city"
        )
        checkInput(
            ArchitectureCatalog.occupancies.any {
                it.id == draft.occupancyId
            },
            "context_required:occupancy"
        )
        checkInput(draft.grade in setOf("EC", "EC+", "EC++"), "context_required:grade")
        val context = Input(mapOf("area" to draft.area, "floors" to draft.floors))
        val area = context.number("area", positive = true)
        val floors = context.number("floors", positive = true)
        checkInput(floors != null && floors % 1 == 0.0, "number_invalid:floors")
        return json(
            "city_id" to text(draft.cityId),
            "occupancy_id" to text(draft.occupancyId),
            "net_area_m2" to numeric(area),
            "floor_count" to numeric(floors),
            "target_grade" to text(draft.grade)
        )
    }

    private fun walls(input: Input, draft: ArchitectureDraft): JsonObject {
        val context = context(draft)
        val group = ArchitectureRules.group(draft)
        checkInput(group != null && group in 1..3, "context_required:building_group")
        val rows = input.rows("wall")
        checkInput(rows.isNotEmpty(), "rows_required:wall")
        val walls = rows.map { prefix ->
            val row = input.child(prefix)
            json(
                "wall_id" to text(row.required("id")), "name" to text(row.value("name")),
                "assembly_type_id" to JsonNull, "assembly_type_revision" to JsonNull,
                "adjacency" to text(row.choice("adjacency", setOf("outside", "uncontrolled"))),
                "insulation" to text(row.choice("insulation", insulation)),
                "bridges_checked" to JsonPrimitive(row.flag("bridges_checked")),
                "parallel_paths" to JsonPrimitive(row.flag("parallel_paths")),
                "assembly_verified" to JsonPrimitive(row.flag("assembly_verified")),
                "layers" to layers(row)
            )
        }
        requireUnique(walls, "wall_id")
        val checklist = (1..6).associate { index ->
            val row = input.child("checklist.$index")
            val state = row.value("state").ifBlank { "pending" }
            val allowed = setOf("pending", "documented_unverified", "deficiency_reported") +
                if (index == 4) setOf("not_applicable_claimed") else emptySet()
            checkInput(state in allowed, "choice_invalid:checklist.$index.state")
            "item_$index" to json("state" to text(state), "document_ref" to text(row.value("doc")))
        }
        val payload = JsonObject(
            context + mapOf(
                "building_group" to JsonPrimitive(group!!),
                "group_documented" to JsonPrimitive(input.flag("group_documented")),
                "wall_checklist" to JsonObject(checklist),
                "walls" to JsonArray(walls)
            )
        )
        return json("request" to text("evaluate"), "payload" to payload)
    }

    private fun roofFloor(input: Input, draft: ArchitectureDraft): JsonObject {
        val context = context(draft)
        val rows = input.rows("roof")
        checkInput(rows.size == 1, "single_roof_required")
        val row = input.child(rows.single())
        val surface = row.choice(
            "surface",
            setOf("roof", "roof_uncontrolled", "floor_air", "floor_uncontrolled", "floor_ground")
        )
        val ground = surface == "floor_ground"
        val perimeter = ground && row.value("soil_insulation_scheme") == "perimeter"
        return JsonObject(
            context + mapOf(
                "assembly_type_id" to JsonNull, "assembly_type_revision" to JsonNull,
                "assembly_location_id" to text(row.required("id")),
                "assembly_name" to text(row.value("name")), "surface" to text(surface),
                "design_source" to text(row.value("design_source")),
                "envelope_insulation" to
                    text(row.choice("insulation", setOf("external", "internal"))),
                "wall_insulation" to text(row.choice("wall_insulation", insulation)),
                "soil_insulation_scheme" to if (ground) {
                    text(row.choice("soil_insulation_scheme", setOf("whole", "perimeter")))
                } else {
                    JsonNull
                },
                "height_above_grade_cm" to
                    numeric(row.number("height_above_grade_cm", required = ground)),
                "perimeter_width_cm" to
                    numeric(
                        row.number("perimeter_width_cm", required = perimeter, positive = perimeter)
                    ),
                "thermal_bridges_checked" to JsonPrimitive(row.flag("bridges_checked")),
                "parallel_paths" to JsonPrimitive(row.flag("parallel_paths")),
                "layers" to layers(row)
            )
        )
    }

    private val insulation = setOf("external", "internal", "middle", "homogeneous")

    private fun layers(row: Input): JsonArray {
        val rows = row.rows("layer")
        checkInput(rows.isNotEmpty(), "rows_required:${row.path}layer")
        return JsonArray(
            rows.map { prefix ->
                val layer = row.child(prefix)
                val type = layer.choice("type", setOf("lambda", "resistance"))
                val thickness = if (type ==
                    "lambda"
                ) {
                    layer.number("thickness_mm", positive = true)
                } else {
                    null
                }
                json(
                    "type" to text(type), "material" to text(layer.required("material")),
                    "source" to text(layer.value("source")), "material_id" to JsonNull,
                    "appendix8_id" to JsonNull,
                    "thickness_cm" to numeric(thickness?.div(10)),
                    "lambda_w_mk" to
                        numeric(
                            if (type ==
                                "lambda"
                            ) {
                                layer.number("lambda_w_mk", positive = true)
                            } else {
                                null
                            }
                        ),
                    "resistance_m2kw" to
                        numeric(
                            if (type ==
                                "resistance"
                            ) {
                                layer.number("resistance_m2kw", positive = true)
                            } else {
                                null
                            }
                        ),
                    "is_insulation" to JsonPrimitive(layer.flag("is_insulation")),
                    "geometry_match_documented" to
                        JsonPrimitive(layer.flag("geometry_match_documented")),
                    "geometry_drawing_ref" to text(layer.value("geometry_drawing_ref")),
                    "geometry_inputs" to JsonObject(emptyMap())
                )
            }
        )
    }

    private fun glazing(input: Input, draft: ArchitectureDraft): JsonObject {
        val context = context(draft)
        val rows = input.rows("opening")
        checkInput(rows.isNotEmpty(), "rows_required:opening")
        val openings = rows.map { prefix ->
            val row = input.child(prefix)
            json(
                "opening_id" to text(row.required("id")),
                "parent_wall_id" to text(row.required("parent_wall_id")),
                "name" to text(row.value("name")),
                "orientation" to text(row.choice("orientation", orientations)),
                "adjacency" to text(row.choice("adjacency", setOf("outside", "uncontrolled"))),
                "U_wm2k" to numeric(row.number("U_wm2k", positive = true)),
                "SHGC" to numeric(row.unitRatio("SHGC")), "Tv" to numeric(row.unitRatio("Tv")),
                "product_source" to text(row.value("product_source")),
                "appendix10_shading_requested" to JsonPrimitive(row.flag("shading_requested")),
                "appendix10_document" to text(row.value("shading_source")),
                "glass_plane" to nullableText(row.value("glass_plane")),
                "appendix9_window" to appendixWindow(row), "appendix9_glass" to appendixGlass(row)
            )
        }
        requireUnique(openings, "opening_id")
        val city = ArchitectureCatalog.cities.single { it.id == draft.cityId }
        return JsonObject(
            context + mapOf(
                "dominant_need" to text(city.need),
                "dominant_need_source" to text("Appendix 3, page ${city.page}, row ${city.row}"),
                "openings" to JsonArray(openings)
            )
        )
    }

    private val orientations = setOf("north", "south", "east", "west", "other")

    private fun appendixWindow(row: Input): JsonElement {
        if (row.value("opening_layout").isBlank()) return JsonNull
        val frame = row.choice("frame_type", setOf("metal_thermal_break", "upvc", "wood"))
        return json(
            "frame_type" to text(frame),
            "opening_type" to text(row.required("opening_layout")),
            (if (frame == "wood") "lambda_frame_W_mK" else "U_frame_W_m2K") to
                numeric(row.number("frame_u", positive = true)),
            "U_glass_W_m2K" to numeric(row.number("glass_u", positive = true))
        )
    }

    private fun appendixGlass(row: Input): JsonElement {
        if (row.value("glass_table").isBlank()) return JsonNull
        return json(
            "table" to text(row.required("glass_table")),
            "air_gap_mm" to numeric(row.number("gas_gap_mm", positive = true)),
            "emissivity" to
                if (row.value("emissivity") ==
                    "clear"
                ) {
                    JsonNull
                } else {
                    numeric(row.unitRatio("emissivity"))
                }
        )
    }

    private fun airtightness(input: Input): JsonObject {
        val ids = listOf(
            "window_perimeter", "operable_seals", "wall_roof_joint", "wall_floor_joint",
            "wall_corner",
            "penetrations", "shafts", "hatches", "curtain_wall", "air_barrier", "inspection"
        )
        val entries = ids.associateWith { id ->
            val row = input.child("air.$id")
            json(
                "status" to text(row.choice("status", setOf("yes", "no", "na", "unknown"))),
                "evidence" to text(row.value("evidence")),
                "comment" to text(row.value("comment"))
            )
        }
        // This native editor owns checklist entries only. Empty measured-test objects remain unknown.
        return json(
            "edition" to text("1399-v4"),
            "entries" to JsonObject(entries),
            "design" to JsonObject(emptyMap()),
            "test" to JsonObject(emptyMap())
        )
    }

    private fun shading(input: Input): JsonObject {
        val rows = input.rows("shade")
        checkInput(rows.isNotEmpty(), "rows_required:shade")
        val items = rows.map { prefix ->
            val row = input.child(prefix)
            val type = row.choice("type", setOf("none", "horizontal", "vertical", "complex"))
            val mandatory = row.choice("mandatory_for_window", setOf("true", "false"))
            val installed = type != "none"
            json(
                "window_id" to text(row.required("opening_id")),
                "orientation" to text(
                    row.choice("orientation", orientations)
                ),
                "type" to text(type),
                "shading_installed" to JsonPrimitive(installed),
                "mandatory_for_window" to JsonPrimitive(mandatory == "true"),
                "win_u" to numeric(row.number("win_u", positive = true)),
                "win_shgc" to numeric(row.unitRatio("win_shgc")),
                "win_tv" to numeric(row.unitRatio("win_tv")),
                "glare_status" to
                    text(row.choice("glare_status", setOf("none", "controlled", "uncontrolled"))),
                "control_day" to
                    text(
                        row.choice(
                            "control_day",
                            setOf("mordad_31", "tir_1", "app10_table", "mehr_1", "dey_1")
                        )
                    ),
                "control_hour" to
                    text(
                        row.choice(
                            "control_hour",
                            (7..17).map {
                                "${it.toString().padStart(2, '0')}:00"
                            }.toSet()
                        )
                    ),
                "window_height_m" to
                    numeric(
                        row.number("window_height_m", required = installed, positive = installed)
                    ),
                "depth_m" to numeric(row.number("depth_m", required = installed)),
                "top_gap_m" to numeric(row.number("gap_m", required = installed)),
                "profile_angle_deg" to
                    numeric(row.number("profile_angle_deg", required = installed)),
                "drawing_ref" to text(row.value("drawing")),
                "sun_source" to text(row.value("solar_basis")),
                "note" to text(row.value("notes"))
            )
        }
        return json("items" to JsonArray(items))
    }

    /** A11 prescriptive room request; mandatory-zone evaluation requires its own explicit action. */
    private fun daylight(input: Input, draft: ArchitectureDraft): JsonObject {
        checkInput(draft.grade in setOf("EC", "EC+", "EC++"), "context_required:grade")
        val rows = input.rows("room")
        checkInput(rows.isNotEmpty(), "rows_required:room")
        return json(
            "mode" to text("prescriptive"),
            "grade" to text(draft.grade),
            "usable_area_m2" to numeric(input.child("daylight").number("area_m2", positive = true)),
            "complete_space_inventory" to
                text(if (input.flag("all_rooms_recorded")) "yes" else "no"),
            "rooms" to JsonArray(rows.map { daylightRoom(input.child(it)) })
        )
    }

    private fun daylightRoom(row: Input): JsonObject {
        val count = row.number("window_count")
        checkInput(
            count != null && count in 0.0..12.0 && count % 1 == 0.0,
            "number_invalid:${row.path}window_count"
        )
        val hasWindow = count!! > 0
        val single = count == 1.0
        val windows = if (count > 1) daylightWindows(row, count.toInt()) else JsonArray(emptyList())
        val geometry = json(
            "name" to text(row.required("name")), "window_count" to numeric(count),
            "width_m" to numeric(row.number("width_m", positive = true)),
            "depth_m" to numeric(row.number("depth_m", positive = true)),
            "orientation" to
                if (hasWindow) {
                    text(
                        row.choice("orientation", orientations - "other")
                    )
                } else {
                    JsonNull
                },
            "lux_band" to
                if (hasWindow) {
                    text(
                        row.choice("lux_band", setOf("100_300", "300_500", "500_700"))
                    )
                } else {
                    JsonNull
                },
            "window_width_m" to
                numeric(row.number("window_width_m", required = single, positive = single)),
            "window_left_m" to numeric(row.number("window_left_m", required = single)),
            "window_head_m" to
                numeric(row.number("window_head_m", required = single, positive = single)),
            "reflectance" to if (hasWindow) numeric(row.unitRatio("reflectance")) else JsonNull,
            "shaded" to if (single) text(row.choice("shaded", setOf("yes", "no"))) else JsonNull,
            "obstacle_angle_deg" to numeric(row.number("obstruction_angle_deg", required = single)),
            "adjacent_opening" to
                if (single) {
                    text(
                        row.choice("adjacent_opening", setOf("yes", "no", "unknown"))
                    )
                } else {
                    JsonNull
                }
        )
        return JsonObject(geometry + daylightWork(row, hasWindow) + daylightEvidence(row, windows))
    }

    private fun daylightWork(row: Input, hasWindow: Boolean): JsonObject = json(
        "work_type" to if (hasWindow) {
            text(
                row.choice(
                    "work_type",
                    setOf("residential", "office", "industrial", "corridor", "other")
                )
            )
        } else {
            JsonNull
        },
        "work_height_m" to numeric(row.number("work_height_m", required = false))
    )

    private fun daylightEvidence(row: Input, windows: JsonArray): JsonObject = json(
        "plan_verified" to text(if (row.flag("plan_checked")) "yes" else "no"),
        "lux_ref" to text(row.value("lux_reference")),
        "multi_window_geometry" to nullableText(row.value("multi_window_geometry")),
        "all_facade_windows_included" to
            text(if (row.flag("all_facade_windows")) "yes" else "no"),
        "lateral_obstructions_clear" to nullableText(row.value("lateral_obstructions_clear")),
        "multi_windows" to windows
    )

    private fun daylightWindows(row: Input, count: Int): JsonArray {
        val windows = row.rows("window")
        checkInput(windows.size == count, "window_inventory_mismatch:${row.path}")
        return JsonArray(
            windows.map { prefix ->
                val window = row.child(prefix)
                json(
                    "width_m" to numeric(window.number("width_m", positive = true)),
                    "left_m" to numeric(window.number("left_m")),
                    "head_m" to numeric(window.number("head_m", positive = true)),
                    "shaded" to text(window.choice("shaded", setOf("yes", "no"))),
                    "obstacle_angle_deg" to numeric(window.number("obstacle_angle_deg"))
                )
            }
        )
    }

    private fun bridges(input: Input): JsonObject {
        val context = input.child("bridge_context")
        val rows = input.rows("bridge")
        checkInput(rows.isNotEmpty(), "rows_required:bridge")
        val bridges = rows.map { prefix ->
            val row = input.child(prefix)
            val length = row.number("length_m", required = false)
            val psi = row.number("psi_w_mk", required = false)
            val chi = row.number("chi_w_k", required = false)
            checkInput(
                (length != null && length >= 0 && psi != null && psi >= 0) ||
                    (chi != null && chi >= 0),
                "coefficient_required:$prefix"
            )
            val count = row.number("count", positive = true)
            checkInput(count != null && count % 1 == 0.0, "number_invalid:$prefix.count")
            json(
                "type" to text(row.required("type")), "label" to text(row.required("label")),
                "count" to numeric(
                    count
                ),
                "length_m" to numeric(length), "psi_w_mk" to numeric(psi),
                "chi_w_k" to numeric(chi),
                "source_type" to text(
                    row.choice(
                        "source_type",
                        setOf(
                            "numerical",
                            "appendix11_table",
                            "test_certificate",
                            "other_document"
                        )
                    )
                ),
                "reference" to text(row.required("reference")),
                "drawing" to text(row.value("drawing")),
                "notes" to text(row.value("notes"))
            )
        }
        return json(
            "method" to
                text(
                    context.choice(
                        "method",
                        setOf("prescriptive", "balance", "energy_need", "energy_performance")
                    )
                ),
            "dimension_basis" to
                text(context.choice("dimension_basis", setOf("internal", "external"))),
            "length_unit" to text("m"),
            "discontinuous_insulation" to JsonPrimitive(input.flag("discontinuous_insulation")),
            "balance_exterior_reference" to JsonPrimitive(input.flag("balance_exterior_reference")),
            "calculation_requested" to JsonPrimitive(input.flag("calculation_requested")),
            "envelope_area_m2" to numeric(context.number("envelope_area_m2", positive = true)),
            "base_u" to numeric(context.number("base_u", positive = true)),
            "bridges" to JsonArray(bridges)
        )
    }

    private fun boundaries(input: Input): JsonObject {
        val adjacent = input.rows("adjacent").map { prefix ->
            val row = input.child(prefix)
            json(
                "id" to text(row.required("id")),
                "area_m2" to numeric(row.number("area_m2", positive = true)),
                "interface" to text(row.choice("interface", setOf("gap", "attached", "unknown"))),
                "gap" to
                    text(
                        row.choice(
                            "gap",
                            setOf("covered", "uncovered", "unknown", "not_applicable")
                        )
                    ),
                "neighbor_condition" to
                    text(
                        row.choice(
                            "neighbor_condition",
                            setOf("controlled", "uncontrolled", "unknown")
                        )
                    ),
                "wall_ref" to text(row.value("wall_ref")),
                "drawing_ref" to text(row.value("drawing_ref")),
                "neighbor_evidence" to text(row.value("neighbor_evidence"))
            )
        }
        val connections = input.rows("connection").map { prefix ->
            val row = input.child(prefix)
            json(
                "id" to text(row.required("id")),
                "destination" to
                    text(row.choice("destination", setOf("uncontrolled", "outside", "controlled"))),
                "separation" to text(row.choice("separation", setOf("yes", "no", "unknown"))),
                "door" to
                    text(
                        row.choice(
                            "door",
                            setOf("none", "ordinary", "self_closing", "revolving", "unknown")
                        )
                    ),
                "frequent_external" to JsonPrimitive(row.flag("frequent_external")),
                "drawing_ref" to text(row.value("drawing_ref"))
            )
        }
        checkInput(adjacent.isNotEmpty() || connections.isNotEmpty(), "rows_required:boundary")
        return json(
            "adjacent_walls" to JsonArray(adjacent),
            "space_connections" to JsonArray(connections)
        )
    }

    private fun requireUnique(rows: List<JsonObject>, key: String) {
        checkInput(rows.map { it[key] }.distinct().size == rows.size, "duplicate_id:$key")
    }

    private class Input(val values: Map<String, String>, val path: String = "") {
        fun value(key: String): String = values[path + key].orEmpty().trim()
        fun required(key: String): String =
            value(key).also { checkInput(it.isNotBlank(), "field_required:$path$key") }
        fun choice(key: String, choices: Set<String>): String = required(key).also {
            checkInput(it in choices, "choice_invalid:$path$key")
        }
        fun child(prefix: String) = Input(values, "$path$prefix.")
        fun rows(prefix: String): List<String> {
            val pattern = Regex("^${Regex.escape(path + prefix)}\\.(\\d+)\\.")
            return values.keys.mapNotNull { pattern.find(it)?.groupValues?.get(1)?.toIntOrNull() }
                .distinct().sorted().map { "$prefix.$it" }
        }
        fun flag(key: String): Boolean {
            val raw = value(key)
            checkInput(raw in setOf("", "true", "false"), "boolean_invalid:$path$key")
            return raw == "true"
        }
        fun number(key: String, required: Boolean = true, positive: Boolean = false): Double? {
            val raw = value(key)
            if (raw.isBlank()) {
                checkInput(!required, "field_required:$path$key")
                return null
            }
            val number = ArchitectureRules.number(raw)
            checkInput(number != null && (!positive || number > 0), "number_invalid:$path$key")
            return number
        }
        fun unitRatio(key: String): Double? = number(key).also {
            checkInput(it != null && it in 0.0..1.0, "number_invalid:$path$key")
        }
    }

    private class PayloadProblem(message: String) : IllegalArgumentException(message)
    private fun checkInput(condition: Boolean, code: String) {
        if (!condition) throw PayloadProblem(code)
    }
    private fun text(value: String) = JsonPrimitive(value)
    private fun nullableText(value: String): JsonElement =
        if (value.isBlank()) JsonNull else text(value)
    private fun numeric(value: Double?): JsonElement = value?.let(::JsonPrimitive) ?: JsonNull
    private fun json(vararg pairs: Pair<String, JsonElement>) = JsonObject(mapOf(*pairs))
}
