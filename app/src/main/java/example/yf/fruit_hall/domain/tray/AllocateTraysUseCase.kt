package example.yf.fruit_hall.domain.tray

import example.yf.fruit_hall.core.AllocationCandidate
import example.yf.fruit_hall.core.AllocationConfig
import example.yf.fruit_hall.core.Tray
import example.yf.fruit_hall.core.TrayAllocator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AllocateTraysUseCase @Inject constructor() {
    private val allocator = TrayAllocator()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    suspend operator fun invoke(
        trays: List<Tray>,
        config: AllocationConfig,
        onAttempt: () -> Unit = {}
    ): List<AllocationCandidate> = withContext(dispatcher) {
        allocator.allocate(trays, config, onAttempt)
    }
}
