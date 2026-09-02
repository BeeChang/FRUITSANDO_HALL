package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

/** 탭하면 Material3 시간 선택기가 뜨는 읽기 전용 시간 입력 필드. minutes(0~1739)를 다룬다. */
@Composable
fun RotationTimePickerField(
    label: String,
    minutes: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { showPicker = true }
        )
    ) {
        OutlinedTextField(
            value = formatMinutes(minutes),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showPicker) {
        val h = (minutes / 60).coerceIn(0, 23)
        val m = minutes % 60
        val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = true)
        val appColors = AppTheme.colors

        Dialog(onDismissRequest = { showPicker = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, style = MaterialTheme.typography.titleSmall, color = appColors.grey700)
                    Box(Modifier.padding(top = 12.dp)) {
                        TimePicker(state = state)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPicker = false }) { Text("취소", color = appColors.grey600) }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = {
                                onChange(state.hour * 60 + state.minute)
                                showPicker = false
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("확인") }
                    }
                }
            }
        }
    }
}
