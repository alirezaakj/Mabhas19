package ir.mabhas19.feature.architecture

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitecturePayloadsTest {
    @Test fun a04WallRequestIsEvaluationOnlyAndConvertsMillimetresToCentimetres() {
        val values = mapOf(
            "wall.5.id" to "WL-006", "wall.5.name" to "Test wall", "wall.5.adjacency" to "outside",
            "wall.5.insulation" to "external", "wall.5.layer.3.type" to "lambda",
            "wall.5.layer.3.material" to "EPS", "wall.5.layer.3.thickness_mm" to "۵۰",
            "wall.5.layer.3.lambda_w_mk" to "۰٫۰۳۸", "wall.5.layer.3.is_insulation" to "true"
        )
        val payload = ArchitecturePayloads.forSection("walls", draft("walls", values))!!
        assertEquals(setOf("request", "payload"), payload.keys)
        assertEquals("evaluate", payload["request"]!!.jsonPrimitive.content)
        val body = payload["payload"]!!.jsonObject
        assertEquals(1000.0, body["net_area_m2"]!!.jsonPrimitive.double, 0.0)
        assertEquals(2.0, body["building_group"]!!.jsonPrimitive.double, 0.0)
        assertFalse(body["group_documented"]!!.jsonPrimitive.boolean)
        val wall = body["walls"]!!.jsonArray.single().jsonObject
        val layer = wall["layers"]!!.jsonArray.single().jsonObject
        assertEquals(5.0, layer["thickness_cm"]!!.jsonPrimitive.double, 0.0)
        assertEquals(0.038, layer["lambda_w_mk"]!!.jsonPrimitive.double, 0.0)
        assertEquals(JsonNull, layer["resistance_m2kw"])
        assertTrue(layer["is_insulation"]!!.jsonPrimitive.boolean)
        assertFalse(payload.toString().contains("_csrf"))
        assertFalse(payload.toString().contains("project_id"))
        assertFalse(payload.toString().contains("\"save\""))
    }

    @Test fun a04IncompleteAndMalformedLayersCannotBecomeZeroResistance() {
        val incomplete = draft("walls", mapOf("wall.0.id" to "WL-001"))
        assertNull(ArchitecturePayloads.forSection("walls", incomplete))
        assertTrue(
            ArchitecturePayloads.validation(
                "walls",
                incomplete
            ).single().startsWith("field_required:")
        )
        val values = mapOf(
            "wall.0.id" to "WL-001",
            "wall.0.adjacency" to "outside",
            "wall.0.insulation" to "external",
            "wall.0.layer.0.type" to "resistance",
            "wall.0.layer.0.material" to "Block",
            "wall.0.layer.0.resistance_m2kw" to "invalid"
        )
        val malformed = draft("walls", values)
        assertNull(ArchitecturePayloads.forSection("walls", malformed))
        assertEquals(
            listOf("number_invalid:wall.0.layer.0.resistance_m2kw"),
            ArchitecturePayloads.validation("walls", malformed)
        )
    }

    @Test fun a05MultipleRoofLocationsAreNotSilentlyReducedToTheFirst() {
        val values = mapOf("roof.0.id" to "RF-001", "roof.1.id" to "RF-002")
        assertNull(ArchitecturePayloads.forSection("roof_floor", draft("roof_floor", values)))
        assertEquals(
            listOf("single_roof_required"),
            ArchitecturePayloads.validation("roof_floor", draft("roof_floor", values))
        )
    }

    @Test fun a06GlazingUsesSourceFieldNamesAndCanonicalNumbers() {
        val values = mapOf(
            "opening.2.id" to "GL-003", "opening.2.parent_wall_id" to "WL-001",
            "opening.2.orientation" to "south", "opening.2.adjacency" to "outside",
            "opening.2.U_wm2k" to "۲٫۱", "opening.2.SHGC" to "۰٫۵", "opening.2.Tv" to "۰٫۶۸",
            "opening.2.shading_requested" to "true", "opening.2.shading_source" to "A-107"
        )
        val body = ArchitecturePayloads.forSection("glazing", draft("glazing", values))!!
        val opening = body["openings"]!!.jsonArray.single().jsonObject
        assertEquals("GL-003", opening["opening_id"]!!.jsonPrimitive.content)
        assertEquals(2.1, opening["U_wm2k"]!!.jsonPrimitive.double, 0.0)
        assertEquals(0.68, opening["Tv"]!!.jsonPrimitive.double, 0.0)
        assertEquals("heating", body["dominant_need"]!!.jsonPrimitive.content)
        assertTrue(opening["appendix10_shading_requested"]!!.jsonPrimitive.boolean)
        assertEquals(JsonNull, opening["appendix9_window"])
        assertEquals(JsonNull, opening["appendix9_glass"])
        assertNull(
            ArchitecturePayloads.forSection(
                "glazing",
                draft(
                    "glazing",
                    values + ("opening.2.Tv" to "2")
                )
            )
        )
    }

    @Test fun a12ChecklistEvaluationLeavesMeasuredTestUnknown() {
        val ids = listOf(
            "window_perimeter", "operable_seals", "wall_roof_joint", "wall_floor_joint",
            "wall_corner",
            "penetrations", "shafts", "hatches", "curtain_wall", "air_barrier", "inspection"
        )
        val values =
            ids.associate { "air.$it.status" to "unknown" } +
                ("air.window_perimeter.status" to "yes")
        val payload = ArchitecturePayloads.forSection(
            "airtightness",
            draft("airtightness", values)
        )!!
        assertEquals(11, payload["entries"]!!.jsonObject.size)
        assertTrue(payload["test"]!!.jsonObject.isEmpty())
        assertTrue(payload["design"]!!.jsonObject.isEmpty())
        assertEquals(
            "yes",
            payload["entries"]!!.jsonObject["window_perimeter"]!!.jsonObject["status"]!!
                .jsonPrimitive.content
        )
    }

    @Test fun a09ShadingNeverConvertsUnknownApplicabilityToFalse() {
        val values = mapOf(
            "shade.0.opening_id" to "GL-001",
            "shade.0.orientation" to "south",
            "shade.0.type" to "none",
            "shade.0.mandatory_for_window" to "unknown"
        )
        assertNull(ArchitecturePayloads.forSection("shading", draft("shading", values)))
        assertEquals(
            listOf("choice_invalid:shade.0.mandatory_for_window"),
            ArchitecturePayloads.validation("shading", draft("shading", values))
        )
        val complete = values + mapOf(
            "shade.0.mandatory_for_window" to "false",
            "shade.0.win_u" to "۲٫۱",
            "shade.0.win_shgc" to "۰٫۵",
            "shade.0.win_tv" to "۰٫۶۸",
            "shade.0.glare_status" to "none",
            "shade.0.control_day" to "tir_1",
            "shade.0.control_hour" to "12:00"
        )
        val payload = ArchitecturePayloads.forSection("shading", draft("shading", complete))!!
        val item = payload["items"]!!.jsonArray.single().jsonObject
        assertEquals("GL-001", item["window_id"]!!.jsonPrimitive.content)
        assertFalse(item["shading_installed"]!!.jsonPrimitive.boolean)
        assertFalse(item["mandatory_for_window"]!!.jsonPrimitive.boolean)
        assertEquals(JsonNull, item["window_height_m"])
        assertEquals("12:00", item["control_hour"]!!.jsonPrimitive.content)
    }

    @Test fun a11WindowlessRoomDoesNotAcquireInventedDaylightGeometry() {
        val values = mapOf(
            "daylight.area_m2" to "۳۰",
            "room.0.name" to "Room 1",
            "room.0.window_count" to "۰",
            "room.0.width_m" to "۶",
            "room.0.depth_m" to "۵",
            "all_rooms_recorded" to "true"
        )
        val payload = ArchitecturePayloads.forSection("daylight", draft("daylight", values))!!
        assertEquals("prescriptive", payload["mode"]!!.jsonPrimitive.content)
        assertEquals("yes", payload["complete_space_inventory"]!!.jsonPrimitive.content)
        val room = payload["rooms"]!!.jsonArray.single().jsonObject
        assertEquals(0.0, room["window_count"]!!.jsonPrimitive.double, 0.0)
        assertEquals(JsonNull, room["window_width_m"])
        assertEquals(JsonNull, room["window_head_m"])
        assertEquals(JsonNull, room["shaded"])
        val mismatch = values + ("room.0.window_count" to "2")
        assertNull(ArchitecturePayloads.forSection("daylight", draft("daylight", mismatch)))
        assertEquals(
            listOf("window_inventory_mismatch:room.0."),
            ArchitecturePayloads.validation("daylight", draft("daylight", mismatch))
        )
    }

    @Test fun a14BoundaryMappingRetainsUnknownInsteadOfInventingConditionedNeighbor() {
        val values = mapOf(
            "adjacent.3.id" to "ADJ-004",
            "adjacent.3.area_m2" to "۱۲٫۵",
            "adjacent.3.interface" to "unknown",
            "adjacent.3.gap" to "unknown",
            "adjacent.3.neighbor_condition" to "unknown",
            "adjacent.3.wall_ref" to "WL-001"
        )
        val payload = ArchitecturePayloads.forSection("boundaries", draft("boundaries", values))!!
        val adjacent = payload["adjacent_walls"]!!.jsonArray.single().jsonObject
        assertEquals(12.5, adjacent["area_m2"]!!.jsonPrimitive.double, 0.0)
        assertEquals("unknown", adjacent["neighbor_condition"]!!.jsonPrimitive.content)
        assertTrue(payload["space_connections"]!!.jsonArray.isEmpty())
    }

    private fun draft(section: String, values: Map<String, String>) =
        ArchitectureDraft.sample().copy(
            grade = "EC",
            sections = mapOf(section to values)
        )
}
