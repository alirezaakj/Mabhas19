package ir.mabhas19.feature.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.Project
import ir.mabhas19.core.model.ProjectId

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    loadFailed: Boolean,
    onCreate: () -> Unit,
    onOpen: (ProjectId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag("projects_screen"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                stringResource(R.string.projects_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(stringResource(R.string.edition_label), color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.local_notice), style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().testTag("new_project")) {
                Text(stringResource(R.string.create_project))
            }
        }
        if (loadFailed) {
            item {
                Text(
                    stringResource(R.string.storage_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (projects.isEmpty() && !loadFailed) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.empty_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(stringResource(R.string.empty_body))
                    }
                }
            }
        }
        items(projects, key = { it.id.value }) { project ->
            Card(onClick = {
                onOpen(project.id)
            }, modifier = Modifier.fillMaxWidth().testTag("project_${project.id.value}")) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(project.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.project_saved),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun NewProjectScreen(
    title: String,
    state: EditorState,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.testTag("new_project_screen"), contentPadding = PaddingValues(24.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    stringResource(R.string.create_project),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(stringResource(R.string.local_notice))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth().testTag("project_title"),
                    label = { Text(stringResource(R.string.project_title_label)) },
                    isError = state.error == ProjectError.INVALID_TITLE,
                    enabled = !state.saving,
                    singleLine = true
                )
                state.error?.let {
                    Text(
                        stringResource(
                            if (it ==
                                ProjectError.INVALID_TITLE
                            ) {
                                R.string.invalid_title
                            } else {
                                R.string.storage_error
                            }
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth().testTag("save_project")
                ) {
                    Text(
                        stringResource(if (state.saving) R.string.saving else R.string.save_project)
                    )
                }
                if (state.saving) CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProjectDetailScreen(project: Project?, modifier: Modifier = Modifier) {
    LazyColumn(modifier.testTag("project_detail"), contentPadding = PaddingValues(24.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    project?.title ?: stringResource(R.string.project_missing),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(stringResource(R.string.edition_label))
                if (project != null) {
                    Text(
                        stringResource(R.string.project_saved),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(R.string.foundation_notice))
                    Text(
                        stringResource(R.string.preliminary_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
