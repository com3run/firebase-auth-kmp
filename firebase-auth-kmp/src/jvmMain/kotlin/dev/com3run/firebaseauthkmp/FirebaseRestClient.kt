package dev.com3run.firebaseauthkmp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HTTP client for Firebase Authentication REST API
 * https://firebase.google.com/docs/reference/rest/auth
 */
class FirebaseRestClient(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
    }

    private val baseUrl = "https://identitytoolkit.googleapis.com/v1/accounts"

    // Sign up with email and password
    suspend fun signUp(email: String, password: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:signUp") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(email, password, returnSecureToken = true))
        }
        return response.body()
    }

    // Sign in with email and password
    suspend fun signInWithPassword(email: String, password: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:signInWithPassword") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(SignInRequest(email, password, returnSecureToken = true))
        }
        return response.body()
    }

    // Sign in anonymously
    suspend fun signInAnonymously(): FirebaseAuthResponse {
        val response = client.post("$baseUrl:signUp") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(AnonymousSignInRequest(returnSecureToken = true))
        }
        return response.body()
    }

    // Sign in with OAuth credential (Google, Apple, Facebook)
    suspend fun signInWithIdp(idToken: String, providerId: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:signInWithIdp") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(IdpSignInRequest(
                postBody = "id_token=$idToken&providerId=$providerId",
                requestUri = "http://localhost",
                returnSecureToken = true
            ))
        }
        return response.body()
    }

    // Get user account info
    suspend fun getAccountInfo(idToken: String): AccountInfoResponse {
        val response = client.post("$baseUrl:lookup") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(LookupRequest(idToken))
        }
        return response.body()
    }

    // Update user profile
    suspend fun updateProfile(idToken: String, displayName: String? = null, photoUrl: String? = null): FirebaseAuthResponse {
        val response = client.post("$baseUrl:update") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(
                idToken = idToken,
                displayName = displayName,
                photoUrl = photoUrl,
                returnSecureToken = true
            ))
        }
        return response.body()
    }

    // Update email
    suspend fun updateEmail(idToken: String, newEmail: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:update") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(UpdateEmailRequest(
                idToken = idToken,
                email = newEmail,
                returnSecureToken = true
            ))
        }
        return response.body()
    }

    // Update password
    suspend fun updatePassword(idToken: String, newPassword: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:update") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(UpdatePasswordRequest(
                idToken = idToken,
                password = newPassword,
                returnSecureToken = true
            ))
        }
        return response.body()
    }

    // Delete account
    suspend fun deleteAccount(idToken: String) {
        client.post("$baseUrl:delete") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(DeleteAccountRequest(idToken))
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): OobCodeResponse {
        val response = client.post("$baseUrl:sendOobCode") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(SendOobCodeRequest(
                requestType = "PASSWORD_RESET",
                email = email
            ))
        }
        return response.body()
    }

    // Send email verification
    suspend fun sendEmailVerification(idToken: String): OobCodeResponse {
        val response = client.post("$baseUrl:sendOobCode") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(SendEmailVerificationRequest(
                requestType = "VERIFY_EMAIL",
                idToken = idToken
            ))
        }
        return response.body()
    }

    // Confirm password reset
    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): FirebaseAuthResponse {
        val response = client.post("$baseUrl:resetPassword") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(ConfirmPasswordResetRequest(
                oobCode = oobCode,
                newPassword = newPassword
            ))
        }
        return response.body()
    }

    // Refresh ID token
    suspend fun refreshToken(refreshToken: String): RefreshTokenResponse {
        val response = client.post("https://securetoken.googleapis.com/v1/token") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(
                grantType = "refresh_token",
                refreshToken = refreshToken
            ))
        }
        return response.body()
    }

    fun close() {
        client.close()
    }
}

// Request/Response models

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean
)

@Serializable
data class AnonymousSignInRequest(
    val returnSecureToken: Boolean
)

@Serializable
data class IdpSignInRequest(
    val postBody: String,
    val requestUri: String,
    val returnSecureToken: Boolean
)

@Serializable
data class LookupRequest(
    val idToken: String
)

@Serializable
data class UpdateProfileRequest(
    val idToken: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val returnSecureToken: Boolean = true
)

@Serializable
data class UpdateEmailRequest(
    val idToken: String,
    val email: String,
    val returnSecureToken: Boolean
)

@Serializable
data class UpdatePasswordRequest(
    val idToken: String,
    val password: String,
    val returnSecureToken: Boolean
)

@Serializable
data class DeleteAccountRequest(
    val idToken: String
)

@Serializable
data class SendOobCodeRequest(
    val requestType: String,
    val email: String
)

@Serializable
data class SendEmailVerificationRequest(
    val requestType: String,
    val idToken: String
)

@Serializable
data class ConfirmPasswordResetRequest(
    val oobCode: String,
    val newPassword: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("grant_type")
    val grantType: String,
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class FirebaseAuthResponse(
    val idToken: String? = null,
    val email: String? = null,
    val refreshToken: String? = null,
    val expiresIn: String? = null,
    val localId: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val emailVerified: Boolean? = null,
    val error: FirebaseError? = null
)

@Serializable
data class AccountInfoResponse(
    val users: List<UserInfo>? = null,
    val error: FirebaseError? = null
)

@Serializable
data class UserInfo(
    val localId: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,
    val providerUserInfo: List<ProviderInfo>? = null
)

@Serializable
data class ProviderInfo(
    val providerId: String,
    val federatedId: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
)

@Serializable
data class OobCodeResponse(
    val email: String? = null,
    val error: FirebaseError? = null
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: String? = null,
    val error: FirebaseError? = null
)

@Serializable
data class FirebaseError(
    val code: Int? = null,
    val message: String? = null,
    val errors: List<ErrorDetail>? = null
)

@Serializable
data class ErrorDetail(
    val message: String? = null,
    val domain: String? = null,
    val reason: String? = null
)
