package az.random.testauth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.random.testauth.auth.AuthError
import az.random.testauth.auth.AuthRepository
import az.random.testauth.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.SignedOut)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _uiState.value = if (user != null) {
                    AuthUiState.SignedIn(user.displayName ?: "Unknown")
                } else {
                    AuthUiState.SignedOut
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    // State will be updated by the authState collector
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(mapError(result.error))
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signUpWithEmailAndPassword(email, password)) {
                is AuthResult.Success -> {
                    // State will be updated by the authState collector
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(mapError(result.error))
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signInAnonymously()) {
                is AuthResult.Success -> {
                    // State will be updated by the authState collector
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(mapError(result.error))
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Success("Password reset email sent! Check your inbox.")
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(mapError(result.error))
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    // State will be updated by the authState collector
                }
                is AuthResult.Failure -> {
                    _uiState.value = AuthUiState.Error(mapError(result.error))
                }
            }
        }
    }

    private fun mapError(error: AuthError): String = when (error) {
        AuthError.InvalidCredential -> "Invalid credentials"
        AuthError.InvalidEmailOrPassword -> "Invalid email or password"
        AuthError.UserNotFound -> "User not found"
        AuthError.WrongPassword -> "Incorrect password"
        AuthError.UserDisabled -> "This account has been disabled"
        AuthError.TooManyRequests -> "Too many attempts. Please try again later"
        AuthError.InvalidEmail -> "Invalid email address"
        AuthError.EmailAlreadyInUse -> "Email already in use"
        AuthError.WeakPassword -> "Password is too weak"
        AuthError.EmailNotVerified -> "Email not verified"
        AuthError.RequiresRecentLogin -> "Please sign in again"
        AuthError.ProviderAlreadyLinked -> "Provider already linked"
        AuthError.NoSuchProvider -> "Provider not found"
        is AuthError.Network -> error.message ?: "Network error"
        is AuthError.Unknown -> error.message ?: "An unknown error occurred"
    }
}

sealed class AuthUiState {
    object SignedOut : AuthUiState()
    data class SignedIn(val displayName: String) : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Success(val message: String) : AuthUiState()
}
