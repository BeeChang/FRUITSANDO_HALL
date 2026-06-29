package example.yf.fruit_hall.ui.position

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.positionGraph() {
    composable<MainRoute.Position> {
        PositionScreen()
    }
}
