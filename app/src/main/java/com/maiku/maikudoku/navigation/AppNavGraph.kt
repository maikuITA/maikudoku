package com.maiku.maikudoku.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maiku.maikudoku.domain.model.Difficulty
import com.maiku.maikudoku.ui.game.GameScreen
import com.maiku.maikudoku.ui.menu.MenuScreen

private object Routes {
    const val MENU = "menu"
    const val GAME = "game/{difficulty}"

    fun game(difficulty: Difficulty): String = "game/${difficulty.routeValue}"
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MENU,
        modifier = modifier
    ) {
        composable(route = Routes.MENU) {
            MenuScreen(onDifficultySelected = { difficulty ->
                navController.navigate(Routes.game(difficulty))
            })
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) {
            GameScreen(onNavigateHome = {
                navController.navigate(Routes.MENU) {
                    popUpTo(Routes.MENU) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
    }
}

