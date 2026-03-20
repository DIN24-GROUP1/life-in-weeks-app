package com.example.memento

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.memento.ui.components.BottomNavBar
import com.example.memento.ui.theme.MementoTheme
import com.example.memento.view.StartScreen
import com.example.memento.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MementoApp: Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MementoTheme {
                Scaffold { innerPadding ->
                    StartScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MementoPreview() {
    MementoTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar()
            }
        ) { innerPadding ->
            StartScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
