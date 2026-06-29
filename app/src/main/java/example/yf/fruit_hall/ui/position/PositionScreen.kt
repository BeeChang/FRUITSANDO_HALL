package example.yf.fruit_hall.ui.position

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
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
                        onDraw = { viewModel.onEvent(PositionEvent.StartDraw) }
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
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val fmt = SimpleDateFormat("M.d  HH:mm", Locale.KOREA)
        while (true) {
            currentTime = fmt.format(Date())
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
            Text(
                text = currentTime,
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

@Composable
private fun DrawPrompt(uiState: PositionUiState, onDraw: () -> Unit) {
    val appColors = AppTheme.colors
    val workingCount = uiState.members.count { it.isWorking }
    val canDraw = workingCount > 0 && uiState.positions.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${uiState.currentSlot}차 배정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when {
                uiState.positions.isEmpty() -> "포지션을 먼저 추가해주세요 (우측 상단 목록 아이콘)"
                else -> "출근 ${workingCount}명 · 포지션 ${uiState.positions.size}개"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (uiState.positions.isEmpty()) appColors.crimson400
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            uiState.members.filter { it.isWorking }.forEach { member ->
                val memberColor = try {
                    Color(android.graphics.Color.parseColor(member.colorHex))
                } catch (e: Exception) {
                    Color(0xFFFFB3C6)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = memberColor.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2D2D2D),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ClickShrinkEffect(
            shrinkFactor = if (canDraw) 0.93f else 1f,
            onClick = { if (canDraw) onDraw() }
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
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

        if (!canDraw) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (uiState.positions.isEmpty()) "포지션을 먼저 추가해주세요"
                       else "왼쪽에서 출근 중인 멤버를 선택하세요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}