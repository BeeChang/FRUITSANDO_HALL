package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.R
import example.yf.fruit_hall.data.discord.DiscordWebhookTarget
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.RoundSummary
import example.yf.fruit_hall.ui.traysplit.buildDiscordSummaryMessage
import example.yf.fruit_hall.ui.traysplit.defaultRoundItemsText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(E)", Locale.KOREAN)
private val fullWeekdayFormatter = DateTimeFormatter.ofPattern("M/d일(EEEE)", Locale.KOREAN)

// 화면 회전으로 액티비티가 재생성돼도 유저가 직접 수정한 차수별 품목 문구가 날아가지 않도록 rememberSaveable에 담기 위한 Saver
private val roundItemsTextSaver = Saver<SnapshotStateMap<Int, String>, String>(
    save = { Json.encodeToString(it.toMap()) },
    restore = { encoded -> mutableStateMapOf<Int, String>().apply { putAll(Json.decodeFromString<Map<Int, String>>(encoded)) } }
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscordSendDialog(
    summaries: List<RoundSummary>,
    initialTitlePrefix: String,
    initialTitleSuffix: String,
    initialRoundTimes: Map<Int, String>,
    initialExtraText: String,
    webhookTargets: List<DiscordWebhookTarget>,
    selectedWebhookName: String,
    isLoadingWebhookTargets: Boolean,
    onSelectWebhook: (String) -> Unit,
    isSending: Boolean,
    errorMessage: String?,
    onSend: (titleLine: String, titlePrefix: String, titleSuffix: String, roundTimes: Map<Int, String>, roundItemsText: Map<Int, String>, extraText: String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var titlePrefix by remember { mutableStateOf(initialTitlePrefix) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var useFullWeekday by remember { mutableStateOf(false) }
    val dateText = selectedDate.format(if (useFullWeekday) fullWeekdayFormatter else shortWeekdayFormatter)
    var titleSuffix by remember { mutableStateOf(initialTitleSuffix) }
    val roundTimes = remember {
        mutableStateMapOf(*summaries.map { it.round to initialRoundTimes[it.round].orEmpty() }.toTypedArray())
    }
    // 차수별 실제 배정 품목으로 기본값을 채우되, 유저가 자유롭게 추가/삭제할 수 있도록 편집 가능한 텍스트로 둠
    val roundItemsText = rememberSaveable(saver = roundItemsTextSaver) {
        mutableStateMapOf(*summaries.map { it.round to defaultRoundItemsText(it) }.toTypedArray())
    }
    var extraText by remember { mutableStateOf(initialExtraText) }

    val titleLine = listOf(titlePrefix.trim(), dateText, titleSuffix.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
    val previewText = buildDiscordSummaryMessage(titleLine, summaries, roundTimes, roundItemsText, extraText)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.widthIn(max = 1080.dp).fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                TraySplitDialogHeader(
                    title = stringResource(R.string.tray_discord_dialog_title),
                    onDismiss = onDismiss,
                    icon = Icons.AutoMirrored.Filled.Send
                )

                Row(modifier = Modifier.heightIn(max = 480.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tray_discord_date_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.grey900
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = titlePrefix,
                                onValueChange = { titlePrefix = it },
                                placeholder = { Text(stringResource(R.string.tray_discord_title_prefix_placeholder)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ClickShrinkEffect(onClick = { showDatePicker = true }, shrinkFactor = 0.95f) {
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.grey700,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(horizontal = 12.dp, vertical = 14.dp)
                                )
                            }
                            OutlinedTextField(
                                value = titleSuffix,
                                onValueChange = { titleSuffix = it },
                                placeholder = { Text(stringResource(R.string.tray_discord_title_suffix_placeholder)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(false, true).forEach { isFull ->
                                val weekdayLabel = "(" +
                                    selectedDate.format(DateTimeFormatter.ofPattern(if (isFull) "EEEE" else "E", Locale.KOREAN)) +
                                    ")"
                                val selected = useFullWeekday == isFull
                                ClickShrinkEffect(onClick = { useFullWeekday = isFull }, shrinkFactor = 0.95f) {
                                    Text(
                                        text = weekdayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) appColors.white else appColors.grey700,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = stringResource(R.string.tray_discord_round_time_section_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.grey900
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            summaries.forEach { summary ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = roundTimes[summary.round].orEmpty(),
                                        onValueChange = { roundTimes[summary.round] = it },
                                        label = { Text(stringResource(R.string.tray_discord_round_time_label, summary.round)) },
                                        placeholder = { Text(stringResource(R.string.tray_discord_round_time_placeholder)) },
                                        modifier = Modifier.width(140.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = roundItemsText[summary.round].orEmpty(),
                                        onValueChange = { roundItemsText[summary.round] = it },
                                        label = { Text(stringResource(R.string.tray_discord_round_items_label, summary.round)) },
                                        placeholder = { Text(stringResource(R.string.tray_discord_round_items_placeholder)) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        OutlinedTextField(
                            value = extraText,
                            onValueChange = { extraText = it },
                            label = { Text(stringResource(R.string.tray_discord_extra_text_label)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tray_discord_preview_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.grey900
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = stringResource(R.string.tray_discord_webhook_target_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.grey900
                    )
                    Spacer(Modifier.height(6.dp))
                    if (isLoadingWebhookTargets) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else if (webhookTargets.isEmpty()) {
                        Text(
                            text = stringResource(R.string.tray_discord_webhook_target_empty),
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.grey400
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            webhookTargets.forEach { target ->
                                val selected = target.name == selectedWebhookName
                                ClickShrinkEffect(onClick = { onSelectWebhook(target.name) }, shrinkFactor = 0.95f) {
                                    Text(
                                        text = target.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) appColors.white else appColors.grey700,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(if (errorMessage != null) 12.dp else 4.dp))

                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appColors.crimson50)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = appColors.crimson500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tray_discord_send_error, errorMessage),
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.crimson500
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSending,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel), color = appColors.grey600)
                    }
                    Button(
                        onClick = { onSend(titleLine, titlePrefix, titleSuffix, roundTimes.toMap(), roundItemsText.toMap(), extraText) },
                        enabled = !isSending && selectedWebhookName.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = appColors.white,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.tray_discord_send_button))
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
