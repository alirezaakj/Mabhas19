package ir.mabhas19.feature.architecture

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureRulesTest {
    @Test fun f03CataloguePreservesAllSourceRowsAndSampleIdentity() {
        assertEquals(245, ArchitectureCatalog.cities.size)
        assertEquals(245, ArchitectureCatalog.cities.map { it.id }.distinct().size)
        assertEquals(46, ArchitectureCatalog.occupancies.size)
        assertEquals(46, ArchitectureCatalog.occupancies.map { it.id }.distinct().size)
        assertEquals(31, ArchitectureCatalog.provinces.size)
        val karaj = ArchitectureCatalog.cities.single { it.id == "c181" }
        assertEquals("کرج", karaj.name)
        assertEquals("medium", karaj.degree)
        assertEquals("heating", karaj.need)
        assertTrue(ArchitectureCatalog.cities.all { it.page.isNotBlank() && it.row.isNotBlank() })
        assertEquals(2, ArchitectureRules.group(ArchitectureDraft.sample()))
        assertEquals("", ArchitectureDraft().cityId)
    }

    @Test fun f03GroupBUsesStrictAreaAndFloorBoundaries() {
        val draft =
            ArchitectureDraft(cityId = "c004", occupancyId = "o10", area = "2000", floors = "9")
        assertEquals(2, ArchitectureRules.group(draft))
        assertEquals(2, ArchitectureRules.group(draft.copy(area = "1999.99")))
        assertEquals(1, ArchitectureRules.group(draft.copy(area = "2000.01")))
        assertEquals(1, ArchitectureRules.group(draft.copy(floors = "10")))
        assertNull(ArchitectureRules.group(draft.copy(floors = "9.1")))
        assertNull(ArchitectureRules.group(draft.copy(area = "NaN")))
        assertNull(ArchitectureRules.group(draft.copy(area = "")))
        assertNull(ArchitectureRules.group(draft.copy(edition = "1404-v5")))
        val medium = draft.copy(cityId = "c181")
        assertEquals(3, ArchitectureRules.group(medium))
        assertEquals(2, ArchitectureRules.group(medium.copy(area = "2001")))
    }

    @Test fun f03GroupAAndCAndDUseSourceMatrices() {
        assertEquals(
            1,
            ArchitectureRules.group(ArchitectureDraft(cityId = "c004", occupancyId = "o01"))
        )
        assertEquals(
            3,
            ArchitectureRules.group(ArchitectureDraft(cityId = "c006", occupancyId = "o01"))
        )
        assertEquals(
            2,
            ArchitectureRules.group(ArchitectureDraft(cityId = "c004", occupancyId = "o27"))
        )
        assertEquals(
            3,
            ArchitectureRules.group(ArchitectureDraft(cityId = "c181", occupancyId = "o27"))
        )
        assertEquals(
            4,
            ArchitectureRules.group(ArchitectureDraft(cityId = "c181", occupancyId = "o37"))
        )
    }

    @Test fun f03FacadeFortyPercentIsExcludedAndMissingIsUnknown() {
        val complete = completeDraft()
        assertEquals(true, ArchitectureRules.screening(complete).methodAllowed)
        val exact = complete.copy(
            facades = complete.facades + ("north" to FacadeDraft("100", "40"))
        )
        assertTrue("facade_ratio_at_least_40:north" in ArchitectureRules.screening(exact).issues)
        assertEquals(false, ArchitectureRules.screening(exact).methodAllowed)
        assertEquals(0.4, ArchitectureRules.facadeRatio(FacadeDraft("100", "40"))!!, 0.0)
        assertNull(ArchitectureRules.facadeRatio(FacadeDraft("0", "0")))
        assertNull(ArchitectureRules.facadeRatio(FacadeDraft("100", "101")))
        assertNull(ArchitectureRules.facadeRatio(FacadeDraft("100", "30", absent = true)))
        val absent = complete.copy(
            facades = ArchitectureRules.directions.associateWith {
                FacadeDraft(absent = true)
            }
        )
        assertTrue("facade_required" in ArchitectureRules.screening(absent).pending)
        assertNull(ArchitectureRules.screening(absent).methodAllowed)
    }

    @Test fun f03SolarRequiresStrictRatioAndAngleAndKeepsUnknown() {
        val draft = ArchitectureDraft.sample().copy(
            area = "900",
            solarArea = "100",
            obstacleAngle = "24.9"
        )
        assertEquals(false, ArchitectureRules.solarEligibility(draft))
        assertEquals(true, ArchitectureRules.solarEligibility(draft.copy(solarArea = "100.01")))
        assertEquals(
            false,
            ArchitectureRules.solarEligibility(draft.copy(solarArea = "101", obstacleAngle = "25"))
        )
        assertNull(
            ArchitectureRules.solarEligibility(draft.copy(solarArea = "101", obstacleAngle = ""))
        )
        assertNull(ArchitectureRules.solarEligibility(draft.copy(solarArea = "invalid")))
        assertEquals(
            false,
            ArchitectureRules.solarEligibility(draft.copy(cityId = "c001", solarArea = "500"))
        )
    }

    @Test fun f03MalformedNumbersAreIssuesWhileMissingNumbersArePending() {
        val missing = ArchitectureRules.screening(ArchitectureDraft())
        assertTrue("area_required" in missing.pending)
        assertFalse("area_invalid" in missing.issues)
        assertNull(missing.methodAllowed)
        val invalid = ArchitectureRules.screening(
            ArchitectureDraft(area = "one", floors = "0", year = "1398")
        )
        assertTrue(
            invalid.issues.containsAll(listOf("area_invalid", "floors_invalid", "year_before_1399"))
        )
        assertEquals(false, invalid.methodAllowed)
        assertEquals(1234.5, ArchitectureRules.number("۱٬۲۳۴٫۵")!!, 0.0)
        assertNull(ArchitectureRules.number("1,2"))
        assertNull(ArchitectureRules.number("Infinity"))
    }

    @Test fun f03SourceOptionLocksPreserveDraftSections() {
        val sections = mapOf("arch_walls" to mapOf("wall_id" to "w1"))
        val draft = ArchitectureDraft.sample().copy(
            occupancyId = "o37",
            method = "prescriptive",
            grade = "EC++",
            sections = sections
        )
        val normalized = ArchitectureRules.normalize(draft)
        assertEquals("mandatory_only", normalized.method)
        assertEquals("mandatory_ch4_only", normalized.grade)
        assertEquals(sections, normalized.sections)
        assertEquals("", ArchitectureRules.normalize(ArchitectureDraft()).method)
        assertEquals("", ArchitectureRules.normalize(ArchitectureDraft()).grade)
        assertEquals(
            "continuous",
            ArchitectureRules.normalize(
                ArchitectureDraft.sample().copy(continuity = "intermittent")
            ).continuity
        )
        val grade = completeDraft().copy(grade = "ECnZ")
        assertEquals(false, ArchitectureRules.gradeAllowed(grade))
        assertEquals(
            true,
            ArchitectureRules.gradeAllowed(grade.copy(method = "energy_performance"))
        )
        assertEquals(
            false,
            ArchitectureRules.gradeAllowed(grade.copy(grade = "EC", minGrade = "EC+"))
        )
    }

    @Test fun f03UnimplementedMethodsAndUnknownIdentifiersDoNotPass() {
        val draft = completeDraft().copy(method = "energy_performance")
        val screen = ArchitectureRules.screening(draft)
        assertTrue("method_evaluation_unavailable" in screen.pending)
        assertNull(screen.methodAllowed)
        assertTrue(
            "city_invalid" in ArchitectureRules.screening(draft.copy(cityId = "unknown")).issues
        )
        assertNull(ArchitectureRules.group(draft.copy(occupancyId = "unknown")))
        assertTrue(
            "unsupported_edition" in
                ArchitectureRules.screening(draft.copy(edition = "1404-v5")).issues
        )
    }

    @Test fun f20SerializationRoundTripPreservesAllSectionFields() {
        val draft = completeDraft().copy(
            sections = mapOf("arch_glazing" to mapOf("opening_id" to "پنجره-۱", "area" to "۱۲٫۵"))
        )
        val encoded = Json.encodeToString(draft)
        val restored = Json.decodeFromString<ArchitectureDraft>(encoded)
        assertEquals(draft, restored)
        assertNotNull(restored.facades["north"])
        assertEquals("پنجره-۱", restored.sections["arch_glazing"]?.get("opening_id"))
    }

    private fun completeDraft() = ArchitectureDraft.sample().copy(
        method = "prescriptive",
        grade = "EC",
        inertia = "medium",
        inertiaDocumented = true,
        ministryChecked = true,
        facades = ArchitectureRules.directions.associateWith { FacadeDraft("100", "39.9") }
    )
}
