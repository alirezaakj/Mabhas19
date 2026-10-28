@file:Suppress("TooManyFunctions")

package ir.mabhas19.feature.architecture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId
import java.util.Locale

private val WorkspaceColors = lightColorScheme(
    primary = Color(0xFF065F46),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF065F46),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF0F4F0),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainerHighest = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun ArchitectureScreen(
    state: ArchitectureState,
    evaluation: ArchitectureEvaluation,
    projects: List<Project>,
    onChange: (ArchitectureDraft) -> Unit,
    onChooseProject: (ProjectId) -> Unit,
    onCreateProject: (String, Boolean) -> Unit,
    onSave: () -> Unit,
    onEvaluate: (String) -> Unit,
    onBack: () -> Unit
) {
    MaterialTheme(colorScheme = WorkspaceColors) {
        ArchitectureWorkspace(
            state, evaluation, projects, onChange, onChooseProject, onCreateProject,
            onSave, onEvaluate, onBack
        )
    }
}

@Composable
private fun ArchitectureWorkspace(
    state: ArchitectureState,
    evaluation: ArchitectureEvaluation,
    projects: List<Project>,
    onChange: (ArchitectureDraft) -> Unit,
    onChooseProject: (ProjectId) -> Unit,
    onCreateProject: (String, Boolean) -> Unit,
    onSave: () -> Unit,
    onEvaluate: (String) -> Unit,
    onBack: () -> Unit
) {
    var step by rememberSaveable(state.projectId?.value) { mutableIntStateOf(0) }
    var substep by rememberSaveable(state.projectId?.value) { mutableStateOf("general") }
    var control by rememberSaveable(state.projectId?.value) { mutableStateOf("shading") }
    val scroll = rememberLazyListState()
    val headings = listOf(
        R.string.architecture_general,
        R.string.architecture_walls,
        R.string.architecture_roof,
        R.string.architecture_windows,
        R.string.architecture_controls,
        R.string.architecture_dossier
    )
    LaunchedEffect(step, substep, control) { scroll.scrollToItem(0) }
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("architecture_screen").semantics {
                testTagsAsResourceId =
                    true
            },
            state = scroll,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("architecture_back")) {
                        Text(stringResource(R.string.architecture_back))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            stringResource(R.string.architecture_edition),
                            Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Text(
                    stringResource(R.string.architecture_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.architecture_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    headings.forEachIndexed { index, label ->
                        FilterChip(
                            selected = step == index,
                            onClick = { step = index },
                            label = { Text("${persian(index + 1)}  ${stringResource(label)}") },
                            modifier = Modifier.testTag("architecture_step_$index")
                        )
                    }
                }
            }
            item {
                if (state.loading) {
                    Text(stringResource(R.string.architecture_loading))
                } else if (state.projectId == null) {
                    ProjectChooser(projects, onChooseProject, onCreateProject)
                } else {
                    ActiveProject(state, projects, onChooseProject)
                }
            }
            if (state.error != null) {
                item {
                    Text(
                        stringResource(architectureErrorLabel(state.error)),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (state.projectId != null && !state.loading && state.editable) {
                if (step == 0) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(substep == "general", {
                                substep = "general"
                            }, { Text(stringResource(R.string.architecture_conditions)) })
                            FilterChip(substep == "occupancy", {
                                substep = "occupancy"
                            }, { Text(stringResource(R.string.architecture_occupancy)) })
                        }
                    }
                    if (substep == "general") {
                        item { ContextPanel(state.draft, onChange) }
                        item { MethodPanel(state.draft, onChange) }
                        item { EligibilityPanel(state.draft, onChange) }
                        item { DynamicMatrix(state.draft) }
                        item { ScreeningPanel(state.draft) }
                    } else {
                        item { SectionEditor("occupancy", state.draft, onChange) }
                    }
                } else if (step == 4) {
                    item {
                        val sections = listOf(
                            "shading" to R.string.architecture_control_shading,
                            "daylight" to R.string.architecture_control_daylight,
                            "airtightness" to R.string.architecture_control_airtightness,
                            "thermal_bridges" to R.string.architecture_control_bridges,
                            "boundaries" to R.string.architecture_control_boundaries
                        )
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sections.forEach { (key, label) ->
                                FilterChip(control == key, {
                                    control = key
                                }, {
                                    Text(stringResource(label))
                                }, modifier = Modifier.testTag("architecture_control_$key"))
                            }
                        }
                    }
                    item {
                        SectionEditor(control, state.draft, onChange)
                        CalculationPanel(control, evaluation) { onEvaluate(control) }
                    }
                } else if (step == 5) {
                    item { WorkspaceSummary(state.draft) }
                } else {
                    val section = mapOf(1 to "walls", 2 to "roof_floor", 3 to "glazing")
                        .getValue(step)
                    item {
                        SectionEditor(section, state.draft, onChange)
                        CalculationPanel(section, evaluation) { onEvaluate(section) }
                    }
                }
                item {
                    Button(
                        onClick = onSave,
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth().testTag("architecture_save")
                    ) {
                        Text(
                            stringResource(
                                if (state.saving) {
                                    R.string.architecture_saving
                                } else {
                                    R.string.architecture_save
                                }
                            )
                        )
                    }
                    Text(
                        stringResource(
                            if (state.saved) {
                                R.string.architecture_saved
                            } else {
                                R.string.architecture_unsaved
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp).testTag("architecture_save_status")
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = {
                            step--
                        }, enabled = step > 0) {
                            Text(stringResource(R.string.architecture_previous))
                        }
                        Button(onClick = {
                            step++
                        }, enabled = step < 5) { Text(stringResource(R.string.architecture_next)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectChooser(
    projects: List<Project>,
    onChoose: (ProjectId) -> Unit,
    onCreate: (String, Boolean) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    val sampleTitle = stringResource(R.string.architecture_sample_title)
    ArchitecturePanel(
        stringResource(R.string.architecture_project_required),
        description = stringResource(R.string.architecture_project_help)
    ) {
        if (projects.isNotEmpty()) {
            ArchitecturePicker(
                stringResource(R.string.architecture_select_project),
                "",
                projects.map {
                    it.id.value to
                        it.title
                },
                {
                    if (it.isNotBlank()) onChoose(ProjectId(it))
                },
                "architecture_project_picker",
                clearable = false
            )
        }
        ArchitectureField(stringResource(R.string.architecture_project_title), title, {
            title = it
        }, "architecture_new_project_title")
        Button(onClick = {
            onCreate(title, false)
        }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.architecture_create))
        }
        OutlinedButton(onClick = {
            onCreate(sampleTitle, true)
        }, modifier = Modifier.fillMaxWidth().testTag("architecture_create_sample")) {
            Text(stringResource(R.string.architecture_sample))
        }
        Text(
            stringResource(R.string.architecture_sample_notice),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ActiveProject(
    state: ArchitectureState,
    projects: List<Project>,
    onChoose: (ProjectId) -> Unit
) {
    ArchitecturePanel(stringResource(R.string.architecture_active_project)) {
        ArchitecturePicker(
            stringResource(R.string.architecture_select_project),
            state.projectId?.value.orEmpty(),
            projects.map {
                it.id.value to it.title
            },
            { if (it.isNotBlank()) onChoose(ProjectId(it)) },
            "architecture_project_picker",
            clearable = false
        )
        Text(
            stringResource(R.string.architecture_local),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (state.projectTitle == stringResource(R.string.architecture_sample_title)) {
            Text(
                stringResource(R.string.architecture_sample_notice),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ContextPanel(draft: ArchitectureDraft, onChange: (ArchitectureDraft) -> Unit) {
    val city = ArchitectureCatalog.cities.find { it.id == draft.cityId }
    val province = draft.province.ifBlank { city?.province.orEmpty() }
    ArchitecturePanel(
        stringResource(R.string.architecture_context_heading),
        "۰۱",
        stringResource(R.string.architecture_context_help)
    ) {
        ArchitecturePicker(
            stringResource(R.string.architecture_province),
            province,
            ArchitectureCatalog.provinces.map { it to it },
            {
                onChange(
                    draft.copy(
                        province = it,
                        cityId = if (it.isBlank() ||
                            city?.province == it
                        ) {
                            draft.cityId
                        } else {
                            ""
                        }
                    )
                )
            },
            "architecture_province"
        )
        ArchitecturePicker(
            stringResource(R.string.architecture_city),
            draft.cityId,
            ArchitectureCatalog.cities.filter {
                province.isBlank() || it.province == province
            }.map {
                it.id to
                    "${it.province} / ${it.name} (${it.id})"
            },
            {
                onChange(
                    draft.copy(
                        cityId = it,
                        province = ArchitectureCatalog.cities.find { cityOption ->
                            cityOption.id ==
                                it
                        }?.province.orEmpty()
                    )
                )
            },
            "architecture_city"
        )
        ArchitecturePicker(
            stringResource(R.string.architecture_main_occupancy),
            draft.occupancyId,
            ArchitectureCatalog.occupancies.map { it.id to "${it.type} · ${it.label} (${it.id})" },
            { onChange(draft.copy(occupancyId = it)) },
            "architecture_occupancy"
        )
        ArchitectureField(stringResource(R.string.architecture_area), draft.area, {
            onChange(draft.copy(area = it))
        }, "architecture_area", numeric = true)
        ArchitectureField(stringResource(R.string.architecture_floors), draft.floors, {
            onChange(draft.copy(floors = it))
        }, "architecture_floors", numeric = true)
        Text(
            stringResource(R.string.architecture_area_hint),
            style = MaterialTheme.typography.bodySmall
        )
        ClimateSummary(draft)
        if (city != null) {
            Text(
                stringResource(R.string.architecture_source, city.page, city.row, city.id),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            stringResource(R.string.architecture_mixed_use_hint),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ClimateSummary(draft: ArchitectureDraft) {
    val city = ArchitectureCatalog.cities.find { it.id == draft.cityId }
    val degree = when (city?.degree) {
        "high" -> R.string.architecture_degree_high
        "medium" -> R.string.architecture_degree_medium
        "low" -> R.string.architecture_degree_low
        else -> R.string.architecture_unknown
    }
    val need = when (city?.need) {
        "heating" -> R.string.architecture_heating
        "cooling" -> R.string.architecture_cooling
        "both" -> R.string.architecture_both
        else -> R.string.architecture_unknown
    }
    val group = ArchitectureRules.group(draft)
    Surface(
        color = Color(0xFFF0FDF4),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "${stringResource(R.string.architecture_degree)}: ${stringResource(degree)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "${stringResource(R.string.architecture_need)}: ${stringResource(need)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "${stringResource(
                    R.string.architecture_group
                )}: ${group?.let(::persian) ?: stringResource(R.string.architecture_unknown)}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("architecture_group")
            )
        }
    }
}

@Composable
private fun MethodPanel(draft: ArchitectureDraft, onChange: (ArchitectureDraft) -> Unit) {
    val group = ArchitectureRules.group(draft)
    val methodOptions = if (group == 4) {
        listOf("mandatory_only" to stringResource(R.string.architecture_mandatory_only))
    } else {
        listOf(
            "prescriptive" to stringResource(R.string.architecture_prescriptive),
            "tradeoff" to stringResource(R.string.architecture_tradeoff),
            "energy_need" to stringResource(R.string.architecture_energy_need),
            "energy_performance" to stringResource(R.string.architecture_energy_performance)
        )
    }
    ArchitecturePanel(
        stringResource(R.string.architecture_method_heading),
        "۰۲",
        stringResource(R.string.architecture_method_help)
    ) {
        ArchitecturePicker(
            stringResource(
                R.string.architecture_method
            ),
            draft.method,
            methodOptions,
            {
                onChange(draft.copy(method = it))
            },
            "architecture_method"
        )
        ArchitectureField(stringResource(R.string.architecture_year), draft.year, {
            onChange(draft.copy(year = it))
        }, "architecture_year", numeric = true)
        ArchitecturePicker(
            stringResource(R.string.architecture_min_grade),
            draft.minGrade,
            listOf("EC", "EC+", "EC++").map {
                it to it
            },
            { onChange(draft.copy(minGrade = it)) },
            clearable = false
        )
        val grades = listOf("mandatory_ch4_only", "EC", "EC+", "EC++", "ECnZ")
            .filter { ArchitectureRules.gradeAllowed(draft.copy(grade = it)) == true }
            .map {
                it to
                    if (it ==
                        "mandatory_ch4_only"
                    ) {
                        stringResource(R.string.architecture_mandatory_only)
                    } else {
                        it
                    }
            }
        ArchitecturePicker(
            stringResource(
                R.string.architecture_target_grade
            ),
            draft.grade,
            grades,
            {
                onChange(draft.copy(grade = it))
            },
            "architecture_grade"
        )
        ArchitectureField(stringResource(R.string.architecture_solar_area), draft.solarArea, {
            onChange(draft.copy(solarArea = it))
        }, numeric = true)
        ArchitectureField(stringResource(R.string.architecture_obstacle), draft.obstacleAngle, {
            onChange(draft.copy(obstacleAngle = it))
        }, numeric = true)
        val solarOptions =
            mutableListOf("low_or_none" to stringResource(R.string.architecture_solar_low))
        if (ArchitectureRules.solarEligibility(draft) ==
            true
        ) {
            solarOptions.add("high" to stringResource(R.string.architecture_solar_high))
        }
        ArchitecturePicker(stringResource(R.string.architecture_solar), draft.solar, solarOptions, {
            onChange(draft.copy(solar = it))
        }, clearable = false)
        Text(
            stringResource(R.string.architecture_solar_hint),
            style = MaterialTheme.typography.bodySmall
        )
        val continuityOptions =
            mutableListOf("continuous" to stringResource(R.string.architecture_continuous))
        if (ArchitectureRules.intermittentAllowed(draft) ==
            true
        ) {
            continuityOptions.add(
                "intermittent" to stringResource(R.string.architecture_intermittent)
            )
        }
        ArchitecturePicker(
            stringResource(
                R.string.architecture_continuity
            ),
            draft.continuity,
            continuityOptions,
            {
                onChange(draft.copy(continuity = it))
            },
            clearable = false
        )
    }
}

@Composable
private fun EligibilityPanel(draft: ArchitectureDraft, onChange: (ArchitectureDraft) -> Unit) {
    ArchitecturePanel(
        stringResource(R.string.architecture_eligibility_heading),
        "۰۳",
        stringResource(R.string.architecture_eligibility_help)
    ) {
        ArchitecturePicker(
            stringResource(R.string.architecture_inertia),
            draft.inertia,
            listOf(
                "low" to stringResource(R.string.architecture_inertia_low),
                "medium" to stringResource(R.string.architecture_inertia_medium),
                "high" to stringResource(R.string.architecture_inertia_high)
            ),
            { onChange(draft.copy(inertia = it)) },
            "architecture_inertia"
        )
        ArchitectureCheck(
            stringResource(
                R.string.architecture_inertia_doc
            ),
            draft.inertiaDocumented,
            {
                onChange(draft.copy(inertiaDocumented = it))
            }
        )
        ArchitectureCheck(
            stringResource(
                R.string.architecture_ministry_doc
            ),
            draft.ministryChecked,
            {
                onChange(draft.copy(ministryChecked = it))
            }
        )
        Text(stringResource(R.string.architecture_facades), fontWeight = FontWeight.Bold)
        listOf(
            "north" to R.string.architecture_north,
            "south" to R.string.architecture_south,
            "east" to R.string.architecture_east,
            "west" to R.string.architecture_west
        ).forEach { (direction, label) ->
            val facade = draft.facades[direction] ?: FacadeDraft()
            fun change(next: FacadeDraft) = onChange(
                draft.copy(
                    facades =
                    draft.facades + (direction to next)
                )
            )
            ArchitecturePanel(stringResource(label)) {
                ArchitectureCheck(
                    stringResource(
                        R.string.architecture_facade_absent
                    ),
                    facade.absent,
                    {
                        change(facade.copy(absent = it))
                    }
                )
                if (!facade.absent) {
                    ArchitectureField(
                        stringResource(
                            R.string.architecture_facade_area
                        ),
                        facade.area,
                        {
                            change(facade.copy(area = it))
                        },
                        "facade_${direction}_area",
                        numeric = true
                    )
                    ArchitectureField(
                        stringResource(
                            R.string.architecture_glazing_area
                        ),
                        facade.glazing,
                        {
                            change(facade.copy(glazing = it))
                        },
                        "facade_${direction}_glazing",
                        numeric = true
                    )
                    val ratio = ArchitectureRules.facadeRatio(facade)
                    Text(
                        stringResource(
                            R.string.architecture_wwr,
                            ratio?.let { String.format(Locale.ROOT, "%.2f%%", it * 100) } ?: "—"
                        )
                    )
                }
            }
        }
        Text(
            stringResource(R.string.architecture_wwr_hint),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ScreeningPanel(draft: ArchitectureDraft) {
    val screening = ArchitectureRules.screening(draft)
    ArchitecturePanel(stringResource(R.string.architecture_result_heading), "۰۵") {
        Text(
            stringResource(
                when (screening.methodAllowed) {
                    true -> R.string.architecture_preliminary
                    false -> R.string.architecture_not_eligible
                    null -> R.string.architecture_needs_data
                }
            ),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("architecture_screening")
        )
        (screening.issues + screening.pending).map {
            it.substringBefore(':')
        }.distinct().forEach { code ->
            Text("• ${ruleMessage(code)}", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            stringResource(R.string.architecture_not_certificate),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ruleMessage(code: String): String = stringResource(
    issueLabelIds[code.substringBefore('_')] ?: R.string.architecture_rule_missing
)

private val issueLabelIds = mapOf(
    "city" to R.string.architecture_rule_city,
    "occupancy" to R.string.architecture_rule_occupancy,
    "area" to R.string.architecture_rule_area,
    "floors" to R.string.architecture_rule_floors,
    "year" to R.string.architecture_rule_year,
    "method" to R.string.architecture_rule_method,
    "grade" to R.string.architecture_rule_grade,
    "min" to R.string.architecture_rule_grade,
    "inertia" to R.string.architecture_rule_inertia,
    "ministry" to R.string.architecture_rule_evidence,
    "solar" to R.string.architecture_rule_solar,
    "facade" to R.string.architecture_rule_facade,
    "intermittent" to R.string.architecture_rule_continuity,
    "continuity" to R.string.architecture_rule_continuity
)

private fun architectureErrorLabel(error: ArchitectureError): Int = when (error) {
    ArchitectureError.STORAGE -> R.string.architecture_storage_error
    ArchitectureError.CONFLICT -> R.string.architecture_conflict_error
    ArchitectureError.UNSUPPORTED_DRAFT -> R.string.architecture_unsupported_error
    ArchitectureError.INVALID_TITLE -> R.string.architecture_invalid_title
}

@Composable
private fun DynamicMatrix(draft: ArchitectureDraft) {
    ArchitecturePanel(stringResource(R.string.architecture_status_matrix), "۰۴") {
        architectureSwitches(draft).forEach { row ->
            val label = matrixLabelIds.getValue(row.key)
            val enabled = row.enabled == true
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(label),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(
                        when (row.enabled) {
                            true -> R.string.architecture_enabled
                            false -> R.string.architecture_disabled
                            null -> R.string.architecture_unknown
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionEditor(
    section: String,
    draft: ArchitectureDraft,
    onChange: (ArchitectureDraft) -> Unit
) {
    EnvelopeEditor(section, draft.sections[section].orEmpty()) { values ->
        onChange(draft.copy(sections = draft.sections + (section to values)))
    }
}

@Composable
private fun WorkspaceSummary(draft: ArchitectureDraft) {
    ArchitecturePanel(stringResource(R.string.architecture_summary)) {
        ClimateSummary(draft)
        Text("${stringResource(R.string.architecture_area)}: ${draft.area}")
        Text("${stringResource(R.string.architecture_floors)}: ${draft.floors}")
        Text("${stringResource(R.string.architecture_target_grade)}: ${draft.grade}")
        Text(
            stringResource(
                R.string.architecture_summary_entries,
                draft.sections.values.sumOf {
                    it.count { value -> value.value.isNotBlank() }
                },
                draft.sections.size
            )
        )
        Text(
            stringResource(R.string.architecture_summary_draft),
            style = MaterialTheme.typography.bodySmall
        )
    }
    ScreeningPanel(draft)
}

private fun persian(number: Int): String = number.toString().map {
    if (it in
        '0'..'9'
    ) {
        '۰' + (it - '0')
    } else {
        it
    }
}.joinToString("")

private val matrixLabelIds = mapOf(
    "solar" to R.string.architecture_solar_high,
    "intermittent" to R.string.architecture_intermittent,
    "chapter5" to R.string.architecture_matrix_prescriptive,
    "nz" to R.string.architecture_matrix_nz,
    "chiller" to R.string.architecture_matrix_chiller,
    "roof_alternative" to R.string.architecture_matrix_roof_alternative,
    "cool_roof" to R.string.architecture_matrix_cooling,
    "water_labels" to R.string.architecture_matrix_water_labels,
    "vsd" to R.string.architecture_matrix_vsd,
    "lms" to R.string.architecture_matrix_lms,
    "year" to R.string.architecture_matrix_year
)
