package example.yf.fruit_hall.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import example.yf.fruit_hall.R
import example.yf.fruit_hall.ui.theme.AppTheme

private data class RailItemData(
    val route: MainRoute,
    @field:StringRes val labelRes: Int,
    @field:StringRes val cdRes: Int,
    val icon: ImageVector,
    val isSelected: (NavDestination?) -> Boolean,
)

private val railItems = listOf(
    RailItemData(
        route = MainRoute.Home,
        labelRes = R.string.nav_home,
        cdRes = R.string.cd_nav_home,
        icon = Icons.Default.PointOfSale,
        isSelected = { it?.hasRoute<MainRoute.Home>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Second,
        labelRes = R.string.nav_second,
        cdRes = R.string.cd_nav_second,
        icon = Icons.Default.Blender,
        isSelected = { it?.hasRoute<MainRoute.Second>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Third,
        labelRes = R.string.nav_third,
        cdRes = R.string.cd_nav_third,
        icon = Icons.Default.Casino,
        isSelected = { it?.hasRoute<MainRoute.Third>() ?: false },
    ),
)

@Composable
fun AppNavigationRail(
    currentDestination: NavDestination?,
    onNavigate: (MainRoute) -> Unit,
    onToggleRail: () -> Unit,
) {
    val appColors = AppTheme.colors
    val routeColorMap = mapOf(
        MainRoute.Home to appColors.primary500,
        MainRoute.Second to appColors.success500,
        MainRoute.Third to appColors.crimson200,
    )

    NavigationRail {
        Spacer(Modifier.height(8.dp))
        IconButton(
            onClick = onToggleRail,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "메뉴 닫기",
                tint = appColors.grey600,
            )
        }
        Spacer(Modifier.height(4.dp))
        railItems.forEach { item ->
            val iconColor = routeColorMap[item.route] ?: Color.Unspecified
            NavigationRailItem(
                selected = item.isSelected(currentDestination),
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.cdRes),
                        modifier = Modifier.size(30.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = iconColor,
                    unselectedIconColor = iconColor,
                    selectedTextColor = iconColor,
                    unselectedTextColor = iconColor,
                    indicatorColor = iconColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}