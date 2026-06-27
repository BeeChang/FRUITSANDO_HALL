package example.yf.fruit_hall.ui.third

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.thirdGraph() {
    composable<MainRoute.Third> {
        ThirdScreen()
    }
}