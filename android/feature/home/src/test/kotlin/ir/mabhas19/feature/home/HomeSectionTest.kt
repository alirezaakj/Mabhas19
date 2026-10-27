package ir.mabhas19.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HomeSectionTest {
    @Test fun keysAreStableAndUnique() {
        val keys = HomeSection.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        keys.forEach { assertNotNull(HomeSection.fromKey(it)) }
    }

    @Test fun designSectionsKeepWebsiteOrder() {
        assertEquals(
            listOf("architecture", "mechanical", "electrical", "renewables", "airtightness"),
            HomeSection.designSections.map { it.key }
        )
    }

    @Test fun unknownKeyIsRejected() {
        assertNull(HomeSection.fromKey("does-not-exist"))
    }

    @Test fun countsRenderWithPersianDigits() {
        assertEquals("۰", 0.toPersianDigits())
        assertEquals("۱۲", 12.toPersianDigits())
        assertEquals("۱۳۹۹", 1399.toPersianDigits())
    }
}
