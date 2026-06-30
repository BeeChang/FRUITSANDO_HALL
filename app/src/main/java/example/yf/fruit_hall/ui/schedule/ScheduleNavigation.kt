package example.yf.fruit_hall.ui.schedule

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.scheduleGraph() {
    composable<MainRoute.Schedule> {
        ScheduleScreen()
    }
}
