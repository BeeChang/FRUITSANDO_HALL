package example.yf.fruit_hall.ui.rotation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.rotation.RotationPositionUi
import example.yf.fruit_hall.ui.theme.AppTheme

/**
 * §10-3 "탭 → 포지션 직접 선택 다이얼로그". 매번 알고리즘이 정하는 게 원칙이지만, 강제로 특정 포지션을
 * 꽂아야 할 때(신입 붙이기, 손님 몰림 대응 등) 여기서 직접 골라 그 자리를 대체한다. 휴게도 고를 수 있다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionPickerDialog(
    positions: List<RotationPositionUi>,
    onPickPosition: (RotationPositionUi) -> Unit,
    onPickBreak: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    RotationDialogScaffold(
        title = stringResource(R.string.rotation_position_picker_title),
        icon = Icons.AutoMirrored.Filled.List,
        headerColor = appColors.secondary500,
        onDismiss = onDismiss,
        widthFraction = 0.4f,
        maxHeight = 400.dp,
        footer = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.rotation_close), color = appColors.grey600) } }
    ) {
        Text(
            stringResource(R.string.rotation_position_picker_desc),
            style = MaterialTheme.typography.bodySmall, color = appColors.grey500
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            positions.filter { it.isActive }.forEach { position ->
                FilterChip(
                    selected = false,
                    onClick = { onPickPosition(position) },
                    leadingIcon = {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(position.colorHex.toRotationColor(rotationDefaultChipColor)))
                    },
                    label = { Text(position.name, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
            FilterChip(
                selected = false,
                onClick = onPickBreak,
                leadingIcon = { Icon(Icons.Default.FreeBreakfast, contentDescription = null, tint = appColors.crimson400, modifier = Modifier.size(16.dp)) },
                label = { Text(stringResource(R.string.rotation_grid_break_label), style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(containerColor = appColors.crimson50, labelColor = appColors.crimson700)
            )
        }
    }
}
