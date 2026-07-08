package example.yf.fruit_hall.ui.traysplit.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.core.RoughSize
import example.yf.fruit_hall.ui.theme.AppTheme
import example.yf.fruit_hall.ui.traysplit.SnackTypeUi

private const val DARK_TEXT = 0xFF2D2D2D

private fun RoughSize.next(): RoughSize? = when (this) {
    RoughSize.S -> RoughSize.M
    RoughSize.M -> RoughSize.L
    RoughSize.L -> null // 다음은 선택 해제
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTrayDialog(
    snackTypes: List<SnackTypeUi>,
    onConfirm: (List<Pair<Long, RoughSize>>) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = AppTheme.colors
    var selections by remember { mutableStateOf<Map<Long, RoughSize>>(emptyMap()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.55f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = appColors.white, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "판 추가",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.white,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = appColors.grey300, modifier = Modifier.size(18.dp))
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    if (snackTypes.isEmpty()) {
                        Text(
                            text = "먼저 왼쪽 품목 관리에서 과자 종류를 등록해주세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.crimson400
                        )
                    } else {
                        Text(
                            text = "탭하여 담기 (S→M→L→해제) · 비워두고 나중에 드래그로 채워도 됩니다",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.grey500
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(snackTypes, key = { it.id }) { type ->
                                val bg = type.colorHex.toSnackColor()
                                val selectedSize = selections[type.id]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(
                                            if (selectedSize != null)
                                                Modifier.snackColorBackground(type.colorHex, type.secondaryColorHex, alpha = 0.35f)
                                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        )
                                        .then(
                                            if (selectedSize != null) Modifier.border(1.5.dp, bg, RoundedCornerShape(12.dp))
                                            else Modifier
                                        )
                                        .clickable {
                                            selections = selections.toMutableMap().apply {
                                                val next = selectedSize?.next()
                                                if (next == null && selectedSize != null) remove(type.id)
                                                else put(type.id, next ?: RoughSize.S)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(20.dp).clip(CircleShape)
                                            .snackColorBackground(type.colorHex, type.secondaryColorHex)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = type.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(DARK_TEXT),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (selectedSize != null) {
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(50))
                                                .snackColorBackground(type.colorHex, type.secondaryColorHex)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = selectedSize.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(DARK_TEXT)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("취소", color = appColors.grey600)
                        }
                        Button(
                            onClick = { onConfirm(selections.map { (id, size) -> id to size }) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("판 추가")
                        }
                    }
                }
            }
        }
    }
}
