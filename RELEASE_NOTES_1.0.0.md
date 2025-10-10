# Firebase Auth KMP v1.0.0 🎉

First stable release of Firebase Auth KMP library!

## Features

- ✅ **Email/Password Authentication** - Sign up and sign in with email
- ✅ **Google Sign-In** - Works on both Android and iOS
- ✅ **Apple Sign-In** - iOS only
- ✅ **Anonymous Authentication** - Guest access
- ✅ **Real-time Auth State** - Flow-based state monitoring
- ✅ **Comprehensive Error Handling** - Detailed error types
- ✅ **Unit Testing Support** - FakeAuthBackend included

## Installation

### Step 1: Add JitPack Repository

In your project's `settings.gradle.kts`:

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

```kotlin
import dev.com3run.firebaseauthkmp.AuthRepository
import dev.com3run.firebaseauthkmp.platformAuthBackend

// Create auth repository
val authRepository = AuthRepository(platformAuthBackend())

// Sign in
val result = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "password123"
)
```

## Documentation

- 📚 [README.md](https://github.com/com3run/testauth/blob/main/README.md) - Complete documentation
- 📖 [Library Integration Guide](https://github.com/com3run/testauth/blob/main/LIBRARY_INTEGRATION.md) - Setup instructions
- 🚀 [Publishing Guide](https://github.com/com3run/testauth/blob/main/PUBLISHING_GUIDE.md) - For contributors

## Package Information

- **Group ID**: `dev.com3run`
- **Artifact ID**: `firebase-auth-kmp`
- **Version**: `1.0.0`
- **License**: MIT

## Platform Support

- **Android**: API 24+
- **iOS**: 13.0+
- **Kotlin**: 2.0+

## What's Next?

Check out the [sample app](https://github.com/com3run/testauth/tree/main/composeApp) to see all features in action!

---

Made with ❤️ using Kotlin Multiplatform
