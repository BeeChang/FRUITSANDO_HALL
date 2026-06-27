package example.yf.fruit_hall.ui.pos

import example.yf.fruit_hall.data.pos.entity.OrderEntity
import example.yf.fruit_hall.data.pos.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val qty: Int = 1,
    val discount: Int = 0,
) {
    val unitFinalPrice: Int get() = maxOf(0, product.price - discount)
    val subtotal: Int get() = unitFinalPrice * qty
}

enum class ProductCategory(val dbValue: String) {
    MAIN("main"), OTHER("other")
}

enum class OrderFilter(val label: String) {
    ALL("전체 내역 보기"),
    PENDING("미완료 (포스 미입력)"),
    DONE("처리완료 (포스 입력됨)"),
}

data class PosUiState(
    val products: List<ProductEntity> = emptyList(),
    val currentTab: ProductCategory = ProductCategory.MAIN,
    val isEditMode: Boolean = false,
    val productCardMinSizeDp: Int = 110,
    val cartItems: List<CartItem> = emptyList(),
    val showInitDialog: Boolean = false,
    val showProductFormDialog: Boolean = false,
    val editingProductId: Long? = null,
    val showDiscountDialog: Boolean = false,
    val needsReceipt: Boolean = false,
    val needsPoints: Boolean = false,
    val separateNumbers: Boolean = false,
    val phoneNumber: String = "",
    val receiptPhone: String = "",
    val pointPhone: String = "",
    val showHistoryDialog: Boolean = false,
    val orders: List<OrderEntity> = emptyList(),
    val orderFilter: OrderFilter = OrderFilter.ALL,
) {
    val filteredProducts: List<ProductEntity>
        get() = products.filter { it.category == currentTab.dbValue }

    val totalAmount: Int
        get() = cartItems.sumOf { it.subtotal }

    val filteredOrders: List<OrderEntity>
        get() = when (orderFilter) {
            OrderFilter.ALL -> orders
            OrderFilter.PENDING -> orders.filter { it.status == "pending" }
            OrderFilter.DONE -> orders.filter { it.status == "done" }
        }
}
