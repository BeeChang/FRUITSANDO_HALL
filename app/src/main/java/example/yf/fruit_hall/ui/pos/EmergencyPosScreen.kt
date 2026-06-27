package example.yf.fruit_hall.ui.pos

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.yf.fruit_hall.ui.pos.component.CartPanel
import example.yf.fruit_hall.ui.pos.component.DiscountDialog
import example.yf.fruit_hall.ui.pos.component.InitMenuDialog
import example.yf.fruit_hall.ui.pos.component.OrderHistoryDialog
import example.yf.fruit_hall.ui.pos.component.ProductFormDialog
import example.yf.fruit_hall.ui.pos.component.ProductGridPanel

@Composable
fun EmergencyPosScreen(
    viewModel: EmergencyPosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        viewModel.orderSuccessEvent.collect {
            snackbarHostState.showSnackbar(
                message = "✓  주문 내역이 임시 보관함에 기록되었습니다.",
                duration = SnackbarDuration.Short,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                ProductGridPanel(
                    modifier = Modifier.weight(7f).fillMaxHeight(),
                    uiState = uiState,
                    onTabSelected = viewModel::selectTab,
                    onProductClick = viewModel::addToCart,
                    onToggleEditMode = viewModel::toggleEditMode,
                    onShowInitDialog = viewModel::showInitDialog,
                    onOpenHistory = viewModel::openHistoryDialog,
                    onCardSizeChange = viewModel::setCardMinSize,
                    onAddProduct = viewModel::openAddProduct,
                    onEditProduct = viewModel::openEditProduct,
                    onDeleteProduct = viewModel::deleteProduct,
                    onMoveUp = viewModel::moveProductUp,
                    onMoveDown = viewModel::moveProductDown,
                )
                VerticalDivider()
                CartPanel(
                    modifier = Modifier.weight(3f).fillMaxHeight(),
                    uiState = uiState,
                    onUpdateQty = viewModel::updateQty,
                    onOpenDiscount = viewModel::openDiscountDialog,
                    onClearCart = viewModel::clearCart,
                    onNeedsReceiptChange = viewModel::setNeedsReceipt,
                    onNeedsPointsChange = viewModel::setNeedsPoints,
                    onSeparateNumbersChange = viewModel::setSeparateNumbers,
                    onPhoneNumberChange = viewModel::setPhoneNumber,
                    onReceiptPhoneChange = viewModel::setReceiptPhone,
                    onPointPhoneChange = viewModel::setPointPhone,
                    onCheckout = viewModel::processOrder,
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                ProductGridPanel(
                    modifier = Modifier.fillMaxWidth().weight(0.45f),
                    uiState = uiState,
                    onTabSelected = viewModel::selectTab,
                    onProductClick = viewModel::addToCart,
                    onToggleEditMode = viewModel::toggleEditMode,
                    onShowInitDialog = viewModel::showInitDialog,
                    onOpenHistory = viewModel::openHistoryDialog,
                    onCardSizeChange = viewModel::setCardMinSize,
                    onAddProduct = viewModel::openAddProduct,
                    onEditProduct = viewModel::openEditProduct,
                    onDeleteProduct = viewModel::deleteProduct,
                    onMoveUp = viewModel::moveProductUp,
                    onMoveDown = viewModel::moveProductDown,
                )
                HorizontalDivider()
                CartPanel(
                    modifier = Modifier.fillMaxWidth().weight(0.55f),
                    uiState = uiState,
                    onUpdateQty = viewModel::updateQty,
                    onOpenDiscount = viewModel::openDiscountDialog,
                    onClearCart = viewModel::clearCart,
                    onNeedsReceiptChange = viewModel::setNeedsReceipt,
                    onNeedsPointsChange = viewModel::setNeedsPoints,
                    onSeparateNumbersChange = viewModel::setSeparateNumbers,
                    onPhoneNumberChange = viewModel::setPhoneNumber,
                    onReceiptPhoneChange = viewModel::setReceiptPhone,
                    onPointPhoneChange = viewModel::setPointPhone,
                    onCheckout = viewModel::processOrder,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (uiState.showInitDialog) {
        InitMenuDialog(
            hasProducts = uiState.products.isNotEmpty(),
            onLoadDefault = viewModel::loadDefaultProducts,
            onClearAll = viewModel::clearAllProducts,
            onDismiss = viewModel::dismissInitDialog,
        )
    }

    if (uiState.showProductFormDialog) {
        ProductFormDialog(
            editingProduct = uiState.products.find { it.id == uiState.editingProductId },
            currentTab = uiState.currentTab,
            onSave = viewModel::saveProduct,
            onDismiss = viewModel::dismissProductForm,
        )
    }

    if (uiState.showDiscountDialog) {
        DiscountDialog(
            cartItems = uiState.cartItems,
            onApplyAmount = viewModel::applyDiscountAmount,
            onApplyPercent = viewModel::applyDiscountPercent,
            onClearDiscount = viewModel::clearDiscounts,
            onDismiss = viewModel::dismissDiscountDialog,
        )
    }

    if (uiState.showHistoryDialog) {
        OrderHistoryDialog(
            uiState = uiState,
            onDismiss = viewModel::dismissHistoryDialog,
            onFilterChange = viewModel::setOrderFilter,
            onToggleStatus = viewModel::toggleOrderStatus,
            onClearAll = viewModel::clearOrders,
        )
    }
}