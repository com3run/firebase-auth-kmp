# Coding Guidelines

## Core Principles

### 1. Clean Architecture First
Always follow the established architecture pattern:
- **AuthBackend** (Interface) → Platform-agnostic contract
- **Platform Implementations** → Android/iOS specific Firebase code
- **AuthRepository** → High-level API with validation
- **UI Layer** → ViewModels and Compose screens

### 2. Platform Isolation
- Keep platform-specific code in `androidMain` and `iosMain`
- Use `expect`/`actual` pattern for platform abstraction
- Never expose platform types in common code
- All platform implementations must implement the same interface

### 3. Error Handling
- Use `AuthError` sealed class for all auth-related errors
- Return `AuthResult<T>` for operations that can fail
- Map platform-specific errors to `AuthError` types
- Never throw exceptions for expected auth failures

## Implementation Patterns

### Token-Based Authentication

The library expects OAuth tokens to be obtained externally:

```kotlin
// ✅ Correct: Repository accepts tokens as strings
suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser>

// ❌ Wrong: Don't handle token acquisition in repository
suspend fun signInWithGoogle(activity: Activity): AuthResult<AuthUser>
```

**Rationale**: Token acquisition requires platform-specific UI flows that vary by implementation.

### Auth State Management

Use `StateFlow` for reactive auth state:

```kotlin
// ✅ Correct: Expose auth state as StateFlow
interface AuthBackend {
    val authState: StateFlow<AuthUser?>
}

// ✅ Correct: Collect in UI
val authState by authRepository.authState.collectAsState()

// ❌ Wrong: Don't poll for auth state
suspend fun getCurrentUser(): AuthUser?
```

### Input Validation

Validation belongs in `AuthRepository`, not `AuthBackend`:

```kotlin
// ✅ Correct: AuthRepository validates before delegating
suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthUser> {
    if (email.isBlank()) return AuthResult.Failure(AuthError.InvalidCredential)
    if (password.length < 6) return AuthResult.Failure(AuthError.WeakPassword)
    return backend.signInWithEmail(email, password)
}

// ❌ Wrong: Don't validate in AuthBackend
// Backend assumes inputs are pre-validated
```

### Error Mapping

Platform implementations must map native errors to `AuthError`:

```kotlin
// ✅ Correct: Map Firebase errors to AuthError
catch (e: FirebaseAuthException) {
    val error = when (e.errorCode) {
        "ERROR_USER_NOT_FOUND" -> AuthError.UserNotFound
        "ERROR_WRONG_PASSWORD" -> AuthError.InvalidCredential
        "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EmailAlreadyInUse
        else -> AuthError.Unknown(e.message)
    }
    AuthResult.Failure(error)
}
```

## Test-Driven Development (TDD)

### Required Approach
1. **Write test first** that defines expected behavior
2. **Run test** and verify it fails
3. **Implement** minimum code to make test pass
4. **Refactor** while keeping tests green
5. **Never commit** code without passing tests

### Test Structure

```kotlin
// ✅ Correct: Use FakeAuthBackend for testing
class AuthRepositoryTest {
    private lateinit var fakeBackend: FakeAuthBackend
    private lateinit var repository: AuthRepository

    @BeforeTest
    fun setup() {
        fakeBackend = FakeAuthBackend()
        repository = AuthRepository(fakeBackend)
    }

    @Test
    fun `signInWithEmail should return success when valid credentials`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        fakeBackend.setAuthResult(AuthResult.Success(testUser))

        // Act
        val result = repository.signInWithEmail(email, password)

        // Assert
        assertTrue(result is AuthResult.Success)
    }
}
```

### Test Coverage Requirements
- All public methods in `AuthRepository` must have tests
- Both success and failure paths must be tested
- Edge cases (empty strings, null values, etc.) must be covered
- Error mapping must be verified

## Dependency Injection

### Koin Setup

```kotlin
// ✅ Correct: Use expect/actual for platform auth backend
val appModule = module {
    single<AuthBackend> { platformAuthBackend() }
    single { AuthRepository(get()) }
    factory { AuthViewModel(get()) }
}

// Platform-specific files
// androidMain
actual fun platformAuthBackend(): AuthBackend = AndroidFirebaseAuthBackend()

// iosMain
actual fun platformAuthBackend(): AuthBackend = IosFirebaseAuthBackend()
```

### Injection Best Practices
- Use `single` for stateful services (AuthBackend, AuthRepository)
- Use `factory` for ViewModels (new instance per injection)
- Never create platform implementations directly in common code
- Always inject dependencies through constructor

## Platform-Specific Guidelines

### Android

#### Activity References
```kotlin
// ✅ Correct: Set activity reference in onCreate
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.current = this
        // ...
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.current = null
    }
}
```

#### Firebase Integration
```kotlin
// ✅ Correct: Use Firebase Auth directly
class AndroidFirebaseAuthBackend : AuthBackend {
    private val auth = Firebase.auth

    override suspend fun signInWithEmail(email: String, password: String) =
        suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { /* ... */ }
                .addOnFailureListener { /* ... */ }
        }
}
```

### iOS

#### Notification-Based Bridge
```kotlin
// ✅ Correct: Use notification bridge for iOS
class IosFirebaseAuthBackend : AuthBackend {
    private val requestId = UUID.randomUUID().toString()

    override suspend fun signInWithEmail(email: String, password: String) =
        suspendCoroutine { continuation ->
            // Post request notification
            NSNotificationCenter.defaultCenter.postNotificationName(
                name = "AuthRequest",
                `object` = null,
                userInfo = mapOf(
                    "requestId" to requestId,
                    "action" to "signInWithEmail",
                    "email" to email,
                    "password" to password
                )
            )

            // Listen for response
            observeAuthResponse(requestId, continuation)
        }
}
```

## Code Style

### Naming Conventions
- **Classes**: PascalCase (e.g., `AuthRepository`, `AuthBackend`)
- **Functions**: camelCase (e.g., `signInWithEmail`, `resetPassword`)
- **Properties**: camelCase (e.g., `authState`, `currentUser`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `PROVIDER_GOOGLE`, `MIN_PASSWORD_LENGTH`)

### Kotlin Conventions
```kotlin
// ✅ Correct: Use sealed classes for fixed hierarchies
sealed class AuthError {
    data object InvalidCredential : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data class Unknown(val message: String?) : AuthError()
}

// ✅ Correct: Use data classes for models
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean,
    val providers: List<String>
)

// ✅ Correct: Use suspend functions for async operations
suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthUser>

// ❌ Wrong: Don't use callbacks
fun signInWithEmail(email: String, password: String, callback: (AuthResult<AuthUser>) -> Unit)
```

### Documentation
```kotlin
// ✅ Correct: Document public APIs
/**
 * Signs in a user with email and password.
 *
 * @param email The user's email address (must be valid format)
 * @param password The user's password (minimum 6 characters)
 * @return AuthResult.Success with AuthUser or AuthResult.Failure with AuthError
 */
suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthUser>
```

## Common Mistakes to Avoid

### 1. Breaking Platform Abstraction
```kotlin
// ❌ Wrong: Exposing Android types in common code
fun signInWithGoogle(context: Context): AuthResult<AuthUser>

// ✅ Correct: Keep platform types in platform code
fun signInWithGoogle(idToken: String): AuthResult<AuthUser>
```

### 2. Inconsistent Error Handling
```kotlin
// ❌ Wrong: Throwing exceptions for auth failures
suspend fun signInWithEmail(email: String, password: String): AuthUser {
    throw AuthException("Invalid credentials")
}

// ✅ Correct: Return AuthResult
suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthUser> {
    return AuthResult.Failure(AuthError.InvalidCredential)
}
```

### 3. Blocking Operations
```kotlin
// ❌ Wrong: Blocking main thread
fun signInWithEmail(email: String, password: String): AuthUser {
    return runBlocking { backend.signInWithEmail(email, password) }
}

// ✅ Correct: Use suspend functions
suspend fun signInWithEmail(email: String, password: String): AuthResult<AuthUser> {
    return backend.signInWithEmail(email, password)
}
```

### 4. Ignoring Auth State
```kotlin
// ❌ Wrong: Polling current user
fun isUserLoggedIn(): Boolean {
    return getCurrentUser() != null
}

// ✅ Correct: Observe auth state
val authState: StateFlow<AuthUser?>
```

## Important Implementation Notes

1. **Re-authentication Required**: Some operations (email change, password change, account deletion) may fail with `AuthError.RequiresRecentLogin`. Handle this by prompting user to re-authenticate.

2. **Account Linking**: When linking accounts, ensure providers are not already linked. Use `AuthUser.providers` to check existing providers.

3. **Email Verification**: After sign-up, always prompt users to verify email. Some operations may require verified email.

4. **Anonymous Account Conversion**: When converting anonymous accounts, be aware that anonymous account will be deleted if conversion fails.

5. **Provider Consistency**: Use correct provider IDs:
   - Google: `"google.com"`
   - Apple: `"apple.com"`
   - Facebook: `"facebook.com"`
   - Email/Password: `"password"`

## Performance Considerations

1. **StateFlow over Callbacks**: Use StateFlow for auth state to avoid memory leaks from unmanaged callbacks

2. **Suspend Functions**: Use suspend functions instead of blocking calls for better coroutine integration

3. **Platform-Specific Optimization**:
   - Android: Reuse Firebase Auth instance
   - iOS: Minimize notification overhead by batching requests when possible

4. **Testing Performance**: Keep tests fast by using FakeAuthBackend instead of real Firebase

## Security Best Practices

1. **Never log sensitive data** (passwords, tokens, email addresses in production)
2. **Validate all inputs** in AuthRepository before passing to backend
3. **Handle token expiration** gracefully
4. **Clear auth state** on sign-out
5. **Use secure storage** for refresh tokens (if implementing token management)
6. **Implement rate limiting** for password reset and other sensitive operations
7. **Follow Firebase security best practices** for configuration files
