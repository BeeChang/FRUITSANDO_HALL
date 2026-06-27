package example.yf.fruit_hall.ui.pos.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.yf.fruit_hall.data.pos.entity.ProductEntity
import example.yf.fruit_hall.ui.component.util.ClickShrinkEffect
import example.yf.fruit_hall.ui.pos.ProductCategory
import example.yf.fruit_hall.ui.pos.PosUiState
import example.yf.fruit_hall.ui.theme.AppTheme

@Composable
fun ProductGridPanel(
    modifier: Modifier = Modifier,
    uiState: PosUiState,
    onTabSelected: (ProductCategory) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onToggleEditMode: () -> Unit,
    onShowInitDialog: () -> Unit,
    onOpenHistory: () -> Unit,
    onCardSizeChange: (Int) -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = modifier.background(appColors.grey50)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .background(cs.background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabButton(
                    text = "메인상품",
                    selected = uiState.currentTab == ProductCategory.MAIN,
                    onClick = { onTabSelected(ProductCategory.MAIN) },
                )
                TabButton(
                    text = "그외상품",
                    selected = uiState.currentTab == ProductCategory.OTHER,
                    onClick = { onTabSelected(ProductCategory.OTHER) },
                )
                GridSizeControl(
                    value = uiState.productCardMinSizeDp,
                    onValueChange = onCardSizeChange,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val hasProducts = uiState.products.isNotEmpty()
                UtilButton(
                    text = if (hasProducts) "메뉴 초기화" else "메뉴 불러오기",
                    onClick = onShowInitDialog,
                    containerColor = if (hasProducts) cs.surfaceVariant else cs.primary,
                    contentColor = if (hasProducts) cs.onSurfaceVariant else cs.onPrimary,
                    border = if (hasProducts) BorderStroke(1.dp, cs.outline) else null,
                )
                UtilButton(
                    text = if (uiState.isEditMode) "설정 완료" else "상품 수정",
                    onClick = onToggleEditMode,
                    containerColor = if (uiState.isEditMode) appColors.success700 else appColors.grey800,
                    contentColor = appColors.white,
                )
                UtilButton(
                    text = "주문내역 조회",
                    onClick = onOpenHistory,
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary,
                )
            }
        }

        if (uiState.filteredProducts.isEmpty() && !uiState.isEditMode) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "등록된 상품이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurfaceVariant,
                    )
                    Text(
                        text = "상단의 [메뉴 불러오기]를 눌러주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(uiState.productCardMinSizeDp.dp),
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                if (uiState.isEditMode) {
                    item(key = "add_new") { AddProductCard(onClick = onAddProduct) }
                }
                items(uiState.filteredProducts, key = { it.id }) { product ->
                    if (uiState.isEditMode) {
                        EditableProductCard(
                            product = product,
                            onEdit = { onEditProduct(product.id) },
                            onDelete = { pendingDeleteId = product.id },
                            onMoveUp = { onMoveUp(product.id) },
                            onMoveDown = { onMoveDown(product.id) },
                        )
                    } else {
                        ProductCard(product = product, onClick = { onProductClick(product) })
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("상품 삭제", style = MaterialTheme.typography.titleMedium) },
            text = { Text("이 상품을 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = { onDeleteProduct(id); pendingDeleteId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ClickShrinkEffect(onClick = onClick, shrinkFactor = 0.93f) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(if (selected) cs.primary else cs.surfaceVariant)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) cs.onPrimary else cs.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GridSizeControl(value: Int, onValueChange: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cs.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "A", fontSize = 10.sp, color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 80f..160f,
            steps = 15,
            modifier = Modifier.width(90.dp),
            colors = SliderDefaults.colors(
                thumbColor = cs.primary,
                activeTrackColor = cs.primary,
                inactiveTrackColor = cs.primaryContainer,
            ),
        )
        Text(text = "A", fontSize = 16.sp, color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UtilButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        border = border,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductCard(product: ProductEntity, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ClickShrinkEffect(onClick = onClick, shrinkFactor = 0.94f) {
        Card(
            modifier = Modifier.height(96.dp),
            colors = CardDefaults.cardColors(containerColor = hexToColor(product.bgColor)),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, cs.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = cs.onSurface,
                )
                Text(
                    text = "%,d원".format(product.price),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.primary,
                )
            }
        }
    }
}

@Composable
private fun EditableProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val appColors = AppTheme.colors
    Card(
        modifier = Modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = hexToColor(product.bgColor)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, cs.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = cs.onSurface,
            )
            Text(
                text = "%,d원".format(product.price),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                CardActionBtn(text = "수정", onClick = onEdit, color = appColors.grey700, modifier = Modifier.weight(1f))
                CardActionBtn(text = "삭제", onClick = onDelete, color = cs.error, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                CardActionBtn(text = "▲", onClick = onMoveUp, color = appColors.grey600, modifier = Modifier.weight(1f))
                CardActionBtn(text = "▼", onClick = onMoveDown, color = appColors.grey600, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AddProductCard(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    ClickShrinkEffect(onClick = onClick, shrinkFactor = 0.94f) {
        Card(
            modifier = Modifier.height(96.dp),
            colors = CardDefaults.cardColors(containerColor = cs.primaryContainer),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, cs.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "+", fontSize = 28.sp, color = cs.primary, fontWeight = FontWeight.Light)
                Text(text = "새 상품 추가", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.primary)
            }
        }
    }
}

@Composable
private fun CardActionBtn(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text = text, fontSize = 10.sp, lineHeight = 10.sp)
    }
}

internal fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color.White
}
