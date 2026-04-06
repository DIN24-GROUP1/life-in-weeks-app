package com.example.memento

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.memento.ui.components.BottomNavBar
import com.example.memento.ui.theme.MementoTheme

import com.example.memento.utils.routeToIndex
import com.example.memento.view.StartScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

@HiltAndroidApp
class MementoApp: Application()


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        // Initialize Firebase Auth
        Log.d("Firebase", "Auth initialized. Current user: ${auth.currentUser}")
        enableEdgeToEdge()
        setContent {
            MementoTheme {
                val navController = rememberNavController()

                val currentRoute = navController.currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                    .value?.destination?.route

                var selectedIndex = routeToIndex(currentRoute)
                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                selectedIndex = index
                                when (index) {
                                    0 -> navController.navigate(LifeGridRoute)
                                    1 -> navController.navigate(StatsRoute)
                                    2 -> navController.navigate(SettingsRoute)
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    MementoNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}




//@Preview(showBackground = true)
//@Composable
//fun MementoPreview() {
//    MementoTheme {
//        Scaffold(
//            bottomBar = {
//                BottomNavBar(
//                    selectedIndex = 0,
//                    onItemSelected = {}
//                )
//            }
//        ) { innerPadding ->
//            StartScreen(modifier = Modifier.padding(innerPadding))
//        }
//    }
//}
//}
