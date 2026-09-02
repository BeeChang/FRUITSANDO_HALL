package example.yf.fruit_hall.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
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
        route = MainRoute.Calc,
        labelRes = R.string.nav_calc,
        cdRes = R.string.cd_nav_calc,
        icon = Icons.Default.Calculate,
        isSelected = { it?.hasRoute<MainRoute.Calc>() ?: false },
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
    RailItemData(
        route = MainRoute.Schedule,
        labelRes = R.string.nav_schedule,
        cdRes = R.string.cd_nav_schedule,
        icon = Icons.Default.CalendarMonth,
        isSelected = { it?.hasRoute<MainRoute.Schedule>() ?: false },
    ),
    RailItemData(
        route = MainRoute.TraySplit,
        labelRes = R.string.nav_tray_split,
        cdRes = R.string.cd_nav_tray_split,
        icon = Icons.Default.Inventory2,
        isSelected = { it?.hasRoute<MainRoute.TraySplit>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Rotation,
        labelRes = R.string.nav_rotation,
        cdRes = R.string.cd_nav_rotation,
        icon = Icons.Default.SwapHoriz,
        isSelected = { it?.hasRoute<MainRoute.Rotation>() ?: false },
    ),
    RailItemData(
        route = MainRoute.Discord,
        labelRes = R.string.nav_discord,
        cdRes = R.string.cd_nav_discord,
        icon = Icons.AutoMirrored.Filled.Send,
        isSelected = { it?.hasRoute<MainRoute.Discord>() ?: false },
    ),
)

@Composable
fun AppNavigationRail(
    currentDestination: NavDestination?,
    onNavigate: (MainRoute) -> Unit,
    onToggleRail: () -> Unit,
) {
    val appColors = AppTheme.colors
    val routeIconColorMap = mapOf(
        MainRoute.Pos      to Color(0xFF89C4F4),  // 소프트 스카이블루
        MainRoute.Beomuri  to Color(0xFF85D9B5),  // 소프트 민트그린
        MainRoute.Calc     to Color(0xFFFFB347),  // 소프트 오렌지
        MainRoute.Position to Color(0xFFFF9BB5),  // 소프트 로즈핑크
        MainRoute.Clock    to Color(0xFFFFD580),  // 소프트 앰버
        MainRoute.Schedule to Color(0xFFCE93D8),  // 소프트 라벤더
        MainRoute.TraySplit to Color(0xFFB5C99A), // 소프트 세이지그린
        MainRoute.Rotation to Color(0xFF7FD1D1),  // 소프트 틸
        MainRoute.Discord to Color(0xFF9DA9F2),   // 소프트 인디고 (디스코드 브랜드 톤)
    )
    val routeTextColorMap = mapOf(
        MainRoute.Pos      to Color(0xFF1565A8),  // 진한 블루
        MainRoute.Beomuri  to Color(0xFF1A7A52),  // 진한 그린
        MainRoute.Calc     to Color(0xFFC25000),  // 진한 오렌지
        MainRoute.Position to Color(0xFFB02060),  // 진한 핑크
        MainRoute.Clock    to Color(0xFF8C6200),  // 진한 앰버
        MainRoute.Schedule to Color(0xFF6A3D9A),  // 진한 퍼플
        MainRoute.TraySplit to Color(0xFF4A6B2A), // 진한 올리브그린
        MainRoute.Rotation to Color(0xFF0B6E6E),  // 진한 틸
        MainRoute.Discord to Color(0xFF3F4BB0),   // 진한 인디고
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
            val iconColor = routeIconColorMap[item.route] ?: Color.Unspecified
            val textColor = routeTextColorMap[item.route] ?: Color.Unspecified
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
                    selectedIconColor   = iconColor,
                    unselectedIconColor = iconColor,
                    selectedTextColor   = textColor,
                    unselectedTextColor = textColor,
                    indicatorColor      = iconColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
