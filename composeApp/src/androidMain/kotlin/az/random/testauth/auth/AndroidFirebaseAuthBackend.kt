package az.random.testauth.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android implementation of AuthBackend backed by Firebase Auth.
 *
 * This class expects FirebaseApp to be initialized (either via google-services plugin
 * with a google-services.json present or via programmatic FirebaseOptions).
 *
 * NOTE: The UI/token acquisition for Google/Facebook/Apple is intentionally left to
 * the caller. Pass the obtained ID/Access token strings to these methods; this class
 * only exchanges them for Firebase credentials.
 */
class AndroidFirebaseAuthBackend(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthBackend {

    private val _authState = MutableStateFlow<AuthUser?>(auth.currentUser?.toAuthUser())
    override val authState: StateFlow<AuthUser?> = _authState

    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authState.value = firebaseAuth.currentUser?.toAuthUser()
    }

    init {
        auth.addAuthStateListener(listener)
    }

    override suspend fun signInAnonymously(): AuthResult =
        runCatching {
            Log.d("FirebaseAuth", "Attempting anonymous sign in")
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw IllegalStateException("Firebase returned null user")
            Log.d("FirebaseAuth", "Anonymous sign in successful: ${user.uid}")
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e ->
            Log.e("FirebaseAuth", "Anonymous sign in failed: ${e.message}", e)
            AuthResult.Failure(mapError(e))
        }

    override suspend fun signInWithGoogle(idToken: String): AuthResult =
        runCatching {
            Log.d("FirebaseAuth", "Attempting Google sign in with idToken")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = signInWithCredential(credential)
            Log.d("FirebaseAuth", "Google sign in successful")
            result
        }.getOrElse { e ->
            Log.e("FirebaseAuth", "Google sign in failed: ${e.message}", e)
            AuthResult.Failure(mapError(e))
        }

    override suspend fun signInWithApple(idToken: String): AuthResult =
        runCatching {
            // Generic OIDC via Apple provider. This assumes you've obtained the ID token elsewhere.
            val credential = OAuthProvider.newCredentialBuilder("apple.com")
                .setIdToken(idToken)
                .build()
            signInWithCredential(credential)
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun signInWithFacebook(accessToken: String): AuthResult =
        runCatching {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            signInWithCredential(credential)
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult =
        runCatching {
            Log.d("FirebaseAuth", "Attempting sign up with email: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw IllegalStateException("Firebase returned null user")
            Log.d("FirebaseAuth", "Sign up successful: ${user.email}")
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e ->
            Log.e("FirebaseAuth", "Sign up failed: ${e.message}", e)
            AuthResult.Failure(mapError(e))
        }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult =
        runCatching {
            Log.d("FirebaseAuth", "Attempting sign in with email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw IllegalStateException("Firebase returned null user")
            Log.d("FirebaseAuth", "Sign in successful: ${user.email}")
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e ->
            Log.e("FirebaseAuth", "Sign in failed: ${e.message}", e)
            AuthResult.Failure(mapError(e))
        }

    override suspend fun signOut() {
        auth.signOut()
        // Listener will update the state, but set defensively as well
        _authState.value = auth.currentUser?.toAuthUser()
    }

    // Password management
    override suspend fun sendPasswordResetEmail(email: String): AuthResult =
        runCatching {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(AuthUser(uid = "", isAnonymous = true)) // No user data needed for this operation
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult =
        runCatching {
            auth.confirmPasswordReset(code, newPassword).await()
            AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun updatePassword(newPassword: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            user.updatePassword(newPassword).await()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    // Email verification
    override suspend fun sendEmailVerification(): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            user.sendEmailVerification().await()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun applyActionCode(code: String): AuthResult =
        runCatching {
            auth.applyActionCode(code).await()
            AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    // Profile management
    override suspend fun updateProfile(displayName: String?, photoUrl: String?): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
            }.build()
            user.updateProfile(profileUpdates).await()
            user.reload().await()
            _authState.value = user.toAuthUser()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun updateEmail(newEmail: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            user.updateEmail(newEmail).await()
            user.reload().await()
            _authState.value = user.toAuthUser()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun reloadUser(): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            user.reload().await()
            _authState.value = user.toAuthUser()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun deleteAccount(): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            user.delete().await()
            _authState.value = null
            AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    // Account linking
    override suspend fun linkWithGoogle(idToken: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw IllegalStateException("Firebase returned null user")
            _authState.value = linkedUser.toAuthUser()
            AuthResult.Success(linkedUser.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun linkWithApple(idToken: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = OAuthProvider.newCredentialBuilder("apple.com")
                .setIdToken(idToken)
                .build()
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw IllegalStateException("Firebase returned null user")
            _authState.value = linkedUser.toAuthUser()
            AuthResult.Success(linkedUser.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun linkWithFacebook(accessToken: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw IllegalStateException("Firebase returned null user")
            _authState.value = linkedUser.toAuthUser()
            AuthResult.Success(linkedUser.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw IllegalStateException("Firebase returned null user")
            _authState.value = linkedUser.toAuthUser()
            AuthResult.Success(linkedUser.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun unlinkProvider(providerId: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val result = user.unlink(providerId).await()
            val unlinkedUser = result.user ?: throw IllegalStateException("Firebase returned null user")
            _authState.value = unlinkedUser.toAuthUser()
            AuthResult.Success(unlinkedUser.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    // Re-authentication
    override suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun reauthenticateWithGoogle(idToken: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            user.reauthenticate(credential).await()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    override suspend fun reauthenticateWithApple(idToken: String): AuthResult =
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("No authenticated user")
            val credential = OAuthProvider.newCredentialBuilder("apple.com")
                .setIdToken(idToken)
                .build()
            user.reauthenticate(credential).await()
            AuthResult.Success(user.toAuthUser())
        }.getOrElse { e -> AuthResult.Failure(mapError(e)) }

    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Firebase returned null user")
        return AuthResult.Success(user.toAuthUser())
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser =
    AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString(),
        isAnonymous = isAnonymous,
        isEmailVerified = isEmailVerified,
        providerData = providerData.mapNotNull { it.providerId },
    )

private fun mapError(throwable: Throwable): AuthError {
    Log.d("FirebaseAuth", "Mapping error: ${throwable::class.simpleName} - ${throwable.message}")
    return when (throwable) {
        is CancellationException -> AuthError.Unknown(throwable)
        is FirebaseNetworkException -> AuthError.Network(throwable.message)
        is FirebaseAuthException -> {
            Log.d("FirebaseAuth", "Firebase error code: ${throwable.errorCode}")
            when (throwable.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EmailAlreadyInUse
                "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
                "ERROR_USER_NOT_FOUND" -> AuthError.UserNotFound
                "ERROR_WRONG_PASSWORD" -> AuthError.WrongPassword
                "ERROR_USER_DISABLED" -> AuthError.UserDisabled
                "ERROR_TOO_MANY_REQUESTS" -> AuthError.TooManyRequests
                "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
                "ERROR_INVALID_CREDENTIAL" -> AuthError.InvalidCredential
                "ERROR_REQUIRES_RECENT_LOGIN" -> AuthError.RequiresRecentLogin
                "ERROR_PROVIDER_ALREADY_LINKED" -> AuthError.ProviderAlreadyLinked
                "ERROR_NO_SUCH_PROVIDER" -> AuthError.NoSuchProvider
                else -> {
                    Log.w("FirebaseAuth", "Unmapped Firebase error code: ${throwable.errorCode}")
                    AuthError.Unknown(throwable, throwable.message)
                }
            }
        }
        else -> AuthError.Unknown(throwable)
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { e ->
        if (cont.isActive) cont.resumeWithException(e)
    }
}
