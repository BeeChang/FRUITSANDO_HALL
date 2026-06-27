package example.yf.fruit_hall.ui

import kotlinx.serialization.Serializable

sealed interface MainRoute {
    @Serializable data object Home : MainRoute
    @Serializable data object Second : MainRoute
    @Serializable data object Third : MainRoute
}