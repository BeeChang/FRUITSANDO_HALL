package example.yf.fruit_hall.ui.calc

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.calcGraph() {
    composable<MainRoute.Calc> {
        CalcScreen()
    }
}
