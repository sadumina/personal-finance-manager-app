package com.example.financeflow.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeflow.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val registrationCompleted: Boolean = false,
    val passwordResetSent: Boolean = false,
    val userName: String = "User",
    val userEmail: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = repository.isUserLoggedIn)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        refreshCurrentUser()
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val profile = repository.getCurrentUserProfile()
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        userName = profile.name,
                        userEmail = profile.email
                    )
                }
            }
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    passwordResetSent = false,
                    registrationCompleted = false
                )
            }

            repository.login(email, password)
                .onSuccess {
                    val profile = repository.getCurrentUserProfile()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            registrationCompleted = false,
                            userName = profile?.name ?: it.userName,
                            userEmail = profile?.email ?: it.userEmail
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = error.message ?: "Login failed. Please try again."
                        )
                    }
                }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ) {

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    passwordResetSent = false,
                    registrationCompleted = false
                )
            }

            repository.register(fullName, email, phone, password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            registrationCompleted = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            errorMessage = error.message ?: "Registration failed. Please try again."
                        )
                    }
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    passwordResetSent = false,
                    registrationCompleted = false
                )
            }

            repository.sendPasswordResetEmail(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, passwordResetSent = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordResetSent = false,
                            errorMessage = error.message ?: "Could not send reset email."
                        )
                    }
                }
        }
    }

    fun logout() {

        repository.logout()
        _uiState.update {
            AuthUiState(isAuthenticated = false)
        }
    }

    fun clearAuthMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                passwordResetSent = false,
                registrationCompleted = false
            )
        }
    }
}
