package example.yf.fruit_hall.ui.pos.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import example.yf.fruit_hall.data.pos.entity.ProductEntity
import example.yf.fruit_hall.ui.pos.ProductCategory
import example.yf.fruit_hall.ui.theme.AppTheme

private val PALETTE_COLORS = listOf(
    "#ffffff", "#fff0f0", "#fff5e6", "#fffce6",
    "#f0fdf4", "#e6fcf5", "#e6f2ff", "#eef2ff",
    "#f0e6ff", "#fff0f5", "#f8f9fa", "#e9ecef",
)

@Composable
fun ProductFormDialog(
    editingProduct: ProductEntity?,
    currentTab: ProductCategory,
    onSave: (name: String, price: Int, category: String, bgColor: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    var name by remember(editingProduct) { mutableStateOf(editingProduct?.name ?: "") }
    var priceText by remember(editingProduct) { mutableStateOf(editingProduct?.price?.toString() ?: "") }
    var category by remember(editingProduct) { mutableStateOf(editingProduct?.category ?: currentTab.dbValue) }
    var bgColor by remember(editingProduct) { mutableStateOf(editingProduct?.bgColor ?: "#ffffff") }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

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
                        text = if (editingProduct != null) "상품 수정" else "새 상품 추가",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.white,
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, color = appColors.white, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 상품 이름
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormLabel("상품 이름")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            placeholder = { Text("예: 아메리카노", style = MaterialTheme.typography.bodyMedium) },
                            isError = nameError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cs.primary,
                                unfocusedBorderColor = cs.outline,
                                errorBorderColor = cs.error,
                            ),
                        )
                        if (nameError) {
                            Text("상품 이름을 입력해주세요.", color = cs.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 상품 종류
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormLabel("상품 종류")
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProductCategoryRadio(text = "메인상품", selected = category == "main", onClick = { category = "main" })
                            ProductCategoryRadio(text = "그외상품", selected = category == "other", onClick = { category = "other" })
                        }
                    }

                    // 상품 가격
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormLabel("상품 가격 (원)")
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { c -> c.isDigit() }; priceError = false },
                            placeholder = { Text("예: 4500", style = MaterialTheme.typography.bodyMedium) },
                            isError = priceError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cs.primary,
                                unfocusedBorderColor = cs.outline,
                                errorBorderColor = cs.error,
                            ),
                        )
                        if (priceError) {
                            Text("올바른 가격을 입력해주세요.", color = cs.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 배경 색상
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FormLabel("배경 색상 (선택)")
                        ColorPalette(selectedColor = bgColor, onColorSelected = { bgColor = it })
                    }

                    // 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.surfaceVariant,
                                contentColor = cs.onSurface,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text("취소", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {
                                val trimmedName = name.trim()
                                val price = priceText.toIntOrNull()
                                nameError = trimmedName.isEmpty()
                                priceError = price == null || price <= 0
                                if (!nameError && !priceError) onSave(trimmedName, price!!, category, bgColor)
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.primary,
                                contentColor = cs.onPrimary,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("저장하기", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ProductCategoryRadio(text: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = cs.primary),
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
    }
}

@Composable
private fun ColorPalette(selectedColor: String, onColorSelected: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PALETTE_COLORS.forEach { hex ->
            val isSelected = hex == selectedColor
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(hexToColor(hex))
                    .then(
                        if (isSelected)
                            Modifier.border(3.dp, cs.primary, CircleShape)
                        else
                            Modifier.border(1.5.dp, cs.outlineVariant, CircleShape)
                    )
                    .clickable { onColorSelected(hex) },
            )
        }
    }
}
