@file:OptIn(ExperimentalObjCName::class)

package az.random.firebaseauth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUUID
import kotlin.coroutines.resume
import kotlin.experimental.ExperimentalObjCName

// iOS logging helper - safe string formatting
private fun logDebug(tag: String, message: String) {
    println("[$tag] $message")
}

private fun logError(tag: String, message: String) {
    println("[ERROR][$tag] $message")
}

actual fun platformAuthBackend(): AuthBackend = IosFirebaseAuthBackend()

class IosFirebaseAuthBackend : AuthBackend {
    private val _authState = MutableStateFlow<AuthUser?>(null)
    override val authState: StateFlow<AuthUser?> = _authState

    private val center = NSNotificationCenter.defaultCenter

    init {
        // Listen for auth state changes from Swift bridge
        center.addObserverForName(
            name = "AuthState",
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification: NSNotification? ->
            val userInfo = notification?.userInfo as? Map<*, *>
            val user = userInfo?.toAuthUser()
            _authState.value = user
        }
    }

    override suspend fun signInAnonymously(): AuthResult {
        logDebug("FirebaseAuth", "Attempting anonymous sign in")
        val result = performAuthRequest(action = "anonymous")
        when (result) {
            is AuthResult.Success -> logDebug("FirebaseAuth", "Anonymous sign in successful: ${result.user.uid}")
            is AuthResult.Failure -> logError("FirebaseAuth", "Anonymous sign in failed: ${result.error}")
        }
        return result
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        logDebug("FirebaseAuth", "Attempting Google sign in with idToken")
        val result = performAuthRequest(
            action = "google",
            params = mapOf("idToken" to idToken)
        )
        when (result) {
            is AuthResult.Success -> logDebug("FirebaseAuth", "Google sign in successful")
            is AuthResult.Failure -> logError("FirebaseAuth", "Google sign in failed: ${result.error}")
        }
        return result
    }

    override suspend fun signInWithApple(idToken: String): AuthResult {
        return performAuthRequest(
            action = "apple",
            params = mapOf("idToken" to idToken)
        )
    }

    override suspend fun signInWithFacebook(accessToken: String): AuthResult {
        return performAuthRequest(
            action = "facebook",
            params = mapOf("accessToken" to accessToken)
        )
    }
    
    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
        logDebug("FirebaseAuth", "Attempting sign up with email: $email")
        val result = performAuthRequest(
            action = "signUpWithEmailAndPassword",
            params = mapOf("email" to email, "password" to password)
        )
        when (result) {
            is AuthResult.Success -> logDebug("FirebaseAuth", "Sign up successful: $email")
            is AuthResult.Failure -> logError("FirebaseAuth", "Sign up failed: ${result.error}")
        }
        return result
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        logDebug("FirebaseAuth", "Attempting sign in with email: $email")
        val result = performAuthRequest(
            action = "signInWithEmailAndPassword",
            params = mapOf("email" to email, "password" to password)
        )
        when (result) {
            is AuthResult.Success -> logDebug("FirebaseAuth", "Sign in successful: $email")
            is AuthResult.Failure -> logError("FirebaseAuth", "Sign in failed: ${result.error}")
        }
        return result
    }

    override suspend fun signOut() {
        when (val result = performAuthRequest(action = "signOut")) {
            is AuthResult.Success -> {
                _authState.value = null
            }
            is AuthResult.Failure -> {
                throw (result.error as? AuthError.Unknown)?.throwable
                    ?: Exception("Sign out failed")
            }
        }
    }

    // Password management
    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return performAuthRequest(
            action = "sendPasswordResetEmail",
            params = mapOf("email" to email)
        )
    }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
        return performAuthRequest(
            action = "confirmPasswordReset",
            params = mapOf("code" to code, "newPassword" to newPassword)
        )
    }

    override suspend fun updatePassword(newPassword: String): AuthResult {
        return performAuthRequest(
            action = "updatePassword",
            params = mapOf("newPassword" to newPassword)
        )
    }

    // Email verification
    override suspend fun sendEmailVerification(): AuthResult {
        return performAuthRequest(action = "sendEmailVerification")
    }

    override suspend fun applyActionCode(code: String): AuthResult {
        return performAuthRequest(
            action = "applyActionCode",
            params = mapOf("code" to code)
        )
    }

    // Profile management
    override suspend fun updateProfile(displayName: String?, photoUrl: String?): AuthResult {
        val params = mutableMapOf<String, Any>()
        displayName?.let { params["displayName"] = it }
        photoUrl?.let { params["photoUrl"] = it }
        return performAuthRequest(
            action = "updateProfile",
            params = params
        )
    }

    override suspend fun updateEmail(newEmail: String): AuthResult {
        return performAuthRequest(
            action = "updateEmail",
            params = mapOf("newEmail" to newEmail)
        )
    }

    override suspend fun reloadUser(): AuthResult {
        return performAuthRequest(action = "reloadUser")
    }

    override suspend fun deleteAccount(): AuthResult {
        return performAuthRequest(action = "deleteAccount")
    }

    // Account linking
    override suspend fun linkWithGoogle(idToken: String): AuthResult {
        return performAuthRequest(
            action = "linkWithGoogle",
            params = mapOf("idToken" to idToken)
        )
    }

    override suspend fun linkWithApple(idToken: String): AuthResult {
        return performAuthRequest(
            action = "linkWithApple",
            params = mapOf("idToken" to idToken)
        )
    }

    override suspend fun linkWithFacebook(accessToken: String): AuthResult {
        return performAuthRequest(
            action = "linkWithFacebook",
            params = mapOf("accessToken" to accessToken)
        )
    }

    override suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult {
        return performAuthRequest(
            action = "linkWithEmailAndPassword",
            params = mapOf("email" to email, "password" to password)
        )
    }

    override suspend fun unlinkProvider(providerId: String): AuthResult {
        return performAuthRequest(
            action = "unlinkProvider",
            params = mapOf("providerId" to providerId)
        )
    }

    // Re-authentication
    override suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult {
        return performAuthRequest(
            action = "reauthenticateWithEmail",
            params = mapOf("email" to email, "password" to password)
        )
    }

    override suspend fun reauthenticateWithGoogle(idToken: String): AuthResult {
        return performAuthRequest(
            action = "reauthenticateWithGoogle",
            params = mapOf("idToken" to idToken)
        )
    }

    override suspend fun reauthenticateWithApple(idToken: String): AuthResult {
        return performAuthRequest(
            action = "reauthenticateWithApple",
            params = mapOf("idToken" to idToken)
        )
    }

    /**
     * Performs an auth request via notification bridge
     *
     * Think of this like sending a letter (notification) to Swift and waiting
     * for a reply letter (response notification) that has the same tracking number (requestId)
     */
    private suspend fun performAuthRequest(
        action: String,
        params: Map<String, Any> = emptyMap()
    ): AuthResult = suspendCancellableCoroutine { continuation ->
        val requestId = NSUUID().UUIDString
        var observer: Any? = null

        logDebug("FirebaseAuth", "Sending auth request: action=$action, requestId=$requestId")

        // Set up listener for response BEFORE sending request
        observer = center.addObserverForName(
            name = "AuthResponse",
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification: NSNotification? ->
            val responseInfo = notification?.userInfo as? Map<*, *> ?: return@addObserverForName
            val responseId = responseInfo["requestId"] as? String

            // Only process responses matching our request ID
            if (responseId != requestId) return@addObserverForName

            logDebug("FirebaseAuth", "Received auth response for requestId=$requestId")

            // Clean up observer
            observer?.let { center.removeObserver(it) }

            // Parse response
            val status = responseInfo["status"] as? String
            val result = when (status) {
                "success" -> {
                    val user = responseInfo.toAuthUser()
                    logDebug("FirebaseAuth", "Auth request successful")
                    AuthResult.Success(user ?: AuthUser(uid = "", isAnonymous = true))
                }
                else -> {
                    val errorMessage = responseInfo["errorMessage"] as? String
                        ?: "Unknown error"
                    val errorCode = responseInfo["errorCode"] as? String

                    logError("FirebaseAuth", "Auth request failed: code=$errorCode, message=$errorMessage")

                    AuthResult.Failure(
                        AuthError.Unknown(
                            Exception("[$errorCode] $errorMessage")
                        )
                    )
                }
            }

            // Resume coroutine with result
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }

        // Send the request
        val requestInfo = buildMap {
            put("requestId", requestId)
            put("action", action)
            putAll(params)
        }

        center.postNotificationName(
            aName = "AuthRequest",
            `object` = null,
            userInfo = requestInfo as Map<Any?, *>
        )

        // Clean up if coroutine is cancelled
        continuation.invokeOnCancellation {
            observer?.let { center.removeObserver(it) }
        }
    }
}

/**
 * Request Google ID token via native sign-in flow
 *
 * This is like requesting a Google Sign-In dialog to appear,
 * and waiting for the user to complete it
 */
actual suspend fun requestGoogleIdToken(): String? = suspendCancellableCoroutine { continuation ->
    val center = NSNotificationCenter.defaultCenter
    var observer: Any? = null

    logDebug("GoogleSignIn", "Requesting Google ID token")

    // Listen for completion
    observer = center.addObserverForName(
        name = "GoogleSignInCompleted",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification: NSNotification? ->
        val userInfo = notification?.userInfo as? Map<*, *>
        val idToken = userInfo?.get("idToken") as? String

        if (idToken != null) {
            logDebug("GoogleSignIn", "Successfully got Google ID token")
        } else {
            logError("GoogleSignIn", "Failed to get Google ID token (null)")
        }

        // Clean up
        observer?.let { center.removeObserver(it) }

        // Resume with result
        if (continuation.isActive) {
            continuation.resume(idToken)
        }
    }

    // Trigger the sign-in flow
    logDebug("GoogleSignIn", "Sending GoogleSignInRequest notification")
    center.postNotificationName(
        aName = "GoogleSignInRequest",
        `object` = null,
        userInfo = null
    )

    // Clean up on cancellation
    continuation.invokeOnCancellation {
        observer?.let { center.removeObserver(it) }
    }
}

actual suspend fun requestAppleIdToken(): String? = suspendCancellableCoroutine { continuation ->
    val center = NSNotificationCenter.defaultCenter
    var observer: Any? = null

    logDebug("AppleSignIn", "Requesting Apple ID token")

    // Listen for completion
    observer = center.addObserverForName(
        name = "AppleSignInCompleted",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification: NSNotification? ->
        val userInfo = notification?.userInfo as? Map<*, *>
        val idToken = userInfo?.get("idToken") as? String

        if (idToken != null) {
            logDebug("AppleSignIn", "Successfully got Apple ID token")
        } else {
            logError("AppleSignIn", "Failed to get Apple ID token (null)")
        }

        // Clean up
        observer?.let { center.removeObserver(it) }

        // Resume with result
        if (continuation.isActive) {
            continuation.resume(idToken)
        }
    }

    // Trigger the sign-in flow
    logDebug("AppleSignIn", "Sending AppleSignInRequest notification")
    center.postNotificationName(
        aName = "AppleSignInRequest",
        `object` = null,
        userInfo = null
    )

    // Clean up on cancellation
    continuation.invokeOnCancellation {
        observer?.let { center.removeObserver(it) }
    }
}

actual fun isAppleSignInAvailable(): Boolean = true

/**
 * Extension to safely convert notification userInfo to AuthUser
 */
private fun Map<*, *>.toAuthUser(): AuthUser? {
    val uid = this["uid"] as? String ?: return null

    // Handle NSNull case for uid (means no user)
    if (uid.isEmpty()) return null

    return AuthUser(
        uid = uid,
        displayName = this["displayName"] as? String,
        email = this["email"] as? String,
        photoUrl = this["photoUrl"] as? String,
        isAnonymous = (this["isAnonymous"] as? Boolean) ?: false,
        isEmailVerified = (this["isEmailVerified"] as? Boolean) ?: false,
        providerData = (this["providerData"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    )
}
