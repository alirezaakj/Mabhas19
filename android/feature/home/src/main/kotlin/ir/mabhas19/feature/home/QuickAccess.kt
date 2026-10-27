package ir.mabhas19.feature.home

internal data class QuickAccess(
    val tag: String,
    val titleRes: Int,
    val section: HomeSection? = null
)

internal val QuickAccessItems = listOf(
    QuickAccess("projects", R.string.home_quick_projects),
    QuickAccess("daylight", R.string.section_daylight_title, HomeSection.DAYLIGHT),
    QuickAccess("dossier", R.string.section_dossier_title, HomeSection.DOSSIER),
    QuickAccess("team", R.string.section_team_title, HomeSection.TEAM),
    QuickAccess("dashboard", R.string.section_dashboard_title, HomeSection.DASHBOARD)
)
