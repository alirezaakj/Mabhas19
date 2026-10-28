package ir.mabhas19.feature.mechanical

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val MechanicalCatalogJson = Json { ignoreUnknownKeys = true }

@Composable
fun MechanicalScreen(
    state: MechanicalState,
    projects: List<Project>,
    onChooseProject: (ProjectId) -> Unit,
    onCreateProject: (String) -> Unit,
    onChange: (MechanicalDraft) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val resources = LocalResources.current
    val catalog = remember(resources) {
        resources.openRawResource(R.raw.mechanical_catalog).bufferedReader().use {
            MechanicalCatalogJson.decodeFromString<MechanicalCatalog>(it.readText())
        }
    }
    var selectedTopic by rememberSaveable(state.projectId?.value) {
        mutableStateOf(catalog.topics.first().id)
    }
    val topic = catalog.topics.first { it.id == selectedTopic }
    val clauses = catalog.clauses.filter { it.topic == topic.id }
    val answers = state.draft.answers

    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("mechanical_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("mechanical_back")) {
                        Text("بازگشت")
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("مبحث ۱۹ · ویرایش چهارم ۱۳۹۹", Modifier.padding(10.dp))
                    }
                }
                Text(
                    "چک‌لیست عملیاتی تأسیسات مکانیکی",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ضوابط اجباری فصل ۴ و روش تجویزی فصل ۵",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ProjectChooser(
                    state = state,
                    projects = projects,
                    onChooseProject = onChooseProject,
                    onCreateProject = onCreateProject
                )
            }

            if (state.projectId != null && state.editable) {
                item { ProjectContextCard(state) }
                item {
                    TopicPicker(catalog.topics, selectedTopic) { selectedTopic = it }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(topic.title, style = MaterialTheme.typography.titleMedium)
                        Text(topic.desc, style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    TechnicalPanel(
                        fields = catalog.technicalFields.filter { it.topic == topic.id },
                        values = state.draft.technicalValues,
                        onChange = { id, value ->
                            onChange(
                                state.draft.copy(
                                    technicalValues = state.draft.technicalValues + (id to value)
                                )
                            )
                        }
                    )
                }
                items(clauses, key = { it.id }) { clause ->
                    ClauseCard(
                        clause,
                        answers,
                        onAnswer = { id, answer ->
                            onChange(
                                state.draft.copy(
                                    answers = state.draft.answers + (id to answer)
                                )
                            )
                        }
                    )
                }
                item {
                    SaveStatus(state, onSave)
                }
            } else if (state.projectId != null) {
                item { ErrorMessage(state.error) }
            }

            if (state.error != null && state.projectId == null) {
                item { ErrorMessage(state.error) }
            }
            item {
                Text(
                    "مرجع: mabhas19.ir · نمایش مکانیکی این برنامه یک پیش‌نویس محلی است و گواهی انطباق یا تأییدیه مقرراتی محسوب نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProjectChooser(
    state: MechanicalState,
    projects: List<Project>,
    onChooseProject: (ProjectId) -> Unit,
    onCreateProject: (String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("پروژه و زمینه مشترک", style = MaterialTheme.typography.titleMedium)
            if (state.loading) {
                Text("در حال بارگذاری پروژه…")
            } else if (state.projectId != null) {
                Text(state.projectTitle, fontWeight = FontWeight.SemiBold)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    projects.filter { it.id != state.projectId }.forEach { project ->
                        TextButton(onClick = { onChooseProject(project.id) }) {
                            Text("تغییر به ${project.title}")
                        }
                    }
                }
            } else {
                Text("برای ثبت بندها، یک پروژه محلی انتخاب کنید یا بسازید.")
                if (projects.isNotEmpty()) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        projects.forEach { project ->
                            OutlinedButton(onClick = { onChooseProject(project.id) }) {
                                Text(project.title)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عنوان پروژه") },
                    singleLine = true
                )
                Button(
                    onClick = { onCreateProject(title) },
                    enabled = title.isNotBlank() && !state.loading
                ) { Text("ایجاد پروژه محلی") }
            }
        }
    }
}

@Composable
private fun ProjectContextCard(state: MechanicalState) {
    val context = state.context
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("زمینه محاسبه", style = MaterialTheme.typography.titleSmall)
            Text(
                listOfNotNull(
                    context?.cityId?.let { "شهر: $it" },
                    context?.occupancyId?.let { "کاربری: $it" },
                    context?.conditionedAreaM2?.let { "مساحت: $it m²" },
                    context?.floorCount?.let { "تعداد طبقات: $it" }
                ).ifEmpty {
                    listOf("برای این پروژه هنوز شهر، کاربری و مشخصات ساختمانی ثبت نشده است.")
                }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "شمول بندها تا زمان تکمیل زمینه پروژه نامشخص می‌ماند.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopicPicker(
    topics: List<MechanicalTopic>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topics.forEach { topic ->
            FilterChip(
                selected = topic.id == selected,
                onClick = { onSelect(topic.id) },
                label = { Text("بخش ${persianNumber(topic.order)}") },
                modifier = Modifier.testTag("mechanical_topic_${topic.order}")
            )
        }
    }
}

@Composable
private fun ClauseCard(
    clause: MechanicalClause,
    answers: Map<String, MechanicalAnswer>,
    onAnswer: (String, MechanicalAnswer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("mechanical_clause_${clause.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) { Text(clause.code, Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) }
                Text(
                    if (clause.code.startsWith("19-4")) "فصل ۴ · اجباری" else "فصل ۵ · تجویزی",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                clause.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (clause.summary.isNotBlank()) {
                Text(clause.summary, style = MaterialTheme.typography.bodySmall)
            }
            clause.subclauses.forEach { subclause ->
                val answer = answers[subclause.id] ?: MechanicalAnswer()
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(subclause.text, style = MaterialTheme.typography.bodyMedium)
                        StatusPicker(answer.status) { status ->
                            onAnswer(subclause.id, answer.copy(status = status))
                        }
                        OutlinedTextField(
                            value = answer.evidence,
                            onValueChange = { onAnswer(subclause.id, answer.copy(evidence = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شماره نقشه / مدرک") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = answer.note,
                            onValueChange = { onAnswer(subclause.id, answer.copy(note = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("یادداشت بررسی") },
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPicker(status: String, onSelect: (String) -> Unit) {
    val choices = listOf(
        MechanicalAnswer.STATUS_UNKNOWN to "بررسی‌نشده",
        MechanicalAnswer.STATUS_COMPLIANT to "منطبق",
        MechanicalAnswer.STATUS_NONCOMPLIANT to "نقص",
        MechanicalAnswer.STATUS_NOT_APPLICABLE to "نامرتبط"
    )
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        choices.forEach { (value, label) ->
            FilterChip(
                selected = status == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun SaveStatus(state: MechanicalState, onSave: () -> Unit) {
    val saveStatusText = when {
        state.saving -> "در حال ذخیره در دستگاه…"
        state.saved -> "پیش‌نویس این پروژه روی دستگاه ذخیره شد."
        else -> listOf("تغییرات", "ذخیره‌نشده").joinToString(" ")
    }
    val saveStatusColor = if (state.error == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.error
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = saveStatusText, color = saveStatusColor)
        if (!state.saved || state.error != null) {
            Button(onClick = onSave, enabled = !state.saving) { Text("ذخیره پیش‌نویس") }
        }
        if (state.error != null) ErrorMessage(state.error)
    }
}

@Composable
private fun ErrorMessage(error: MechanicalError?) {
    val message = when (error) {
        MechanicalError.CONFLICT ->
            "نسخه محلی تغییر کرده است؛ " +
                "پیش‌نویس فقط‌خواندنی شد تا داده‌ای بازنویسی نشود."
        MechanicalError.UNSUPPORTED_DRAFT ->
            "ساختار پیش‌نویس این پروژه پشتیبانی نمی‌شود؛ " +
                "برای جلوگیری از بازنویسی، ویرایش غیرفعال است."
        MechanicalError.STORAGE -> "ذخیره‌سازی محلی انجام نشد. دوباره ذخیره کنید."
        MechanicalError.INVALID_TITLE -> "عنوان پروژه را وارد کنید."
        null -> return
    }
    Text(message, color = MaterialTheme.colorScheme.error)
}

private fun persianNumber(value: Int): String = value.toString().map { c ->
    if (c in '0'..'9') (c.code + 1728).toChar() else c
}.joinToString("")
