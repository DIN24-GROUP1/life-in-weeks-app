package com.example.memento.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.memento.LifeGridRoute
import com.example.memento.StartRoute
import com.example.memento.viewmodel.AuthViewModel

private val Bg = Color(0xFF0D0D1A)
private val Surface = Color(0xFF16162A)
private val Surface2 = Color(0xFF1E1E35)
private val Border = Color(0xFF2A2A48)
private val TextColor = Color(0xFFE8E8F5)
private val Muted = Color(0xFF5A5A80)
private val Accent = Color(0xFF7C3AED)
private val AccentSoft = Color(0xFFA78BFA)

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isSignInMode by remember { mutableStateOf(true) }

    // If already signed in, skip login
    LaunchedEffect(authViewModel.isSignedIn) {
        if (authViewModel.isSignedIn) {
            navController.navigate(StartRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))

        Text(
            text = "Memento",
            color = AccentSoft,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your life in weeks",
            color = Muted,
            fontSize = 15.sp,
        )

        Spacer(Modifier.height(48.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Surface,
            contentColor = AccentSoft,
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text(
                    "Google",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTab == 0) AccentSoft else Muted,
                )
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text(
                    "Email",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTab == 1) AccentSoft else Muted,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (selectedTab == 0) {
            Button(
                onClick = { authViewModel.signInWithGoogle(context) },
                enabled = !authViewModel.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(
                    text = if (authViewModel.isLoading) "Signing in…" else "Sign in with Google",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        } else {
            // Sign in / Register toggle
            TabRow(
                selectedTabIndex = if (isSignInMode) 0 else 1,
                containerColor = Surface2,
                contentColor = AccentSoft,
            ) {
                Tab(selected = isSignInMode, onClick = { isSignInMode = true }) {
                    Text("Sign in", modifier = Modifier.padding(vertical = 10.dp), color = if (isSignInMode) AccentSoft else Muted)
                }
                Tab(selected = !isSignInMode, onClick = { isSignInMode = false }) {
                    Text("Register", modifier = Modifier.padding(vertical = 10.dp), color = if (!isSignInMode) AccentSoft else Muted)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    cursorColor = AccentSoft,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Password", color = Muted) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    cursorColor = AccentSoft,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSignInMode) authViewModel.signInWithEmail(emailInput, passwordInput)
                    else authViewModel.registerWithEmail(emailInput, passwordInput)
                },
                enabled = !authViewModel.isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(
                    text = when {
                        authViewModel.isLoading -> "Please wait…"
                        isSignInMode -> "Sign in"
                        else -> "Register"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }

        authViewModel.errorMessage?.let { msg ->
            Spacer(Modifier.height(10.dp))
            Text(text = msg, color = Color(0xFFEF4444), fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = {
                navController.navigate(StartRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) {
            Text(
                text = "Continue without signing in",
                color = Muted,
                fontSize = 14.sp,
            )
        }
    }
}
