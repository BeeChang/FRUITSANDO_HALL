package example.yf.fruit_hall.ui.discord

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import example.yf.fruit_hall.ui.MainRoute

fun NavGraphBuilder.discordGraph() {
    composable<MainRoute.Discord> {
        DiscordScreen()
    }
}
