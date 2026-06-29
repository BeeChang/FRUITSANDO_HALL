package example.yf.fruit_hall.ui.clock

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.clockGraph() {
    composable<MainRoute.Clock> {
        ClockScreen()
    }
}
