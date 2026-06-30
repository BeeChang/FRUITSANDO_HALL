package example.yf.fruit_hall.ui.position

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.position.component.HistoryDialog
import example.yf.fruit_hall.ui.position.component.MemberDialog
import example.yf.fruit_hall.ui.position.component.MemberPanel
import example.yf.fruit_hall.ui.position.component.PositionDialog
import example.yf.fruit_hall.ui.position.component.ResultCardGrid
import example.yf.fruit_hall.ui.position.component.SlotSettingsDialog
import example.yf.fruit_hall.ui.position.component.WeightSettingsDialog
import example.yf.fruit_hall.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cancelBtnBg    = Color(0xFFEEEEEE)
private val cancelBtnText  = Color(0xFF888888)
private val reshuffleBtnBg = Color(0xFFB3D9FF)
private val reshuffleBtnText = Color(0xFF2D6FA8)
private val confirmBtnBg   = Color(0xFFFF9BB5)
private val confirmBtnText = Color(0xFFFFFFFF)
private val closeBtnBg     = Color(0xFFEEEEEE)
private val closeBtnText   = Color(0xFF888888)

@Composable
private fun PositionActionButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClickShrinkEffect(
        shrinkFactor = 0.93f,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun PositionScreen(
    viewModel: PositionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MemberPanel(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.28f),
            members = uiState.members,
            memberWeights = uiState.memberWeights,
            positions = uiState.positions,
            onToggleWorking = { viewModel.onEvent(PositionEvent.ToggleWorking(it)) },
            onManageClick = { viewModel.onEvent(PositionEvent.ShowMemberDialog) },
            onDeleteMember = { viewModel.onEvent(PositionEvent.DeleteMember(it)) },
            onReorderMembers = { ids -> viewModel.onEvent(PositionEvent.ReorderMembers(ids)) }
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.72f)
        ) {
            PositionTopBar(uiState = uiState, onEvent = viewModel::onEvent)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isDrawDone) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ResultCardGrid(
                            drawResult = uiState.drawResult,
                            onSwap = { from, memberId, to ->
                                viewModel.onEvent(PositionEvent.SwapMembers(from, memberId, to))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                            ) {
                                if (uiState.isConfirmedSlot) {
                                    PositionActionButton(
                                        text = "다시 섞기",
                                        bgColor = reshuffleBtnBg,
                                        textColor = reshuffleBtnText,
                                        onClick = { viewModel.onEvent(PositionEvent.StartDraw) }
                                    )
                                    PositionActionButton(
                                        text = "닫기",
                                        bgColor = closeBtnBg,
                                        textColor = closeBtnText,
                                        onClick = { viewModel.onEvent(PositionEvent.CancelDraw) }
                                    )
                                } else {
                                    PositionActionButton(
                                        text = "취소",
                                        bgColor = cancelBtnBg,
                                        textColor = cancelBtnText,
                                        onClick = { viewModel.onEvent(PositionEvent.CancelDraw) }
                                    )
                                    PositionActionButton(
                                        text = "다시 섞기",
                                        bgColor = reshuffleBtnBg,
                                        textColor = reshuffleBtnText,
                                        onClick = { viewModel.onEvent(PositionEvent.StartDraw) }
                                    )
                                    PositionActionButton(
                                        text = "확정",
                                        bgColor = confirmBtnBg,
                                        textColor = confirmBtnText,
                                        onClick = { viewModel.onEvent(PositionEvent.ConfirmDraw) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    DrawPrompt(
                        uiState = uiState,
                        onDraw = { viewModel.onEvent(PositionEvent.StartDraw) },
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }

    if (uiState.showMemberDialog) {
        MemberDialog(
            members = uiState.members,
            onAddMember = { name, colorHex ->
                viewModel.onEvent(PositionEvent.AddMember(name, colorHex))
            },
            onDeleteMember = { viewModel.onEvent(PositionEvent.DeleteMember(it)) },
            onDismiss = { viewModel.onEvent(PositionEvent.HideMemberDialog) }
        )
    }

    if (uiState.showPositionDialog) {
        PositionDialog(
            positions = uiState.positions,
            onAddPosition = { name, isMulti ->
                viewModel.onEvent(PositionEvent.AddPosition(name, isMulti))
            },
            onDeletePosition = { viewModel.onEvent(PositionEvent.DeletePosition(it)) },
            onDismiss = { viewModel.onEvent(PositionEvent.HidePositionDialog) }
        )
    }

    if (uiState.showWeightDialog) {
        WeightSettingsDialog(
            positions = uiState.positions,
            onUpdatePosition = { viewModel.onEvent(it) },
            onDismiss = { viewModel.onEvent(PositionEvent.HideWeightDialog) }
        )
    }

    if (uiState.showSlotDialog) {
        SlotSettingsDialog(
            currentTotalSlots = uiState.totalSlots,
            onConfirm = { viewModel.onEvent(PositionEvent.UpdateSlotSettings(it)) },
            onDismiss = { viewModel.onEvent(PositionEvent.HideSlotDialog) }
        )
    }

    if (uiState.showHistoryDialog) {
        HistoryDialog(
            history = uiState.todayHistory,
            onResetToday = { viewModel.onEvent(PositionEvent.ResetToday) },
            onResetAll = { viewModel.onEvent(PositionEvent.ResetAll) },
            onDismiss = { viewModel.onEvent(PositionEvent.HideHistoryDialog) }
        )
    }
}

@Composable
private fun PositionTopBar(
    uiState: PositionUiState,
    onEvent: (PositionEvent) -> Unit
) {
    val appColors = AppTheme.colors
    var dateMonth by remember { mutableStateOf("") }
    var dateDay by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val monthFmt = SimpleDateFormat("M", Locale.KOREA)
        val dayFmt = SimpleDateFormat("d", Locale.KOREA)
        val timeFmt = SimpleDateFormat("HH:mm", Locale.KOREA)
        while (true) {
            val now = Date()
            dateMonth = monthFmt.format(now)
            dateDay = dayFmt.format(now)
            timeText = timeFmt.format(now)
            delay(30_000L)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 날짜: 6월 30일 (숫자 크게, 월·일 작게)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = dateMonth,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary500
                )
                Text(
                    text = "월",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary500.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 1.dp, bottom = 3.dp, end = 6.dp)
                )
                Text(
                    text = dateDay,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary500
                )
                Text(
                    text = "일",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary500.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 1.dp, bottom = 3.dp)
                )
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.primary500
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (slot in 1..uiState.totalSlots) {
                    val isSelected = uiState.currentSlot == slot
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) appColors.primary500
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .then(
                                if (!isSelected) Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(50)
                                ) else Modifier
                            )
                            .clickable { onEvent(PositionEvent.SelectSlot(slot)) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${slot}차",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = { onEvent(PositionEvent.ShowHistoryDialog) }) {
                Icon(Icons.Default.History, contentDescription = "기록", tint = appColors.grey600)
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowPositionDialog) }) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "포지션", tint = appColors.grey600)
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowWeightDialog) }) {
                Icon(Icons.Default.Tune, contentDescription = "가중치", tint = appColors.grey600)
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowSlotDialog) }) {
                Icon(Icons.Default.Settings, contentDescription = "차수 설정", tint = appColors.grey600)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawPrompt(
    uiState: PositionUiState,
    onDraw: () -> Unit,
    onEvent: (PositionEvent) -> Unit
) {
    val appColors = AppTheme.colors
    val workingMembers = uiState.members.filter { it.isWorking }
    val enabledPositions = uiState.positions.filter { it.id in uiState.enabledPositionIds }
    val canDraw = workingMembers.isNotEmpty() && enabledPositions.isNotEmpty()
    val tooManyPositions = enabledPositions.size > workingMembers.size && workingMembers.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        // ── 헤더 ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${uiState.currentSlot}차 배정",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "출근 ${workingMembers.size}명 · 포지션 ${enabledPositions.size}개 활성",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (tooManyPositions) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(appColors.crimson50)
                        .border(1.dp, appColors.crimson200.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = appColors.crimson500,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "출근 인원이 포지션보다 적습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.crimson500
                        )
                        Text(
                            text = "포지션 ${enabledPositions.size - workingMembers.size}개가 배정에서 제외됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = appColors.crimson400
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 출근 멤버 strip ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "출근 중",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (workingMembers.isEmpty()) {
                Text(
                    text = "왼쪽 패널에서 출근 멤버를 선택하세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.crimson400
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workingMembers.forEach { member ->
                        val memberColor = try {
                            Color(android.graphics.Color.parseColor(member.colorHex))
                        } catch (_: Exception) { Color(0xFFFFB3C6) }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = memberColor.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2D2D2D),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // ── 포지션 선택 헤더 ───────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "포지션 선택",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (uiState.positions.isNotEmpty()) {
                Text(
                    text = "${enabledPositions.size} / ${uiState.positions.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tooManyPositions) appColors.crimson400
                            else appColors.primary400
                )
            }
            Text(
                text = "탭하여 켜기/끄기",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── 포지션 3열 그리드 ──────────────────────────────────────
        if (uiState.positions.isEmpty()) {
            Text(
                text = "우측 상단 목록 아이콘으로 포지션을 추가하세요",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.crimson400
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                uiState.positions.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        rowItems.forEach { position ->
                            PositionToggleChip(
                                name = position.name,
                                isEnabled = position.id in uiState.enabledPositionIds,
                                onClick = { onEvent(PositionEvent.TogglePositionEnabled(position.id)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── 뽑기 버튼 ─────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ClickShrinkEffect(
                shrinkFactor = if (canDraw) 0.93f else 1f,
                onClick = { if (canDraw) onDraw() }
            ) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (canDraw) confirmBtnBg
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "뽑기!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canDraw) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionToggleChip(
    name: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors

    ClickShrinkEffect(shrinkFactor = 0.96f, onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isEnabled) appColors.primary300.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(
                    width = if (isEnabled) 1.dp else 1.dp,
                    color = if (isEnabled) appColors.primary300.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) appColors.primary400.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isEnabled) Color(0xFF2D2D2D)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f)
            )
            if (isEnabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = appColors.primary400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}