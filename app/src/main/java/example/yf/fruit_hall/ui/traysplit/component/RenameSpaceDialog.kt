package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun RenameSpaceDialog(
    currentName: String,
    currentCapacity: Int?,
    onConfirm: (name: String, capacity: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var name by remember { mutableStateOf(currentName) }
    var capacityText by remember { mutableStateOf(currentCapacity?.toString() ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.4f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.tray_rename_space_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.tray_rename_space_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it.filter(Char::isDigit).take(3) },
                        label = { Text(stringResource(R.string.tray_rename_space_capacity_label)) },
                        placeholder = { Text(stringResource(R.string.tray_rename_space_capacity_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.tray_rename_space_capacity_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.grey500
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.cancel), color = appColors.grey600)
                        }
                        Button(
                            onClick = { if (name.isNotBlank()) onConfirm(name.trim(), capacityText.toIntOrNull()) },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}
