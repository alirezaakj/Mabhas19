package ir.mabhas19.core.calculation

/** Unit conversion only, without a regulatory compliance or certification decision. */
object LayerResistance {
    fun fromConductivity(thicknessCm: Double, conductivityWmK: Double): Double {
        require(thicknessCm.isFinite() && thicknessCm > 0)
        require(conductivityWmK.isFinite() && conductivityWmK > 0)
        return (thicknessCm / CENTIMETRES_PER_METRE) / conductivityWmK
    }

    private const val CENTIMETRES_PER_METRE = 100.0
}
