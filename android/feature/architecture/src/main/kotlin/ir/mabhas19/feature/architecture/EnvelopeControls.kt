package ir.mabhas19.feature.architecture

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun EnvelopeCard(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFF86BCA6))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = Color(0xFFE7F4ED), shape = RoundedCornerShape(9.dp)) {
                Text(
                    stringResource(title),
                    Modifier.fillMaxWidth().padding(12.dp),
                    color = Color(0xFF14543A),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            content()
        }
    }
}

@Composable
internal fun RepeatedCards(
    kind: String,
    @StringRes title: Int,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit,
    content: @Composable ColumnScope.(String) -> Unit
) {
    envelopeRows(values, kind).forEach { index ->
        val prefix = "$kind.$index"
        EnvelopeCard(title) {
            content(prefix)
            RemoveRow(prefix, values, onChange)
        }
    }
}

@Composable
internal fun AddRow(
    kind: String,
    @StringRes label: Int,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    Button(onClick = {
        onChange(envelopeAddRow(values, kind))
    }, modifier = Modifier.testTag("${kind}_add")) {
        Text(stringResource(label))
    }
}

@Composable
internal fun RemoveRow(
    prefix: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    TextButton(
        onClick = { onChange(envelopeRemoveRow(values, prefix)) },
        modifier = Modifier.testTag("${prefix}_remove")
    ) {
        Text(stringResource(R.string.envelope_remove), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
internal fun EnvelopeFields(
    values: Map<String, String>,
    prefix: String,
    fields: List<EnvelopeField>,
    onChange: (Map<String, String>) -> Unit
) {
    fields.forEach { field ->
        val key = "$prefix.${field.key}"
        val value = values[key].orEmpty()
        if (field.options.isEmpty()) {
            OutlinedTextField(
                value = value,
                onValueChange = { onChange(values + (key to it)) },
                label = { Text(stringResource(field.label)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (field.numeric) KeyboardType.Decimal else KeyboardType.Text
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(key)
            )
        } else {
            EnvelopePicker(key, field, value) { onChange(values + (key to it)) }
        }
    }
}

@Composable
private fun EnvelopePicker(
    key: String,
    field: EnvelopeField,
    value: String,
    onChange: (String) -> Unit
) {
    var expanded by remember(key) { mutableStateOf(false) }
    Column {
        Text(stringResource(field.label), style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().testTag(key)
        ) {
            val label =
                field.options.firstOrNull { it.first == value }?.second ?: R.string.envelope_choose
            Text(stringResource(label))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.envelope_choose)) },
                onClick = {
                    onChange("")
                    expanded = false
                }
            )
            field.options.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(stringResource(label)) },
                    onClick = {
                        onChange(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun EnvelopeCheck(
    key: String,
    @StringRes label: Int,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = values[key] == "true",
            onCheckedChange = { onChange(values + (key to it.toString())) },
            modifier = Modifier.testTag(key)
        )
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
    }
}

internal data class EnvelopeField(
    val key: String,
    @StringRes val label: Int,
    val numeric: Boolean = false,
    val options: List<Pair<String, Int>> = emptyList()
)

internal fun field(key: String, @StringRes label: Int) = EnvelopeField(key, label)
internal fun number(key: String, @StringRes label: Int) = EnvelopeField(key, label, numeric = true)
internal fun choice(key: String, @StringRes label: Int, options: List<Pair<String, Int>>) =
    EnvelopeField(key, label, options = options)
