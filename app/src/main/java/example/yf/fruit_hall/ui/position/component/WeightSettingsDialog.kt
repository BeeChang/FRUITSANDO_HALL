package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.data.position.entity.WeightDecayMode
import example.yf.fruit_hall.ui.position.PositionEvent
import example.yf.fruit_hall.ui.position.PositionUi
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun WeightSettingsDialog(
    positions: List<PositionUi>,
    onUpdatePosition: (PositionEvent.UpdatePosition) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = appColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "가중치 설정",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = appColors.grey300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text(
                        text = "같은 포지션에 자주 배정된 멤버의 당첨 확률을 자동으로 낮춥니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.grey500
                    )
                    Spacer(Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 460.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(positions, key = { it.id }) { position ->
                            val hasWeight = localHasWeight[position.id] ?: position.hasWeight
                            val strength = localStrength[position.id] ?: position.weightStrength
                            val mode = localMode[position.id] ?: position.weightDecayMode

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = position.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
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
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedTrackColor = appColors.primary500,
                                                checkedThumbColor = appColors.white
                                            )
                                        )
                                    }

                                    if (hasWeight) {
                                        Spacer(Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        Spacer(Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "가중치 강도",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = appColors.grey600
                                            )
                                            Text(
                                                text = "${(strength * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.primary500
                                            )
                                        }
                                        Slider(
                                            value = strength,
                                            onValueChange = { localStrength[position.id] = it },
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
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = appColors.primary500,
                                                activeTrackColor = appColors.primary500,
                                                inactiveTrackColor = appColors.primary200
                                            )
                                        )

                                        Spacer(Modifier.height(4.dp))

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
                                                label = { Text("당일 유지", style = MaterialTheme.typography.labelSmall) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = appColors.primary100,
                                                    selectedLabelColor = appColors.primary700
                                                )
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
                                                label = { Text("점진 회복", style = MaterialTheme.typography.labelSmall) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = appColors.secondary100,
                                                    selectedLabelColor = appColors.secondary700
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("닫기", color = appColors.grey600)
                    }
                }
            }
        }
    }
}
