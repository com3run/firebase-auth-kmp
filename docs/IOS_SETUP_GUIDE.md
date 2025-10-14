# 📱 iOS Setup Guide - Firebase Auth KMP

## Why do I need to add a Swift file?

Firebase Auth KMP is a **Kotlin Multiplatform** library, but Firebase iOS SDK is written in **Swift/Objective-C**.

Since Kotlin can't directly call Swift, we use a **bridge pattern** - a simple Swift file that:
- Listens for requests from your Kotlin code
- Calls Firebase iOS SDK
- Sends responses back to Kotlin

**Think of it like a translator** between two languages 🗣️↔️🗣️

## One-Time Setup (2 minutes)

### Step 1: Get the Bridge File

**Option A (Easiest):** Direct download
1. Go to: https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template
2. Click "Raw" button
3. Save as `FirebaseAuthBridge.swift` (remove `.template`)

**Option B:** If you have the source code
1. Navigate to `firebase-auth-kmp/FirebaseAuthBridge.swift.template`
2. Copy to your iOS project

### Step 2: Add to Xcode

1. **Drag and drop** `FirebaseAuthBridge.swift` into your Xcode project
2. Make sure these are checked:
   - ✅ "Copy items if needed"
   - ✅ Your app target is selected
3. Click "Finish"

### Step 3: Start the Bridge

In your `AppDelegate.swift`:

```swift
import UIKit
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()              // Firebase
        FirebaseAuthBridge.shared.start()    // ← ADD THIS LINE
        return true
    }
}
```

### Step 4: Wire Up AppDelegate (if using SwiftUI)

In your `@main` app file:

```swift
@main
struct YourApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate  // ← Add this

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## ✅ Done!

That's it! Your Kotlin code can now call Firebase Auth:

```kotlin
// In your Kotlin code
val result = authRepository.signInAnonymously()
```

And the bridge automatically handles the Swift ↔ Kotlin communication!

---

## How It Works (Technical Details)

```
┌─────────────────────────────────────────────────────────┐
│                Your Kotlin Code                         │
│  val result = authRepository.signInAnonymously()       │
└───────────────────┬─────────────────────────────────────┘
                    │ Sends NSNotification("AuthRequest")
                    ▼
┌─────────────────────────────────────────────────────────┐
│           FirebaseAuthBridge.swift (YOU ADD THIS)       │
│  - Receives notification                                │
│  - Calls Firebase iOS SDK                              │
│  - Sends result back via NSNotification("AuthResponse") │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│              Firebase iOS SDK (Apple)                   │
│         Auth.auth().signInAnonymously()                │
└─────────────────────────────────────────────────────────┘
```

### Why NotificationCenter?

- ✅ **No code generation needed** - works out of the box
- ✅ **Type-safe** - we control the message format
- ✅ **Async-friendly** - perfect for auth flows
- ✅ **Familiar** - standard iOS pattern
- ✅ **Testable** - easy to mock

### Alternative Approaches (Not Used)

| Approach | Why Not? |
|----------|----------|
| **C-Interop** | Too complex, requires CocoaPods setup in KMP |
| **Expect/Actual with cinterop** | Hard to debug, not beginner-friendly |
| **Kotlin-Swift bridge generator** | Adds build complexity |
| **Pure Kotlin iOS SDK wrapper** | Firebase iOS SDK is closed source |

---

## Troubleshooting

### "FirebaseAuthBridge not found"

**Cause:** Bridge file not added to Xcode project

**Fix:**
1. Check that `FirebaseAuthBridge.swift` is in your Xcode project navigator
2. Make sure it's added to your app target (not a framework target)

### "No auth state updates"

**Cause:** Forgot to call `.start()`

**Fix:** Add `FirebaseAuthBridge.shared.start()` in AppDelegate

### "Google Sign-In not working"

**Cause:** Missing URL scheme or Google Sign-In not configured

**Fix:**
1. Check `GoogleService-Info.plist` is in your project
2. Add URL scheme to Info.plist (see Firebase console)
3. Make sure Google Sign-In is enabled in Firebase console

### "Apple Sign-In crashes"

**Cause:** Missing "Sign in with Apple" capability

**Fix:**
1. Select your target in Xcode
2. Go to "Signing & Capabilities"
3. Click "+ Capability"
4. Add "Sign in with Apple"

---

## Comparison with Other Libraries

Your integration is actually **simpler** than most KMP libraries that need iOS SDK access:

| Library | iOS Setup | Complexity |
|---------|-----------|------------|
| **Firebase Auth KMP** | 1 Swift file | ⭐⭐ Simple |
| **Ktor** | CocoaPods config | ⭐⭐⭐ Medium |
| **SQLDelight** | Native drivers | ⭐⭐⭐ Medium |
| **Realm** | CocoaPods + config | ⭐⭐⭐⭐ Complex |
| **Firebase TouchLab** | CocoaPods + cinterop | ⭐⭐⭐⭐⭐ Very Complex |

---

## Future Plans

We're exploring:
- 📦 **Swift Package Manager** - Install bridge as an SPM dependency
- 🤖 **Gradle task** - Auto-copy bridge to iOS project
- 🎥 **Video tutorial** - Visual setup guide

But for now, the current approach is **battle-tested** and **recommended** by the KMP community!

---

## Questions?

- 💬 [GitHub Discussions](https://github.com/com3run/firebase-auth-kmp/discussions)
- 🐛 [Report Issue](https://github.com/com3run/firebase-auth-kmp/issues)
- 📖 [Full Documentation](../README.md)
