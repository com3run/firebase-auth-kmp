# Firebase Auth Library for Kotlin Multiplatform

A comprehensive, production-ready Firebase Authentication library for Kotlin Multiplatform projects supporting Android and iOS.

## Features

### Authentication Methods
- ✅ **Email & Password** - Sign up, sign in with email/password
- ✅ **Anonymous/Guest** - Sign in anonymously
- ✅ **Google Sign-In** - OAuth authentication with Google
- ✅ **Apple Sign-In** - OAuth authentication with Apple (iOS native support)
- ✅ **Facebook Sign-In** - OAuth authentication with Facebook

### User Management
- ✅ **Password Management**
  - Send password reset emails
  - Confirm password reset with code
  - Update current user password
- ✅ **Email Verification**
  - Send email verification
  - Apply action codes
- ✅ **Profile Management**
  - Update display name
  - Update photo URL
  - Update email address
  - Reload user data
  - Delete account

### Account Operations
- ✅ **Account Linking** - Link multiple auth providers to one account
- ✅ **Account Unlinking** - Unlink providers from account
- ✅ **Re-authentication** - Re-authenticate for sensitive operations
- ✅ **Auth State Management** - Real-time auth state with Kotlin Flow

### Error Handling
Comprehensive error types for better UX:
- `InvalidCredential`
- `InvalidEmailOrPassword`
- `EmailAlreadyInUse`
- `WeakPassword`
- `UserNotFound`
- `WrongPassword`
- `UserDisabled`
- `TooManyRequests`
- `EmailNotVerified`
- `RequiresRecentLogin`
- `ProviderAlreadyLinked`
- `NoSuchProvider`
- `InvalidEmail`
- `Network`
- `Unknown`

## Architecture

The library follows clean architecture principles with platform-agnostic interfaces:

```
┌─────────────────────────────────────┐
│         AuthRepository              │  ← Your app uses this
│  (Validation & High-level API)     │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│         AuthBackend (Interface)     │
└─────────────┬───────────────────────┘
              │
      ┌───────┴────────┐
      ▼                ▼
┌─────────────┐  ┌──────────────┐
│  Android    │  │     iOS      │
│  Firebase   │  │   Firebase   │
└─────────────┘  └──────────────┘
```

## Usage

### Basic Setup

```kotlin
// Initialize the auth repository
val authRepository = AuthRepository(
    backend = platformAuthBackend() // Platform-specific implementation
)
```

### Sign Up with Email & Password

```kotlin
suspend fun signUp(email: String, password: String) {
    when (val result = authRepository.signUpWithEmailAndPassword(email, password)) {
        is AuthResult.Success -> {
            println("Signed up: ${result.user.email}")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.EmailAlreadyInUse -> showError("Email already in use")
                AuthError.WeakPassword -> showError("Password is too weak")
                AuthError.InvalidEmail -> showError("Invalid email format")
                else -> showError("Sign up failed")
            }
        }
    }
}
```

### Sign In with Email & Password

```kotlin
suspend fun signIn(email: String, password: String) {
    when (val result = authRepository.signInWithEmailAndPassword(email, password)) {
        is AuthResult.Success -> {
            println("Signed in: ${result.user.uid}")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.UserNotFound -> showError("User not found")
                AuthError.WrongPassword -> showError("Incorrect password")
                AuthError.UserDisabled -> showError("Account disabled")
                else -> showError("Sign in failed")
            }
        }
    }
}
```

### Anonymous Sign In

```kotlin
suspend fun signInAnonymously() {
    when (val result = authRepository.signInAnonymously()) {
        is AuthResult.Success -> {
            println("Signed in anonymously: ${result.user.uid}")
        }
        is AuthResult.Failure -> {
            showError("Anonymous sign in failed")
        }
    }
}
```

### Google Sign In

```kotlin
suspend fun signInWithGoogle() {
    // Request Google ID token using platform-specific flow
    val idToken = requestGoogleIdToken() ?: run {
        showError("Google sign in cancelled")
        return
    }

    when (val result = authRepository.signInWithGoogle(idToken)) {
        is AuthResult.Success -> {
            println("Signed in with Google: ${result.user.email}")
        }
        is AuthResult.Failure -> {
            showError("Google sign in failed")
        }
    }
}
```

### Apple Sign In

```kotlin
suspend fun signInWithApple() {
    val idToken = requestAppleIdToken() ?: run {
        showError("Apple sign in cancelled")
        return
    }

    when (val result = authRepository.signInWithApple(idToken)) {
        is AuthResult.Success -> {
            println("Signed in with Apple: ${result.user.uid}")
        }
        is AuthResult.Failure -> {
            showError("Apple sign in failed")
        }
    }
}
```

### Password Reset

```kotlin
suspend fun resetPassword(email: String) {
    when (val result = authRepository.sendPasswordResetEmail(email)) {
        is AuthResult.Success -> {
            showMessage("Password reset email sent")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.UserNotFound -> showError("No account with this email")
                AuthError.InvalidEmail -> showError("Invalid email address")
                else -> showError("Failed to send reset email")
            }
        }
    }
}
```

### Email Verification

```kotlin
suspend fun sendVerificationEmail() {
    when (val result = authRepository.sendEmailVerification()) {
        is AuthResult.Success -> {
            showMessage("Verification email sent")
        }
        is AuthResult.Failure -> {
            showError("Failed to send verification email")
        }
    }
}
```

### Update Profile

```kotlin
suspend fun updateUserProfile(name: String, photoUrl: String?) {
    when (val result = authRepository.updateProfile(displayName = name, photoUrl = photoUrl)) {
        is AuthResult.Success -> {
            println("Profile updated: ${result.user.displayName}")
        }
        is AuthResult.Failure -> {
            showError("Failed to update profile")
        }
    }
}
```

### Update Password

```kotlin
suspend fun changePassword(newPassword: String) {
    when (val result = authRepository.updatePassword(newPassword)) {
        is AuthResult.Success -> {
            showMessage("Password updated successfully")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.RequiresRecentLogin -> {
                    showError("Please sign in again to change password")
                }
                AuthError.WeakPassword -> showError("Password is too weak")
                else -> showError("Failed to update password")
            }
        }
    }
}
```

### Link Account with Provider

```kotlin
suspend fun linkGoogleAccount() {
    val idToken = requestGoogleIdToken() ?: return

    when (val result = authRepository.linkWithGoogle(idToken)) {
        is AuthResult.Success -> {
            showMessage("Google account linked")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.ProviderAlreadyLinked -> {
                    showError("Google account already linked")
                }
                AuthError.RequiresRecentLogin -> {
                    showError("Please sign in again to link account")
                }
                else -> showError("Failed to link account")
            }
        }
    }
}
```

### Unlink Provider

```kotlin
suspend fun unlinkGoogle() {
    when (val result = authRepository.unlinkProvider("google.com")) {
        is AuthResult.Success -> {
            showMessage("Google account unlinked")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.NoSuchProvider -> showError("Google not linked")
                else -> showError("Failed to unlink account")
            }
        }
    }
}
```

### Observe Auth State

```kotlin
// In your ViewModel or Composable
authRepository.authState.collectAsState().value?.let { user ->
    Text("Signed in as: ${user.email ?: user.uid}")
} ?: Text("Not signed in")
```

### Sign Out

```kotlin
suspend fun signOut() {
    authRepository.signOut()
}
```

### Delete Account

```kotlin
suspend fun deleteAccount() {
    when (val result = authRepository.deleteAccount()) {
        is AuthResult.Success -> {
            showMessage("Account deleted")
        }
        is AuthResult.Failure -> {
            when (result.error) {
                AuthError.RequiresRecentLogin -> {
                    showError("Please sign in again to delete account")
                }
                else -> showError("Failed to delete account")
            }
        }
    }
}
```

## Android Setup

### MainActivity.kt

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set activity reference for Google Sign-In
        ActivityHolder.current = this

        setContent {
            App()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GoogleSignInInterop.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.current = null
    }
}
```

## iOS Setup

The iOS implementation uses a notification-based bridge to communicate with Swift Firebase SDK. You'll need to implement the Swift side:

### SwiftAuthBridge.swift

```swift
import FirebaseAuth
import FirebaseCore

class FirebaseAuthBridge {
    init() {
        // Listen for auth state changes
        Auth.auth().addStateDidChangeListener { auth, user in
            self.notifyAuthStateChange(user: user)
        }

        // Listen for auth requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleAuthRequest),
            name: NSNotification.Name("AuthRequest"),
            object: nil
        )

        // Listen for Google Sign-In requests
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleGoogleSignInRequest),
            name: NSNotification.Name("GoogleSignInRequest"),
            object: nil
        )
    }

    @objc func handleAuthRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let requestId = userInfo["requestId"] as? String,
              let action = userInfo["action"] as? String else {
            return
        }

        Task {
            let result = await performAuthAction(action: action, params: userInfo)
            notifyAuthResponse(requestId: requestId, result: result)
        }
    }

    private func performAuthAction(action: String, params: [AnyHashable: Any]) async -> AuthResult {
        switch action {
        case "anonymous":
            return await signInAnonymously()
        case "signUpWithEmailAndPassword":
            guard let email = params["email"] as? String,
                  let password = params["password"] as? String else {
                return .failure(error: "Missing parameters")
            }
            return await signUpWithEmail(email: email, password: password)
        case "signInWithEmailAndPassword":
            guard let email = params["email"] as? String,
                  let password = params["password"] as? String else {
                return .failure(error: "Missing parameters")
            }
            return await signInWithEmail(email: email, password: password)
        case "sendPasswordResetEmail":
            guard let email = params["email"] as? String else {
                return .failure(error: "Missing email")
            }
            return await sendPasswordReset(email: email)
        // ... implement other actions
        default:
            return .failure(error: "Unknown action")
        }
    }

    private func notifyAuthResponse(requestId: String, result: AuthResult) {
        var response: [String: Any] = ["requestId": requestId]

        switch result {
        case .success(let user):
            response["status"] = "success"
            response["uid"] = user.uid
            response["email"] = user.email ?? ""
            response["displayName"] = user.displayName ?? ""
            response["photoURL"] = user.photoURL?.absoluteString ?? ""
            response["isAnonymous"] = user.isAnonymous
        case .failure(let error):
            response["status"] = "failure"
            response["errorMessage"] = error
        }

        NotificationCenter.default.post(
            name: NSNotification.Name("AuthResponse"),
            object: nil,
            userInfo: response
        )
    }

    private func notifyAuthStateChange(user: User?) {
        var userInfo: [String: Any] = [:]

        if let user = user {
            userInfo["uid"] = user.uid
            userInfo["email"] = user.email ?? ""
            userInfo["displayName"] = user.displayName ?? ""
            userInfo["photoUrl"] = user.photoURL?.absoluteString ?? ""
            userInfo["isAnonymous"] = user.isAnonymous
        }

        NotificationCenter.default.post(
            name: NSNotification.Name("AuthState"),
            object: nil,
            userInfo: userInfo
        )
    }
}
```

## Testing

The library includes a test for `AuthRepository`. To add more tests, create a fake implementation of `AuthBackend`:

```kotlin
class FakeAuthBackend : AuthBackend {
    private val _authState = MutableStateFlow<AuthUser?>(null)
    override val authState: StateFlow<AuthUser?> = _authState

    override suspend fun signInAnonymously(): AuthResult {
        val user = AuthUser(uid = "fake-uid", isAnonymous = true)
        _authState.value = user
        return AuthResult.Success(user)
    }

    // Implement other methods for testing...
}
```

## Provider IDs

For linking/unlinking operations, use these provider IDs:
- Google: `"google.com"`
- Apple: `"apple.com"`
- Facebook: `"facebook.com"`
- Email/Password: `"password"`

## Best Practices

1. **Handle Re-authentication**: Sensitive operations (delete account, change password, update email) may require recent login. Catch `RequiresRecentLogin` error and prompt user to re-authenticate.

2. **Email Verification**: After sign up, send verification email and check `user.emailVerified` before granting access to sensitive features.

3. **Error Handling**: Always handle specific error types to provide better user feedback.

4. **State Management**: Use `authRepository.authState` Flow to reactively update UI based on auth state.

5. **Account Linking**: Allow users to link multiple providers for easier sign-in options.

## License

MIT License

## Roadmap

- [ ] Phone authentication
- [ ] Custom auth tokens
- [ ] Multi-factor authentication
- [ ] Desktop (JVM) support
- [ ] Web support
