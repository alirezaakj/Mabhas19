package ir.mabhas19.feature.architecture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.mabhas19.core.model.EngineeringNumber

@Composable
internal fun ArchitecturePanel(
    title: String,
    number: String = "",
    description: String = "",
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (number.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(number, Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                if (description.isNotBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
internal fun ArchitectureField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    tag: String = "",
    numeric: Boolean = false,
    enabled: Boolean = true,
    hint: String = ""
) {
    val invalid = numeric && value.isNotBlank() && EngineeringNumber.parse(value) == null
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            enabled = enabled,
            isError = invalid,
            shape = RoundedCornerShape(9.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth().testTag(tag)
        )
        if (invalid || hint.isNotBlank()) {
            Text(
                if (invalid) stringResource(R.string.architecture_invalid_number) else hint,
                style = MaterialTheme.typography.bodySmall,
                color = if (invalid) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
internal fun ArchitecturePicker(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    tag: String = "",
    clearable: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(
            onClick = {
                query = ""
                expanded = true
            },
            shape = RoundedCornerShape(9.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag)
        ) {
            Text(
                options.firstOrNull { it.first == value }?.second
                    ?: stringResource(R.string.architecture_choose),
                Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text("⌄")
        }
    }
    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (options.size > 8) {
                        OutlinedTextField(
                            query,
                            { query = it },
                            label = { Text(stringResource(R.string.architecture_search)) },
                            singleLine = true
                        )
                    }
                    val filtered = options.filter {
                        query.isBlank() ||
                            it.second.contains(query, ignoreCase = true)
                    }
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        if (filtered.isEmpty()) {
                            item { Text(stringResource(R.string.architecture_no_matches)) }
                        }
                        items(filtered, key = { it.first }) { option ->
                            TextButton(
                                onClick = {
                                    onSelect(option.first)
                                    expanded = false
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) { Text(option.second, Modifier.fillMaxWidth()) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expanded = false
                }) { Text(stringResource(R.string.architecture_cancel)) }
            },
            dismissButton = {
                if (clearable) {
                    TextButton(onClick = {
                        onSelect("")
                        expanded = false
                    }) { Text(stringResource(R.string.architecture_clear)) }
                }
            }
        )
    }
}

@Composable
internal fun ArchitectureCheck(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    tag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag(tag).clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onChange)
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}
