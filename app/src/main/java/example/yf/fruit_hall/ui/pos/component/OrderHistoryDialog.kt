package example.yf.fruit_hall.ui.pos.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.yf.fruit_hall.data.pos.entity.OrderEntity
import example.yf.fruit_hall.ui.pos.OrderFilter
import example.yf.fruit_hall.ui.pos.PosUiState
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun OrderHistoryDialog(
    uiState: PosUiState,
    onDismiss: () -> Unit,
    onFilterChange: (OrderFilter) -> Unit,
    onToggleStatus: (Long) -> Unit,
    onClearAll: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    var showClearConfirm by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .background(cs.background, RoundedCornerShape(12.dp))
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = appColors.grey900,
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "비상 보관함 (로컬 주문 내역)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.white,
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = appColors.white, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                ) {
                    // 필터 + 초기화 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "상태 필터",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = cs.onSurface,
                            )
                            ExposedDropdownMenuBox(
                                expanded = filterExpanded,
                                onExpandedChange = { filterExpanded = !filterExpanded },
                            ) {
                                OutlinedTextField(
                                    value = uiState.orderFilter.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = cs.primary,
                                        unfocusedBorderColor = cs.outline,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = filterExpanded,
                                    onDismissRequest = { filterExpanded = false },
                                ) {
                                    OrderFilter.entries.forEach { filter ->
                                        DropdownMenuItem(
                                            text = { Text(filter.label, style = MaterialTheme.typography.bodyMedium) },
                                            onClick = { onFilterChange(filter); filterExpanded = false },
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = cs.error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text("전체 내역 초기화", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 테이블
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(8.dp)),
                    ) {
                        OrderTableHeader()
                        HorizontalDivider(color = cs.outlineVariant)

                        if (uiState.filteredOrders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "내역이 없습니다.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.filteredOrders, key = { it.id }) { order ->
                                    OrderTableRow(order = order, onToggleStatus = onToggleStatus)
                                    HorizontalDivider(color = cs.surfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("내역 초기화", style = MaterialTheme.typography.titleMedium) },
            text = { Text("모든 내역이 삭제됩니다. 계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = { onClearAll(); showClearConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun OrderTableHeader() {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell("주문 일시", weight = 20f, isHeader = true)
        TableCell("주문 상품 내역", weight = 30f, isHeader = true)
        TableCell("최종 금액", weight = 15f, isHeader = true)
        TableCell("입력 번호", weight = 20f, isHeader = true)
        TableCell("상태", weight = 7f, isHeader = true, textAlign = TextAlign.Center)
        TableCell("관리", weight = 8f, isHeader = true, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OrderTableRow(order: OrderEntity, onToggleStatus: (Long) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    val phoneDisplay = buildPhoneDisplay(order.receiptPhone, order.pointPhone)
    val isDone = order.status == "done"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(order.date, weight = 20f)
        TableCell(order.itemsText, weight = 30f, fontWeight = FontWeight.SemiBold)
        TableCell(
            text = "%,d원".format(order.totalAmount),
            weight = 15f,
            fontWeight = FontWeight.Bold,
            color = cs.primary,
        )
        TableCell(phoneDisplay, weight = 20f)

        Box(modifier = Modifier.weight(7f), contentAlignment = Alignment.Center) {
            Text(
                text = if (isDone) "완료처리" else "미완료",
                modifier = Modifier
                    .background(
                        color = if (isDone) appColors.success100 else cs.errorContainer,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) appColors.success800 else cs.error,
            )
        }

        Box(modifier = Modifier.weight(8f), contentAlignment = Alignment.Center) {
            Button(
                onClick = { onToggleStatus(order.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isDone) appColors.success700 else appColors.warning400,
                    contentColor = if (!isDone) Color.White else appColors.grey900,
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                modifier = Modifier.heightIn(min = 32.dp),
            ) {
                Text(
                    text = if (!isDone) "포스입력" else "되돌리기",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    fontWeight: FontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
    color: Color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight).padding(horizontal = 4.dp),
        fontSize = 12.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 3,
    )
}

private fun buildPhoneDisplay(receiptPhone: String, pointPhone: String): String {
    val hasReceipt = receiptPhone != "-"
    val hasPoint = pointPhone != "-"
    return when {
        hasReceipt && hasPoint -> if (receiptPhone == pointPhone) "통합: $receiptPhone"
        else "현금: $receiptPhone\n포인트: $pointPhone"
        hasReceipt -> "현금: $receiptPhone"
        hasPoint -> "포인트: $pointPhone"
        else -> "-"
    }
}
