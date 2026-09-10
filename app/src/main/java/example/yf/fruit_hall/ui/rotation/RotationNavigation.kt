package example.yf.fruit_hall.ui.rotation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.rotationGraph() {
    composable<MainRoute.Rotation> {
        RotationScreen()
    }
}
