package example.yf.fruit_hall.ui.discord

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.discord.component.PhrasePickerDialog
import example.yf.fruit_hall.ui.discord.component.PhrasePresetDialog
import example.yf.fruit_hall.ui.discord.component.TagPickerDialog
import example.yf.fruit_hall.ui.theme.AppColor
import example.yf.fruit_hall.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun segmentAccent(type: DiscordSegmentType, appColors: AppColor): Color = when (type) {
    DiscordSegmentType.CUSTOM -> appColors.grey600
    DiscordSegmentType.PHRASE -> appColors.primary500
    DiscordSegmentType.TAG -> appColors.secondary500
    DiscordSegmentType.DATE -> appColors.warning500
}

private fun segmentIcon(type: DiscordSegmentType): ImageVector = when (type) {
    DiscordSegmentType.CUSTOM -> Icons.Default.Edit
    DiscordSegmentType.PHRASE -> Icons.Default.FormatQuote
    DiscordSegmentType.TAG -> Icons.Default.AlternateEmail
    DiscordSegmentType.DATE -> Icons.Default.CalendarMonth
}

private fun segmentLabel(type: DiscordSegmentType): String = when (type) {
    DiscordSegmentType.CUSTOM -> "직접 작성"
    DiscordSegmentType.PHRASE -> "저장 문구"
    DiscordSegmentType.TAG -> "태그"
    DiscordSegmentType.DATE -> "날짜"
}

private val screenShortWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(E)", Locale.KOREAN)
private val screenFullWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(EEEE)", Locale.KOREAN)
private fun formatShortDate(date: LocalDate, useFullWeekday: Boolean): String =
    date.format(if (useFullWeekday) screenFullWeekdayFormatter else screenShortWeekdayFormatter)

@Composable
fun DiscordScreen(
    viewModel: DiscordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val appColors = AppTheme.colors
    var customText by remember { mutableStateOf("") }
    var pendingDeleteSentMessage by remember { mutableStateOf<DiscordSentMessageUi?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = appColors.secondary500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("디스코드 전송", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(Modifier.weight(1f).fillMaxWidth()) {
            // 왼쪽: 메시지 조합
            Column(Modifier.weight(0.55f).fillMaxSize().padding(16.dp)) {
                Text("메시지 조합", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = customText, onValueChange = { customText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                    placeholder = { Text("직접 입력해서 조합 목록에 추가") },
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComposerAddButton(
                        text = "직접 작성 추가", icon = Icons.Default.Add, color = appColors.grey700,
                        enabled = customText.isNotEmpty(),
                        onClick = { viewModel.addCustomSegment(customText); customText = "" }
                    )
                    ComposerAddButton(
                        text = "저장 문구", icon = Icons.Default.FormatQuote, color = appColors.primary500,
                        onClick = { viewModel.setDialog(showPhrasePicker = true) }
                    )
                    ComposerAddButton(
                        text = "태그", icon = Icons.Default.AlternateEmail, color = appColors.secondary500,
                        onClick = { viewModel.setDialog(showTagPicker = true) }
                    )
                    ComposerAddButton(
                        text = "날짜(내일)", icon = Icons.Default.CalendarMonth, color = appColors.warning500,
                        onClick = viewModel::addDateSegment
                    )
                    ComposerAddButton(
                        text = "프리셋", icon = Icons.Default.Collections, color = appColors.warning500,
                        onClick = { viewModel.setDialog(showPhrasePresetPicker = true) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("각 줄 오른쪽의 ↵는 줄바꿈, —는 앞 조각과 공백으로 이어붙입니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                if (state.segments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("위에서 문구·태그를 추가해 메시지를 조합하세요", style = MaterialTheme.typography.bodySmall, color = appColors.grey400)
                    }
                } else {
                    LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.segments, key = { it.id }) { segment ->
                            val accent = segmentAccent(segment.type, appColors)
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.width(70.dp)) {
                                    Icon(segmentIcon(segment.type), contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                                    Text(segmentLabel(segment.type), style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Medium)
                                }
                                if (segment.type == DiscordSegmentType.DATE) {
                                    DateSegmentContent(
                                        segment = segment,
                                        onPickDate = { date -> viewModel.updateDateSegment(segment.id, date = date) },
                                        onToggleWeekday = { full -> viewModel.updateDateSegment(segment.id, useFullWeekday = full) },
                                        onSuffixChange = { text -> viewModel.updateDateSegment(segment.id, suffix = text) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = segment.text,
                                        onValueChange = { viewModel.updateSegmentText(segment.id, it) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                ClickShrinkEffect(shrinkFactor = 0.9f, onClick = { viewModel.toggleLineBreakAfter(segment.id) }) {
                                    Text(
                                        text = if (segment.lineBreakAfter) "↵" else "—",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (segment.lineBreakAfter) appColors.primary500 else appColors.grey500,
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .width(24.dp)
                                    )
                                }
                                Column {
                                    IconButton(onClick = { viewModel.moveSegment(segment.id, -1) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "위로", tint = appColors.grey600, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.moveSegment(segment.id, 1) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "아래로", tint = appColors.grey600, modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(onClick = { viewModel.removeSegment(segment.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 오른쪽: 미리보기 + 전송
            Column(Modifier.weight(0.45f).fillMaxSize().padding(16.dp)) {
                Text("미리보기", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    if (state.previewText.isBlank()) {
                        Text("미리보기가 여기에 표시됩니다", style = MaterialTheme.typography.bodySmall, color = appColors.grey400)
                    } else {
                        Text(state.previewText, style = MaterialTheme.typography.bodyMedium)
                    }
                }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("전송 대상", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = viewModel::refreshWebhookTargets, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "전송 대상 새로고침", tint = appColors.grey600, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                if (state.isLoadingWebhookTargets) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (state.webhookTargets.isEmpty()) {
                    Text("등록된 전송 대상이 없습니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.webhookTargets.forEach { target ->
                            val selected = target.name == state.selectedWebhookName
                            ClickShrinkEffect(shrinkFactor = 0.95f, onClick = { viewModel.selectWebhook(target.name) }) {
                                Text(
                                    text = target.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) appColors.white else appColors.grey700,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (selected) appColors.secondary500 else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (state.sendError != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(appColors.crimson50).padding(12.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = appColors.crimson500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.sendError.orEmpty(), style = MaterialTheme.typography.bodySmall, color = appColors.crimson500)
                    }
                }
                if (state.sendSuccess) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(appColors.success50).padding(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = appColors.success500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("전송했습니다", style = MaterialTheme.typography.bodySmall, color = appColors.success700)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = viewModel::send,
                    enabled = !state.isSending && state.segments.isNotEmpty() && state.selectedWebhookName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = appColors.white, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("전송")
                    }
                }

                if (state.sentMessages.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    Text("최근 전송", style = MaterialTheme.typography.labelLarge, color = appColors.grey700, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("수정·삭제는 실제 디스코드 메시지에도 반영됩니다", style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.sentMessages.forEach { sent ->
                            SentMessageRow(
                                sent = sent,
                                isEditing = state.editingSentMessageId == sent.id,
                                editingText = state.editingText,
                                isEditingSending = state.isEditingSending,
                                editError = state.editError,
                                onStartEdit = { viewModel.startEditSentMessage(sent) },
                                onEditTextChange = viewModel::updateEditingText,
                                onCancelEdit = viewModel::cancelEditSentMessage,
                                onSubmitEdit = viewModel::submitEditSentMessage,
                                onDelete = { pendingDeleteSentMessage = sent }
                            )
                        }
                    }
                }
            }
            }
        }
    }

    if (state.showTagPicker) {
        TagPickerDialog(
            tags = state.savedTags,
            onPick = { viewModel.addTagSegment(it); viewModel.setDialog(showTagPicker = false) },
            onSave = viewModel::saveTag,
            onDelete = viewModel::deleteTag,
            onDismiss = { viewModel.setDialog(showTagPicker = false) }
        )
    }
    if (state.showPhrasePicker) {
        PhrasePickerDialog(
            phrases = state.savedPhrases,
            onPick = { viewModel.addPhraseSegment(it); viewModel.setDialog(showPhrasePicker = false) },
            onSave = viewModel::savePhrase,
            onDelete = viewModel::deletePhrase,
            onDismiss = { viewModel.setDialog(showPhrasePicker = false) }
        )
    }
    if (state.showPhrasePresetPicker) {
        PhrasePresetDialog(
            presets = state.phrasePresets,
            canSaveCurrent = state.segments.isNotEmpty(),
            onPick = { viewModel.loadPhrasePreset(it); viewModel.setDialog(showPhrasePresetPicker = false) },
            onSaveCurrent = viewModel::saveCurrentAsPhrasePreset,
            onDelete = viewModel::deletePhrasePreset,
            onDismiss = { viewModel.setDialog(showPhrasePresetPicker = false) }
        )
    }

    pendingDeleteSentMessage?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSentMessage = null },
            title = { Text("메시지 삭제") },
            text = { Text("디스코드에 보낸 메시지를 삭제할까요? 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSentMessage(target)
                    pendingDeleteSentMessage = null
                }) { Text("삭제", color = appColors.crimson500) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSentMessage = null }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun ComposerAddButton(text: String, icon: ImageVector, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    ClickShrinkEffect(shrinkFactor = if (enabled) 0.95f else 1f, onClick = { if (enabled) onClick() }) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = if (enabled) 0.12f else 0.06f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = if (enabled) 1f else 0.4f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = color.copy(alpha = if (enabled) 1f else 0.4f))
        }
    }
}

@Composable
private fun DateSegmentContent(
    segment: DiscordSegmentUi,
    onPickDate: (LocalDate) -> Unit,
    onToggleWeekday: (Boolean) -> Unit,
    onSuffixChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    var showDatePicker by remember(segment.id) { mutableStateOf(false) }
    val date = segment.date ?: LocalDate.now()

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ClickShrinkEffect(shrinkFactor = 0.95f, onClick = { showDatePicker = true }) {
                Text(
                    text = formatShortDate(date, segment.useFullWeekday),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.grey700,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false, true).forEach { isFull ->
                    val label = "(" + date.format(DateTimeFormatter.ofPattern(if (isFull) "EEEE" else "E", Locale.KOREAN)) + ")"
                    val selected = segment.useFullWeekday == isFull
                    ClickShrinkEffect(shrinkFactor = 0.95f, onClick = { onToggleWeekday(isFull) }) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) appColors.white else appColors.grey700,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) appColors.warning500 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = segment.suffix,
            onValueChange = onSuffixChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("날짜 옆에 붙일 문구 (선택)") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onPickDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SentMessageRow(
    sent: DiscordSentMessageUi,
    isEditing: Boolean,
    editingText: String,
    isEditingSending: Boolean,
    editError: String?,
    onStartEdit: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSubmitEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val appColors = AppTheme.colors

    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(sent.webhookName, style = MaterialTheme.typography.labelSmall, color = appColors.secondary700, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (!isEditing) {
                IconButton(onClick = onStartEdit, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "수정", tint = appColors.primary500, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "삭제", tint = appColors.crimson400.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                }
            }
        }

        if (isEditing) {
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = editingText, onValueChange = onEditTextChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            if (editError != null) {
                Spacer(Modifier.height(4.dp))
                Text(editError, style = MaterialTheme.typography.labelSmall, color = appColors.crimson500)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancelEdit, enabled = !isEditingSending) { Text("취소", color = appColors.grey600) }
                Button(onClick = onSubmitEdit, enabled = !isEditingSending, shape = RoundedCornerShape(8.dp)) {
                    if (isEditingSending) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = appColors.white, strokeWidth = 2.dp)
                    } else {
                        Text("수정 반영")
                    }
                }
            }
        } else {
            Text(sent.content, style = MaterialTheme.typography.bodySmall, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
