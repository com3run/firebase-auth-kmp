package az.random.firebaseauth

/**
 * Common auth models to keep the domain independent from platform SDKs.
 */

data class AuthUser(
    val uid: String,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val isEmailVerified: Boolean = false,
    val providerData: List<String> = emptyList(), // List of provider IDs (e.g., "google.com", "password")
)

sealed class AuthError {
    data object InvalidCredential : AuthError()
    data object InvalidEmailOrPassword : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object WeakPassword : AuthError()
    data object UserNotFound : AuthError()
    data object WrongPassword : AuthError()
    data object UserDisabled : AuthError()
    data object TooManyRequests : AuthError()
    data object EmailNotVerified : AuthError()
    data object RequiresRecentLogin : AuthError()
    data object ProviderAlreadyLinked : AuthError()
    data object NoSuchProvider : AuthError()
    data object InvalidEmail : AuthError()
    data class Network(val message: String? = null) : AuthError()
    data class Unknown(val throwable: Throwable? = null, val message: String? = null) : AuthError()
}

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Failure(val error: AuthError) : AuthResult()
}
