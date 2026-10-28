package ir.mabhas19.feature.architecture

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvelopeNumbersTest {
    @Test
    fun `F05 millimetres and Persian conductivity are converted once`() {
        val values = mapOf(
            "wall.0.layer.0.type" to "lambda",
            "wall.0.layer.0.thickness_mm" to "۵۰",
            "wall.0.layer.0.lambda_w_mk" to "۰٫۰۴"
        )
        assertEquals(1.25, EnvelopeNumbers.layerResistance(values, "wall.0.layer.0")!!, 0.000001)
    }

    @Test
    fun `F05 direct R does not add conductivity or require thickness`() {
        val values = mapOf(
            "wall.0.layer.0.type" to "resistance",
            "wall.0.layer.0.resistance_m2kw" to "0.35",
            "wall.0.layer.0.lambda_w_mk" to "0.04",
            "wall.0.layer.0.thickness_mm" to "50"
        )
        assertEquals(0.35, EnvelopeNumbers.layerResistance(values, "wall.0.layer.0")!!, 0.000001)
    }

    @Test
    fun `F05 incomplete layers never yield a partial assembly resistance`() {
        val values = mapOf(
            "roof.0.layer.0.type" to "resistance",
            "roof.0.layer.0.resistance_m2kw" to "0.26",
            "roof.0.layer.1.type" to "lambda",
            "roof.0.layer.1.thickness_mm" to "80",
            "roof.0.layer.1.lambda_w_mk" to ""
        )
        assertNull(EnvelopeNumbers.layersResistance(values, "roof.0"))
        assertNull(EnvelopeNumbers.layersResistance(emptyMap(), "roof.0"))
    }

    @Test
    fun `F05 invalid unknown and zero values remain unresolved`() {
        listOf("", "0", "-0.1", "NaN", "Infinity", "۱٫۲٫۳").forEach { value ->
            val values = mapOf("layer.0.type" to "resistance", "layer.0.resistance_m2kw" to value)
            assertNull(EnvelopeNumbers.layerResistance(values, "layer.0"))
        }
        assertNull(EnvelopeNumbers.layerResistance(mapOf("layer.0.type" to "unknown"), "layer.0"))
    }

    @Test
    fun `F05 removing a row never reuses its identity or deletes a different wall`() {
        val source = mapOf(
            "wall.0.id" to "WL-001",
            "wall.0.layer.0.material" to "old",
            "wall.1.id" to "WL-002",
            "wall.10.id" to "WL-011",
            "opening.0.parent_wall_id" to "WL-011"
        )
        val removed = envelopeRemoveRow(source, "wall.10")
        val added = envelopeAddRow(removed, "wall")
        assertEquals("WL-012", added["wall.11.id"])
        assertEquals("WL-002", added["wall.1.id"])
        assertEquals("old", added["wall.0.layer.0.material"])
        assertEquals("WL-011", added["opening.0.parent_wall_id"])
        assertFalse(added.containsKey("wall.10.id"))
    }

    @Test
    fun `F05 samples append and never affirm technical checks`() {
        val initial = envelopeSampleAssembly(emptyMap(), roof = false)
        val appended = envelopeSampleAssembly(initial, roof = false)
        assertEquals(listOf(0, 1), envelopeRows(appended, "wall"))
        assertEquals(
            initial["wall.0.layer.2.thickness_mm"],
            appended["wall.0.layer.2.thickness_mm"]
        )
        assertTrue(
            appended.keys.none {
                it.endsWith("geometry_match_documented") ||
                    it.endsWith("assembly_verified")
            }
        )
        assertTrue(envelopeSample("airtightness").values.all { it == "unknown" })
        assertEquals("unknown", envelopeSample("shading")["shade.0.mandatory_for_window"])
    }

    @Test
    fun `F05 F06 F08 F10 F11 F14 complete sample inputs map without manufacturing evidence`() {
        val sections =
            listOf("walls", "roof_floor", "glazing", "daylight", "airtightness", "boundaries")
        sections.forEach { section ->
            val draft = ArchitectureDraft.sample().copy(
                grade = "EC",
                sections = mapOf(section to envelopeSample(section))
            )
            assertEquals(
                "Validation for $section",
                emptyList<String>(),
                ArchitecturePayloads.validation(section, draft)
            )
            assertNotNull(ArchitecturePayloads.forSection(section, draft))
        }
        val roof = envelopeSample("roof_floor")
        assertEquals("external", roof["roof.0.wall_insulation"])
        assertFalse(roof.containsKey("roof.0.bridges_checked"))
    }

    @Test
    fun `F05 wall evidence remains pending until user enters a documented state`() {
        val source = envelopeSample("walls")
        val draft = ArchitectureDraft.sample().copy(
            grade = "EC",
            sections = mapOf("walls" to source)
        )
        val pending = ArchitecturePayloads.forSection("walls", draft)!!["payload"]!!.jsonObject
        assertEquals("false", pending["group_documented"]!!.jsonPrimitive.content)
        val checklist = pending["wall_checklist"]!!.jsonObject
        assertTrue(
            checklist.values.all {
                it.jsonObject["state"]!!.jsonPrimitive.content ==
                    "pending"
            }
        )
        val entered = source + mapOf(
            "group_documented" to "true",
            "checklist.2.state" to "documented_unverified",
            "checklist.2.doc" to "A-102"
        )
        val result = ArchitecturePayloads.forSection(
            "walls",
            draft.copy(sections = mapOf("walls" to entered))
        )!!
        val checklistResult = result["payload"]!!.jsonObject["wall_checklist"]!!.jsonObject
        val item = checklistResult["item_2"]!!.jsonObject
        assertEquals("documented_unverified", item["state"]!!.jsonPrimitive.content)
        assertEquals("A-102", item["document_ref"]!!.jsonPrimitive.content)
        val invalid = entered + ("checklist.2.state" to "approved")
        assertNull(
            ArchitecturePayloads.forSection(
                "walls",
                draft.copy(sections = mapOf("walls" to invalid))
            )
        )
    }
}
