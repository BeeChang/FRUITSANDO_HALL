package example.yf.fruit_hall.ui.beomuri

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.beomuriGraph() {
    composable<MainRoute.Beomuri> {
        BeomuriScreen()
    }
}
