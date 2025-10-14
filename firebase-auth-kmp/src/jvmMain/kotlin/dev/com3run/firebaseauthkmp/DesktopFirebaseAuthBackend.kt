package dev.com3run.firebaseauthkmp

import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Desktop/JVM implementation of AuthBackend using Firebase REST API.
 *
 * This implementation uses Firebase Authentication REST API endpoints instead of
 * platform-specific SDKs, making it suitable for desktop JVM applications.
 *
 * Configuration: Requires firebase-config.json with format:
 * {
 *   "apiKey": "your-firebase-api-key",
 *   "projectId": "your-project-id"
 * }
 *
 * Limitations:
 * - OAuth providers (Google/Apple/Facebook) require external browser-based flow
 * - No automatic token refresh (manual refresh required)
 * - No offline persistence (tokens stored in memory only)
 */
class DesktopFirebaseAuthBackend : AuthBackend {

    private val apiKey: String by lazy { loadApiKey() }
    private val client by lazy { FirebaseRestClient(apiKey) }

    private val _authState = MutableStateFlow<AuthUser?>(null)
    override val authState: StateFlow<AuthUser?> = _authState

    // Store tokens in memory (consider secure storage for production)
    private var currentIdToken: String? = null
    private var currentRefreshToken: String? = null

    override suspend fun signInAnonymously(): AuthResult = runCatching {
        val response = client.signInAnonymously()
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult = runCatching {
        val response = client.signInWithIdp(idToken, "google.com")
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signInWithApple(idToken: String): AuthResult = runCatching {
        val response = client.signInWithIdp(idToken, "apple.com")
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signInWithFacebook(accessToken: String): AuthResult = runCatching {
        val response = client.signInWithIdp(accessToken, "facebook.com")
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult = runCatching {
        val response = client.signUp(email, password)
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult = runCatching {
        val response = client.signInWithPassword(email, password)
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun signOut() {
        currentIdToken = null
        currentRefreshToken = null
        _authState.value = null
    }

    // Password management
    override suspend fun sendPasswordResetEmail(email: String): AuthResult = runCatching {
        val response = client.sendPasswordResetEmail(email)
        if (response.error != null) {
            throw FirebaseRestException(response.error.message ?: "Unknown error")
        }
        AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult = runCatching {
        val response = client.confirmPasswordReset(code, newPassword)
        if (response.error != null) {
            throw FirebaseRestException(response.error.message ?: "Unknown error")
        }
        AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun updatePassword(newPassword: String): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        val response = client.updatePassword(idToken, newPassword)
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    // Email verification
    override suspend fun sendEmailVerification(): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        val response = client.sendEmailVerification(idToken)
        if (response.error != null) {
            throw FirebaseRestException(response.error.message ?: "Unknown error")
        }
        _authState.value?.let { AuthResult.Success(it) }
            ?: AuthResult.Failure(AuthError.Unknown(null))
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun applyActionCode(code: String): AuthResult = runCatching {
        // Firebase REST API doesn't have a direct applyActionCode endpoint
        // This is typically handled by the web widget or client SDK
        // For now, we'll reload the user to verify email status
        reloadUser()
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    // Profile management
    override suspend fun updateProfile(displayName: String?, photoUrl: String?): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        val response = client.updateProfile(idToken, displayName, photoUrl)
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun updateEmail(newEmail: String): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        val response = client.updateEmail(idToken, newEmail)
        handleAuthResponse(response)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun reloadUser(): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        val accountInfo = client.getAccountInfo(idToken)

        if (accountInfo.error != null) {
            throw FirebaseRestException(accountInfo.error.message ?: "Unknown error")
        }

        val user = accountInfo.users?.firstOrNull()
            ?: throw IllegalStateException("No user data returned")

        val authUser = user.toAuthUser()
        _authState.value = authUser
        AuthResult.Success(authUser)
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    override suspend fun deleteAccount(): AuthResult = runCatching {
        val idToken = currentIdToken ?: throw IllegalStateException("No authenticated user")
        client.deleteAccount(idToken)
        signOut()
        AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    // Account linking (Note: Firebase REST API has limited support for account linking)
    override suspend fun linkWithGoogle(idToken: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown(UnsupportedOperationException("Account linking not fully supported via REST API on desktop")))

    override suspend fun linkWithApple(idToken: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown(UnsupportedOperationException("Account linking not fully supported via REST API on desktop")))

    override suspend fun linkWithFacebook(accessToken: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown(UnsupportedOperationException("Account linking not fully supported via REST API on desktop")))

    override suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown(UnsupportedOperationException("Account linking not fully supported via REST API on desktop")))

    override suspend fun unlinkProvider(providerId: String): AuthResult =
        AuthResult.Failure(AuthError.Unknown(UnsupportedOperationException("Account linking not fully supported via REST API on desktop")))

    // Re-authentication
    override suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult =
        signInWithEmailAndPassword(email, password)

    override suspend fun reauthenticateWithGoogle(idToken: String): AuthResult =
        signInWithGoogle(idToken)

    override suspend fun reauthenticateWithApple(idToken: String): AuthResult =
        signInWithApple(idToken)

    // Helper methods

    private suspend fun handleAuthResponse(response: FirebaseAuthResponse): AuthResult {
        if (response.error != null) {
            throw FirebaseRestException(response.error.message ?: "Unknown error")
        }

        currentIdToken = response.idToken
        currentRefreshToken = response.refreshToken

        // Fetch full user info
        val idToken = response.idToken ?: throw IllegalStateException("No ID token returned")
        val accountInfo = client.getAccountInfo(idToken)

        val user = accountInfo.users?.firstOrNull()
            ?: throw IllegalStateException("No user data returned")

        val authUser = user.toAuthUser()
        _authState.value = authUser

        return AuthResult.Success(authUser)
    }

    /**
     * Refresh the current ID token using the refresh token.
     * Call this when you receive TOKEN_EXPIRED errors.
     */
    suspend fun refreshIdToken(): AuthResult = runCatching {
        val refreshToken = currentRefreshToken
            ?: throw IllegalStateException("No refresh token available")

        val response = client.refreshToken(refreshToken)

        if (response.error != null) {
            throw FirebaseRestException(response.error.message ?: "Unknown error")
        }

        currentIdToken = response.idToken
        currentRefreshToken = response.refreshToken ?: currentRefreshToken

        reloadUser()
    }.getOrElse { e ->
        AuthResult.Failure(mapError(e))
    }

    private fun loadApiKey(): String {
        // Try multiple locations for firebase-config.json
        val possiblePaths = listOf(
            "firebase-config.json",                           // Current working directory
            "../firebase-config.json",                        // Parent directory
            "../../firebase-config.json",                     // Project root when running from build/
            System.getProperty("user.dir") + "/firebase-config.json"  // Explicit working directory
        )

        for (path in possiblePaths) {
            val configFile = File(path)
            if (configFile.exists()) {
                try {
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val config = json.decodeFromString<FirebaseConfig>(configFile.readText())
                    return config.apiKey
                } catch (e: Exception) {
                    // Continue to next path if parsing fails
                    continue
                }
            }
        }

        // Fallback to environment variable
        return System.getenv("FIREBASE_API_KEY")
            ?: throw IllegalStateException(
                "Firebase API key not found. " +
                "Create firebase-config.json with {\"apiKey\": \"...\", \"projectId\": \"...\"} " +
                "in project root or set FIREBASE_API_KEY environment variable. " +
                "Current working directory: ${System.getProperty("user.dir")}"
            )
    }
}

@kotlinx.serialization.Serializable
private data class FirebaseConfig(
    val apiKey: String,
    val projectId: String
)

private fun UserInfo.toAuthUser(): AuthUser =
    AuthUser(
        uid = localId,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        isAnonymous = email == null && providerUserInfo.isNullOrEmpty(),
        isEmailVerified = emailVerified,
        providerData = providerUserInfo?.map { it.providerId } ?: emptyList()
    )

private fun mapError(throwable: Throwable): AuthError {
    return when (throwable) {
        is FirebaseRestException -> {
            val message = throwable.message ?: ""
            when {
                message.contains("EMAIL_EXISTS", ignoreCase = true) -> AuthError.EmailAlreadyInUse
                message.contains("EMAIL_NOT_FOUND", ignoreCase = true) -> AuthError.UserNotFound
                message.contains("INVALID_PASSWORD", ignoreCase = true) -> AuthError.WrongPassword
                message.contains("WEAK_PASSWORD", ignoreCase = true) -> AuthError.WeakPassword
                message.contains("INVALID_EMAIL", ignoreCase = true) -> AuthError.InvalidEmail
                message.contains("USER_DISABLED", ignoreCase = true) -> AuthError.UserDisabled
                message.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) -> AuthError.TooManyRequests
                message.contains("INVALID_ID_TOKEN", ignoreCase = true) ||
                message.contains("TOKEN_EXPIRED", ignoreCase = true) -> AuthError.RequiresRecentLogin
                message.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) -> AuthError.RequiresRecentLogin
                else -> AuthError.Unknown(throwable, message)
            }
        }
        is ResponseException -> AuthError.Network(throwable.message)
        is IllegalStateException -> AuthError.Unknown(throwable, throwable.message)
        else -> AuthError.Unknown(throwable)
    }
}

private class FirebaseRestException(message: String) : Exception(message)
