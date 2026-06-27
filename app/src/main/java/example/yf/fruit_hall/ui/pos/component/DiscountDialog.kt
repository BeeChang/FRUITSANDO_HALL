package example.yf.fruit_hall.ui.pos.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import example.yf.fruit_hall.ui.pos.CartItem
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun DiscountDialog(
    cartItems: List<CartItem>,
    onApplyAmount: (selectedIds: Set<Long>, amount: Int) -> Unit,
    onApplyPercent: (selectedIds: Set<Long>, percent: Int) -> Unit,
    onClearDiscount: (selectedIds: Set<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    val allSelected = selectedIds.size == cartItems.size && cartItems.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cs.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appColors.grey900)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "할인 적용",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.white,
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = appColors.white, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 전체 선택
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) cartItems.map { it.product.id }.toSet() else emptySet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = cs.primary, uncheckedColor = cs.outline),
                        )
                        Text(
                            text = "전체 상품 선택",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface,
                        )
                    }

                    HorizontalDivider(thickness = 1.5.dp, color = cs.outlineVariant)

                    // 상품 목록
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(cartItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = item.product.id in selectedIds,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) selectedIds + item.product.id
                                            else selectedIds - item.product.id
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = cs.primary, uncheckedColor = cs.outline),
                                    )
                                    Text(
                                        text = "${item.product.name}  ×${item.qty}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface,
                                    )
                                }
                                Text(
                                    text = "%,d원".format(item.product.price),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                            HorizontalDivider(color = cs.surfaceVariant)
                        }
                    }

                    // 할인 버튼 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { onApplyAmount(selectedIds, 500) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            enabled = selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appColors.success700,
                                disabledContainerColor = appColors.grey200,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("−500원 할인", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { onApplyPercent(selectedIds, 30) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            enabled = selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.secondary,
                                disabledContainerColor = appColors.grey200,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("30% 할인", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { onClearDiscount(selectedIds) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.surfaceVariant,
                            contentColor = cs.onSurface,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text("할인 초기화 (전체 해제)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}