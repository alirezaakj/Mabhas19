package ir.mabhas19.core.calculation

import org.junit.Assert.assertEquals
import org.junit.Test

class LayerResistanceTest {
    @Test fun `converts centimetres to metres before calculating resistance`() {
        assertEquals(2.0, LayerResistance.fromConductivity(8.0, 0.04), 0.000001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects zero conductivity instead of generating infinite resistance`() {
        LayerResistance.fromConductivity(8.0, 0.0)
    }
}
