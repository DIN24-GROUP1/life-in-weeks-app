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

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            authRepository.signInWithGoogle(context)
                .onSuccess {
                    isSignedInWithGoogle = true
                    displayName = authRepository.displayName
                    email = authRepository.email
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            isSignedInWithGoogle = false
            displayName = null
            email = null
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
