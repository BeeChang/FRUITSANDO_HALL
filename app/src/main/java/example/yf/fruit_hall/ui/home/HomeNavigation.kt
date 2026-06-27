package example.yf.fruit_hall.ui.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.homeGraph() {
    composable<MainRoute.Home> {
        HomeScreen()
    }
}