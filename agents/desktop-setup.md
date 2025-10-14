# Desktop (JVM) Setup Guide

## Overview

The Firebase Auth KMP library now supports desktop JVM applications using Firebase Authentication REST API. This provides core authentication features on desktop without requiring platform-specific Firebase SDKs.

## Quick Start

### 1. Add Dependency

In your `build.gradle.kts`:

```kotlin
kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.1")
}
```

### 2. Firebase Configuration

Create `firebase-config.json` in your project root:

```json
{
  "apiKey": "AIzaSy...",
  "projectId": "your-project-id"
}
```

**Where to find these values:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings (⚙️ icon)
4. Under "General" tab, find "Web API Key" - this is your `apiKey`
5. Project ID is shown at the top of Project Settings

**Alternative**: Set environment variable `FIREBASE_API_KEY`

```bash
export FIREBASE_API_KEY="AIzaSy..."
./gradlew :composeApp:run
```

### 3. Initialize Authentication

```kotlin
import dev.com3run.firebaseauthkmp.*
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    // Initialize Koin with auth module
    startKoin {
        modules(module {
            single<AuthBackend> { platformAuthBackend() }
            single { AuthRepository(get()) }
        })
    }

    // Your desktop app code
}
```

## Supported Features

### ✅ Fully Supported

- **Email/Password Authentication**
  - Sign up with email and password
  - Sign in with email and password
  - Password reset via email
  - Password update

- **Anonymous Authentication**
  - Sign in anonymously

- **Email Verification**
  - Send verification email
  - Email verified status

- **Profile Management**
  - Update display name
  - Update photo URL
  - Update email address

- **Account Management**
  - Delete account
  - Sign out
  - Reload user data

- **Re-authentication**
  - Re-authenticate with email/password
  - Re-authenticate with OAuth tokens

### ⚠️ Requires External Implementation

- **OAuth Providers (Google, Apple, Facebook)**
  - Desktop OAuth requires browser-based flow
  - See [OAuth on Desktop](#oauth-on-desktop) section below

### ❌ Not Supported

- **Account Linking/Unlinking**
  - Firebase REST API has limited support for account linking
  - Will return `UnsupportedOperationException`

## Usage Examples

### Email/Password Authentication

```kotlin
val authRepository = get<AuthRepository>()

// Sign up
val signUpResult = authRepository.signUpWithEmailAndPassword(
    email = "user@example.com",
    password = "securePassword123"
)

when (signUpResult) {
    is AuthResult.Success -> {
        val user = signUpResult.data
        println("Signed up: ${user.email}")
    }
    is AuthResult.Failure -> {
        when (signUpResult.error) {
            AuthError.EmailAlreadyInUse -> println("Email already registered")
            AuthError.WeakPassword -> println("Password too weak")
            else -> println("Error: ${signUpResult.error}")
        }
    }
}

// Sign in
val signInResult = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "securePassword123"
)
```

### Anonymous Authentication

```kotlin
val result = authRepository.signInAnonymously()

when (result) {
    is AuthResult.Success -> {
        println("Signed in anonymously: ${result.data.uid}")
    }
    is AuthResult.Failure -> {
        println("Failed: ${result.error}")
    }
}
```

### Password Reset

```kotlin
val result = authRepository.sendPasswordResetEmail("user@example.com")

when (result) {
    is AuthResult.Success -> println("Password reset email sent")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Profile Updates

```kotlin
val result = authRepository.updateProfile(
    displayName = "John Doe",
    photoUrl = "https://example.com/photo.jpg"
)

when (result) {
    is AuthResult.Success -> println("Profile updated")
    is AuthResult.Failure -> {
        when (result.error) {
            AuthError.RequiresRecentLogin -> {
                // User needs to re-authenticate
                println("Please sign in again to update profile")
            }
            else -> println("Error: ${result.error}")
        }
    }
}
```

### Auth State Monitoring

```kotlin
val authRepository = get<AuthRepository>()

// Collect auth state in your UI
authRepository.authState.collect { user ->
    if (user != null) {
        println("User signed in: ${user.email}")
    } else {
        println("User signed out")
    }
}
```

## OAuth on Desktop

OAuth providers (Google, Apple, Facebook) require browser-based authentication flow on desktop. The library provides the infrastructure, but you must implement the OAuth flow yourself.

### Implementation Steps

1. **Open Browser for OAuth**
2. **Handle Redirect with Local Server**
3. **Extract Authorization Code**
4. **Exchange Code for ID Token**
5. **Sign In with ID Token**

### Example Implementation

```kotlin
import java.awt.Desktop
import java.net.ServerSocket
import java.net.URI

suspend fun googleSignInDesktop(): AuthResult {
    // 1. Open browser with OAuth URL
    val clientId = "YOUR_GOOGLE_CLIENT_ID"
    val redirectUri = "http://localhost:8080/callback"
    val oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id=$clientId&" +
            "redirect_uri=$redirectUri&" +
            "response_type=code&" +
            "scope=openid%20email%20profile"

    Desktop.getDesktop().browse(URI(oauthUrl))

    // 2. Start local server to handle callback
    val authCode = waitForCallback()

    // 3. Exchange code for ID token
    val idToken = exchangeCodeForToken(authCode, clientId, redirectUri)

    // 4. Sign in with Firebase
    val authRepository = get<AuthRepository>()
    return authRepository.signInWithGoogle(idToken)
}

fun waitForCallback(): String {
    val serverSocket = ServerSocket(8080)
    val socket = serverSocket.accept()
    val input = socket.getInputStream().bufferedReader().readLine()

    // Parse auth code from callback
    val code = extractCodeFromRequest(input)

    // Send success response
    val response = "HTTP/1.1 200 OK\r\n\r\nAuthentication successful! You can close this window."
    socket.getOutputStream().write(response.toByteArray())
    socket.close()
    serverSocket.close()

    return code
}

suspend fun exchangeCodeForToken(code: String, clientId: String, redirectUri: String): String {
    // Use HTTP client to exchange code for tokens
    // https://oauth2.googleapis.com/token
    // This requires your client secret and proper OAuth configuration
    TODO("Implement token exchange")
}
```

### Recommended Libraries for OAuth

- **Ktor Client** - For HTTP requests (already included)
- **kotlinx.serialization** - For JSON parsing (already included)
- **Java Desktop API** - For opening browser (built-in)

## Token Management

### Token Expiration

Firebase ID tokens expire after 1 hour. The library stores the refresh token, but automatic refresh is not implemented.

**Manual Token Refresh:**

```kotlin
val backend = get<AuthBackend>() as DesktopFirebaseAuthBackend
val result = backend.refreshIdToken()

when (result) {
    is AuthResult.Success -> println("Token refreshed")
    is AuthResult.Failure -> println("Refresh failed: ${result.error}")
}
```

### Token Storage

Currently, tokens are stored in memory only. For production apps, consider:

1. **Encrypted file storage**
2. **System keychain/credential manager**
3. **Secure preferences**

## Build & Run

### Run Desktop App

```bash
# Run desktop application
./gradlew :composeApp:run

# Build desktop distribution
./gradlew :composeApp:packageDistributionForCurrentOS

# Create platform-specific installers
./gradlew :composeApp:package
```

### Distribution Formats

- **macOS**: DMG
- **Windows**: MSI
- **Linux**: DEB

## Configuration Best Practices

### Security

1. **Never commit `firebase-config.json`** to version control
   ```gitignore
   firebase-config.json
   ```

2. **Use environment variables for CI/CD**
   ```bash
   export FIREBASE_API_KEY="..."
   ```

3. **Restrict API key** in Firebase Console:
   - Go to Project Settings → API Keys
   - Add application restrictions
   - Limit to specific domains/IPs

### Multi-Environment Setup

```kotlin
// Development config
val devConfig = FirebaseConfig(
    apiKey = System.getenv("FIREBASE_DEV_API_KEY") ?: loadFromFile("firebase-dev.json")
)

// Production config
val prodConfig = FirebaseConfig(
    apiKey = System.getenv("FIREBASE_PROD_API_KEY") ?: loadFromFile("firebase-prod.json")
)
```

## Troubleshooting

### API Key Not Found

**Error**: `Firebase API key not found`

**Solution**:
1. Create `firebase-config.json` in project root
2. Or set `FIREBASE_API_KEY` environment variable
3. Verify file is not in `.gitignore`

### Authentication Fails

**Error**: `INVALID_API_KEY`

**Solution**:
1. Verify API key is correct in Firebase Console
2. Check API key restrictions (remove restrictions for testing)
3. Ensure project ID matches

### Token Expired

**Error**: `TOKEN_EXPIRED` or `RequiresRecentLogin`

**Solution**:
```kotlin
// Refresh token manually
val backend = get<AuthBackend>() as DesktopFirebaseAuthBackend
backend.refreshIdToken()
```

### Network Errors

**Error**: `Network error` or connection timeout

**Solution**:
1. Check internet connection
2. Verify Firebase service status
3. Check firewall/proxy settings
4. Ensure HTTPS traffic is allowed

## Limitations

1. **No Offline Support**: REST API requires active internet connection
2. **Manual Token Refresh**: No automatic token refresh
3. **Limited Account Linking**: REST API doesn't fully support account linking
4. **OAuth Requires Custom Implementation**: Browser-based OAuth flow not built-in
5. **No Real-time Database**: Only authentication is supported

## Comparison: Desktop vs Mobile

| Feature | Android/iOS | Desktop |
|---------|-------------|---------|
| Email/Password | ✅ Native SDK | ✅ REST API |
| Anonymous Auth | ✅ Native SDK | ✅ REST API |
| OAuth (Google/Apple/FB) | ✅ Built-in | ⚠️ Manual |
| Account Linking | ✅ Full Support | ❌ Limited |
| Offline Support | ✅ Yes | ❌ No |
| Token Refresh | ✅ Automatic | ⚠️ Manual |
| Profile Management | ✅ Yes | ✅ Yes |

## Resources

- [Firebase Auth REST API Reference](https://firebase.google.com/docs/reference/rest/auth)
- [Compose Multiplatform Desktop](https://www.jetbrains.com/lp/compose-multiplatform/)
- [OAuth 2.0 for Desktop Apps](https://developers.google.com/identity/protocols/oauth2/native-app)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)

## Support

For issues specific to desktop implementation:
1. Check Firebase API key configuration
2. Verify internet connectivity
3. Review Firebase Console for error logs
4. Check REST API quotas and limits

For library issues:
- GitHub Issues: [Your Repository]
- Documentation: `/agents` folder in project root
