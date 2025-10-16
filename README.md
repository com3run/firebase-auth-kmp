# Firebase Auth KMP

<div align="center">

[![Maven Central](https://img.shields.io/maven-central/v/dev.com3run/firebase-auth-kmp?style=for-the-badge&logo=sonatype&logoColor=white&color=blue)](https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp)
[![GitHub Release](https://img.shields.io/github/v/release/com3run/firebase-auth-kmp?style=for-the-badge&logo=github&logoColor=white&color=brightgreen)](https://github.com/com3run/firebase-auth-kmp/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)

[![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-13.0%2B-000000?style=for-the-badge&logo=apple&logoColor=white)](https://developer.apple.com/ios/)
[![Desktop](https://img.shields.io/badge/Desktop-JVM%2011%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![GitHub Stars](https://img.shields.io/github/stars/com3run/firebase-auth-kmp?style=for-the-badge&logo=github&logoColor=white&color=gold)](https://github.com/com3run/firebase-auth-kmp/stargazers)

<h3>🔥 Production-Ready Firebase Authentication for Kotlin Multiplatform 🔥</h3>

<p>
A complete Firebase Authentication solution for <strong>Android</strong>, <strong>iOS</strong>, and <strong>Desktop</strong><br/>
with a unified, type-safe API. Zero-config on Android, one-line setup on iOS!
</p>

[📦 Installation](#-quick-start) •
[📖 Docs](QUICKSTART.md) •
[🎯 Examples](#-usage) •
[⭐ Star Us!](https://github.com/com3run/firebase-auth-kmp/stargazers)

</div>

---

## ✨ Features

- 🎯 **Cross-platform**: Single codebase for Android, iOS & Desktop (JVM)
- 🚀 **Easy Integration**: Auto-initialization on Android, one-line setup on iOS
- 🔐 **Complete Authentication**:
  - Email/Password (Sign up & Sign in)
  - Google Sign-In
  - Apple Sign-In (iOS)
  - Anonymous Authentication
  - Facebook Sign-In
- 🔄 **Real-time Auth State**: Flow-based auth state monitoring
- 🛡️ **Type-safe**: Kotlin-first API with sealed classes for results
- 🧪 **Testable**: Includes FakeAuthBackend for unit testing
- 📱 **Platform-optimized**: Native Firebase SDKs on Android/iOS, REST API on Desktop
- 💎 **Production-ready**: Published on Maven Central

## 🚀 Quick Start

**⚡ Super fast?** See [QUICKSTART.md](QUICKSTART.md) (30 seconds)

**📖 Detailed setup:** See below (2 minutes)

### Installation

#### From Maven Central (Recommended)

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.com3run:firebase-auth-kmp:1.0.3")
        }
    }
}
```

#### From JitPack (Alternative)

For JitPack, if you have a `jvm("desktop")` target, use platform-specific dependencies:

```kotlin
repositories {
    maven("https://jitpack.io")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation("com.github.com3run.firebase-auth-kmp:firebase-auth-kmp-android:v1.0.3")
        }
        iosMain.dependencies {
            implementation("com.github.com3run.firebase-auth-kmp:firebase-auth-kmp-iosarm64:v1.0.3")
        }
        val desktopMain by getting {
            dependencies {
                implementation("com.github.com3run.firebase-auth-kmp:firebase-auth-kmp-jvm:v1.0.3")
            }
        }
    }
}
```

> **Note**: JitPack requires platform-specific artifacts for `desktopMain` source sets. For automatic resolution, use Maven Central instead.

### Platform Setup

#### Android ✅ **AUTOMATIC!**
**No code needed!** Just add your `google-services.json` file.

~~❌ OLD:~~ ~~You had to manually set `ActivityHolder.current`~~
✅ **NEW:** Auto-initializes via ContentProvider!

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // That's it! No Firebase Auth initialization needed! 🎉
        setContent {
            App()
        }
    }
}
```

#### iOS 📱 **ONE LINE!**

1. Copy `FirebaseAuthBridge.swift` from the library to your iOS app
2. Add to AppDelegate:

```swift
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        FirebaseAuthBridge.shared.start()  // ← ONE LINE!
        return true
    }
}
```

[Get the bridge file →](firebase-auth-kmp/FirebaseAuthBridge.swift.template)

#### Desktop 💻 **SIMPLE CONFIG!**

Create `firebase-config.json` in your project root:

```json
{
  "apiKey": "YOUR_FIREBASE_API_KEY",
  "projectId": "your-project-id"
}
```

[Find your API key in Firebase Console →](https://console.firebase.google.com/)

---

**🎯 Want detailed setup?** See [EASY-INTEGRATION.md](EASY-INTEGRATION.md)

## 💡 Usage

### Basic Authentication

```kotlin
import dev.com3run.firebaseauthkmp.*
import org.koin.core.context.startKoin

// Initialize Koin
startKoin {
    modules(module {
        single<AuthBackend> { platformAuthBackend() }
        single { AuthRepository(get()) }
    })
}

// Use auth repository
val authRepository = get<AuthRepository>()

// Monitor auth state
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
    is AuthResult.Success -> println("Welcome ${result.data.displayName}!")
    is AuthResult.Failure -> when (result.error) {
        AuthError.InvalidCredential -> println("Wrong email or password")
        AuthError.UserNotFound -> println("No account found")
        else -> println("Error: ${result.error}")
    }
}
```

### Sign In with Google

```kotlin
// Request ID token (platform-specific UI flow)
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

## 📚 Documentation

### Getting Started
- 🚀 **[Easy Integration Guide](EASY-INTEGRATION.md)** - 5-minute setup for all platforms
- 📘 [Library Integration Guide](docs/LIBRARY_INTEGRATION.md) - Detailed setup instructions
- 📖 [Usage Examples](docs/USAGE_EXAMPLES.md) - More code examples

### Platform-Specific
- 🤖 [Android Setup](agents/firebase-setup.md#android-setup) - Auto-initialization details
- 🍎 [iOS Setup](agents/firebase-setup.md#ios-setup) - Bridge configuration
- 💻 [Desktop Setup](agents/desktop-setup.md) - JVM configuration & limitations

### Advanced
- 🔧 [Troubleshooting](docs/TROUBLESHOOTING.md) - Common issues and solutions
- 🏗️ [Architecture](agents/project-overview.md) - Clean architecture design
- 👨‍💻 [Coding Guidelines](agents/coding-guidelines.md) - Best practices
- 🔄 [Migration Guide](MIGRATION.md) - Upgrading from v1.0.0/1.0.1

## 🏗️ Architecture

The library follows clean architecture principles:

```
┌─────────────────────────────────────┐
│      AuthRepository                 │  ← Validation + High-level API
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      AuthBackend (Interface)        │  ← Platform-agnostic contract
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┬─────────────┐
       ▼                ▼              ▼
┌──────────────┐  ┌─────────────┐  ┌──────────────┐
│  Android     │  │     iOS     │  │   Desktop    │
│  Firebase    │  │  Firebase   │  │  Firebase    │
│  SDK         │  │  Bridge     │  │  REST API    │
└──────────────┘  └─────────────┘  └──────────────┘
```

### Key Components

- **AuthRepository**: High-level API with input validation
- **AuthBackend**: Platform-specific interface (Android/iOS/Desktop)
- **AuthModels**: Common data models (AuthUser, AuthResult, AuthError)

### Platform Implementations

| Platform | Implementation | Features |
|----------|----------------|----------|
| **Android** | Native Firebase SDK | ✅ Auto-init, Full OAuth, Offline support |
| **iOS** | Notification bridge | ✅ Native SDK, Full OAuth, One-line setup |
| **Desktop** | REST API | ✅ Email/Password, Anonymous, Manual OAuth |

## 🧪 Testing

The library includes `FakeAuthBackend` for unit testing:

```kotlin
import dev.com3run.firebaseauthkmp.FakeAuthBackend
import dev.com3run.firebaseauthkmp.AuthRepository

@Test
fun `test sign in success`() = runTest {
    val fakeBackend = FakeAuthBackend()
    val authRepository = AuthRepository(fakeBackend)

    fakeBackend.setAuthResult(AuthResult.Success(testUser))

    val result = authRepository.signInWithEmailAndPassword("test@example.com", "password")

    assertTrue(result is AuthResult.Success)
}
```

## 📦 Sample App

This repository includes a complete sample app demonstrating all authentication methods:

- ✅ Email/Password authentication
- ✅ Google Sign-In
- ✅ Apple Sign-In (iOS)
- ✅ Anonymous authentication
- ✅ Profile management
- ✅ Password reset

Run the `composeApp` module to see it in action!

## ⚙️ Requirements

- Kotlin 2.0+
- Android API 24+ (Android 7.0)
- iOS 13.0+
- JVM 11+ (Desktop)
- Firebase project with Authentication enabled

## 🆕 What's New in v1.0.3

- ✨ **Desktop/JVM support** - Run on Windows, macOS, Linux
- 🚀 **Android auto-initialization** - Zero manual setup required!
- 📱 **Simplified iOS setup** - Ready-to-use bridge template
- 📚 **Easy Integration Guide** - Get started in 2 minutes
- 🔧 **Better error messages** - Clear, actionable feedback

[See full changelog →](CHANGELOG.md)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- 📦 [Maven Central](https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp)
- 📚 [Full Documentation](agents/)
- 🐛 [Report Issues](https://github.com/com3run/firebase-auth-kmp/issues)
- ⭐ [Star on GitHub](https://github.com/com3run/firebase-auth-kmp)
- 💬 [Discussions](https://github.com/com3run/firebase-auth-kmp/discussions)

## 👨‍💻 Credits

Created by [Kamran Mammadov](https://github.com/com3run)

Special thanks to all contributors!

---

**Made with ❤️ using Kotlin Multiplatform**

⭐ If you find this library helpful, please star the repo!
