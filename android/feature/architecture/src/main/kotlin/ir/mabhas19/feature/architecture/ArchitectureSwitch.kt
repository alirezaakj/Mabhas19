package ir.mabhas19.feature.architecture

/** F03 browser-source option visibility; no row is an engineering compliance decision. */
data class ArchitectureSwitch(val key: String, val enabled: Boolean?)

fun architectureSwitches(draft: ArchitectureDraft): List<ArchitectureSwitch> {
    val group = ArchitectureRules.group(draft)
    val city = ArchitectureCatalog.cities.find { it.id == draft.cityId }
    val gradeKnown = draft.grade in setOf("EC", "EC+", "EC++", "ECnZ")
    val contextKnown = group != null && gradeKnown
    val higherGrade = draft.grade in setOf("EC+", "EC++", "ECnZ")
    val highestGrade = draft.grade in setOf("EC++", "ECnZ")
    val chapterFive = group?.let { it != 4 }
    return listOf(
        ArchitectureSwitch("solar", ArchitectureRules.solarEligibility(draft)),
        ArchitectureSwitch("intermittent", ArchitectureRules.intermittentAllowed(draft)),
        ArchitectureSwitch("chapter5", chapterFive),
        ArchitectureSwitch("nz", nearZeroSwitch(group, draft.method)),
        ArchitectureSwitch("chiller", whenKnown(gradeKnown, !highestGrade)),
        ArchitectureSwitch(
            "roof_alternative",
            whenKnown(contextKnown, !higherGrade && chapterFive == true)
        ),
        ArchitectureSwitch(
            "cool_roof",
            whenKnown(contextKnown, !higherGrade && city?.need == "cooling" && chapterFive == true)
        ),
        ArchitectureSwitch(
            "water_labels",
            whenKnown(contextKnown, higherGrade && chapterFive == true)
        ),
        ArchitectureSwitch("vsd", whenKnown(contextKnown, higherGrade && chapterFive == true)),
        ArchitectureSwitch("lms", whenKnown(contextKnown, highestGrade && chapterFive == true)),
        ArchitectureSwitch("year", ArchitectureRules.number(draft.year)?.let { it >= 1399 })
    )
}

private fun whenKnown(known: Boolean, condition: Boolean): Boolean? = if (known) condition else null

private fun nearZeroSwitch(group: Int?, method: String): Boolean? =
    group?.let { it != 4 && method == "energy_performance" }
