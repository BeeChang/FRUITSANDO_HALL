package example.yf.fruit_hall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import example.yf.fruit_hall.ui.beomuri.beomuriGraph
import example.yf.fruit_hall.ui.calc.calcGraph
import example.yf.fruit_hall.ui.clock.clockGraph
import example.yf.fruit_hall.ui.pos.posGraph
import example.yf.fruit_hall.ui.position.positionGraph
import example.yf.fruit_hall.ui.schedule.scheduleGraph

@Composable
fun FruitHallApp(
    viewModel: AppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Activity 재생성(화면 회전) 시 Composition이 새로 만들어져 LaunchedEffect가 항상 실행된다.
    // ViewModel의 플래그로 최초 1회만 initialRoute로 이동하고, 이후에는 NavController SavedState가 복원한다.
    LaunchedEffect(viewModel) {
        if (!viewModel.hasNavigatedInitially) {
            viewModel.markNavigatedInitially()
            val initial = viewModel.initialRoute
            if (initial != MainRoute.Pos) {
                navController.navigate(initial) {
                    popUpTo<MainRoute.Pos> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = viewModel.isRailVisible,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it }
            ) {
                AppNavigationRail(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        viewModel.onRouteSelected(route)
                        navController.navigate(route) {
                            popUpTo<MainRoute.Pos> { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onToggleRail = viewModel::toggleRail
                )
            }

            NavHost(
                navController = navController,
                startDestination = MainRoute.Pos,
                modifier = Modifier.fillMaxSize()
            ) {
                posGraph()
                beomuriGraph()
                calcGraph()
                positionGraph()
                clockGraph()
                scheduleGraph()
            }
        }

        AnimatedVisibility(
            visible = !viewModel.isRailVisible,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            SmallFloatingActionButton(onClick = viewModel::toggleRail) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "메뉴 열기"
                )
            }
        }
    }
}
