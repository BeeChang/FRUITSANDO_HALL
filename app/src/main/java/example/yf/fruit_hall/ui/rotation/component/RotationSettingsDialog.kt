package example.yf.fruit_hall.ui.rotation.component

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.ui.rotation.RotationSettingsUi
import example.yf.fruit_hall.ui.theme.AppTheme

private fun tierLabel(tier: Tier) = when (tier) {
    Tier.CUMULATIVE_HIGH -> "누적 힘듬 편차"
    Tier.CONSTRAINT -> "연속 위반"
    Tier.HIGH_VARIETY -> "힘듬 종류별 편차"
    Tier.LOW_FAIRNESS -> "비힘듬 공평"
    Tier.TIEBREAK -> "타이브레이크"
}

private fun remainderLabel(p: RemainderPolicy) = when (p) {
    RemainderPolicy.REDISTRIBUTE -> "재분배"
    RemainderPolicy.ABSORB -> "흡수"
    RemainderPolicy.STANDALONE -> "독립"
    RemainderPolicy.DROP -> "제외"
}

private fun handoverLabel(m: HandoverMode) = when (m) {
    HandoverMode.DISPLAY_ONLY -> "표시만"
    HandoverMode.DEDUCT -> "부채 차감"
}

private fun targetBasisLabel(t: TargetBasis) = when (t) {
    TargetBasis.TOTAL_MINUTES -> "총분(1/N)"
    TargetBasis.PRESENCE_RATIO -> "재실 비율"
}

private fun priorityLabel(p: FairnessPriority) = when (p) {
    FairnessPriority.HIGH -> "높음"
    FairnessPriority.MID -> "중간"
    FairnessPriority.LOW -> "낮음"
    FairnessPriority.OFF -> "끔"
}

private val settingsTabs = listOf("시간", "공평성", "제약", "우선순위", "탐색")

@Composable
private fun FieldHint(text: String) {
    val appColors = AppTheme.colors
    Text(text, style = MaterialTheme.typography.labelSmall, color = appColors.grey500, modifier = Modifier.padding(bottom = 8.dp, top = 1.dp))
}

@Composable
private fun <T> ChipRow(label: String, hint: String, value: T, options: List<T>, labelOf: (T) -> String, onChange: (T) -> Unit) {
    val appColors = AppTheme.colors
    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
    FieldHint(hint)
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { opt ->
            FilterChip(
                selected = opt == value,
                onClick = { onChange(opt) },
                label = { Text(labelOf(opt), style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appColors.primary100, selectedLabelColor = appColors.primary700)
            )
        }
    }
}

@Composable
private fun NumberField(label: String, hint: String, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text, onValueChange = { text = it; it.toIntOrNull()?.let(onChange) },
        label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
    FieldHint(hint)
}

@Composable
private fun TimeField(label: String, hint: String, minutes: Int, onChange: (Int) -> Unit) {
    RotationTimePickerField(
        label = label, minutes = minutes, onChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    )
    FieldHint(hint)
}

@Composable
private fun SwitchRow(label: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val appColors = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = appColors.grey500)
        }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = appColors.primary500))
    }
}

@Composable
fun RotationSettingsDialog(
    settings: RotationSettingsUi,
    onSave: (RotationSettingsUi) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var s by remember(settings) { mutableStateOf(settings) }
    var tab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.62f).heightIn(max = 720.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("설정", style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                TabRow(
                    selectedTabIndex = tab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = appColors.primary600,
                    indicator = { _ ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier, color = appColors.primary500
                        )
                    }
                ) {
                    settingsTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tab == index,
                            onClick = { tab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false).verticalScroll(rememberScrollState())
                ) {
                    when (tab) {
                        0 -> {
                            TimeField("로테이션 시작", "이 시각부터 오늘 배정을 계산합니다.", s.windowStart) { s = s.copy(windowStart = it) }
                            TimeField("타겟타임(종료)", "이 시각까지 배정을 마감합니다.", s.windowEnd) { s = s.copy(windowEnd = it) }
                            NumberField("브레이크 전 희망 슬롯 길이(분)", "브레이크 전 구간을 몇 분 단위로 쪼갤지의 기준값입니다.", s.preBreakDesiredMinutes) { s = s.copy(preBreakDesiredMinutes = it) }
                            NumberField("브레이크 후 희망 슬롯 길이(분)", "브레이크 후 구간을 몇 분 단위로 쪼갤지의 기준값입니다.", s.postBreakDesiredMinutes) { s = s.copy(postBreakDesiredMinutes = it) }
                            NumberField("최소 슬롯 길이(분)", "이보다 짧게 남는 구간은 자투리로 보고 앞/뒤 슬롯에 합칩니다.", s.minSlotMinutes) { s = s.copy(minSlotMinutes = it) }
                            TimeField("기본 브레이크 시작 시각", "'브레이크 편집'에서 기본값으로 자동 생성할 시작 시각입니다.", s.defaultBreakStartMin) { s = s.copy(defaultBreakStartMin = it) }
                            NumberField(
                                "기본 근무 시간(분)",
                                "프리셋 관리에서 출근 시각만 입력해도 이 길이만큼 퇴근 시각을 자동으로 채웁니다. 현재 ${s.defaultShiftDurationMinutes / 60}시간 ${s.defaultShiftDurationMinutes % 60}분.",
                                s.defaultShiftDurationMinutes
                            ) { s = s.copy(defaultShiftDurationMinutes = it) }
                            ChipRow(
                                "자투리 처리", "최소 슬롯보다 짧은 자투리 구간을 어떻게 처리할지 정합니다.",
                                s.remainderPolicy, RemainderPolicy.entries, ::remainderLabel
                            ) { s = s.copy(remainderPolicy = it) }
                            ChipRow(
                                "교대 텀 모드", "교대 인수인계 시간을 결과에 표시만 할지, 근무시간에서 차감할지 정합니다.",
                                s.handoverMode, HandoverMode.entries, ::handoverLabel
                            ) { s = s.copy(handoverMode = it) }
                        }
                        1 -> {
                            OutlinedTextField(
                                value = s.alpha.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { a -> s = s.copy(alpha = a) } },
                                label = { Text("α (아침 구간 반영 계수)") }, singleLine = true, shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                            FieldHint("브레이크 전(아침) 구간의 힘든 정도를 몇 배로 계산에 반영할지. 1.0이면 그대로 반영합니다.")
                            ChipRow(
                                "목표 기준", "공평 배분 목표를 총 근무시간(1/N) 기준으로 볼지, 실제 매장에 있던 시간 비율로 볼지 정합니다.",
                                s.targetBasis, TargetBasis.entries, ::targetBasisLabel
                            ) { s = s.copy(targetBasis = it) }
                            ChipRow(
                                "누적 공평 우선도", "여러 날에 걸쳐 쌓인 누적 공평성을 얼마나 강하게 반영할지 정합니다.",
                                s.fairnessPriority, FairnessPriority.entries, ::priorityLabel
                            ) { s = s.copy(fairnessPriority = it) }
                        }
                        2 -> {
                            NumberField("동일 포지션 연속 허용 슬롯 수", "같은 포지션에 연속으로 배정할 수 있는 최대 슬롯 수입니다.", s.samePositionMaxRun) { s = s.copy(samePositionMaxRun = it) }
                            SwitchRow("브레이크가 연속을 끊음", "브레이크를 지나면 연속 배정 횟수를 다시 0부터 셉니다.", s.breakInterruptsRun) { s = s.copy(breakInterruptsRun = it) }
                            SwitchRow("힘듬 연속 허용", "힘든 포지션을 연달아 배정하는 것을 허용할지 정합니다.", s.allowHighChain) { s = s.copy(allowHighChain = it) }
                            NumberField("힘듬 연속 허용 슬롯 수", "힘든 포지션을 최대 몇 슬롯까지 연속으로 배정할 수 있는지입니다.", s.highMaxRun) { s = s.copy(highMaxRun = it) }
                            NumberField("힘듬 후 쿨다운(슬롯)", "힘든 포지션을 마친 뒤 최소 몇 슬롯은 쉬운 자리를 배정할지입니다.", s.highCooldownSlots) { s = s.copy(highCooldownSlots = it) }
                            SwitchRow("브레이크 전 구간 제약 완화", "브레이크 직전 구간에서는 위 제약들을 조금 느슨하게 적용합니다.", s.relaxPreBreak) { s = s.copy(relaxPreBreak = it) }
                        }
                        3 -> {
                            FieldHint("여러 기준이 동시에 걸릴 때 무엇을 먼저 맞출지 순서를 정합니다. 위에 있을수록 우선순위가 높습니다.")
                            s.tierOrder.forEachIndexed { index, tier ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}. ${tierLabel(tier)}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = s.tierOrder.toMutableList()
                                                list[index] = list[index - 1].also { list[index - 1] = list[index] }
                                                s = s.copy(tierOrder = list)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "위로", tint = appColors.grey600) }
                                    IconButton(
                                        onClick = {
                                            if (index < s.tierOrder.size - 1) {
                                                val list = s.tierOrder.toMutableList()
                                                list[index] = list[index + 1].also { list[index + 1] = list[index] }
                                                s = s.copy(tierOrder = list)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "아래로", tint = appColors.grey600) }
                                }
                            }
                        }
                        4 -> {
                            NumberField(
                                "탐색 반복 횟수", "배정 결과를 몇 번 더 다듬어볼지입니다. 높일수록 더 고르게 나오지만 생성이 느려집니다.",
                                s.ilsIterations
                            ) { s = s.copy(ilsIterations = it) }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("닫기", color = appColors.grey600) }
                    Spacer(Modifier.width(4.dp))
                    Button(onClick = { onSave(s) }, shape = RoundedCornerShape(8.dp)) { Text("저장") }
                }
            }
        }
    }
}
