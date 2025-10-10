package dev.com3run.firebaseauthkmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private class FakeAuthBackend : AuthBackend {
        private val _state = MutableStateFlow<AuthUser?>(null)
        override val authState: StateFlow<AuthUser?> = _state

        var lastCall: String? = null
        var lastToken: String? = null

        override suspend fun signInAnonymously(): AuthResult {
            lastCall = "anonymous"
            val user = AuthUser(uid = "anon-uid", isAnonymous = true)
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signInWithGoogle(idToken: String): AuthResult {
            lastCall = "google"; lastToken = idToken
            val user = AuthUser(uid = "google-uid", isAnonymous = false, email = "g@example.com")
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signInWithApple(idToken: String): AuthResult {
            lastCall = "apple"; lastToken = idToken
            val user = AuthUser(uid = "apple-uid", isAnonymous = false, email = "a@example.com")
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signInWithFacebook(accessToken: String): AuthResult {
            lastCall = "facebook"; lastToken = accessToken
            val user = AuthUser(uid = "fb-uid", isAnonymous = false)
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult {
            lastCall = "signUp"
            val user = AuthUser(uid = "email-uid", isAnonymous = false, email = email)
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
            lastCall = "signIn"
            val user = AuthUser(uid = "email-uid", isAnonymous = false, email = email)
            _state.value = user
            return AuthResult.Success(user)
        }

        override suspend fun signOut() {
            lastCall = "signOut"
            _state.value = null
        }

        // Password management
        override suspend fun sendPasswordResetEmail(email: String): AuthResult {
            lastCall = "sendPasswordResetEmail"
            return AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun confirmPasswordReset(code: String, newPassword: String): AuthResult {
            lastCall = "confirmPasswordReset"
            return AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun updatePassword(newPassword: String): AuthResult {
            lastCall = "updatePassword"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        // Email verification
        override suspend fun sendEmailVerification(): AuthResult {
            lastCall = "sendEmailVerification"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun applyActionCode(code: String): AuthResult {
            lastCall = "applyActionCode"
            return AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }

        // Profile management
        override suspend fun updateProfile(displayName: String?, photoUrl: String?): AuthResult {
            lastCall = "updateProfile"
            val currentUser = _state.value ?: AuthUser(uid = "", isAnonymous = true)
            val updated = currentUser.copy(displayName = displayName, photoUrl = photoUrl)
            _state.value = updated
            return AuthResult.Success(updated)
        }

        override suspend fun updateEmail(newEmail: String): AuthResult {
            lastCall = "updateEmail"
            val currentUser = _state.value ?: AuthUser(uid = "", isAnonymous = true)
            val updated = currentUser.copy(email = newEmail)
            _state.value = updated
            return AuthResult.Success(updated)
        }

        override suspend fun reloadUser(): AuthResult {
            lastCall = "reloadUser"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun deleteAccount(): AuthResult {
            lastCall = "deleteAccount"
            _state.value = null
            return AuthResult.Success(AuthUser(uid = "", isAnonymous = true))
        }

        // Account linking
        override suspend fun linkWithGoogle(idToken: String): AuthResult {
            lastCall = "linkWithGoogle"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun linkWithApple(idToken: String): AuthResult {
            lastCall = "linkWithApple"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun linkWithFacebook(accessToken: String): AuthResult {
            lastCall = "linkWithFacebook"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun linkWithEmailAndPassword(email: String, password: String): AuthResult {
            lastCall = "linkWithEmailAndPassword"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun unlinkProvider(providerId: String): AuthResult {
            lastCall = "unlinkProvider"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        // Re-authentication
        override suspend fun reauthenticateWithEmail(email: String, password: String): AuthResult {
            lastCall = "reauthenticateWithEmail"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun reauthenticateWithGoogle(idToken: String): AuthResult {
            lastCall = "reauthenticateWithGoogle"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }

        override suspend fun reauthenticateWithApple(idToken: String): AuthResult {
            lastCall = "reauthenticateWithApple"
            return AuthResult.Success(_state.value ?: AuthUser(uid = "", isAnonymous = true))
        }
    }

    @Test
    fun signInAnonymously_updatesStateAndReturnsSuccess() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        assertNull(repo.authState.value)
        val result = repo.signInAnonymously()
        assertIs<AuthResult.Success>(result)
        assertEquals("anon-uid", result.user.uid)
        assertEquals(true, result.user.isAnonymous)
        assertEquals("anonymous", backend.lastCall)
        assertEquals("anon-uid", repo.authState.value?.uid)
    }

    @Test
    fun signInWithGoogle_emptyToken_returnsInvalidCredential_andDoesNotCallBackend() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        val result = repo.signInWithGoogle("")
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredential>(result.error)
        assertNull(backend.lastCall)
        assertNull(repo.authState.value)
    }

    @Test
    fun signInWithGoogle_validToken_callsBackendAndUpdatesState() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        val result = repo.signInWithGoogle("valid-token")
        assertIs<AuthResult.Success>(result)
        assertEquals("google-uid", result.user.uid)
        assertEquals("google", backend.lastCall)
        assertEquals("valid-token", backend.lastToken)
        assertEquals("google-uid", repo.authState.value?.uid)
    }

    @Test
    fun signInWithApple_emptyToken_isInvalid() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        val result = repo.signInWithApple(" ")
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredential>(result.error)
        assertNull(backend.lastCall)
    }

    @Test
    fun signInWithFacebook_emptyToken_isInvalid() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        val result = repo.signInWithFacebook("")
        assertIs<AuthResult.Failure>(result)
        assertIs<AuthError.InvalidCredential>(result.error)
        assertNull(backend.lastCall)
    }

    @Test
    fun signOut_clearsState() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        repo.signInAnonymously()
        assertIs<AuthUser>(repo.authState.value)
        repo.signOut()
        assertNull(repo.authState.value)
        assertEquals("signOut", backend.lastCall)
    }

    @Test
    fun authState_emitsInitialNull_thenUserAfterSignIn() = runTest {
        val backend = FakeAuthBackend()
        val repo = AuthRepository(backend)

        val initial = repo.authState.first()
        assertNull(initial)

        repo.signInWithFacebook("fb-token")
        val current = repo.authState.value
        assertEquals("fb-uid", current?.uid)
    }
}
