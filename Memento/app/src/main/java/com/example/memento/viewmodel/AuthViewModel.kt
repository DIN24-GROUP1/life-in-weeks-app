package com.example.memento.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memento.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    var isSignedIn by mutableStateOf(authRepository.isSignedIn)
        private set
    var isSignedInWithGoogle by mutableStateOf(authRepository.isSignedInWithGoogle)
        private set
    var displayName by mutableStateOf(authRepository.displayName)
        private set
    var email by mutableStateOf(authRepository.email)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private fun refreshState() {
        isSignedIn = authRepository.isSignedIn
        isSignedInWithGoogle = authRepository.isSignedInWithGoogle
        displayName = authRepository.displayName
        email = authRepository.email
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            authRepository.signInWithGoogle(context)
                .onSuccess { refreshState() }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            authRepository.registerWithEmail(email, password)
                .onSuccess { refreshState() }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            authRepository.signInWithEmail(email, password)
                .onSuccess { refreshState() }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            isSignedIn = false
            isSignedInWithGoogle = false
            displayName = null
            email = null
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
