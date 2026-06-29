package example.yf.fruit_hall.ui

import kotlinx.serialization.Serializable

sealed interface MainRoute {
    @Serializable data object Pos : MainRoute
    @Serializable data object Beomuri : MainRoute
    @Serializable data object Position : MainRoute
}
