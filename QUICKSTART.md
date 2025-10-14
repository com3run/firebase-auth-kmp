# 🚀 30-Second Quick Start

## 1. Install (All Platforms)

```kotlin
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.1")
}
```

## 2. Platform Setup

### Android ✅
**Nothing needed!** Just add `google-services.json`

### iOS 📱
```swift
// In AppDelegate.swift
FirebaseAuthBridge.shared.start()  // ONE LINE!
```
[Download bridge file →](https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template)

### Desktop 💻
Create `firebase-config.json`:
```json
{"apiKey": "YOUR_KEY", "projectId": "your-project"}
```

## 3. Use It

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

## ✅ Done!

**Need more details?** See [EASY-INTEGRATION.md](EASY-INTEGRATION.md) (5 minutes)

**Full documentation:** [README.md](README.md)
