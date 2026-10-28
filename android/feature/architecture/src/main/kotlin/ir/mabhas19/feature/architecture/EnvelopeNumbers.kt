package ir.mabhas19.feature.architecture

import ir.mabhas19.core.calculation.LayerResistance
import ir.mabhas19.core.model.EngineeringNumber
import java.util.Locale

/** F05/F06 geometric layer resistance only. No reference tables, film terms, or approval decision. */
internal object EnvelopeNumbers {
    fun layerResistance(values: Map<String, String>, prefix: String): Double? =
        when (values["$prefix.type"]) {
            "resistance" -> positive(values["$prefix.resistance_m2kw"])
            "lambda" -> {
                val millimetres = positive(values["$prefix.thickness_mm"])
                val conductivity = positive(values["$prefix.lambda_w_mk"])
                if (millimetres == null || conductivity == null) {
                    null
                } else {
                    LayerResistance.fromConductivity(
                        millimetres / MILLIMETRES_PER_CENTIMETRE,
                        conductivity
                    )
                        .takeIf { it.isFinite() && it > 0 }
                }
            }
            else -> null
        }

    fun layersResistance(values: Map<String, String>, assemblyPrefix: String): Double? {
        val rows = envelopeRows(values, "$assemblyPrefix.layer")
        val resistances = rows.map { row -> layerResistance(values, "$assemblyPrefix.layer.$row") }
        return if (resistances.isEmpty() || resistances.any { it == null }) {
            null
        } else {
            resistances.filterNotNull().sum().takeIf { it.isFinite() && it > 0 }
        }
    }

    fun format(value: Double?): String =
        value?.let { String.format(Locale.ROOT, "%.4f", it) } ?: "—"

    private fun positive(text: String?): Double? =
        EngineeringNumber.parse(text.orEmpty())?.toDouble()?.takeIf {
            it.isFinite() && it > 0
        }

    private const val MILLIMETRES_PER_CENTIMETRE = 10.0
}
