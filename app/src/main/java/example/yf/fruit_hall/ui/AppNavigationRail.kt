package example.yf.fruit_hall.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
)

// 잠금 해제 상태의 바로가기 탭 색상
private val ExtIconColor = Color(0xFF80DEEA)
private val ExtTextColor = Color(0xFF00838F)

@Composable
fun AppNavigationRail(
    currentDestination: NavDestination?,
    onNavigate: (MainRoute) -> Unit,
    onToggleRail: () -> Unit,
    externalPackageName: String,
    isExternalTabLocked: Boolean,
    onExternalSettingsSave: (pkg: String, locked: Boolean) -> Unit,
) {
    val appColors = AppTheme.colors
    val context   = LocalContext.current

    val routeIconColorMap = mapOf(
        MainRoute.Pos      to Color(0xFF89C4F4),
        MainRoute.Beomuri  to Color(0xFF85D9B5),
        MainRoute.Calc     to Color(0xFFFFB347),
        MainRoute.Position to Color(0xFFFF9BB5),
        MainRoute.Clock    to Color(0xFFFFD580),
        MainRoute.Schedule to Color(0xFFCE93D8),
    )
    val routeTextColorMap = mapOf(
        MainRoute.Pos      to Color(0xFF1565A8),
        MainRoute.Beomuri  to Color(0xFF1A7A52),
        MainRoute.Calc     to Color(0xFFC25000),
        MainRoute.Position to Color(0xFFB02060),
        MainRoute.Clock    to Color(0xFF8C6200),
        MainRoute.Schedule to Color(0xFF6A3D9A),
    )

    // 다이얼로그 상태: null = 닫힘, true = 앱 없음 오류 포함, false = 일반 설정
    var showDialogWithError by remember { mutableStateOf<Boolean?>(null) }

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

        // 구분선 + 바로가기 탭을 하단에 배치
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 8.dp),
            color     = appColors.grey100
        )
        Spacer(Modifier.height(4.dp))

        val extIconColor = if (isExternalTabLocked) appColors.grey300 else ExtIconColor
        val extTextColor = if (isExternalTabLocked) appColors.grey400 else ExtTextColor

        NavigationRailItem(
            selected = false,
            onClick  = {
                if (isExternalTabLocked) {
                    // 잠금 상태 → 설정 다이얼로그
                    showDialogWithError = false
                } else {
                    val intent = context.packageManager.getLaunchIntentForPackage(externalPackageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        // 앱 없음 → 에러 메시지 포함 다이얼로그
                        showDialogWithError = true
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = if (isExternalTabLocked) Icons.Default.Lock else Icons.Default.ExitToApp,
                    contentDescription = "바로가기",
                    modifier = Modifier.size(30.dp),
                )
            },
            label = {
                Text(
                    text  = if (isExternalTabLocked) "잠금" else "바로가기",
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            alwaysShowLabel = true,
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor   = extIconColor,
                unselectedIconColor = extIconColor,
                selectedTextColor   = extTextColor,
                unselectedTextColor = extTextColor,
                indicatorColor      = Color.Transparent,
            ),
        )
        Spacer(Modifier.height(8.dp))
    }

    // 다이얼로그
    showDialogWithError?.let { hasError ->
        ExternalAppDialog(
            packageName  = externalPackageName,
            isLocked     = isExternalTabLocked,
            showAppError = hasError,
            onDismiss    = { showDialogWithError = null },
            onSave       = { pkg, locked ->
                onExternalSettingsSave(pkg, locked)
                showDialogWithError = null
            }
        )
    }
}

@Composable
private fun ExternalAppDialog(
    packageName: String,
    isLocked: Boolean,
    showAppError: Boolean,
    onDismiss: () -> Unit,
    onSave: (pkg: String, locked: Boolean) -> Unit,
) {
    val colors = AppTheme.colors
    var inputPkg    by remember(packageName) { mutableStateOf(packageName) }
    var inputLocked by remember(isLocked)    { mutableStateOf(isLocked) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier.fillMaxWidth(0.7f),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = colors.white),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 앱을 찾을 수 없을 때 에러 배너
                if (showAppError) {
                    Text(
                        text  = "앱을 찾을 수 없습니다. 패키지명을 확인해 주세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBF2020)
                    )
                }

                Text(
                    text       = "바로가기 설정",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = colors.grey900
                )

                OutlinedTextField(
                    value         = inputPkg,
                    onValueChange = { inputPkg = it },
                    label         = { Text("앱 패키지명") },
                    placeholder   = { Text("예) co.kr.catchtable.waiting", style = MaterialTheme.typography.bodySmall) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("탭 잠금", style = MaterialTheme.typography.bodyLarge, color = colors.grey800)
                    Switch(checked = inputLocked, onCheckedChange = { inputLocked = it })
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = colors.grey500)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(inputPkg.trim(), inputLocked) },
                        enabled = inputPkg.isNotBlank(),
                        colors  = ButtonDefaults.buttonColors(containerColor = ExtIconColor)
                    ) {
                        Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
