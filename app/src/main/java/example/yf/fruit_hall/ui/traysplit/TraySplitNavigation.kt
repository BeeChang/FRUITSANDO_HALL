package example.yf.fruit_hall.ui.traysplit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.traySplitGraph() {
    composable<MainRoute.TraySplit> {
        TraySplitScreen()
    }
}
