package example.yf.fruit_hall.ui.position

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.ui.position.component.DrawAnimationPanel
import example.yf.fruit_hall.ui.position.component.HistoryDialog
import example.yf.fruit_hall.ui.position.component.MemberDialog
import example.yf.fruit_hall.ui.position.component.MemberPanel
import example.yf.fruit_hall.ui.position.component.PositionDialog
import example.yf.fruit_hall.ui.position.component.ResultCardGrid
import example.yf.fruit_hall.ui.position.component.SlotSettingsDialog
import example.yf.fruit_hall.ui.position.component.WeightSettingsDialog

@Composable
fun PositionScreen(
    viewModel: PositionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        MemberPanel(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.28f),
            members = uiState.members,
            memberWeights = uiState.memberWeights,
            positions = uiState.positions,
            onToggleWorking = { viewModel.onEvent(PositionEvent.ToggleWorking(it)) },
            onManageClick = { viewModel.onEvent(PositionEvent.ShowMemberDialog) }
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.72f)
        ) {
            PositionTopBar(uiState = uiState, onEvent = viewModel::onEvent)

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isAnimating -> {
                        DrawAnimationPanel(
                            members = uiState.members.filter { it.isWorking },
                            onSkip = { viewModel.onEvent(PositionEvent.SkipAnimation) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.isDrawDone -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ResultCardGrid(
                                drawResult = uiState.drawResult,
                                onSwap = { from, memberId, to ->
                                    viewModel.onEvent(PositionEvent.SwapMembers(from, memberId, to))
                                },
                                onRedraw = { viewModel.onEvent(PositionEvent.RedrawPosition(it)) },
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.onEvent(PositionEvent.CancelDraw) }
                                ) {
                                    Text("취소")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.onEvent(PositionEvent.ConfirmDraw) }
                                ) {
                                    Text("확정")
                                }
                            }
                        }
                    }
                    else -> {
                        DrawPrompt(
                            uiState = uiState,
                            onDraw = { viewModel.onEvent(PositionEvent.StartDraw) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showMemberDialog) {
        MemberDialog(
            members = uiState.members,
            onAddMember = { viewModel.onEvent(PositionEvent.AddMember(it)) },
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "포지션 뽑기",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${uiState.currentSlot}차 / ${uiState.totalSlots}차",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { onEvent(PositionEvent.ShowHistoryDialog) }) {
                Icon(Icons.Default.History, contentDescription = "기록 보기")
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowPositionDialog) }) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "포지션 관리")
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowWeightDialog) }) {
                Icon(Icons.Default.Tune, contentDescription = "가중치 설정")
            }
            IconButton(onClick = { onEvent(PositionEvent.ShowSlotDialog) }) {
                Icon(Icons.Default.Settings, contentDescription = "차수 설정")
            }
        }
    }
}

@Composable
private fun DrawPrompt(
    uiState: PositionUiState,
    onDraw: () -> Unit
) {
    val workingCount = uiState.members.count { it.isWorking }
    val isLastSlot = uiState.currentSlot > uiState.totalSlots

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLastSlot) {
            Text(
                text = "오늘의 모든 차수가 완료되었습니다.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "기록에서 초기화 후 다시 시작할 수 있습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "${uiState.currentSlot}차 배정",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "출근 중: $workingCount 명 / 포지션: ${uiState.positions.size} 개",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.padding(16.dp))

            Button(
                onClick = onDraw,
                enabled = workingCount > 0,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "뽑기!",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (workingCount == 0) {
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "왼쪽 패널에서 출근 중인 멤버를 선택하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
