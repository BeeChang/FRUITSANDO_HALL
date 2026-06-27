package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import example.yf.fruit_hall.data.position.entity.WeightDecayMode
import example.yf.fruit_hall.ui.position.PositionEvent
import example.yf.fruit_hall.ui.position.PositionUi
import kotlin.math.roundToInt

@Composable
fun WeightSettingsDialog(
    positions: List<PositionUi>,
    onUpdatePosition: (PositionEvent.UpdatePosition) -> Unit,
    onDismiss: () -> Unit
) {
    val localHasWeight = remember {
        mutableStateMapOf<Long, Boolean>().also { map ->
            positions.forEach { map[it.id] = it.hasWeight }
        }
    }
    val localStrength = remember {
        mutableStateMapOf<Long, Float>().also { map ->
            positions.forEach { map[it.id] = it.weightStrength }
        }
    }
    val localMode = remember {
        mutableStateMapOf<Long, String>().also { map ->
            positions.forEach { map[it.id] = it.weightDecayMode }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "가중치 설정",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(positions, key = { it.id }) { position ->
                        val hasWeight = localHasWeight[position.id] ?: position.hasWeight
                        val strength = localStrength[position.id] ?: position.weightStrength
                        val mode = localMode[position.id] ?: position.weightDecayMode

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = position.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Switch(
                                        checked = hasWeight,
                                        onCheckedChange = { checked ->
                                            localHasWeight[position.id] = checked
                                            onUpdatePosition(
                                                PositionEvent.UpdatePosition(
                                                    id = position.id,
                                                    name = position.name,
                                                    isMultiPerson = position.isMultiPerson,
                                                    hasWeight = checked,
                                                    weightStrength = strength,
                                                    weightDecayMode = mode
                                                )
                                            )
                                        }
                                    )
                                }

                                if (hasWeight) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "가중치 강도: ${(strength * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Slider(
                                        value = strength,
                                        onValueChange = { newStrength ->
                                            localStrength[position.id] = newStrength
                                        },
                                        onValueChangeFinished = {
                                            onUpdatePosition(
                                                PositionEvent.UpdatePosition(
                                                    id = position.id,
                                                    name = position.name,
                                                    isMultiPerson = position.isMultiPerson,
                                                    hasWeight = hasWeight,
                                                    weightStrength = localStrength[position.id] ?: strength,
                                                    weightDecayMode = mode
                                                )
                                            )
                                        },
                                        valueRange = 0f..1f,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = mode == WeightDecayMode.STAY_LOW.name,
                                            onClick = {
                                                localMode[position.id] = WeightDecayMode.STAY_LOW.name
                                                onUpdatePosition(
                                                    PositionEvent.UpdatePosition(
                                                        id = position.id,
                                                        name = position.name,
                                                        isMultiPerson = position.isMultiPerson,
                                                        hasWeight = hasWeight,
                                                        weightStrength = strength,
                                                        weightDecayMode = WeightDecayMode.STAY_LOW.name
                                                    )
                                                )
                                            },
                                            label = { Text("유지") }
                                        )
                                        FilterChip(
                                            selected = mode == WeightDecayMode.RECOVER.name,
                                            onClick = {
                                                localMode[position.id] = WeightDecayMode.RECOVER.name
                                                onUpdatePosition(
                                                    PositionEvent.UpdatePosition(
                                                        id = position.id,
                                                        name = position.name,
                                                        isMultiPerson = position.isMultiPerson,
                                                        hasWeight = hasWeight,
                                                        weightStrength = strength,
                                                        weightDecayMode = WeightDecayMode.RECOVER.name
                                                    )
                                                )
                                            },
                                            label = { Text("회복") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("닫기")
                }
            }
        }
    }
}
