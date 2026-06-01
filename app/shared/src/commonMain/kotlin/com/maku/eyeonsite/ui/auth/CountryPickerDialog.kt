package com.maku.eyeonsite.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maku.eyeonsite.ui.theme.Stone

private val PICKER_MAX_HEIGHT = 360.dp

@Composable
fun CountryPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Country) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered =
        remember(query) {
            if (query.isBlank()) {
                allCountries
            } else {
                allCountries.filter {
                    it.name.contains(query, ignoreCase = true) || it.dialCode.contains(query)
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select country") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.padding(top = 8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = PICKER_MAX_HEIGHT)) {
                    items(filtered, key = { it.isoCode }) { country ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(country)
                                        onDismiss()
                                    }.padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = country.flag,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = country.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "+${country.dialCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Stone,
                            )
                        }
                        HorizontalDivider(color = Stone.copy(alpha = 0.2f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
