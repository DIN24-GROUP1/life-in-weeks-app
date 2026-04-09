package com.example.memento

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.memento.view.LifeGridScreen
import com.example.memento.view.SettingScreen
import com.example.memento.view.StartScreen
import com.example.memento.view.StatScreen
import com.example.memento.viewmodel.UserViewModel
import kotlinx.serialization.Serializable

@Serializable
object StatsRoute
@Serializable
object StartRoute
@Serializable
object SettingsRoute
@Serializable
object LifeGridRoute

@Composable
fun MementoNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
) {
    val userVm: UserViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = StartRoute
    ) {

        composable<StartRoute> {
            StartScreen(navController, userVm)
        }

        composable<LifeGridRoute> {
            LifeGridScreen(userVm)
        }

        composable<SettingsRoute> {
            SettingScreen(viewModel = userVm)
        }
        composable<StatsRoute> {
            StatScreen()
        }
    }
}
