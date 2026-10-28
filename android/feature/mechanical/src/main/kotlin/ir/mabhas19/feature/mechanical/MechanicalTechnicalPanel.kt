package ir.mabhas19.feature.mechanical

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun TechnicalPanel(
    fields: List<MechanicalTechnicalField>,
    values: Map<String, String>,
    onChange: (String, String) -> Unit
) {
    var expanded by rememberSaveable(fields.firstOrNull()?.topic) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("پارامترهای کلیدی تأسیسات مکانیکی", fontWeight = FontWeight.SemiBold)
                    Text("فصل ۴ اجباری و فصل ۵ تجویزی", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "بستن" else "ورود اطلاعات")
                }
            }
            if (expanded) {
                fields.forEach { field ->
                    TechnicalField(
                        field = field,
                        value = values[field.id].orEmpty(),
                        onChange = { onChange(field.id, it) }
                    )
                }
                if (fields.isEmpty()) {
                    Text(
                        "برای این بخش پارامتر فنی جداگانه‌ای در فرم منبع تعریف نشده است."
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicalField(
    field: MechanicalTechnicalField,
    value: String,
    onChange: (String) -> Unit
) {
    if (field.type == "select" && field.options.isNotEmpty()) {
        var expanded by rememberSaveable(field.id) { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(field.label, style = MaterialTheme.typography.bodySmall)
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    val option = field.options.firstOrNull { it.value == value }
                    Text(option?.label?.ifBlank { "انتخاب کنید" } ?: "انتخاب کنید")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    field.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label.ifBlank { "بررسی نشده" }) },
                            onClick = {
                                onChange(option.value)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(field.label) },
            singleLine = true
        )
    }
}
