package example.yf.fruit_hall.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.yf.fruit_hall.data.pos.PosPreferenceRepository
import example.yf.fruit_hall.data.pos.PosRepository
import example.yf.fruit_hall.data.pos.entity.OrderEntity
import example.yf.fruit_hall.data.pos.entity.ProductEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EmergencyPosViewModel @Inject constructor(
    private val posRepository: PosRepository,
    private val posPrefs: PosPreferenceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState = _uiState.asStateFlow()

    // 일회성 이벤트: 화면 전환 후 재발화 없음
    private val _orderSuccessEvent = Channel<Unit>(Channel.BUFFERED)
    val orderSuccessEvent = _orderSuccessEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            posRepository.products.collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        viewModelScope.launch {
            posRepository.orders.collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
        viewModelScope.launch {
            val count = posRepository.productCount.first()
            if (count == 0 && !posPrefs.isInitialized) {
                _uiState.update { it.copy(showInitDialog = true) }
            }
        }
    }

    fun selectTab(tab: ProductCategory) = _uiState.update { it.copy(currentTab = tab) }

    fun toggleEditMode() = _uiState.update { it.copy(isEditMode = !it.isEditMode) }

    fun setCardMinSize(size: Int) = _uiState.update { it.copy(productCardMinSizeDp = size) }

    // Init Dialog
    fun showInitDialog() = _uiState.update { it.copy(showInitDialog = true) }

    fun dismissInitDialog() {
        posPrefs.isInitialized = true
        _uiState.update { it.copy(showInitDialog = false) }
    }

    fun loadDefaultProducts() = viewModelScope.launch {
        posRepository.initDefaultProducts()
        posPrefs.isInitialized = true
        _uiState.update { it.copy(showInitDialog = false, cartItems = emptyList()) }
    }

    fun clearAllProducts() = viewModelScope.launch {
        posRepository.clearProducts()
        posPrefs.isInitialized = true
        _uiState.update { it.copy(showInitDialog = false, cartItems = emptyList()) }
    }

    // Product Form
    fun openAddProduct() = _uiState.update { it.copy(showProductFormDialog = true, editingProductId = null) }

    fun openEditProduct(id: Long) = _uiState.update { it.copy(showProductFormDialog = true, editingProductId = id) }

    fun dismissProductForm() = _uiState.update { it.copy(showProductFormDialog = false, editingProductId = null) }

    fun saveProduct(name: String, price: Int, category: String, bgColor: String) = viewModelScope.launch {
        val editingId = _uiState.value.editingProductId
        if (editingId != null) {
            val existing = _uiState.value.products.find { it.id == editingId } ?: return@launch
            val updated = existing.copy(name = name, price = price, category = category, bgColor = bgColor)
            posRepository.updateProduct(updated)
            _uiState.update { state ->
                state.copy(
                    cartItems = state.cartItems.map { item ->
                        if (item.product.id == editingId) item.copy(product = updated) else item
                    }
                )
            }
        } else {
            val maxOrder = _uiState.value.products.maxOfOrNull { it.sortOrder } ?: -1
            posRepository.addProduct(
                ProductEntity(
                    name = name,
                    price = price,
                    category = category,
                    bgColor = bgColor,
                    sortOrder = maxOrder + 1,
                )
            )
        }
        dismissProductForm()
    }

    fun deleteProduct(id: Long) = viewModelScope.launch {
        val product = _uiState.value.products.find { it.id == id } ?: return@launch
        posRepository.deleteProduct(product)
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filter { it.product.id != id })
        }
    }

    fun moveProductUp(id: Long) = viewModelScope.launch {
        val filtered = _uiState.value.filteredProducts
        val idx = filtered.indexOfFirst { it.id == id }.takeIf { it > 0 } ?: return@launch
        posRepository.swapProductOrder(filtered[idx], filtered[idx - 1])
    }

    fun moveProductDown(id: Long) = viewModelScope.launch {
        val filtered = _uiState.value.filteredProducts
        val idx = filtered.indexOfFirst { it.id == id }.takeIf { it < filtered.size - 1 } ?: return@launch
        posRepository.swapProductOrder(filtered[idx], filtered[idx + 1])
    }

    // Cart
    fun addToCart(product: ProductEntity) = _uiState.update { state ->
        val existing = state.cartItems.find { it.product.id == product.id }
        if (existing != null) {
            state.copy(
                cartItems = state.cartItems.map {
                    if (it.product.id == product.id) it.copy(qty = it.qty + 1) else it
                }
            )
        } else {
            state.copy(cartItems = state.cartItems + CartItem(product = product))
        }
    }

    fun updateQty(productId: Long, change: Int) = _uiState.update { state ->
        state.copy(
            cartItems = state.cartItems.mapNotNull { item ->
                if (item.product.id == productId) {
                    val newQty = item.qty + change
                    if (newQty <= 0) null else item.copy(qty = newQty)
                } else item
            }
        )
    }

    // Discount
    fun openDiscountDialog() = _uiState.update { it.copy(showDiscountDialog = true) }

    fun dismissDiscountDialog() = _uiState.update { it.copy(showDiscountDialog = false) }

    fun applyDiscountAmount(selectedIds: Set<Long>, amount: Int) = _uiState.update { state ->
        state.copy(
            cartItems = state.cartItems.map { item ->
                if (item.product.id in selectedIds)
                    item.copy(discount = minOf(amount, item.product.price))
                else item
            },
            showDiscountDialog = false,
        )
    }

    fun applyDiscountPercent(selectedIds: Set<Long>, percent: Int) = _uiState.update { state ->
        state.copy(
            cartItems = state.cartItems.map { item ->
                if (item.product.id in selectedIds) {
                    val disc = (item.product.price * percent / 100.0).toInt()
                    item.copy(discount = minOf(disc, item.product.price))
                } else item
            },
            showDiscountDialog = false,
        )
    }

    fun clearDiscounts(selectedIds: Set<Long>) = _uiState.update { state ->
        state.copy(
            cartItems = state.cartItems.map { item ->
                if (item.product.id in selectedIds) item.copy(discount = 0) else item
            },
            showDiscountDialog = false,
        )
    }

    // Payment
    fun setNeedsReceipt(value: Boolean) = _uiState.update { state ->
        state.copy(
            needsReceipt = value,
            separateNumbers = if (!value && !state.needsPoints) false else state.separateNumbers,
        )
    }

    fun setNeedsPoints(value: Boolean) = _uiState.update { state ->
        state.copy(
            needsPoints = value,
            separateNumbers = if (!state.needsReceipt && !value) false else state.separateNumbers,
        )
    }

    fun setSeparateNumbers(value: Boolean) = _uiState.update { it.copy(separateNumbers = value) }

    fun setPhoneNumber(value: String) =
        _uiState.update { it.copy(phoneNumber = value.filter { c -> c.isDigit() }.take(11)) }

    fun setReceiptPhone(value: String) =
        _uiState.update { it.copy(receiptPhone = value.filter { c -> c.isDigit() }.take(11)) }

    fun setPointPhone(value: String) =
        _uiState.update { it.copy(pointPhone = value.filter { c -> c.isDigit() }.take(11)) }

    fun processOrder() = viewModelScope.launch {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return@launch

        val receiptPhoneResult = when {
            state.separateNumbers && state.needsReceipt -> state.receiptPhone.ifBlank { "-" }
            !state.separateNumbers && state.needsReceipt -> state.phoneNumber.ifBlank { "-" }
            else -> "-"
        }
        val pointPhoneResult = when {
            state.separateNumbers && state.needsPoints -> state.pointPhone.ifBlank { "-" }
            !state.separateNumbers && state.needsPoints -> state.phoneNumber.ifBlank { "-" }
            else -> "-"
        }
        val itemsText = state.cartItems.joinToString(", ") { item ->
            "${item.product.name}(${item.qty})" + if (item.discount > 0) "[할인]" else ""
        }

        posRepository.addOrder(
            OrderEntity(
                id = System.currentTimeMillis(),
                date = SimpleDateFormat("yyyy. M. d. a h:mm:ss", Locale.KOREA).format(Date()),
                itemsText = itemsText,
                totalAmount = state.totalAmount,
                receiptPhone = receiptPhoneResult,
                pointPhone = pointPhoneResult,
            )
        )

        _uiState.update {
            it.copy(
                cartItems = emptyList(),
                needsReceipt = false,
                needsPoints = false,
                separateNumbers = false,
                phoneNumber = "",
                receiptPhone = "",
                pointPhone = "",
            )
        }
        _orderSuccessEvent.trySend(Unit)
    }

    fun clearCart() = _uiState.update {
        it.copy(
            cartItems = emptyList(),
            needsReceipt = false,
            needsPoints = false,
            separateNumbers = false,
            phoneNumber = "",
            receiptPhone = "",
            pointPhone = "",
        )
    }

    // History
    fun openHistoryDialog() = _uiState.update { it.copy(showHistoryDialog = true) }

    fun dismissHistoryDialog() = _uiState.update { it.copy(showHistoryDialog = false) }

    fun setOrderFilter(filter: OrderFilter) = _uiState.update { it.copy(orderFilter = filter) }

    fun toggleOrderStatus(orderId: Long) = viewModelScope.launch {
        val order = _uiState.value.orders.find { it.id == orderId } ?: return@launch
        val newStatus = if (order.status == "pending") "done" else "pending"
        posRepository.updateOrder(order.copy(status = newStatus))
    }

    fun clearOrders() = viewModelScope.launch {
        posRepository.clearOrders()
    }
}
