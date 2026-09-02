package example.yf.fruit_hall.domain.rotation

import example.yf.fruit_hall.core.rotation.RotationInput
import example.yf.fruit_hall.core.rotation.RotationOutput
import example.yf.fruit_hall.core.rotation.RotationPlanner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlanRotationUseCase @Inject constructor() {
    private val planner = RotationPlanner()
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default

    suspend operator fun invoke(input: RotationInput): RotationOutput = withContext(dispatcher) {
        planner.plan(input)
    }
}
