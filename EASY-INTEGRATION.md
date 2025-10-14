# 🚀 Easy Integration Guide

This guide shows you the **simplest possible way** to integrate Firebase Auth KMP into your project. Most setup is now automatic!

## ⏱️ Quick Start (5 minutes)

### Step 1: Add the Dependency

```kotlin
// In your build.gradle.kts
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.2")
}
```

### Step 2: Platform-Specific Setup

#### Android ✅ **AUTOMATIC!**
**No code needed!** The library auto-initializes via ContentProvider.

Just add your `google-services.json` file to your app module.

~~❌ OLD WAY (No longer needed):~~
```kotlin
// You DON'T need this anymore!
// ActivityHolder.current = this
```

#### iOS (One-time setup - 2 minutes)
1. Download the bridge file: [FirebaseAuthBridge.swift.template](https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template)
2. Drag it into your Xcode project and rename to `FirebaseAuthBridge.swift`
3. In your `AppDelegate.swift`:

```swift
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        FirebaseAuthBridge.shared.start()  // ← ONE LINE!
        return true
    }
}
```

That's it! The bridge handles everything else automatically.

#### Desktop ✅ **AUTOMATIC ERROR MESSAGES!**
Create `firebase-config.json` in your project root:

```json
{
  "apiKey": "YOUR_API_KEY",
  "projectId": "your-project-id"
}
```

If you forget, you'll get a clear error message telling you exactly what to do.

### Step 3: Use the Library

#### Option A: Without Dependency Injection (Simplest)

```kotlin
import dev.com3run.firebaseauthkmp.*

// Create auth repository directly
val authBackend = platformAuthBackend()
val authRepository = AuthRepository(authBackend)
```

#### Option B: With Koin (Recommended for larger apps)

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

// Use authentication
val authRepository = get<AuthRepository>()

// Sign in with email
val result = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "password123"
)

// Monitor auth state
authRepository.authState.collect { user ->
    if (user != null) {
        println("Signed in: ${user.email}")
    } else {
        println("Signed out")
    }
}
```

## 📊 Before vs After

### Android Integration

| Before (OLD) | After (NEW) |
|--------------|-------------|
| ❌ Manually set `ActivityHolder.current` in `onCreate()` | ✅ Automatic! |
| ❌ Manually clear in `onDestroy()` | ✅ Automatic! |
| ❌ Easy to forget | ✅ Can't forget! |
| **3-4 lines of code** | **0 lines of code** |

### iOS Integration

| Before (OLD) | After (NEW) |
|--------------|-------------|
| ❌ Create bridge Swift file manually | ✅ Copy/paste template |
| ❌ Understand NSNotificationCenter | ✅ Just call `.start()` |
| ❌ Remember to call `.stop()` | ✅ Bridge handles it |
| **Complex setup** | **1 line in AppDelegate** |

### Desktop Integration

| Before (OLD) | After (NEW) |
|--------------|-------------|
| ❌ Cryptic "file not found" errors | ✅ Clear instructions |
| ❌ Guess file location | ✅ Told exactly where to put it |
| **Confusing** | **Self-explanatory** |

## 🎯 Platform-Specific Details

### Android: Zero Configuration

The library uses a **ContentProvider** that automatically runs when your app starts. It:
- ✅ Automatically manages Activity references for Google Sign-In
- ✅ Handles lifecycle correctly
- ✅ Cleans up when activities are destroyed
- ✅ Works with any Activity (not just MainActivity)

**You literally don't need to do anything!**

### iOS: One-Time Bridge Setup

The `FirebaseAuthBridge.swift` file:
- ✅ Handles all Kotlin ↔ Swift communication
- ✅ Manages Google and Apple Sign-In flows
- ✅ Broadcasts auth state changes automatically
- ✅ Clean coordinator pattern for Apple Sign-In

**Just call `.start()` once in AppDelegate and forget about it!**

### Desktop: Auto-Discovery

The library:
- ✅ Automatically looks for `firebase-config.json`
- ✅ Falls back to `FIREBASE_API_KEY` environment variable
- ✅ Gives helpful error messages if nothing is found
- ✅ Tells you exactly where to get the API key

**Configuration errors are now beginner-friendly!**

## 💡 Pro Tips

### Tip 1: Android - It Just Works™
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // That's it! No auth initialization needed!
        setContent {
            App()
        }
    }
}
```

### Tip 2: iOS - Get the Bridge File
**Option 1 (Easiest):** Download from GitHub:
```
https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template
```

**Option 2:** If using the library source, find it at:
```
firebase-auth-kmp/FirebaseAuthBridge.swift.template
```

Just drag-and-drop into Xcode and rename to `FirebaseAuthBridge.swift`!

### Tip 3: Desktop - Environment Variable
For CI/CD or different environments:
```bash
export FIREBASE_API_KEY="your-key-here"
./gradlew run
```

## 🔥 What's Auto-Initialized?

### Android
- ✅ Activity reference management
- ✅ Lifecycle tracking
- ✅ Google Sign-In setup
- ✅ Memory cleanup

### iOS (after calling `.start()`)
- ✅ Firebase configuration
- ✅ Google Sign-In configuration
- ✅ Apple Sign-In coordinator
- ✅ Notification observers
- ✅ Auth state listeners

### Desktop
- ✅ Configuration loading
- ✅ Ktor HTTP client
- ✅ Error message formatting

## ❓ Troubleshooting

### Android: "Google Sign-In not working"
**Old problem**: Forgot to set `ActivityHolder.current`
**New solution**: Impossible! It's automatic. Check your SHA-1 in Firebase Console instead.

### iOS: "Bridge not responding"
**Check**: Did you call `FirebaseAuthBridge.shared.start()`?
**Location**: Should be in `AppDelegate.didFinishLaunchingWithOptions`

### Desktop: "API key not found"
**Check**: Is `firebase-config.json` in your project root?
**Alternative**: Set `FIREBASE_API_KEY` environment variable

## 📚 What You Learned

1. **Android**: Zero-config auto-initialization via ContentProvider
2. **iOS**: One-line bridge activation in AppDelegate
3. **Desktop**: Auto-discovery with helpful error messages

## 🎉 Result

**Before**: 10-15 minutes of manual setup, easy to make mistakes
**After**: 2-5 minutes, mostly copy-paste, hard to mess up

Your integration is now **simpler, safer, and faster**!

---

## Need Help?

- 📖 Full documentation: `/agents` folder
- 🐛 Issues: [GitHub Issues](https://github.com/com3run/firebase-auth-kmp/issues)
- 💬 Questions: Check `/agents/firebase-setup.md` for platform-specific details
