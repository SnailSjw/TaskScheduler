package com.tim.autotask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tim.autotask.ui.screens.CreateTaskScreen
import com.tim.autotask.ui.screens.HomeScreen
import com.tim.autotask.ui.screens.MapPickerResult
import com.tim.autotask.ui.screens.MapPickerScreen
import com.tim.autotask.ui.screens.SettingsScreen
import com.tim.autotask.ui.screens.TaskDetailScreen

object Routes {
    const val HOME = "home"
    const val CREATE_TASK = "create_task"
    const val MAP_PICKER = "map_picker/{lat}/{lng}"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val SETTINGS = "settings"

    fun mapPicker(lat: Double, lng: Double) = "map_picker/$lat/$lng"
    fun taskDetail(taskId: Long) = "task_detail/$taskId"
}

@Composable
fun AppNavigation(initialTaskId: Long? = null) {
    val navController = rememberNavController()
    var mapPickerResult by remember { mutableStateOf<MapPickerResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCreate = {
                    mapPickerResult = null
                    navController.navigate(Routes.CREATE_TASK)
                },
                onNavigateToDetail = { taskId ->
                    navController.navigate(Routes.taskDetail(taskId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.CREATE_TASK) {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMapPicker = { lat, lng ->
                    navController.navigate(Routes.mapPicker(lat, lng))
                },
                mapPickerResult = mapPickerResult
            )
        }

        composable(
            route = Routes.MAP_PICKER,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 39.9042
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 116.4074

            MapPickerScreen(
                initialLat = lat,
                initialLng = lng,
                onConfirm = { result ->
                    mapPickerResult = result
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    if (initialTaskId != null) {
        androidx.compose.runtime.LaunchedEffect(initialTaskId) {
            navController.navigate(Routes.taskDetail(initialTaskId))
        }
    }
}
