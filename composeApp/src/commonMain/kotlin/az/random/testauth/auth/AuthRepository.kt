package az.random.testauth.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * High-level authentication API used by the app. Implements basic input validation
 * and delegates to the provided AuthBackend (which can be a Firebase-backed implementation
 * on platforms or a fake in tests).
 */
class AuthRepository(
    private val backend: AuthBackend
) {
    val authState: StateFlow<AuthUser?> get() = backend.authState

    // Sign in/up methods
    suspend fun signInAnonymously(): AuthResult = backend.signInAnonymously()

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.signInWithGoogle(idToken)
    }

    suspend fun signInWithApple(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.signInWithApple(idToken)
    }

    suspend fun signInWithFacebook(accessToken: String): AuthResult {
        if (accessToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.signInWithFacebook(accessToken)
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) return AuthResult.Failure(AuthError.InvalidEmailOrPassword)
        return backend.signUpWithEmailAndPassword(email, password)
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) return AuthResult.Failure(AuthError.InvalidEmailOrPassword)
        return backend.signInWithEmailAndPassword(email, password)
    }

    suspend fun signOut() = backend.signOut()

    // Password management
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        if (email.isBlank()) return AuthResult.Failure(AuthError.InvalidEmail)
        return backend.sendPasswordResetEmail(email)
    }

    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        if (code.isBlank() || newPassword.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.confirmPasswordReset(code, newPassword)
    }

    suspend fun updatePassword(newPassword: String): AuthResult {
        if (newPassword.isBlank()) return AuthResult.Failure(AuthError.WeakPassword)
        return backend.updatePassword(newPassword)
    }

    // Email verification
    suspend fun sendEmailVerification(): AuthResult = backend.sendEmailVerification()

    suspend fun applyActionCode(code: String): AuthResult {
        if (code.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.applyActionCode(code)
    }

    // Profile management
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): AuthResult {
        if (displayName == null && photoUrl == null) {
            return AuthResult.Failure(AuthError.Unknown(message = "At least one parameter must be provided"))
        }
        return backend.updateProfile(displayName, photoUrl)
    }

    suspend fun updateEmail(newEmail: String): AuthResult {
        if (newEmail.isBlank()) return AuthResult.Failure(AuthError.InvalidEmail)
        return backend.updateEmail(newEmail)
    }

    suspend fun reloadUser(): AuthResult = backend.reloadUser()

    suspend fun deleteAccount(): AuthResult = backend.deleteAccount()

    // Account linking
    suspend fun linkWithGoogle(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.linkWithGoogle(idToken)
    }

    suspend fun linkWithApple(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.linkWithApple(idToken)
    }

    suspend fun linkWithFacebook(accessToken: String): AuthResult {
        if (accessToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.linkWithFacebook(accessToken)
    }

    suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) return AuthResult.Failure(AuthError.InvalidEmailOrPassword)
        return backend.linkWithEmailAndPassword(email, password)
    }

    suspend fun unlinkProvider(providerId: String): AuthResult {
        if (providerId.isBlank()) return AuthResult.Failure(AuthError.NoSuchProvider)
        return backend.unlinkProvider(providerId)
    }

    // Re-authentication
    suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) return AuthResult.Failure(AuthError.InvalidEmailOrPassword)
        return backend.reauthenticateWithEmail(email, password)
    }

    suspend fun reauthenticateWithGoogle(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.reauthenticateWithGoogle(idToken)
    }

    suspend fun reauthenticateWithApple(idToken: String): AuthResult {
        if (idToken.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
        return backend.reauthenticateWithApple(idToken)
    }
}
