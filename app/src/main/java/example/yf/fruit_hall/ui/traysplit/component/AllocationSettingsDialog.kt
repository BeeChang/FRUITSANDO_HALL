package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.core.Level
import example.yf.fruit_hall.ui.component.AppOnlyConfirmDialog
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.AllocationSettingsUi

/** 100.0/3 같은 무한소수를 33.3처럼 짧게 — 입력칸이 끝없이 길어지는 것 방지 */
private fun formatPercent(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

@Composable
private fun InfoLabel(label: String, description: String) {
    val appColors = AppTheme.colors
    var showInfo by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = appColors.grey600)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "설명 보기",
            tint = appColors.grey400,
            modifier = Modifier.size(14.dp).clickable { showInfo = true }
        )
    }
    AppOnlyConfirmDialog(
        title = label,
        content = description,
        confirmButtonText = "확인",
        isShowDialog = showInfo,
        onConfirm = { showInfo = false },
        onDismiss = { showInfo = false }
    )
}

@Composable
private fun LevelPicker(label: String, description: String, selected: Level, onSelect: (Level) -> Unit) {
    val appColors = AppTheme.colors
    Column {
        InfoLabel(label, description)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Level.LOW to "하", Level.MID to "중", Level.HIGH to "상").forEach { (level, text) ->
                val isSelected = selected == level
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelect(level) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) appColors.white else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllocationSettingsDialog(
    settings: AllocationSettingsUi,
    onConfirm: (AllocationSettingsUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var rounds by remember { mutableStateOf(settings.rounds) }
    var isExactMode by remember { mutableStateOf(settings.isExactMode) }
    var exactValues by remember {
        mutableStateOf(List(rounds) { i -> settings.exactTraysPerRound.getOrNull(i)?.toString() ?: "" })
    }
    var ratioValues by remember {
        mutableStateOf(List(rounds) { i -> settings.ratioPercents.getOrNull(i)?.let { formatPercent(it) } ?: formatPercent(100.0 / rounds) })
    }
    var maxDeviation by remember { mutableStateOf(settings.maxDeviation) }
    var allowedMissingTypes by remember { mutableStateOf(settings.allowedMissingTypes) }
    var showAdvanced by remember { mutableStateOf(false) }
    var spreadStrength by remember { mutableStateOf(settings.spreadStrength) }
    var orderStrictness by remember { mutableStateOf(settings.orderStrictness) }
    var qtySensitivity by remember { mutableStateOf(settings.qtySensitivity) }

    fun resizeToRounds(newRounds: Int) {
        rounds = newRounds.coerceIn(2, 8)
        exactValues = List(rounds) { i -> exactValues.getOrNull(i) ?: "" }
        ratioValues = List(rounds) { i -> ratioValues.getOrNull(i) ?: formatPercent(100.0 / rounds) }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.5f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = appColors.white)
                    Spacer(Modifier.width(10.dp))
                    Text("배분 설정", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text("차수 수", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = appColors.grey700)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { resizeToRounds(rounds - 1) }) { Text("–", style = MaterialTheme.typography.titleLarge) }
                        Text("${rounds}차", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { resizeToRounds(rounds + 1) }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(false to "비율(%)", true to "지정(판 수)").forEach { (exact, label) ->
                            val isSelected = isExactMode == exact
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) appColors.primary500 else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { isExactMode = exact }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(label, color = if (isSelected) appColors.white else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "정확한 판 수를 안 정해도 된다면 '비율(%)'을 쓰세요 — 대략적인 비율만 정하면 자동으로 배분돼요.",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.grey500
                    )

                    Spacer(Modifier.height(16.dp))
                    if (isExactMode) {
                        val blankCount = exactValues.count { it.isBlank() }
                        Text("차수별 판 수", style = MaterialTheme.typography.labelMedium, color = appColors.grey600)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "빈 칸은 딱 1개까지만 가능해요 — 그 차수가 나머지 판을 전부 가져가요. " +
                                "예: 1차=5, 2차=빈칸이면 나머지 전부가 2차로 가요. 3차까지 있는데 2차·3차를 둘 다 비우면 안 돼요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.grey500
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exactValues.forEachIndexed { i, v ->
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { new -> exactValues = exactValues.toMutableList().also { it[i] = new.filter(Char::isDigit) } },
                                    label = { Text("${i + 1}차") },
                                    placeholder = { Text("나머지") },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                        if (blankCount > 1) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "빈 칸이 ${blankCount}개예요. 1개만 남기고 나머지 차수엔 판 수를 입력해주세요.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = appColors.crimson400
                            )
                        }
                    } else {
                        Text("차수별 목표 비율 (%, 합은 자동 정규화)", style = MaterialTheme.typography.labelMedium, color = appColors.grey600)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ratioValues.forEachIndexed { i, v ->
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { new ->
                                        val filtered = new.filter { c -> c.isDigit() || c == '.' }.take(5)
                                        ratioValues = ratioValues.toMutableList().also { it[i] = filtered }
                                    },
                                    label = { Text("${i + 1}차 %") },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("허용 편차(±판)", style = MaterialTheme.typography.labelMedium, color = appColors.grey600, modifier = Modifier.weight(1f))
                            IconButton(onClick = { maxDeviation = (maxDeviation - 1).coerceAtLeast(0) }) { Text("–") }
                            Text("$maxDeviation")
                            IconButton(onClick = { maxDeviation++ }) { Text("+") }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("1차 누락 허용 종류 수", style = MaterialTheme.typography.labelMedium, color = appColors.grey600, modifier = Modifier.weight(1f))
                        IconButton(onClick = { allowedMissingTypes = (allowedMissingTypes - 1).coerceAtLeast(0) }) { Text("–") }
                        Text("$allowedMissingTypes")
                        IconButton(onClick = { allowedMissingTypes++ }) { Text("+") }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showAdvanced = !showAdvanced },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("고급 설정", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = appColors.grey700, modifier = Modifier.weight(1f))
                        Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = appColors.grey500)
                    }

                    if (showAdvanced) {
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            "분산 선호 강도",
                            "같은 종류의 과자가 여러 차수에 걸쳐 고르게 나타나도록 하는 정도예요. " +
                                "'상'으로 갈수록 한 차수에 몰아넣지 않고 여러 차수에 나눠 담으려는 경향이 강해져요.",
                            spreadStrength
                        ) { spreadStrength = it }
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            "종류 순서 엄격도",
                            "차수가 진행될수록(1차→2차→3차…) 등장하는 과자 종류 수가 줄어드는 흐름을 얼마나 엄격히 지킬지예요. " +
                                "'상'일수록 뒤 차수에 새로운 종류가 섞이는 걸 강하게 피해요.",
                            orderStrictness
                        ) { orderStrictness = it }
                        Spacer(Modifier.height(12.dp))
                        LevelPicker(
                            "수량 밸런스 민감도",
                            "차수별로 담긴 양(개수)이 한쪽에 몰리지 않도록 신경 쓰는 정도예요. " +
                                "'상'일수록 양이 많은 차수를 더 적극적으로 줄이려고 해요.",
                            qtySensitivity
                        ) { qtySensitivity = it }
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("취소", color = appColors.grey600) }
                        Button(
                            enabled = !(isExactMode && exactValues.count { it.isBlank() } > 1),
                            onClick = {
                                onConfirm(
                                    AllocationSettingsUi(
                                        rounds = rounds,
                                        isExactMode = isExactMode,
                                        exactTraysPerRound = exactValues.map { it.toIntOrNull() },
                                        ratioPercents = ratioValues.map { it.toDoubleOrNull() ?: (100.0 / rounds) },
                                        maxDeviation = maxDeviation,
                                        allowedMissingTypes = allowedMissingTypes,
                                        spreadStrength = spreadStrength,
                                        orderStrictness = orderStrictness,
                                        qtySensitivity = qtySensitivity
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("저장")
                        }
                    }
                }
            }
        }
    }
}
