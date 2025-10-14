# Firebase Auth KMP v1.0.1 🎉

A production-ready Kotlin Multiplatform library providing Firebase Authentication for **Android**, **iOS**, and **Desktop** with a unified, type-safe API.

## 🚀 Quick Start

```kotlin
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.1")
}
```

**Get started in 30 seconds:** [QUICKSTART.md](https://github.com/com3run/firebase-auth-kmp/blob/main/QUICKSTART.md)

---

## ✨ What's New in v1.0.1

### 🖥️ Desktop/JVM Platform Support
Firebase Authentication now works on **Windows, macOS, and Linux**!
- REST API-based implementation
- Email/Password and Anonymous auth
- OAuth support (requires external browser flow)
- Auto-discovery of `firebase-config.json`

### 🤖 Android Auto-Initialization
**Zero-code setup!** No more manual Activity management.
- Automatic `ActivityHolder` management via ContentProvider
- Activity lifecycle tracking
- Fully backward compatible

### 📱 iOS Simplified Setup
**One line in AppDelegate** and you're done!
- Ready-to-use Swift bridge template
- [Download bridge file →](https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template)
- Detailed setup guide: [IOS_SETUP_GUIDE.md](https://github.com/com3run/firebase-auth-kmp/blob/main/docs/IOS_SETUP_GUIDE.md)

### 📚 Tiered Documentation
Choose your learning path:
- **30 seconds:** [QUICKSTART.md](https://github.com/com3run/firebase-auth-kmp/blob/main/QUICKSTART.md) - Get running immediately
- **2 minutes:** [README.md](https://github.com/com3run/firebase-auth-kmp/blob/main/README.md) - Quick platform setup
- **5 minutes:** [EASY-INTEGRATION.md](https://github.com/com3run/firebase-auth-kmp/blob/main/EASY-INTEGRATION.md) - Complete understanding

---

## 🎯 Platform Support

| Feature | Android | iOS | Desktop |
|---------|---------|-----|---------|
| Email/Password | ✅ | ✅ | ✅ |
| Anonymous Auth | ✅ | ✅ | ✅ |
| Google Sign-In | ✅ | ✅ | ⚠️ Manual OAuth |
| Apple Sign-In | ❌ | ✅ | ⚠️ Manual OAuth |
| Facebook Sign-In | ✅ | ✅ | ⚠️ Manual OAuth |
| Auto-Init | ✅ | ❌ | ❌ |
| Offline Support | ✅ | ✅ | ❌ |
| Account Linking | ✅ | ✅ | ❌ |

✅ = Full support | ⚠️ = Partial support | ❌ = Not supported

---

## 📦 Installation

### Android
```kotlin
implementation("dev.com3run:firebase-auth-kmp:1.0.1")
```
**That's it!** Auto-initialization handles the rest.

### iOS
1. Add dependency (same as above)
2. Copy [FirebaseAuthBridge.swift.template](https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template) to your iOS app
3. Call `FirebaseAuthBridge.shared.start()` in AppDelegate

### Desktop
1. Add dependency (same as above)
2. Create `firebase-config.json`:
```json
{"apiKey": "YOUR_KEY", "projectId": "your-project"}
```

---

## 💡 Usage Example

```kotlin
// Initialize (with or without Koin)
val authBackend = platformAuthBackend()
val authRepository = AuthRepository(authBackend)

// Sign in
val result = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "password123"
)

when (result) {
    is AuthResult.Success -> println("Signed in!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}

// Monitor auth state
authRepository.authState.collect { user ->
    println(if (user != null) "Logged in" else "Logged out")
}
```

---

## 🆕 Breaking Changes

**None!** v1.0.1 is fully backward compatible with v1.0.0.

### Migration from v1.0.0

**Android:** You can now remove manual `ActivityHolder` code (optional):
```kotlin
// ❌ No longer needed (but still works):
ActivityHolder.current = this

// ✅ Automatic now!
```

**iOS:** Consider using the new bridge template for better error messages.

**All platforms:** Update your dependency to `1.0.1`

See [MIGRATION.md](https://github.com/com3run/firebase-auth-kmp/blob/main/MIGRATION.md) for details.

---

## 📝 Full Changelog

### Added
- ✨ Desktop/JVM platform support with REST API
- 🚀 Android auto-initialization via ContentProvider
- 📱 iOS bridge template with improved documentation
- 📚 QUICKSTART.md (30-second setup)
- 📘 IOS_SETUP_GUIDE.md (comprehensive iOS guide)
- 🔧 Multi-path config file discovery for desktop

### Changed
- 📖 Updated README with tiered documentation
- 🔧 Improved desktop error messages with working directory info
- 📚 Enhanced iOS documentation with download links

### Fixed
- 🐛 Desktop config file discovery now checks multiple paths
- 🐛 Documentation version inconsistencies
- 🐛 Android Activity lifecycle edge cases

[View full changelog →](https://github.com/com3run/firebase-auth-kmp/blob/main/CHANGELOG.md)

---

## 📚 Documentation

- [30-Second Quick Start](https://github.com/com3run/firebase-auth-kmp/blob/main/QUICKSTART.md)
- [Easy Integration Guide](https://github.com/com3run/firebase-auth-kmp/blob/main/EASY-INTEGRATION.md)
- [iOS Setup Guide](https://github.com/com3run/firebase-auth-kmp/blob/main/docs/IOS_SETUP_GUIDE.md)
- [Library Integration](https://github.com/com3run/firebase-auth-kmp/blob/main/docs/LIBRARY_INTEGRATION.md)
- [Full Documentation](https://github.com/com3run/firebase-auth-kmp/tree/main/agents)

---

## 🔗 Links

- 📦 [Maven Central](https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp/1.0.1)
- 🐛 [Report Issues](https://github.com/com3run/firebase-auth-kmp/issues)
- 💬 [Discussions](https://github.com/com3run/firebase-auth-kmp/discussions)
- ⭐ [Star on GitHub](https://github.com/com3run/firebase-auth-kmp)

---

## 👨‍💻 Credits

Created by [Kamran Mammadov](https://github.com/com3run)

## 📄 License

MIT License

---

**Made with ❤️ using Kotlin Multiplatform**

⭐ **If you find this library helpful, please star the repo!**
