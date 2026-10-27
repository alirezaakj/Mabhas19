package ir.mabhas19.feature.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Entry points that the website home page exposes.
 *
 * The keys are stable identifiers used by navigation and tests; they are not API values.
 * Accent colours follow [docs/discovery/design.md] discipline colours (observed brand green and
 * amber, proposed blue/teal/purple accents).
 */
enum class HomeSection(
    val key: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val glyph: String,
    val lightAccent: Color,
    val darkAccent: Color
) {
    ARCHITECTURE(
        key = "architecture",
        titleRes = R.string.section_architecture_title,
        descriptionRes = R.string.section_architecture_description,
        glyph = "⌂",
        lightAccent = Color(0xFF009B60),
        darkAccent = Color(0xFF00DF81)
    ),
    MECHANICAL(
        key = "mechanical",
        titleRes = R.string.section_mechanical_title,
        descriptionRes = R.string.section_mechanical_description,
        glyph = "⚙",
        lightAccent = Color(0xFFB96C00),
        darkAccent = Color(0xFFF2AA22)
    ),
    ELECTRICAL(
        key = "electrical",
        titleRes = R.string.section_electrical_title,
        descriptionRes = R.string.section_electrical_description,
        glyph = "ϟ",
        lightAccent = Color(0xFF1D6FD0),
        darkAccent = Color(0xFF63A9FF)
    ),
    RENEWABLES(
        key = "renewables",
        titleRes = R.string.section_renewables_title,
        descriptionRes = R.string.section_renewables_description,
        glyph = "☼",
        lightAccent = Color(0xFF0F766E),
        darkAccent = Color(0xFF2DD4BF)
    ),
    AIRTIGHTNESS(
        key = "airtightness",
        titleRes = R.string.section_airtightness_title,
        descriptionRes = R.string.section_airtightness_description,
        glyph = "◫",
        lightAccent = Color(0xFF7C3AED),
        darkAccent = Color(0xFFA78BFA)
    ),
    DAYLIGHT(
        key = "daylight",
        titleRes = R.string.section_daylight_title,
        descriptionRes = R.string.section_daylight_description,
        glyph = "☀",
        lightAccent = Color(0xFF0F766E),
        darkAccent = Color(0xFF2DD4BF)
    ),
    DOSSIER(
        key = "dossier",
        titleRes = R.string.section_dossier_title,
        descriptionRes = R.string.section_dossier_description,
        glyph = "▤",
        lightAccent = Color(0xFF6D28D9),
        darkAccent = Color(0xFFC4B5FD)
    ),
    TEAM(
        key = "team",
        titleRes = R.string.section_team_title,
        descriptionRes = R.string.section_team_description,
        glyph = "⚑",
        lightAccent = Color(0xFF1D6FD0),
        darkAccent = Color(0xFF63A9FF)
    ),
    DASHBOARD(
        key = "dashboard",
        titleRes = R.string.section_dashboard_title,
        descriptionRes = R.string.section_dashboard_description,
        glyph = "▦",
        lightAccent = Color(0xFF475569),
        darkAccent = Color(0xFFCBD5E1)
    ),
    LOGIN(
        key = "login",
        titleRes = R.string.section_login_title,
        descriptionRes = R.string.section_login_description,
        glyph = "▣",
        lightAccent = Color(0xFF009B60),
        darkAccent = Color(0xFF00DF81)
    );

    companion object {
        /** Design cards shown on the workspace, in website order. */
        val designSections: List<HomeSection> = listOf(
            ARCHITECTURE,
            MECHANICAL,
            ELECTRICAL,
            RENEWABLES,
            AIRTIGHTNESS
        )

        fun fromKey(key: String): HomeSection? = entries.firstOrNull { it.key == key }
    }
}
