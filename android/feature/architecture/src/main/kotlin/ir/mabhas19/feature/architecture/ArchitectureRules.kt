package ir.mabhas19.feature.architecture

import ir.mabhas19.core.model.EngineeringNumber

/**
 * F03 source parity with assets/arch-general.js (2026-10-27), not independently verified law.
 * A03 server evaluation, the envelope calculations and regulatory approval remain separate.
 */
@Suppress("TooManyFunctions")
object ArchitectureRules {
    private val methods = setOf(
        "prescriptive",
        "tradeoff",
        "energy_need",
        "energy_performance",
        "mandatory_only"
    )
    private val grades = listOf("mandatory_ch4_only", "EC", "EC+", "EC++", "ECnZ")
    private val minimumGrades = setOf("EC", "EC+", "EC++")
    private val continuousOccupancies = setOf("o01", "o02", "o05", "o06", "o16", "o17")
    val directions: List<String> = listOf("north", "south", "east", "west")

    fun number(text: String): Double? = EngineeringNumber.parse(text)?.toDouble()?.takeIf {
        it.isFinite()
    }

    @Suppress("ReturnCount")
    fun group(draft: ArchitectureDraft): Int? {
        if (draft.edition != "1399-v4" || draft.schemaVersion != 1) return null
        val city = ArchitectureCatalog.cities.find { it.id == draft.cityId } ?: return null
        val occupancy = ArchitectureCatalog.occupancies.find { it.id == draft.occupancyId }
            ?: return null
        return when (occupancy.type) {
            "الف" -> mapOf("high" to 1, "medium" to 2, "low" to 3)[city.degree]
            "ج" -> if (city.degree == "high") 2 else 3
            "د" -> 4
            "ب" -> groupB(draft, city.degree)
            else -> null
        }
    }

    @Suppress("ReturnCount")
    private fun groupB(draft: ArchitectureDraft, degree: String): Int? {
        val area = number(draft.area)?.takeIf { it > 0 } ?: return null
        val floors = number(draft.floors)?.takeIf { it >= 1 && it % 1 == 0.0 }
            ?: return null
        val large = area > 2000 || floors > 9
        return when (degree) {
            "high" -> if (large) 1 else 2
            "medium" -> if (large) 2 else 3
            else -> 3
        }
    }

    /** Ratio, not percent; null for absent, malformed, missing, or geometrically invalid façades. */
    @Suppress("ReturnCount")
    fun facadeRatio(facade: FacadeDraft): Double? {
        if (facade.absent) return null
        val area = number(facade.area)?.takeIf { it > 0 } ?: return null
        val glazing = number(facade.glazing)?.takeIf { it >= 0 && it <= area } ?: return null
        return glazing / area
    }

    /** Missing obstruction angle stays unknown; the browser itself permits an empty angle. */
    @Suppress("ReturnCount")
    fun solarEligibility(draft: ArchitectureDraft): Boolean? {
        val city = ArchitectureCatalog.cities.find { it.id == draft.cityId } ?: return null
        if (city.need == "cooling") return false
        val area = number(draft.area)?.takeIf { it > 0 } ?: return null
        val glazing = number(draft.solarArea)?.takeIf { it >= 0 } ?: return null
        val angle = number(draft.obstacleAngle)?.takeIf { it in 0.0..90.0 }
        if (glazing / area <= 1.0 / 9.0 || (angle != null && angle >= 25)) return false
        if (angle == null) return null
        return true
    }

    fun intermittentAllowed(draft: ArchitectureDraft): Boolean? = when {
        draft.occupancyId in continuousOccupancies || draft.inertia == "high" -> false
        ArchitectureCatalog.occupancies.none { it.id == draft.occupancyId } -> null
        draft.inertia !in setOf("low", "medium", "high") -> null
        else -> true
    }

    fun gradeAllowed(draft: ArchitectureDraft): Boolean? {
        val group = group(draft)
        return when {
            draft.grade.isBlank() -> null
            draft.grade !in grades -> false
            group == null -> null
            group == 4 -> draft.grade in setOf("mandatory_ch4_only", "EC")
            draft.minGrade !in minimumGrades -> false
            draft.grade == "mandatory_ch4_only" -> false
            draft.grade == "ECnZ" && draft.method != "energy_performance" -> false
            else -> grades.indexOf(draft.grade) >= grades.indexOf(draft.minGrade)
        }
    }

    /** Website option locks only. Unknown fields and all other section drafts are retained. */
    @Suppress("CyclomaticComplexMethod")
    fun normalize(draft: ArchitectureDraft): ArchitectureDraft {
        if (draft.edition != "1399-v4" || draft.schemaVersion != 1) return draft
        val group = group(draft)
        val method = when {
            group == 4 -> "mandatory_only"
            group != null && draft.method == "mandatory_only" -> "prescriptive"
            else -> draft.method
        }
        var grade = draft.grade
        if (group == 4 && grade !in setOf("mandatory_ch4_only", "EC")) grade = "mandatory_ch4_only"
        if (group != null && group != 4) {
            if (grade == "mandatory_ch4_only") grade = draft.minGrade
            if (grade == "ECnZ" && method != "energy_performance") grade = "EC++"
            if (belowMinimum(grade, draft.minGrade)) {
                grade = draft.minGrade
            }
        }
        val solar = when (solarEligibility(draft)) {
            true -> "high"
            false -> "low_or_none"
            null -> draft.solar
        }
        return draft.copy(
            method = method,
            grade = grade,
            solar = solar,
            continuity = if (intermittentAllowed(draft) == false) "continuous" else draft.continuity
        )
    }

    private fun belowMinimum(grade: String, minimum: String): Boolean =
        grade in grades && minimum in minimumGrades &&
            grades.indexOf(grade) < grades.indexOf(minimum)

    /** Codes, with :direction suffix for façade fields, are mapped to resources by the UI. */
    fun screening(draft: ArchitectureDraft): Screening {
        val pending = mutableListOf<String>()
        val issues = mutableListOf<String>()
        validateIdentity(draft, pending, issues)
        validateNumbers(draft, pending, issues)
        validateSelections(draft, pending, issues)
        if (draft.method in setOf("prescriptive", "tradeoff")) {
            screenPrescriptive(draft, pending, issues)
        } else if (draft.method in methods) {
            pending += "method_evaluation_unavailable"
        }
        val allowed = when {
            issues.isNotEmpty() -> false
            pending.isNotEmpty() -> null
            else -> true
        }
        return Screening(
            pending.distinct(),
            issues.distinct(),
            group(draft),
            solarEligibility(draft),
            allowed
        )
    }

    private fun validateIdentity(
        draft: ArchitectureDraft,
        pending: MutableList<String>,
        issues: MutableList<String>
    ) {
        if (draft.schemaVersion != 1) issues += "unsupported_schema"
        if (draft.edition != "1399-v4") issues += "unsupported_edition"
        if (draft.cityId.isBlank()) {
            pending += "city_required"
        } else if (ArchitectureCatalog.cities.none { it.id == draft.cityId }) {
            issues += "city_invalid"
        }
        if (draft.occupancyId.isBlank()) {
            pending += "occupancy_required"
        } else if (ArchitectureCatalog.occupancies.none { it.id == draft.occupancyId }) {
            issues += "occupancy_invalid"
        }
    }

    private fun validateNumbers(
        draft: ArchitectureDraft,
        pending: MutableList<String>,
        issues: MutableList<String>
    ) {
        for ((key, text) in listOf(
            "area" to draft.area,
            "floors" to draft.floors,
            "year" to draft.year
        )) {
            if (text.isBlank()) {
                pending += "${key}_required"
            } else {
                numericIssue(key, text)?.let { issues += it }
            }
        }
        val invalidSolar = draft.solarArea.isNotBlank() &&
            (number(draft.solarArea)?.let { it >= 0 } != true)
        val invalidAngle = draft.obstacleAngle.isNotBlank() &&
            (number(draft.obstacleAngle)?.let { it in 0.0..90.0 } != true)
        if (invalidSolar || invalidAngle) {
            issues += "solar_geometry_invalid"
        }
    }

    private fun numericIssue(key: String, text: String): String? {
        val value = number(text)
        return when {
            !positiveNumber(value, integer = key != "area") -> "${key}_invalid"
            key == "year" && value != null && value < 1399 -> "year_before_1399"
            else -> null
        }
    }

    private fun positiveNumber(value: Double?, integer: Boolean): Boolean {
        val positive = value?.takeIf { it > 0 } ?: return false
        return !integer || positive % 1 == 0.0
    }

    @Suppress("CyclomaticComplexMethod")
    private fun validateSelections(
        draft: ArchitectureDraft,
        pending: MutableList<String>,
        issues: MutableList<String>
    ) {
        if (draft.method.isBlank()) {
            pending += "method_required"
        } else if (draft.method !in methods) {
            issues += "method_invalid"
        }
        val group = group(draft)
        if (group != null && draft.method in methods &&
            ((group == 4) != (draft.method == "mandatory_only"))
        ) {
            issues += "method_group_mismatch"
        }
        if (draft.grade.isBlank()) {
            pending += "grade_required"
        } else if (draft.grade !in grades) {
            issues += "grade_invalid"
        }
        if (draft.minGrade !in minimumGrades) issues += "min_grade_invalid"
        if (gradeAllowed(draft) == false) issues += "grade_not_allowed"
        if (draft.inertia.isNotBlank() && draft.inertia !in setOf("low", "medium", "high")) {
            issues += "inertia_invalid"
        }
        if (draft.continuity !in setOf("continuous", "intermittent")) issues += "continuity_invalid"
        if (draft.continuity == "intermittent" && intermittentAllowed(draft) == false) {
            issues += "intermittent_not_allowed"
        }
        if (draft.solar !in setOf("high", "low_or_none")) issues += "solar_invalid"
        if (draft.solar == "high") {
            when (solarEligibility(draft)) {
                null -> pending += "solar_geometry_required"
                false -> issues += "solar_not_eligible"
                true -> Unit
            }
        }
    }

    private fun screenPrescriptive(
        draft: ArchitectureDraft,
        pending: MutableList<String>,
        issues: MutableList<String>
    ) {
        if ((number(draft.area) ?: 0.0) > 2000) issues += "area_over_2000"
        if ((number(draft.floors) ?: 0.0) > 9) issues += "floors_over_9"
        when (draft.inertia) {
            "" -> pending += "inertia_required"
            "low" -> issues += "inertia_low"
            "medium", "high" -> if (!draft.inertiaDocumented) pending += "inertia_evidence_required"
        }
        if (!draft.ministryChecked) pending += "ministry_evidence_required"
        val present = directions.filter { draft.facades[it]?.absent != true }
        if (present.isEmpty()) pending += "facade_required"
        for (direction in present) {
            screenFacade(draft.facades[direction] ?: FacadeDraft(), direction, pending, issues)
        }
    }

    private fun screenFacade(
        facade: FacadeDraft,
        direction: String,
        pending: MutableList<String>,
        issues: MutableList<String>
    ) {
        if (facade.area.isBlank()) {
            pending += "facade_area_required:$direction"
        } else if (number(facade.area)?.let { it > 0 } != true) {
            issues += "facade_area_invalid:$direction"
        }
        if (facade.glazing.isBlank()) {
            pending += "facade_glazing_required:$direction"
        } else if (number(facade.glazing)?.let { it >= 0 } != true) {
            issues += "facade_glazing_invalid:$direction"
        }
        val area = number(facade.area)?.takeIf { it > 0 }
        val ratio = area?.let { number(facade.glazing)?.div(it) }
        if (ratio != null && ratio >= 0.4) {
            issues += "facade_ratio_at_least_40:$direction"
        }
    }
}
