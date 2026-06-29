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
import androidx.compose.material.icons.filled.Schedule
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
        route = MainRoute.Pos,
        labelRes = R.string.nav_pos,
        cdRes = R.string.cd_nav_pos,
        icon = Icons.Default.PointOfSale,
        isSelected = { it?.hasRoute<MainRoute.Pos>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Beomuri,
        labelRes = R.string.nav_beomuri,
        cdRes = R.string.cd_nav_beomuri,
        icon = Icons.Default.Blender,
        isSelected = { it?.hasRoute<MainRoute.Beomuri>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Position,
        labelRes = R.string.nav_position,
        cdRes = R.string.cd_nav_position,
        icon = Icons.Default.Casino,
        isSelected = { it?.hasRoute<MainRoute.Position>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Clock,
        labelRes = R.string.nav_clock,
        cdRes = R.string.cd_nav_clock,
        icon = Icons.Default.Schedule,
        isSelected = { it?.hasRoute<MainRoute.Clock>() ?: false },
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
        MainRoute.Pos      to Color(0xFF89C4F4),  // 소프트 스카이블루
        MainRoute.Beomuri  to Color(0xFF85D9B5),  // 소프트 민트그린
        MainRoute.Position to Color(0xFFFF9BB5),  // 소프트 로즈핑크
        MainRoute.Clock    to Color(0xFFFFD580),  // 소프트 앰버
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
