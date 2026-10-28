package ir.mabhas19.feature.architecture

internal fun envelopeRows(values: Map<String, String>, prefix: String): List<Int> {
    val pattern = Regex("^${Regex.escape(prefix)}\\.(\\d+)\\.")
    return values.keys.mapNotNull {
        pattern.find(it)?.groupValues?.get(1)?.toIntOrNull()
    }.distinct().sorted()
}

internal fun envelopeAddRow(values: Map<String, String>, prefix: String): Map<String, String> {
    val index = envelopeNextIndex(values, prefix)
    val kind = when (prefix) {
        "wall" -> "WL"
        "roof" -> "RF"
        "opening" -> "GL"
        "part" -> "PART"
        else -> "ROW"
    }
    return values + mapOf(
        "$prefix.$index.id" to "$kind-${(index + 1).toString().padStart(3, '0')}",
        "$prefix._next" to (index + 1).toString()
    )
}

private fun envelopeNextIndex(values: Map<String, String>, prefix: String): Int = maxOf(
    (envelopeRows(values, prefix).maxOrNull() ?: -1) + 1,
    values["$prefix._next"]?.toIntOrNull() ?: 0
)

internal fun envelopeRemoveRow(values: Map<String, String>, prefix: String): Map<String, String> {
    val parentPrefix = prefix.substringBeforeLast('.')
    val nextIndex = envelopeNextIndex(values, parentPrefix)
    return values.filterKeys { !it.startsWith("$prefix.") } +
        ("$parentPrefix._next" to nextIndex.toString())
}

internal object EnvelopeChoices {
    val adjacency = listOf(
        "outside" to R.string.envelope_outside,
        "uncontrolled" to R.string.envelope_uncontrolled
    )
    val insulation = listOf(
        "external" to R.string.envelope_external,
        "internal" to R.string.envelope_internal,
        "middle" to R.string.envelope_middle,
        "homogeneous" to R.string.envelope_homogeneous
    )
    val roofInsulation = insulation.filter { it.first in setOf("external", "internal") }
    val wallChecklist = listOf(
        "pending" to R.string.envelope_pending,
        "documented_unverified" to R.string.envelope_documented_unverified,
        "deficiency_reported" to R.string.envelope_deficiency
    )
    val bridgeSource = listOf(
        "numerical" to R.string.envelope_numerical_source,
        "appendix11_table" to R.string.envelope_appendix11_source,
        "test_certificate" to R.string.envelope_certificate_source,
        "other_document" to R.string.envelope_other_source
    )
    val coefficient = listOf(
        "lambda" to R.string.envelope_lambda_kind,
        "resistance" to R.string.envelope_r_kind
    )
    val certificate = listOf(
        "provided" to R.string.envelope_provided,
        "missing" to R.string.envelope_missing
    )
    val surface = listOf(
        "roof" to R.string.envelope_roof,
        "roof_uncontrolled" to R.string.envelope_roof_uncontrolled,
        "floor_air" to R.string.envelope_floor_outside,
        "floor_uncontrolled" to R.string.envelope_floor_uncontrolled,
        "floor_ground" to R.string.envelope_floor_ground
    )
    val soil =
        listOf("whole" to R.string.envelope_whole, "perimeter" to R.string.envelope_perimeter)
    val orientation = listOf(
        "south" to R.string.envelope_south,
        "north" to R.string.envelope_north,
        "east" to R.string.envelope_east,
        "west" to R.string.envelope_west,
        "other" to R.string.envelope_other
    )
    val frame = listOf(
        "metal_thermal_break" to R.string.envelope_metal_frame,
        "upvc" to R.string.envelope_upvc,
        "wood" to R.string.envelope_wood
    )
    val plane = listOf(
        "vertical" to R.string.envelope_vertical,
        "horizontal_roof" to R.string.envelope_horizontal
    )
    val glassTable = listOf(
        "پ۹-۱" to R.string.envelope_air_glass,
        "پ۹-۲" to R.string.envelope_argon_glass,
        "پ۹-۳" to R.string.envelope_krypton_glass,
        "پ۹-۴" to R.string.envelope_horizontal_glass
    )
    val assembly = listOf(
        "wall" to R.string.envelope_wall_title,
        "roof_floor" to R.string.envelope_roof_title,
        "glazing" to R.string.envelope_glazing_title
    )
    val shade = listOf(
        "none" to R.string.envelope_no_shade,
        "horizontal" to R.string.envelope_horizontal_shade,
        "vertical" to R.string.envelope_vertical_shade,
        "complex" to R.string.envelope_combined_shade
    )
    val evidenceStatus = listOf(
        "unknown" to R.string.envelope_unknown,
        "yes" to R.string.envelope_checked,
        "no" to R.string.envelope_needs_correction,
        "na" to R.string.envelope_na
    )
    val method = listOf(
        "prescriptive" to R.string.envelope_prescriptive,
        "balance" to R.string.envelope_balance,
        "energy_need" to R.string.envelope_energy_need,
        "energy_performance" to R.string.envelope_energy_performance
    )
    val dimension = listOf(
        "internal" to R.string.envelope_internal_dimensions,
        "external" to R.string.envelope_external_dimensions
    )
    val yesNo = listOf(
        "yes" to R.string.envelope_yes,
        "no" to R.string.envelope_no,
        "unknown" to R.string.envelope_unknown
    )
    val wallInterface = listOf(
        "gap" to R.string.envelope_with_gap,
        "attached" to R.string.envelope_attached,
        "unknown" to R.string.envelope_unknown
    )
    val gap = listOf(
        "covered" to R.string.envelope_gap_covered,
        "uncovered" to R.string.envelope_gap_open,
        "not_applicable" to R.string.envelope_no_gap,
        "unknown" to R.string.envelope_unknown
    )
    val neighbor = listOf(
        "controlled" to R.string.envelope_controlled,
        "uncontrolled" to R.string.envelope_uncontrolled,
        "unknown" to R.string.envelope_unknown
    )
    val destination = listOf(
        "controlled" to R.string.envelope_controlled,
        "uncontrolled" to R.string.envelope_uncontrolled,
        "outside" to R.string.envelope_outside
    )
    val door = listOf(
        "none" to R.string.envelope_no_door,
        "ordinary" to R.string.envelope_ordinary_door,
        "self_closing" to R.string.envelope_self_closing,
        "revolving" to R.string.envelope_revolving,
        "unknown" to R.string.envelope_unknown
    )
    val required = listOf(
        "unknown" to R.string.envelope_unknown,
        "true" to R.string.envelope_required,
        "false" to R.string.envelope_not_required
    )
    val glare = listOf(
        "none" to R.string.envelope_no_glare,
        "controlled" to R.string.envelope_controlled_glare,
        "uncontrolled" to R.string.envelope_uncontrolled_glare
    )
    val controlDay = listOf(
        "mordad_31" to R.string.envelope_mordad_31,
        "tir_1" to R.string.envelope_tir_1,
        "app10_table" to R.string.envelope_app10_table,
        "mehr_1" to R.string.envelope_mehr_1,
        "dey_1" to R.string.envelope_dey_1
    )
    val controlHour = listOf(
        "07:00" to R.string.envelope_hour_07,
        "08:00" to R.string.envelope_hour_08,
        "09:00" to R.string.envelope_hour_09,
        "10:00" to R.string.envelope_hour_10,
        "11:00" to R.string.envelope_hour_11,
        "12:00" to R.string.envelope_hour_12,
        "13:00" to R.string.envelope_hour_13,
        "14:00" to R.string.envelope_hour_14,
        "15:00" to R.string.envelope_hour_15,
        "16:00" to R.string.envelope_hour_16,
        "17:00" to R.string.envelope_hour_17
    )
    val multiGeometry = listOf("same_facade_rectangular" to R.string.envelope_same_facade)
    val luxBand = listOf(
        "100_300" to R.string.envelope_lux_100,
        "300_500" to R.string.envelope_lux_300,
        "500_700" to R.string.envelope_lux_500
    )
    val workType = listOf(
        "residential" to R.string.envelope_residential,
        "office" to R.string.envelope_office,
        "industrial" to R.string.envelope_industrial,
        "corridor" to R.string.envelope_corridor,
        "other" to R.string.envelope_other_work
    )
}

/** Site preset values are draft inputs only; no sample checks technical evidence on the user's behalf. */
internal fun envelopeSampleAssembly(
    values: Map<String, String>,
    roof: Boolean
): Map<String, String> {
    val kind = if (roof) "roof" else "wall"
    val index = envelopeNextIndex(values, kind)
    val prefix = "$kind.$index"
    val data = envelopeAddRow(values, kind).toMutableMap()
    data["$prefix.adjacency"] = "outside"
    data["$prefix.insulation"] = "external"
    data["$prefix.surface"] = if (roof) "roof" else "wall"
    if (roof) data["$prefix.wall_insulation"] = "external"
    data["$prefix.name"] =
        if (roof) "بام تخت با عایق XPS هشت سانتی‌متری" else "دیوار سفالی با عایق EPS پنج سانتی‌متری"
    val layers = if (roof) {
        listOf(
            listOf("اندود گچ زیر سقف", "15", "0.40", "", "پیوست ۷ جدول پ۷-۱"),
            listOf("سقف تیرچه و بلوک سفالی", "250", "", "0.26", "پیوست ۸ جدول پ۸-۱۰"),
            listOf("بتن سبک شیب‌بندی", "80", "0.35", "", "پیوست ۷ جدول پ۷-۱"),
            listOf("عایق XPS بام", "80", "0.032", "", "پیوست ۷ جدول پ۷-۲"),
            listOf("ملات و موزاییک بام", "40", "1.15", "", "پیوست ۷ جدول پ۷-۱")
        )
    } else {
        listOf(
            listOf("اندود گچ و خاک داخلی", "20", "0.50", "", "پیوست ۷ جدول پ۷-۱"),
            listOf("بلوک سفالی سوراخ‌دار افقی", "150", "", "0.35", "پیوست ۸ جدول پ۸-۸"),
            listOf("عایق EPS نوع ۲۰", "50", "0.038", "", "پیوست ۷ جدول پ۷-۲"),
            listOf("ملات ماسه سیمان", "25", "1.15", "", "پیوست ۷ جدول پ۷-۱"),
            listOf("سنگ نمای ساختمانی", "20", "2.30", "", "پیوست ۷ جدول پ۷-۱")
        )
    }
    layers.forEachIndexed { layer, row ->
        val layerPrefix = "$prefix.layer.$layer"
        data["$layerPrefix.material"] = row[0]
        data["$layerPrefix.thickness_mm"] = row[1]
        data["$layerPrefix.lambda_w_mk"] = row[2]
        data["$layerPrefix.resistance_m2kw"] = row[3]
        data["$layerPrefix.source"] = row[4]
        data["$layerPrefix.type"] = if (row[3].isBlank()) "lambda" else "resistance"
        data["$layerPrefix.is_insulation"] = ("عایق" in row[0]).toString()
    }
    return data
}

internal fun envelopeSampleGlazing(values: Map<String, String>): Map<String, String> {
    val index = envelopeNextIndex(values, "opening")
    val prefix = "opening.$index"
    return envelopeAddRow(values, "opening") + mapOf(
        "$prefix.name" to "پنجره دوجداره UPVC با شیشه کم‌گسیل",
        "$prefix.frame_type" to "upvc",
        "$prefix.orientation" to "south",
        "$prefix.adjacency" to "outside",
        "$prefix.U_wm2k" to "2.10",
        "$prefix.SHGC" to "0.50",
        "$prefix.Tv" to "0.68",
        "$prefix.product_source" to "نمونه عمومی سایت؛ شناسنامه فنی محصول واقعی باید ثبت شود"
    )
}

/** Explicitly requested educational data for a new, isolated sample project. */
@Suppress("LongMethod")
fun envelopeSample(section: String): Map<String, String> = when (section) {
    "walls" -> envelopeSampleAssembly(emptyMap(), roof = false)
    "roof_floor" -> envelopeSampleAssembly(emptyMap(), roof = true)
    "glazing" -> envelopeSampleGlazing(emptyMap()) + mapOf("opening.0.parent_wall_id" to "WL-001")
    "occupancy" -> mapOf(
        "main.total_area_m2" to "1200",
        "main.area_m2" to "1000",
        "main.floors" to "4",
        "main.total_floors" to "4",
        "part.0.id" to "PART-001",
        "part.0.name" to "بخش اداری آموزشی",
        "part.0.occupancy_id" to "o10",
        "part.0.area_m2" to "200",
        "part.0.floors" to "1",
        "assignment.0.id" to "ROW-001",
        "assignment.0.kind" to "wall",
        "assignment.0.assembly_id" to "WL-001",
        "assignment.0.part_id" to "main"
    )
    "shading" -> mapOf(
        "shade.0.id" to "SH-001",
        "shade.0.opening_id" to "GL-001",
        "shade.0.orientation" to "south",
        "shade.0.type" to "horizontal",
        "shade.0.control_day" to "tir_1",
        "shade.0.control_hour" to "12:00",
        "shade.0.win_u" to "2.10",
        "shade.0.win_shgc" to "0.50",
        "shade.0.win_tv" to "0.68",
        "shade.0.mandatory_for_window" to "unknown",
        "shade.0.window_height_m" to "1.5",
        "shade.0.depth_m" to "0.6",
        "shade.0.gap_m" to "0.2",
        "shade.0.profile_angle_deg" to "65",
        "shade.0.solar_basis" to "نمونه آموزشی؛ مسیر خورشید و هندسه واقعی هنوز بررسی نشده است"
    )
    "daylight" -> mapOf(
        "daylight.area_m2" to "30",
        "room.0.id" to "ROOM-001",
        "room.0.name" to "پذیرایی نمونه آموزشی",
        "room.0.window_count" to "1",
        "room.0.width_m" to "6",
        "room.0.depth_m" to "5",
        "room.0.orientation" to "south",
        "room.0.window_width_m" to "2",
        "room.0.window_left_m" to "0.5",
        "room.0.window_head_m" to "2.6",
        "room.0.reflectance" to "0.5",
        "room.0.obstruction_angle_deg" to "25",
        "room.0.work_type" to "residential",
        "room.0.work_height_m" to "0.85",
        "room.0.lux_band" to "100_300",
        "room.0.shaded" to "yes",
        "room.0.adjacent_opening" to "unknown",
        "zone.0.id" to "Z1",
        "zone.0.room_id" to "پذیرایی نمونه آموزشی",
        "zone.0.target_lux" to "300",
        "zone.0.reference" to "نمونه آموزشی؛ مرجع و اندازه‌گیری واقعی ارائه نشده است"
    )
    "airtightness" -> airSealingPoints.associate { (id, _) -> "air.$id.status" to "unknown" }
    "thermal_bridges" -> mapOf(
        "bridge_context.method" to "prescriptive",
        "bridge_context.dimension_basis" to "internal",
        "bridge_context.envelope_area_m2" to "150",
        "bridge_context.base_u" to "0.5",
        "bridge.0.id" to "TB-001",
        "bridge.0.label" to "اتصال آموزشی دیوار و بام",
        "bridge.0.type" to "wall_roof",
        "bridge.0.count" to "1",
        "bridge.0.length_m" to "5.6",
        "bridge.0.reference" to "نمونه آموزشی؛ ضریب مستند ψ هنوز وارد نشده است"
    )
    "boundaries" -> mapOf(
        "adjacent.0.id" to "ADJ-001",
        "adjacent.0.wall_ref" to "WL-001",
        "adjacent.0.area_m2" to "20",
        "adjacent.0.interface" to "unknown",
        "adjacent.0.gap" to "unknown",
        "adjacent.0.neighbor_condition" to "unknown",
        "connection.0.id" to "CON-001",
        "connection.0.destination" to "uncontrolled",
        "connection.0.door" to "unknown",
        "connection.0.separation" to "unknown"
    )
    else -> emptyMap()
}
