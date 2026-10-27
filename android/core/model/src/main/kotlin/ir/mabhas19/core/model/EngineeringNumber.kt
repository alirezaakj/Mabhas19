package ir.mabhas19.core.model

import java.math.BigDecimal

/** Commas/Arabic grouping marks must delimit groups of three; decimal comma is rejected. */
object EngineeringNumber {
    private val decimal = Regex("-?(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?")

    fun parse(input: String): BigDecimal? {
        val normalized = buildString {
            input.trim().forEach { character ->
                append(
                    when (character) {
                        in '۰'..'۹' -> '0' + (character - '۰')
                        in '٠'..'٩' -> '0' + (character - '٠')
                        '٫' -> '.'
                        '٬' -> ','
                        else -> character
                    }
                )
            }
        }
        if (!decimal.matches(normalized) || normalized.length > 64) return null
        return normalized.replace(",", "").toBigDecimalOrNull()
    }
}
