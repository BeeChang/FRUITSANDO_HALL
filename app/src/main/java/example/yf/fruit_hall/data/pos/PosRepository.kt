package example.yf.fruit_hall.data.pos

import example.yf.fruit_hall.data.pos.dao.OrderDao
import example.yf.fruit_hall.data.pos.dao.ProductDao
import example.yf.fruit_hall.data.pos.entity.OrderEntity
import example.yf.fruit_hall.data.pos.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosRepository @Inject constructor(
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
) {
    val products: Flow<List<ProductEntity>> = productDao.getAllFlow()
    val productCount: Flow<Int> = productDao.countFlow()
    val orders: Flow<List<OrderEntity>> = orderDao.getAllFlow()

    suspend fun initDefaultProducts() {
        productDao.deleteAll()
        productDao.insertAll(DEFAULT_PRODUCTS.mapIndexed { index, p -> p.copy(sortOrder = index) })
    }

    suspend fun clearProducts() = productDao.deleteAll()

    suspend fun addProduct(product: ProductEntity) = productDao.insert(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.update(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.delete(product)

    suspend fun swapProductOrder(a: ProductEntity, b: ProductEntity) {
        productDao.update(a.copy(sortOrder = b.sortOrder))
        productDao.update(b.copy(sortOrder = a.sortOrder))
    }

    suspend fun addOrder(order: OrderEntity) = orderDao.insert(order)

    suspend fun updateOrder(order: OrderEntity) = orderDao.update(order)

    suspend fun clearOrders() = orderDao.deleteAll()
}

private val DEFAULT_PRODUCTS = listOf(
    ProductEntity(name = "보냉백", price = 1000, category = "main", bgColor = "#ffffff"),
    ProductEntity(name = "500원 추가", price = 500, category = "main", bgColor = "#ffffff"),
    ProductEntity(name = "100원 추가", price = 100, category = "main", bgColor = "#ffffff"),
    ProductEntity(name = "후르츠 산도", price = 7200, category = "main", bgColor = "#fffce6"),
    ProductEntity(name = "딸기 산도", price = 7200, category = "main", bgColor = "#fff0f0"),
    ProductEntity(name = "파인애플 산도", price = 6700, category = "main", bgColor = "#fffce6"),
    ProductEntity(name = "트로피칼 산도", price = 7600, category = "main", bgColor = "#fff5e6"),
    ProductEntity(name = "메리 산도", price = 7500, category = "main", bgColor = "#fff0f0"),
    ProductEntity(name = "초코퍼지 산도", price = 8400, category = "main", bgColor = "#e9ecef"),
    ProductEntity(name = "멜론 산도", price = 6800, category = "main", bgColor = "#f0fdf4"),
    ProductEntity(name = "딸기파인 산도", price = 7000, category = "main", bgColor = "#fff0f5"),
    ProductEntity(name = "망고 산도", price = 7800, category = "main", bgColor = "#fff5e6"),
    ProductEntity(name = "멜론멜론멜론 산도", price = 9300, category = "main", bgColor = "#e6fcf5"),
    ProductEntity(name = "아메리카노", price = 4500, category = "main", bgColor = "#f8f9fa"),
    ProductEntity(name = "멜론소다", price = 5500, category = "main", bgColor = "#e6fcf5"),
    ProductEntity(name = "아메리카노", price = 4500, category = "other", bgColor = "#f8f9fa"),
    ProductEntity(name = "카페라떼", price = 4700, category = "other", bgColor = "#fff5e6"),
    ProductEntity(name = "멜론소다", price = 5500, category = "other", bgColor = "#e6fcf5"),
    ProductEntity(name = "크림소다", price = 5500, category = "other", bgColor = "#e6f2ff"),
    ProductEntity(name = "추억빙수", price = 6500, category = "other", bgColor = "#e9ecef"),
    ProductEntity(name = "멜론파라다이스", price = 8700, category = "other", bgColor = "#f0fdf4"),
    ProductEntity(name = "수박주스", price = 6500, category = "other", bgColor = "#fff0f0"),
    ProductEntity(name = "말차말차라떼", price = 6500, category = "other", bgColor = "#f0fdf4"),
    ProductEntity(name = "밤라떼", price = 6000, category = "other", bgColor = "#fffce6"),
    ProductEntity(name = "초당옥수수라떼", price = 6000, category = "other", bgColor = "#fffce6"),
    ProductEntity(name = "바닐라라떼", price = 5500, category = "other", bgColor = "#fff5e6"),
    ProductEntity(name = "모카", price = 5500, category = "other", bgColor = "#e9ecef"),
    ProductEntity(name = "연유라떼", price = 5500, category = "other", bgColor = "#ffffff"),
    ProductEntity(name = "레몬소다&차", price = 6000, category = "other", bgColor = "#fffce6"),
    ProductEntity(name = "레몬라임티", price = 6000, category = "other", bgColor = "#f0fdf4"),
    ProductEntity(name = "자몽소다", price = 6000, category = "other", bgColor = "#fff0f0"),
    ProductEntity(name = "아이스티소다", price = 5000, category = "other", bgColor = "#f8f9fa"),
    ProductEntity(name = "복몬이소다", price = 5000, category = "other", bgColor = "#fff0f5"),
    ProductEntity(name = "따듯한 말차라떼", price = 6500, category = "other", bgColor = "#f0fdf4"),
    ProductEntity(name = "초코숲둥둥섬", price = 6500, category = "other", bgColor = "#e9ecef"),
    ProductEntity(name = "우베말차라떼", price = 6000, category = "other", bgColor = "#f0e6ff"),
    ProductEntity(name = "핫초코아이스초코", price = 6700, category = "other", bgColor = "#e9ecef"),
    ProductEntity(name = "히비스커스티", price = 5600, category = "other", bgColor = "#fff0f0"),
    ProductEntity(name = "피치패션티", price = 5600, category = "other", bgColor = "#fff0f5"),
    ProductEntity(name = "얼그레이티", price = 5600, category = "other", bgColor = "#f8f9fa"),
    ProductEntity(name = "잉글리쉬블랙퍼스트티", price = 5600, category = "other", bgColor = "#f8f9fa"),
    ProductEntity(name = "요거트볼", price = 7000, category = "other", bgColor = "#ffffff"),
    ProductEntity(name = "안미츠", price = 8000, category = "other", bgColor = "#fffce6"),
    ProductEntity(name = "푸딩", price = 6500, category = "other", bgColor = "#fffce6"),
)
