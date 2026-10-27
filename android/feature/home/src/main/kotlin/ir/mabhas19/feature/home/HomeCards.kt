package ir.mabhas19.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId

@Composable
internal fun sectionAccent(section: HomeSection): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        section.darkAccent
    } else {
        section.lightAccent
    }

@Composable
internal fun DesignSectionCard(section: HomeSection, onOpenSection: (HomeSection) -> Unit) {
    val accent = sectionAccent(section)
    Card(
        onClick = { onOpenSection(section) },
        modifier = Modifier.fillMaxWidth().testTag("home_section_${section.key}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    section.glyph,
                    modifier = Modifier.clearAndSetSemantics {},
                    style = MaterialTheme.typography.titleLarge,
                    color = accent
                )
            }
            Text(
                stringResource(section.titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(section.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(
                    if (section == HomeSection.AIRTIGHTNESS) {
                        R.string.section_enter_test
                    } else {
                        R.string.section_enter
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
        }
    }
}

@Composable
internal fun QuickAccessRow(
    item: QuickAccess,
    onOpenProjects: () -> Unit,
    onOpenSection: (HomeSection) -> Unit
) {
    Card(
        onClick = {
            val section = item.section
            if (section == null) onOpenProjects() else onOpenSection(section)
        },
        modifier = Modifier.fillMaxWidth().testTag("home_quick_${item.tag}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(item.titleRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "←",
                modifier = Modifier.clearAndSetSemantics {},
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun OverviewRow(projectCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OverviewCell(
            label = stringResource(R.string.home_overview_projects),
            value = projectCount.toPersianDigits(),
            hint = stringResource(R.string.home_overview_projects_hint),
            modifier = Modifier.weight(1f)
        )
        OverviewCell(
            label = stringResource(R.string.home_overview_team),
            value = stringResource(R.string.home_overview_team_value),
            hint = stringResource(R.string.home_overview_team_hint),
            modifier = Modifier.weight(1f)
        )
        OverviewCell(
            label = stringResource(R.string.home_overview_account),
            value = stringResource(R.string.home_overview_account_value),
            hint = stringResource(R.string.home_overview_account_hint),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OverviewCell(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun RecentHeader(onOpenProjects: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_recent_kicker),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.home_recent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        TextButton(
            onClick = onOpenProjects,
            modifier = Modifier.testTag("home_projects_all")
        ) {
            Text(stringResource(R.string.home_recent_all))
        }
    }
}

@Composable
internal fun RecentProjectRow(project: Project, onOpenProject: (ProjectId) -> Unit) {
    Card(
        onClick = { onOpenProject(project.id) },
        modifier = Modifier.fillMaxWidth().testTag("home_recent_${project.id.value}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                project.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(R.string.home_recent_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ActiveProjectCard(
    project: Project?,
    onOpenProject: () -> Unit,
    onOpenDetail: (ProjectId) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("home_active_project"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.home_active_kicker),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                project?.title ?: stringResource(R.string.home_active_empty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.home_active_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = { project?.let { onOpenDetail(it.id) } ?: onOpenProject() },
                modifier = Modifier.fillMaxWidth().testTag("home_active_action")
            ) {
                Text(stringResource(R.string.home_active_action))
            }
        }
    }
}

@Composable
internal fun OutputCard(onOpenDossier: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.home_output_kicker),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.home_output_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.home_output_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onOpenDossier,
                modifier = Modifier.fillMaxWidth().testTag("home_open_dossier")
            ) {
                Text(stringResource(R.string.home_output_action))
            }
        }
    }
}
