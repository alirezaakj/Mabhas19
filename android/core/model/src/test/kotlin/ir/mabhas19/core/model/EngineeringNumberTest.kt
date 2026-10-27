package ir.mabhas19.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EngineeringNumberTest {
    @Test fun `normalizes Persian Arabic and ASCII digits without rounding`() {
        listOf("۱۲۳٫۴", "١٢٣٫٤", "123.4", " ۱۲۳٫۴ ").forEach {
            assertEquals("123.4", EngineeringNumber.parse(it)?.toPlainString())
        }
        assertEquals("1234.50", EngineeringNumber.parse("۱٬۲۳۴٫۵۰")?.toPlainString())
        assertEquals("0", EngineeringNumber.parse("۰")?.toPlainString())
    }

    @Test fun `rejects ambiguous malformed and nonfinite numbers`() {
        listOf("", " ", "1,2", "12,34", "1 234", "NaN", "Infinity", "1e3", "۱۲..۳", ".5").forEach {
            assertNull(it, EngineeringNumber.parse(it))
        }
    }
}
