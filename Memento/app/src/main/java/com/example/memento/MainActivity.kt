package com.example.memento

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
import com.example.memento.ui.theme.MementoTheme
import com.example.memento.view.StartScreen
import com.example.memento.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MementoTheme {
                    StartScreen(
                        viewModel = UserViewModel()
                    )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MementoPreview() {
    MementoTheme {
        StartScreen(UserViewModel())
    }
}