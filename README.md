# Firebase Auth KMP

[![](https://jitpack.io/v/com3run/testauth.svg)](https://jitpack.io/#com3run/testauth)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Kotlin Multiplatform library that provides Firebase Authentication for Android and iOS with a unified API.

## Features

- ✅ **Cross-platform**: Single codebase for Android & iOS
- 🔐 **Authentication Methods**:
  - Email/Password (Sign up & Sign in)
  - Google Sign-In
  - Apple Sign-In (iOS)
  - Anonymous Authentication
  - Facebook Sign-In (coming soon)
- 🔄 **Real-time Auth State**: Flow-based auth state monitoring
- 🧪 **Testable**: Includes FakeAuthBackend for unit testing
- 📱 **Platform-optimized**: Uses native Firebase SDKs on both platforms

## Installation

### Step 1: Add JitPack Repository

Add JitPack to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add this
    }
}
```

### Step 2: Add Dependency

In your module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.com3run:testauth:1.0.0")
        }
    }
}
```

## Quick Start

### Android Setup

1. Add `google-services.json` to your `composeApp/` directory
2. Set up Activity reference in your `MainActivity`:

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import dev.com3run.firebaseauthkmp.ActivityHolder
import dev.com3run.firebaseauthkmp.GoogleSignInInterop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.current = this

        setContent {
            // Your app content
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.current = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GoogleSignInInterop.onActivityResult(requestCode, resultCode, data)
    }
}
```

### iOS Setup

1. Add `GoogleService-Info.plist` to your iOS app
2. Create `FirebaseAuthBridge.swift` (see [LIBRARY_INTEGRATION.md](LIBRARY_INTEGRATION.md) for full code)
3. Initialize in AppDelegate:

```swift
import Firebase

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        FirebaseAuthBridge.shared.start()
        return true
    }
}
```

## Usage

### Basic Authentication

```kotlin
import dev.com3run.firebaseauthkmp.AuthRepository
import dev.com3run.firebaseauthkmp.platformAuthBackend

// Create auth repository
val authRepository = AuthRepository(platformAuthBackend())

// Listen to auth state
authRepository.authState.collect { user ->
    if (user != null) {
        println("Signed in: ${user.email}")
    } else {
        println("Signed out")
    }
}
```

### Sign In with Email/Password

```kotlin
val result = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "password123"
)

when (result) {
    is AuthResult.Success -> println("Welcome ${result.user.displayName}!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Sign In with Google

```kotlin
// Request ID token using platform-specific flow
val idToken = requestGoogleIdToken()

if (idToken != null) {
    val result = authRepository.signInWithGoogle(idToken)
    when (result) {
        is AuthResult.Success -> println("Google sign-in successful!")
        is AuthResult.Failure -> println("Error: ${result.error}")
    }
}
```

### Sign In Anonymously

```kotlin
val result = authRepository.signInAnonymously()

when (result) {
    is AuthResult.Success -> println("Signed in as guest!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Sign Out

```kotlin
authRepository.signOut()
```

## Documentation

- [Library Integration Guide](LIBRARY_INTEGRATION.md) - Complete setup instructions
- [Maven Publishing Guide](MAVEN_PUBLISH.md) - How to publish the library
- [Usage Examples](USAGE_EXAMPLES.md) - More code examples

## Architecture

The library uses a clean architecture approach:

- **AuthRepository**: High-level API for authentication operations
- **AuthBackend**: Platform-specific interface (Android/iOS implementations)
- **AuthModels**: Common data models (AuthUser, AuthResult, AuthError)

### iOS Bridge Architecture

On iOS, the library uses an elegant notification-based bridge between Kotlin and Swift:
1. Kotlin sends requests via NSNotificationCenter
2. Swift receives notifications and calls native Firebase Auth SDK
3. Swift sends responses back via NSNotificationCenter
4. Kotlin resumes suspended coroutines with results

This approach keeps the UI thread responsive and provides a seamless async experience.

## Testing

The library includes `FakeAuthBackend` for unit testing:

```kotlin
import dev.com3run.firebaseauthkmp.FakeAuthBackend
import dev.com3run.firebaseauthkmp.AuthRepository

val fakeBackend = FakeAuthBackend()
val authRepository = AuthRepository(fakeBackend)

// Use in tests
```

## Sample App

This repository includes a complete sample app demonstrating all authentication methods. Run the `composeApp` module to see it in action.

## Requirements

- Kotlin 2.0+
- Android API 24+
- iOS 13.0+
- Firebase project with Authentication enabled

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

Created by [Kamran Mammadov](https://github.com/com3run)

## Support

- 📚 [Full Documentation](LIBRARY_INTEGRATION.md)
- 🐛 [Report Issues](https://github.com/com3run/testauth/issues)
- ⭐ Star this repo if you find it helpful!

---

Made with ❤️ using Kotlin Multiplatform
