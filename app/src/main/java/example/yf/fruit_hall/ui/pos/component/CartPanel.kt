package example.yf.fruit_hall.ui.pos.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.yf.fruit_hall.ui.pos.CartItem
import example.yf.fruit_hall.ui.pos.PosUiState
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun CartPanel(
    modifier: Modifier = Modifier,
    uiState: PosUiState,
    onUpdateQty: (productId: Long, change: Int) -> Unit,
    onOpenDiscount: () -> Unit,
    onClearCart: () -> Unit,
    onNeedsReceiptChange: (Boolean) -> Unit,
    onNeedsPointsChange: (Boolean) -> Unit,
    onSeparateNumbersChange: (Boolean) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onReceiptPhoneChange: (String) -> Unit,
    onPointPhoneChange: (String) -> Unit,
    onCheckout: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors

    Column(
        modifier = modifier
            .background(appColors.grey50)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "주문 목록",
            style = MaterialTheme.typography.titleSmall,
            color = cs.onSurface,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(cs.background),
        ) {
            if (uiState.cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "상품을 선택해주세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    items(uiState.cartItems, key = { it.product.id }) { item ->
                        CartItemRow(item = item, onUpdateQty = { change -> onUpdateQty(item.product.id, change) })
                        if (uiState.cartItems.last() != item) {
                            HorizontalDivider(color = appColors.grey100)
                        }
                    }
                }
            }
        }

        // 할인 7 : 비우기 3
        val hasItems = uiState.cartItems.isNotEmpty()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onOpenDiscount,
                modifier = Modifier.weight(7f),
                enabled = hasItems,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cs.primary,
                    disabledContentColor = cs.onSurfaceVariant,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (hasItems) cs.primary else cs.outline,
                ),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                Text(text = "할인 적용", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onClearCart,
                modifier = Modifier.weight(3f),
                enabled = hasItems,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cs.error,
                    disabledContentColor = cs.onSurfaceVariant,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (hasItems) cs.error else cs.outline,
                ),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                Text(text = "비우기", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        // 결제 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(cs.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "최종 결제금액",
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onSurface,
                )
                Text(
                    text = "%,d원".format(uiState.totalAmount),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.primary,
                )
            }

            HorizontalDivider(color = appColors.grey100, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabeledCheckbox(
                    checked = uiState.needsReceipt,
                    onCheckedChange = onNeedsReceiptChange,
                    label = "현금영수증",
                )
                Spacer(Modifier.width(12.dp))
                LabeledCheckbox(
                    checked = uiState.needsPoints,
                    onCheckedChange = onNeedsPointsChange,
                    label = "포인트 적립",
                )
                if (uiState.needsReceipt && uiState.needsPoints) {
                    Spacer(Modifier.weight(1f))
                    LabeledCheckbox(
                        checked = uiState.separateNumbers,
                        onCheckedChange = onSeparateNumbersChange,
                        label = "번호 각각",
                        labelColor = cs.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.needsReceipt || uiState.needsPoints,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.separateNumbers) {
                        PosTextField(value = uiState.receiptPhone, onValueChange = onReceiptPhoneChange, placeholder = "현금영수증 번호")
                        PosTextField(value = uiState.pointPhone, onValueChange = onPointPhoneChange, placeholder = "포인트적립 번호")
                    } else {
                        PosTextField(value = uiState.phoneNumber, onValueChange = onPhoneNumberChange, placeholder = "고객 전화번호 (숫자만)")
                    }
                }
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = hasItems,
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary,
                    disabledContainerColor = cs.primary.copy(alpha = 0.4f),
                    disabledContentColor = cs.onPrimary,
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    text = "주문 완료  (임시저장)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onUpdateQty: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                )
                if (item.discount > 0) {
                    Text(
                        text = " (할인)",
                        fontSize = 12.sp,
                        color = cs.error,
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            if (item.discount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "%,d원".format(item.product.price),
                        fontSize = 11.sp,
                        color = appColors.grey400,
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Text(
                        text = "%,d원".format(item.unitFinalPrice),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.error,
                    )
                }
            } else {
                Text(
                    text = "%,d원".format(item.product.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            QtyButton(text = "−", onClick = { onUpdateQty(-1) })
            Text(
                text = item.qty.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center,
            )
            QtyButton(text = "+", onClick = { onUpdateQty(+1) })
        }

        Text(
            text = "%,d원".format(item.subtotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = cs.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(68.dp),
        )
    }
}

@Composable
private fun QtyButton(text: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cs.surfaceVariant,
            contentColor = cs.onSurface,
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
    }
}

@Composable
private fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(22.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = cs.primary,
                uncheckedColor = cs.outline,
            ),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )
    }
}

@Composable
private fun PosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val cs = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = cs.primary,
            unfocusedBorderColor = cs.outline,
        ),
    )
}