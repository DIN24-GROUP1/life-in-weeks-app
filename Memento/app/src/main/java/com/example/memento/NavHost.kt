package com.example.memento

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.memento.view.LifeGridScreen
import com.example.memento.view.SettingScreen
import com.example.memento.view.StartScreen
import com.example.memento.view.StatScreen
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
    modifier: Modifier,
) {
    NavHost(
        navController,
        startDestination = StartRoute,
    ) {
        composable<StartRoute> {
            StartScreen()
        }
        composable<LifeGridRoute> {
            LifeGridScreen()
        }
        composable<SettingsRoute> {
            SettingScreen()
        }
        composable<StatsRoute> {
            StatScreen()
        }

    }
}