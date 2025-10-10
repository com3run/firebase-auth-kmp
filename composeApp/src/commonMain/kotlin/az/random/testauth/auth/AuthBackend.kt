package az.random.testauth.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Backend contract used by AuthRepository. Platform-specific implementations can
 * integrate Firebase Auth SDKs for Android/iOS and must conform to this API.
 */
interface AuthBackend {
    val authState: StateFlow<AuthUser?>

    suspend fun signInAnonymously(): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInWithApple(idToken: String): AuthResult
    suspend fun signInWithFacebook(accessToken: String): AuthResult
    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun signOut()

    // Password management
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult
    suspend fun updatePassword(newPassword: String): AuthResult

    // Email verification
    suspend fun sendEmailVerification(): AuthResult
    suspend fun applyActionCode(code: String): AuthResult

    // Profile management
    suspend fun updateProfile(displayName: String?, photoUrl: String?): AuthResult
    suspend fun updateEmail(newEmail: String): AuthResult
    suspend fun reloadUser(): AuthResult
    suspend fun deleteAccount(): AuthResult

    // Account linking
    suspend fun linkWithGoogle(idToken: String): AuthResult
    suspend fun linkWithApple(idToken: String): AuthResult
    suspend fun linkWithFacebook(accessToken: String): AuthResult
    suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult
    suspend fun unlinkProvider(providerId: String): AuthResult

    // Re-authentication (required for sensitive operations)
    suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult
    suspend fun reauthenticateWithGoogle(idToken: String): AuthResult
    suspend fun reauthenticateWithApple(idToken: String): AuthResult
}
