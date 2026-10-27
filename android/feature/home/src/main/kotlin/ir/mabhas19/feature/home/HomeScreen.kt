package ir.mabhas19.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId

/**
 * App workspace mirroring the website home page (F01).
 *
 * Only navigation, project counts and local project titles are live state; discipline cards and
 * member features open an explicit "not implemented yet" screen instead of pretending to work.
 */
@Composable
fun HomeScreen(
    projects: List<Project>,
    loadFailed: Boolean,
    onCreateProject: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onOpenSection: (HomeSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val recent = projects.sortedByDescending { it.updatedAtMillis }.take(3)
    val active = recent.firstOrNull()
    LazyColumn(
        modifier = modifier.testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Hero(
                projectCount = projects.size,
                onCreateProject = onCreateProject,
                onOpenProjects = onOpenProjects,
                onLogin = { onOpenSection(HomeSection.LOGIN) }
            )
        }
        if (loadFailed) {
            item {
                Text(
                    stringResource(R.string.home_storage_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item { SectionHeading(stringResource(R.string.home_sections_title)) }
        items(HomeSection.designSections, key = { it.key }) { section ->
            DesignSectionCard(section, onOpenSection)
        }
        item { SectionHeading(stringResource(R.string.home_quick_access)) }
        items(QuickAccessItems, key = { it.tag }) { item ->
            QuickAccessRow(
                item = item,
                onOpenProjects = onOpenProjects,
                onOpenSection = onOpenSection
            )
        }
        item { OverviewRow(projects.size) }
        item { RecentHeader(onOpenProjects) }
        if (recent.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.home_recent_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recent, key = { it.id.value }) { project ->
                RecentProjectRow(project, onOpenProject)
            }
        }
        item {
            ActiveProjectCard(
                project = active,
                onOpenProject = onOpenProjects,
                onOpenDetail = onOpenProject
            )
        }
        item {
            OutputCard(onOpenDossier = { onOpenSection(HomeSection.DOSSIER) })
        }
        item {
            Text(
                stringResource(R.string.home_footer),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Hero(
    projectCount: Int,
    onCreateProject: () -> Unit,
    onOpenProjects: () -> Unit,
    onLogin: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(primary.copy(alpha = 0.20f), surface, secondary.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = primary.copy(alpha = 0.12f),
            contentColor = primary,
            border = BorderStroke(1.dp, primary.copy(alpha = 0.35f))
        ) {
            Text(
                stringResource(R.string.home_eyebrow),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = secondary.copy(alpha = 0.18f),
            contentColor = secondary,
            border = BorderStroke(1.dp, secondary.copy(alpha = 0.45f))
        ) {
            Text(
                stringResource(R.string.home_edition_badge),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetaChip(stringResource(R.string.home_guest_chip), primary)
            MetaChip(stringResource(R.string.home_local_chip))
        }
        Button(
            onClick = onCreateProject,
            modifier = Modifier.fillMaxWidth().testTag("home_new_project")
        ) {
            Text(stringResource(R.string.home_new_project))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onOpenProjects,
                modifier = Modifier.weight(1f).testTag("home_open_projects")
            ) {
                Text(
                    stringResource(R.string.home_open_projects),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier.weight(1f).testTag("home_login")
            ) {
                Text(
                    stringResource(R.string.home_login),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (projectCount > 0) {
            Text(
                stringResource(R.string.home_overview_projects) + " · " +
                    projectCount.toPersianDigits(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MetaChip(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        contentColor = color
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}
