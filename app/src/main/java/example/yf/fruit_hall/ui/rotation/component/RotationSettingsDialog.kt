package example.yf.fruit_hall.ui.rotation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import example.yf.fruit_hall.R
import example.yf.fruit_hall.core.rotation.FairnessPriority
import example.yf.fruit_hall.core.rotation.HandoverMode
import example.yf.fruit_hall.core.rotation.RemainderPolicy
import example.yf.fruit_hall.core.rotation.TargetBasis
import example.yf.fruit_hall.core.rotation.Tier
import example.yf.fruit_hall.ui.rotation.RotationSettingsUi
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlin.math.roundToInt

// 선택지 라벨은 문자열이 아니라 리소스 id로 돌려준다 — @Composable이 아닌 곳에서도 부를 수 있어야
// ChipRow 같은 공용 컴포넌트에 그대로 넘길 수 있다. 문자열로 푸는 건 화면을 그리는 쪽 몫이다.
@StringRes
private fun tierLabel(tier: Tier) = when (tier) {
    Tier.CUMULATIVE_HIGH -> R.string.rotation_tier_cumulative_high
    Tier.CONSTRAINT -> R.string.rotation_tier_constraint
    Tier.HIGH_VARIETY -> R.string.rotation_tier_high_variety
    Tier.LOW_FAIRNESS -> R.string.rotation_tier_low_fairness
    Tier.TIEBREAK -> R.string.rotation_tier_tiebreak
}

@StringRes
private fun tierDescription(tier: Tier) = when (tier) {
    Tier.CUMULATIVE_HIGH -> R.string.rotation_tier_cumulative_high_desc
    Tier.CONSTRAINT -> R.string.rotation_tier_constraint_desc
    Tier.HIGH_VARIETY -> R.string.rotation_tier_high_variety_desc
    Tier.LOW_FAIRNESS -> R.string.rotation_tier_low_fairness_desc
    Tier.TIEBREAK -> R.string.rotation_tier_tiebreak_desc
}

@StringRes
private fun remainderLabel(p: RemainderPolicy) = when (p) {
    RemainderPolicy.REDISTRIBUTE -> R.string.rotation_remainder_redistribute
    RemainderPolicy.ABSORB -> R.string.rotation_remainder_absorb
    RemainderPolicy.STANDALONE -> R.string.rotation_remainder_standalone
    RemainderPolicy.DROP -> R.string.rotation_remainder_drop
}

@StringRes
private fun handoverLabel(m: HandoverMode) = when (m) {
    HandoverMode.DISPLAY_ONLY -> R.string.rotation_handover_display_only
    HandoverMode.DEDUCT -> R.string.rotation_handover_deduct
}

@StringRes
private fun targetBasisLabel(t: TargetBasis) = when (t) {
    TargetBasis.TOTAL_MINUTES -> R.string.rotation_target_total_minutes
    TargetBasis.PRESENCE_RATIO -> R.string.rotation_target_presence_ratio
}

@StringRes
private fun priorityLabel(p: FairnessPriority) = when (p) {
    FairnessPriority.HIGH -> R.string.rotation_priority_high
    FairnessPriority.MID -> R.string.rotation_priority_mid
    FairnessPriority.LOW -> R.string.rotation_priority_low
    FairnessPriority.OFF -> R.string.rotation_priority_off
}

private val settingsTabs = listOf(
    R.string.rotation_settings_section_time,
    R.string.rotation_settings_section_fairness,
    R.string.rotation_settings_section_constraint,
    R.string.rotation_settings_section_tier,
    R.string.rotation_settings_section_search
)

@Composable
private fun FieldHint(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp, top = 1.dp)
    )
}

@Composable
private fun <T> ChipRow(label: String, hint: String, value: T, options: List<T>, labelOf: (T) -> Int, onChange: (T) -> Unit) {
    val appColors = AppTheme.colors
    Text(
        label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 6.dp)
    )
    FieldHint(hint)
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { opt ->
            FilterChip(
                selected = opt == value,
                onClick = { onChange(opt) },
                label = { Text(stringResource(labelOf(opt)), style = MaterialTheme.typography.labelMedium) },
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
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
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

    // 필드를 바꾸는 즉시 저장한다 — 별도 "저장" 버튼을 누르지 않아도 바로 반영된다.
    fun apply(next: RotationSettingsUi) {
        s = next
        onSave(next)
    }

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
                    Text(stringResource(R.string.rotation_settings_title), style = MaterialTheme.typography.titleMedium, color = appColors.white, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.rotation_settings_applies_immediately), style = MaterialTheme.typography.labelSmall, color = appColors.grey300)
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.rotation_close), tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                TabRow(
                    selectedTabIndex = tab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = appColors.primary700
                ) {
                    settingsTabs.forEachIndexed { index, titleRes ->
                        val selected = tab == index
                        Tab(
                            selected = selected,
                            onClick = { tab = index },
                            selectedContentColor = appColors.primary700,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            text = {
                                Text(
                                    stringResource(titleRes), style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 16.dp).weight(1f, fill = false).verticalScroll(rememberScrollState())
                ) {
                    when (tab) {
                        0 -> {
                            TimeField(stringResource(R.string.rotation_settings_window_start), stringResource(R.string.rotation_settings_window_start_hint), s.windowStart) { apply(s.copy(windowStart = it)) }
                            TimeField(stringResource(R.string.rotation_settings_window_end), stringResource(R.string.rotation_settings_window_end_hint), s.windowEnd) { apply(s.copy(windowEnd = it)) }
                            NumberField(stringResource(R.string.rotation_settings_pre_break_desired), stringResource(R.string.rotation_settings_pre_break_desired_hint), s.preBreakDesiredMinutes) { apply(s.copy(preBreakDesiredMinutes = it)) }
                            NumberField(stringResource(R.string.rotation_settings_post_break_desired), stringResource(R.string.rotation_settings_post_break_desired_hint), s.postBreakDesiredMinutes) { apply(s.copy(postBreakDesiredMinutes = it)) }
                            NumberField(stringResource(R.string.rotation_settings_min_slot), stringResource(R.string.rotation_settings_min_slot_hint), s.minSlotMinutes) { apply(s.copy(minSlotMinutes = it)) }
                            TimeField(stringResource(R.string.rotation_settings_default_break_start), stringResource(R.string.rotation_settings_default_break_start_hint), s.defaultBreakStartMin) { apply(s.copy(defaultBreakStartMin = it)) }
                            NumberField(
                                stringResource(R.string.rotation_settings_default_shift_duration),
                                stringResource(
                                    R.string.rotation_settings_default_shift_duration_hint,
                                    s.defaultShiftDurationMinutes / 60, s.defaultShiftDurationMinutes % 60
                                ),
                                s.defaultShiftDurationMinutes
                            ) { apply(s.copy(defaultShiftDurationMinutes = it)) }
                            ChipRow(
                                stringResource(R.string.rotation_settings_remainder_policy),
                                stringResource(R.string.rotation_settings_remainder_policy_hint),
                                s.remainderPolicy, RemainderPolicy.entries, ::remainderLabel
                            ) { apply(s.copy(remainderPolicy = it)) }
                            ChipRow(
                                stringResource(R.string.rotation_settings_handover_mode),
                                stringResource(R.string.rotation_settings_handover_mode_hint),
                                s.handoverMode, HandoverMode.entries, ::handoverLabel
                            ) { apply(s.copy(handoverMode = it)) }
                            if (s.handoverMode == HandoverMode.DEDUCT) {
                                NumberField(stringResource(R.string.rotation_settings_handover_minutes), stringResource(R.string.rotation_settings_handover_minutes_hint), s.handoverMinutes) { apply(s.copy(handoverMinutes = it)) }
                                NumberField(stringResource(R.string.rotation_settings_handover_min_slot), stringResource(R.string.rotation_settings_handover_min_slot_hint), s.handoverMinSlotMinutes) { apply(s.copy(handoverMinSlotMinutes = it)) }
                            }
                        }
                        1 -> {
                            OutlinedTextField(
                                value = s.alpha.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { a -> apply(s.copy(alpha = a)) } },
                                label = { Text(stringResource(R.string.rotation_settings_alpha)) }, singleLine = true, shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                            FieldHint(stringResource(R.string.rotation_settings_alpha_hint))
                            ChipRow(
                                stringResource(R.string.rotation_settings_target_basis),
                                stringResource(R.string.rotation_settings_target_basis_hint),
                                s.targetBasis, TargetBasis.entries, ::targetBasisLabel
                            ) { apply(s.copy(targetBasis = it)) }
                            ChipRow(
                                stringResource(R.string.rotation_settings_fairness_priority),
                                stringResource(R.string.rotation_settings_fairness_priority_hint),
                                s.fairnessPriority, FairnessPriority.entries, ::priorityLabel
                            ) { apply(s.copy(fairnessPriority = it)) }
                        }
                        2 -> {
                            NumberField(stringResource(R.string.rotation_settings_same_position_run), stringResource(R.string.rotation_settings_same_position_run_hint), s.samePositionMaxRun) { apply(s.copy(samePositionMaxRun = it.coerceAtLeast(1))) }
                            SwitchRow(stringResource(R.string.rotation_settings_break_interrupts), stringResource(R.string.rotation_settings_break_interrupts_hint), s.breakInterruptsRun) { apply(s.copy(breakInterruptsRun = it)) }
                            SwitchRow(stringResource(R.string.rotation_settings_allow_high_chain), stringResource(R.string.rotation_settings_allow_high_chain_hint), s.allowHighChain) { apply(s.copy(allowHighChain = it)) }
                            NumberField(stringResource(R.string.rotation_settings_high_max_run), stringResource(R.string.rotation_settings_high_max_run_hint), s.highMaxRun) { apply(s.copy(highMaxRun = it)) }
                            NumberField(stringResource(R.string.rotation_settings_high_cooldown), stringResource(R.string.rotation_settings_high_cooldown_hint), s.highCooldownSlots) { apply(s.copy(highCooldownSlots = it)) }
                            SwitchRow(
                                stringResource(R.string.rotation_settings_relax_pre_break),
                                stringResource(R.string.rotation_settings_relax_pre_break_hint),
                                s.relaxPreBreak
                            ) { apply(s.copy(relaxPreBreak = it)) }
                        }
                        3 -> {
                            FieldHint(stringResource(R.string.rotation_settings_tier_hint))
                            TierOrderList(tierOrder = s.tierOrder, onReorder = { apply(s.copy(tierOrder = it)) })
                        }
                        4 -> {
                            NumberField(
                                stringResource(R.string.rotation_settings_ils_iterations),
                                stringResource(R.string.rotation_settings_ils_iterations_hint),
                                s.ilsIterations
                            ) { apply(s.copy(ilsIterations = it)) }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.rotation_close)) }
                }
            }
        }
    }
}

@Composable
private fun TierOrderList(tierOrder: List<Tier>, onReorder: (List<Tier>) -> Unit) {
    var itemHeightPx by remember { mutableIntStateOf(0) }
    var dragTier by remember { mutableStateOf<Tier?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Column {
        tierOrder.forEachIndexed { index, tier ->
            val isDragging = dragTier == tier
            Row(
                Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                    .padding(vertical = 3.dp)
                    .onSizeChanged { if (it.height > 0) itemHeightPx = it.height }
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .pointerInput(tier) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragTier = tier; dragOffsetY = 0f },
                            onDragEnd = { dragTier = null; dragOffsetY = 0f },
                            onDragCancel = { dragTier = null; dragOffsetY = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                                val h = itemHeightPx.toFloat().coerceAtLeast(1f)
                                val list = tierOrder.toMutableList()
                                val curIdx = list.indexOf(tier)
                                if (dragOffsetY > h / 2 && curIdx < list.lastIndex) {
                                    list[curIdx] = list[curIdx + 1].also { list[curIdx + 1] = list[curIdx] }
                                    onReorder(list)
                                    dragOffsetY -= h
                                } else if (dragOffsetY < -h / 2 && curIdx > 0) {
                                    list[curIdx] = list[curIdx - 1].also { list[curIdx - 1] = list[curIdx] }
                                    onReorder(list)
                                    dragOffsetY += h
                                }
                            }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DragHandle, contentDescription = stringResource(R.string.rotation_settings_tier_reorder_cd), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.rotation_settings_tier_entry, index + 1, stringResource(tierLabel(tier))),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(stringResource(tierDescription(tier)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
