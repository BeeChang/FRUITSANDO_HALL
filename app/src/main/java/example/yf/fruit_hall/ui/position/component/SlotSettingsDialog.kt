package example.yf.fruit_hall.ui.position.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun SlotSettingsDialog(
    currentTotalSlots: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var totalSlots by remember { mutableIntStateOf(currentTotalSlots) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.38f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = appColors.white,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "차수 설정",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = appColors.grey300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "하루 총 차수를 설정합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.grey500
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledIconButton(
                            onClick = { if (totalSlots > 1) totalSlots-- },
                            modifier = Modifier.size(44.dp),
                            enabled = totalSlots > 1,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = appColors.grey100,
                                contentColor = appColors.grey800,
                                disabledContainerColor = appColors.grey50,
                                disabledContentColor = appColors.grey300
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "감소",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(24.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalSlots",
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.primary500,
                                lineHeight = 56.sp
                            )
                            Text(
                                text = "차",
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.grey500
                            )
                        }

                        Spacer(Modifier.width(24.dp))

                        FilledIconButton(
                            onClick = { if (totalSlots < 10) totalSlots++ },
                            modifier = Modifier.size(44.dp),
                            enabled = totalSlots < 10,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = appColors.grey100,
                                contentColor = appColors.grey800,
                                disabledContainerColor = appColors.grey50,
                                disabledContentColor = appColors.grey300
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "증가",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..10).forEach { n ->
                            val isActive = n == totalSlots
                            Spacer(
                                modifier = Modifier
                                    .size(if (isActive) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) appColors.primary500 else appColors.grey200
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("취소", color = appColors.grey600)
                        }
                        Button(
                            onClick = { onConfirm(totalSlots) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("확인", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
