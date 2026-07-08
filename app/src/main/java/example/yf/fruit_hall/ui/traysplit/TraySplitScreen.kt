package example.yf.fruit_hall.ui.traysplit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.core.RoughSize
import example.yf.fruit_hall.ui.component.AppDialog
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.component.AddSpaceDialog
import example.yf.fruit_hall.ui.traysplit.component.AddTrayDialog
import example.yf.fruit_hall.ui.traysplit.component.AllocationResultDialog
import example.yf.fruit_hall.ui.traysplit.component.AllocationSettingsDialog
import example.yf.fruit_hall.ui.traysplit.component.RoundChoiceSheet
import example.yf.fruit_hall.ui.traysplit.component.RoundSummaryDialog
import example.yf.fruit_hall.ui.traysplit.component.roundColorFor
import example.yf.fruit_hall.ui.traysplit.component.SnackChipPalette
import example.yf.fruit_hall.ui.traysplit.component.SnackTypeManageDialog
import example.yf.fruit_hall.ui.traysplit.component.SpaceColumn
import example.yf.fruit_hall.ui.traysplit.component.strategyDisplayName
import example.yf.fruit_hall.ui.traysplit.component.toSnackColor
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TraySplitScreen(
    viewModel: TraySplitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent

    var draggedType by remember { mutableStateOf<SnackTypeUi?>(null) }
    var dragWindowPos by remember { mutableStateOf(Offset.Zero) }
    val trayBounds = remember { mutableStateMapOf<Long, Rect>() }
    var highlightedTrayId by remember { mutableStateOf<Long?>(null) }
    var rootBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // 삭제/초기화된 판의 낡은 좌표가 남아있으면 드롭 시 존재하지 않는 trayId로 INSERT가 나가
    // FK 제약 위반 크래시가 난다. 판 목록이 바뀔 때마다 현재 존재하는 trayId만 남긴다.
    LaunchedEffect(uiState.trays) {
        val validIds = uiState.trays.mapTo(mutableSetOf()) { it.id }
        trayBounds.keys.retainAll(validIds)
    }

    fun onChipDragStart(type: SnackTypeUi, pos: Offset) {
        draggedType = type
        dragWindowPos = pos
    }
    fun onChipDrag(delta: Offset) {
        dragWindowPos += delta
        highlightedTrayId = trayBounds.entries.firstOrNull { it.value.contains(dragWindowPos) }?.key
    }
    fun onChipDragEnd() {
        val target = highlightedTrayId
        val type = draggedType
        if (target != null && type != null) {
            onEvent(TraySplitEvent.AddItemToTray(target, type.id, RoughSize.M))
        }
        draggedType = null
        highlightedTrayId = null
        dragWindowPos = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { rootBoundsInWindow = it.boundsInWindow() }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth > 900.dp

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    SnackChipPalette(
                        snackTypes = uiState.snackTypes,
                        onManageClick = { onEvent(TraySplitEvent.ShowSnackTypeDialog) },
                        onChipDragStart = ::onChipDragStart,
                        onChipDrag = ::onChipDrag,
                        onChipDragEnd = ::onChipDragEnd,
                        modifier = Modifier.width(150.dp).fillMaxHeight().padding(12.dp)
                    )
                    VerticalDivider()
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        TraySplitTopBar(uiState = uiState, onEvent = onEvent)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TraySplitBody(
                            uiState = uiState,
                            onEvent = onEvent,
                            highlightedTrayId = highlightedTrayId,
                            onTrayBoundsChanged = { id, rect -> trayBounds[id] = rect },
                            modifier = Modifier.weight(1f)
                        )
                        TraySplitBottomBar(uiState = uiState, onEvent = onEvent, onResetClick = { showResetConfirm = true })
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TraySplitTopBar(uiState = uiState, onEvent = onEvent)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SnackChipPalette(
                        snackTypes = uiState.snackTypes,
                        onManageClick = { onEvent(TraySplitEvent.ShowSnackTypeDialog) },
                        onChipDragStart = ::onChipDragStart,
                        onChipDrag = ::onChipDrag,
                        onChipDragEnd = ::onChipDragEnd,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TraySplitBody(
                        uiState = uiState,
                        onEvent = onEvent,
                        highlightedTrayId = highlightedTrayId,
                        onTrayBoundsChanged = { id, rect -> trayBounds[id] = rect },
                        modifier = Modifier.weight(1f)
                    )
                    TraySplitBottomBar(uiState = uiState, onEvent = onEvent, onResetClick = { showResetConfirm = true })
                }
            }
        }

        if (draggedType != null) {
            val chipColor = draggedType?.colorHex?.toSnackColor() ?: Color(0xFFFFB3C6)
            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragWindowPos.x - rootBoundsInWindow.left).roundToInt() - 60,
                            (dragWindowPos.y - rootBoundsInWindow.top).roundToInt() - 18
                        )
                    }
                    .zIndex(100f)
                    .clip(RoundedCornerShape(50))
                    .background(chipColor.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = draggedType?.name ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2D2D2D),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        uiState.snackbarMessage?.let { message ->
            LaunchedEffect(message) {
                delay(2000)
                onEvent(TraySplitEvent.ClearSnackbar)
            }
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomCenter) {
                Surface(shape = RoundedCornerShape(14.dp), color = AppTheme.colors.grey900) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.white,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }

    if (uiState.showSpaceDialog) {
        AddSpaceDialog(
            onAdd = { name -> onEvent(TraySplitEvent.AddSpace(name)) },
            onDismiss = { onEvent(TraySplitEvent.HideSpaceDialog) }
        )
    }

    if (uiState.showSnackTypeDialog) {
        SnackTypeManageDialog(
            snackTypes = uiState.snackTypes,
            onAdd = { name, color, secondary -> onEvent(TraySplitEvent.AddSnackType(name, color, secondary)) },
            onDelete = { onEvent(TraySplitEvent.DeleteSnackType(it)) },
            onReorder = { onEvent(TraySplitEvent.ReorderSnackTypes(it)) },
            onDismiss = { onEvent(TraySplitEvent.HideSnackTypeDialog) }
        )
    }

    uiState.addTrayTargetSpaceId?.let { spaceId ->
        AddTrayDialog(
            snackTypes = uiState.snackTypes,
            onConfirm = { items -> onEvent(TraySplitEvent.SubmitAddTrayDialog(spaceId, items)) },
            onDismiss = { onEvent(TraySplitEvent.HideAddTrayDialog) }
        )
    }

    if (uiState.showSettingsDialog) {
        AllocationSettingsDialog(
            settings = uiState.settings,
            onConfirm = { onEvent(TraySplitEvent.UpdateSettings(it)) },
            onDismiss = { onEvent(TraySplitEvent.HideSettingsDialog) }
        )
    }

    if (uiState.showResultDialog) {
        AllocationResultDialog(
            attemptCount = uiState.attemptCount,
            candidates = uiState.candidates,
            onOpenSummary = { onEvent(TraySplitEvent.ShowCandidateDetail(it)) },
            onDismiss = { onEvent(TraySplitEvent.HideResultDialog) }
        )
    }

    uiState.candidateDetailIndex?.let { index ->
        uiState.candidates.getOrNull(index)?.let { candidate ->
            val candidateAssignment = candidate.assignment.entries.associate { it.key.toLong() to it.value }
            RoundSummaryDialog(
                title = strategyDisplayName(candidate.strategyName),
                summaries = buildRoundSummaries(uiState.trays, candidateAssignment, uiState.settings.rounds),
                violations = candidate.violations,
                onSelectCandidate = { onEvent(TraySplitEvent.SelectCandidate(index)) },
                onDismiss = { onEvent(TraySplitEvent.HideCandidateDetail) }
            )
        }
    }

    if (uiState.showOverallSummary) {
        RoundSummaryDialog(
            title = "차수별 총정리",
            summaries = buildRoundSummaries(uiState.trays, uiState.currentAssignment, uiState.settings.rounds),
            onDismiss = { onEvent(TraySplitEvent.HideOverallSummary) }
        )
    }

    uiState.pinSheetTrayId?.let { trayId ->
        val tray = uiState.trays.find { it.id == trayId }
        RoundChoiceSheet(
            title = "차수 고정",
            rounds = uiState.settings.rounds,
            current = tray?.pinnedRound,
            onSelect = { round -> onEvent(TraySplitEvent.PinTray(trayId, round)) },
            onClear = { onEvent(TraySplitEvent.PinTray(trayId, null)) },
            onDismiss = { onEvent(TraySplitEvent.HidePinSheet) }
        )
    }

    uiState.roundPickerTrayId?.let { trayId ->
        RoundChoiceSheet(
            title = "차수 변경",
            rounds = uiState.settings.rounds,
            current = uiState.currentAssignment[trayId],
            onSelect = { round -> onEvent(TraySplitEvent.MoveTrayToRound(trayId, round)) },
            onDismiss = { onEvent(TraySplitEvent.HideRoundPicker) }
        )
    }

    AppDialog(
        title = "초기화",
        content = "오늘 등록한 판이 모두 삭제됩니다. 공간과 품목은 유지됩니다. 계속할까요?",
        isShowDialog = showResetConfirm,
        onConfirm = { onEvent(TraySplitEvent.ResetDay); showResetConfirm = false },
        onDismiss = { showResetConfirm = false }
    )
}

@Composable
private fun TraySplitTopBar(uiState: TraySplitUiState, onEvent: (TraySplitEvent) -> Unit) {
    val appColors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "판나누기",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "총 ${uiState.trays.size}판",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.primary600
            )
        }
        if (uiState.roundSizes.isNotEmpty()) {
            RoundSummaryStrip(roundSizes = uiState.roundSizes)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(appColors.grey900)
                    .clickable { onEvent(TraySplitEvent.ShowOverallSummary) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "총정리 확인",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.white
                )
            }
            Spacer(Modifier.width(12.dp))
        }
        IconButton(onClick = { onEvent(TraySplitEvent.ShowSpaceDialog) }) {
            Icon(Icons.Default.Add, contentDescription = "공간 추가", tint = appColors.grey600)
        }
        IconButton(onClick = { onEvent(TraySplitEvent.ShowSettingsDialog) }) {
            Icon(Icons.Default.Tune, contentDescription = "배분 설정", tint = appColors.grey600)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoundSummaryStrip(roundSizes: List<Int>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        roundSizes.forEachIndexed { i, size ->
            val (roundBg, roundText) = roundColorFor(i + 1)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(roundBg.copy(alpha = 0.28f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("${i + 1}차 ${size}", style = MaterialTheme.typography.bodyMedium, color = roundText, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun TraySplitBody(
    uiState: TraySplitUiState,
    onEvent: (TraySplitEvent) -> Unit,
    highlightedTrayId: Long?,
    onTrayBoundsChanged: (Long, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    if (uiState.spaces.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "우측 상단 + 로 공간을 추가하고 판을 등록해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.grey400
            )
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(uiState.spaces, key = { it.id }) { space ->
            SpaceColumn(
                space = space,
                trays = uiState.trays.filter { it.spaceId == space.id },
                currentAssignment = uiState.currentAssignment,
                highlightedTrayId = highlightedTrayId,
                onBoundsChanged = onTrayBoundsChanged,
                onQuickAddTray = { onEvent(TraySplitEvent.QuickAddTray(space.id)) },
                onAddTrayDialog = { onEvent(TraySplitEvent.ShowAddTrayDialog(space.id)) },
                onDeleteSpace = { onEvent(TraySplitEvent.DeleteSpace(space.id)) },
                onDeleteTray = { onEvent(TraySplitEvent.DeleteTray(it)) },
                onCycleItemSize = { trayId, snackTypeId -> onEvent(TraySplitEvent.CycleItemSize(trayId, snackTypeId)) },
                onRemoveItem = { trayId, snackTypeId -> onEvent(TraySplitEvent.RemoveItemFromTray(trayId, snackTypeId)) },
                onLongPressPin = { onEvent(TraySplitEvent.ShowPinSheet(it)) },
                onTapRoundBadge = { onEvent(TraySplitEvent.ShowRoundPicker(it)) }
            )
        }
    }
}

@Composable
private fun TraySplitBottomBar(
    uiState: TraySplitUiState,
    onEvent: (TraySplitEvent) -> Unit,
    onResetClick: () -> Unit
) {
    val appColors = AppTheme.colors
    Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomActionButton(
                text = "초기화",
                icon = Icons.Default.RestartAlt,
                bgColor = Color(0xFFEEEEEE),
                textColor = Color(0xFF888888),
                onClick = onResetClick
            )
            Spacer(Modifier.weight(1f))
            BottomActionButton(
                text = "배분 실행",
                icon = Icons.Default.Casino,
                bgColor = Color(0xFFB3D9FF),
                textColor = Color(0xFF2D6FA8),
                enabled = uiState.trays.isNotEmpty() && !uiState.isAllocating,
                onClick = { onEvent(TraySplitEvent.RunAllocation) }
            )
            BottomActionButton(
                text = "확정",
                icon = Icons.Default.Save,
                bgColor = Color(0xFFFF9BB5),
                textColor = Color.White,
                enabled = uiState.currentAssignment.isNotEmpty(),
                onClick = { onEvent(TraySplitEvent.ConfirmAndSave) }
            )
        }
    }
}

@Composable
private fun BottomActionButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ClickShrinkEffect(shrinkFactor = if (enabled) 0.93f else 1f, onClick = { if (enabled) onClick() }) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) textColor else textColor.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
            )
        }
    }
}
