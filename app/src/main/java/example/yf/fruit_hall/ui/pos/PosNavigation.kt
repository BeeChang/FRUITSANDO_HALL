package example.yf.fruit_hall.ui.pos

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.posGraph() {
    composable<MainRoute.Home> {
        EmergencyPosScreen()
    }
}
