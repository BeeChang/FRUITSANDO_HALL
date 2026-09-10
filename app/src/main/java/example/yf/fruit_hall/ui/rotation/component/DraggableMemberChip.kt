package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.ui.rotation.MemberUi


/** 멤버 이름표 칩. 꾹 눌러 끌면 드래그가 시작된다 — 인원배정·결과 화면의 팔레트·이름 칸에서 공용으로 쓴다. */
@Composable
fun DraggableMemberChip(
    member: MemberUi,
    onDragStart: (windowPos: Offset) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val memberColor = member.colorHex.toRotationColor(rotationDefaultChipColor)
    var chipWindowBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(memberColor.copy(alpha = 0.55f))
            .onGloballyPositioned { coords -> chipWindowBounds = coords.boundsInWindow() }
            .pointerInput(member.id) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        onDragStart(Offset(chipWindowBounds.left + localOffset.x, chipWindowBounds.top + localOffset.y))
                    },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .padding(start = 6.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Default.DragIndicator, contentDescription = null, tint = rotationChipTextColor.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
        Box(
            Modifier.size(18.dp).clip(CircleShape).background(memberColor),
            contentAlignment = Alignment.Center
        ) {
            Text(member.name.take(1), style = MaterialTheme.typography.labelSmall, color = rotationChipTextColor, fontWeight = FontWeight.Bold)
        }
        Text(member.name, style = MaterialTheme.typography.labelSmall, color = rotationChipTextColor, fontWeight = FontWeight.SemiBold)
    }
}
