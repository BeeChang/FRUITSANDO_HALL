package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.theme.AppTheme

// 멤버 관리와 동일 팔레트 — 앱 전체에서 컬러 선택 UI 톤을 통일
private val positionColorPalette = listOf(
    "#FFB3C6", "#FF8FAB", "#FFAFD0", "#FFD6E5",
    "#F9C0D0", "#FFA8C0", "#F0C0D8", "#F5C6EC",
    "#D4B8F5", "#C9B2F0", "#E2CCFF", "#D0B4FF",
    "#B3D9FF", "#A8D0F8", "#BDD7FF", "#C8DCFF",
    "#A8E6CF", "#B5EAD7", "#C7F2D4", "#9FD8D8",
    "#FFD6A5", "#FFE5B4", "#FFEAA7", "#FFD0A8",
)

private const val DARK_TEXT = 0xFF2D2D2D

private fun String.toColorOrDefault(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    Color(0xFF7FD1D1)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionManageDialog(
    positions: List<RotationPositionUi>,
    onSave: (RotationPositionUi) -> Unit,
    onDelete: (Long) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var isHigh by remember { mutableStateOf(true) }
    var minCount by remember { mutableStateOf("1") }
    var maxCount by remember { mutableStateOf("1") }
    var openPriority by remember { mutableStateOf((positions.size + 1).toString()) }
    var overflowPriority by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(positionColorPalette[0]) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.6f).heightIn(max = 700.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("포지션 관리", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (positions.isNotEmpty()) {
                        LazyColumn(Modifier.heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(positions, key = { it.id }) { p ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .background(p.colorHex.toColorOrDefault().copy(alpha = 0.18f))
                                        .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(14.dp).clip(CircleShape).background(p.colorHex.toColorOrDefault()))
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            (if (p.isHigh) "힘듬" else "안힘듬") + " · 최소${p.minCount} · 최대${p.maxCount ?: "무제한"}" +
                                                " · 우선${p.openPriority}" + (p.overflowPriority?.let { " · 추가$it" } ?: ""),
                                            style = MaterialTheme.typography.labelSmall, color = appColors.grey600
                                        )
                                    }
                                    Switch(
                                        checked = p.isActive, onCheckedChange = { onToggleActive(p.id, it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = appColors.primary500)
                                    )
                                    IconButton(onClick = { onDelete(p.id) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("새 포지션 추가", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, label = { Text("포지션 이름") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Box(Modifier.size(20.dp).clip(CircleShape).background(selectedColor.toColorOrDefault())) }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("컬러 선택", style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        positionColorPalette.forEach { hex ->
                            val isSelected = selectedColor == hex
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(hex.toColorOrDefault())
                                    .then(if (isSelected) Modifier.border(2.5.dp, Color(DARK_TEXT), CircleShape) else Modifier)
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(DARK_TEXT), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isHigh, onClick = { isHigh = false },
                            label = { Text("안힘듬", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appColors.secondary100, selectedLabelColor = appColors.secondary700)
                        )
                        FilterChip(
                            selected = isHigh, onClick = { isHigh = true },
                            label = { Text("힘듬", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appColors.crimson100, selectedLabelColor = appColors.crimson700)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minCount, onValueChange = { minCount = it }, modifier = Modifier.weight(1f),
                            label = { Text("최소 정원") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = maxCount, onValueChange = { maxCount = it }, modifier = Modifier.weight(1f),
                            label = { Text("최대(비우면 무제한)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openPriority, onValueChange = { openPriority = it }, modifier = Modifier.weight(1f),
                            label = { Text("우선 배정 순위") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = overflowPriority, onValueChange = { overflowPriority = it }, modifier = Modifier.weight(1f),
                            label = { Text("추가 배정 순위(비우면 안받음)") }, singleLine = true, shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Text(
                        "우선 배정: 인원이 부족해도 이 순서대로 먼저·끝까지 채워요 (1이 가장 먼저)\n추가 배정: 인원이 남으면 이 순서대로 더 받아요",
                        style = MaterialTheme.typography.labelSmall, color = appColors.grey500,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(
                                        RotationPositionUi(
                                            id = 0, name = name, isHigh = isHigh,
                                            minCount = minCount.toIntOrNull() ?: 0,
                                            maxCount = maxCount.toIntOrNull(),
                                            openPriority = openPriority.toIntOrNull() ?: (positions.size + 1),
                                            overflowPriority = overflowPriority.toIntOrNull(),
                                            isActive = true, colorHex = selectedColor, sortOrder = positions.size
                                        )
                                    )
                                    name = ""
                                }
                            },
                            enabled = name.isNotBlank(), shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("추가")
                        }
                    }
                }
            }
        }
    }
}
