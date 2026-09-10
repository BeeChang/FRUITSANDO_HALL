package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.rotation.RotationScheduleTemplateUi
import example.yf.fruit_hall.ui.rotation.formatMinutes
import example.yf.fruit_hall.ui.theme.AppTheme

/** 타임테이블 아래 드래그 팔레트에서 놓을 수 있는 항목. */
sealed interface PaletteItem {
    data class Template(val template: RotationScheduleTemplateUi) : PaletteItem
    data class PositionItem(val position: RotationPositionUi) : PaletteItem
    /** 휴게는 포지션 테이블에 등록되는 자리가 아니라 셀 상태라, 등록 여부와 무관하게 항상 제공한다. */
    data object BreakItem : PaletteItem
}


/**
 * 근무 스케줄 템플릿 + 포지션을 늘어놓은 드래그 팔레트. 결과 화면·인원배정 화면에서 공용으로 쓴다.
 * 포지션 칩을 표의 칸에 떨어뜨리면 그 자리를 강제로 그 포지션으로 대체한다 — 핀을 걸면 재생성해도 유지된다.
 * 드래그 제스처가 가로 스크롤과 충돌하므로 스크롤 대신 줄바꿈(FlowRow)으로 다 펼쳐 보여준다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RotationSchedulePalette(
    templates: List<RotationScheduleTemplateUi>,
    positions: List<RotationPositionUi>,
    onDragStart: (PaletteItem, windowPos: Offset) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = AppTheme.colors
    val activePositions = positions.filter { it.isActive }.sortedBy { it.sortOrder }
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.rotation_palette_schedule_title),
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                " · " + stringResource(R.string.rotation_palette_hint),
                style = MaterialTheme.typography.labelSmall, color = appColors.grey500
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (templates.isEmpty()) {
                Text(stringResource(R.string.rotation_palette_empty), style = MaterialTheme.typography.labelSmall, color = appColors.grey400)
            } else {
                templates.sortedBy { it.sortOrder }.forEach { template ->
                    DraggablePaletteChip(
                        label = "${template.label} ${formatMinutes(template.startMin)}~${formatMinutes(template.endMin)}",
                        // 근무 스케줄은 포지션·휴게와 놓는 대상이 달라서 색으로 구분한다(연한 파랑).
                        color = appColors.primary200,
                        dotColor = null,
                        onDragStart = { pos -> onDragStart(PaletteItem.Template(template), pos) },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd
                    )
                }
            }
        }

        // 근무스케줄(출퇴근 시간)과 포지션(시간 칸 강제 지정)은 놓는 대상이 다르므로 줄을 분리한다.
        // 휴게는 등록된 포지션이 하나도 없어도 항상 있어야 하므로 이 줄은 조건 없이 그린다.
        Text(
            stringResource(R.string.rotation_palette_position_title),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            activePositions.forEach { position ->
                DraggablePaletteChip(
                    label = position.name,
                    color = appColors.grey300,
                    dotColor = position.colorHex.toRotationColor(rotationDefaultChipColor),
                    onDragStart = { pos -> onDragStart(PaletteItem.PositionItem(position), pos) },
                    onDrag = onDrag,
                    onDragEnd = onDragEnd
                )
            }
            DraggablePaletteChip(
                label = stringResource(R.string.rotation_grid_break_label),
                color = appColors.grey400,
                dotColor = null,
                onDragStart = { pos -> onDragStart(PaletteItem.BreakItem, pos) },
                onDrag = onDrag,
                onDragEnd = onDragEnd
            )
        }
    }
}

@Composable
private fun DraggablePaletteChip(
    label: String,
    color: Color,
    dotColor: Color?,
    onDragStart: (windowPos: Offset) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var bounds by remember { mutableStateOf(Rect.Zero) }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.55f))
            .onGloballyPositioned { coords -> bounds = coords.boundsInWindow() }
            .pointerInput(label) {
                detectDragGestures(
                    onDragStart = { local -> onDragStart(Offset(bounds.left + local.x, bounds.top + local.y)) },
                    onDrag = { _, delta -> onDrag(delta) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (dotColor != null) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        } else {
            Icon(Icons.Default.DragIndicator, contentDescription = null, tint = rotationChipTextColor.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = rotationChipTextColor)
    }
}
