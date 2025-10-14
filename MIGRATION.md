# Migration Guide

## Upgrading to v1.0.2

This guide helps you migrate from earlier versions (v1.0.0 or v1.0.1) to v1.0.2, which includes:
- ✨ Desktop/JVM support
- 🚀 Android auto-initialization
- 📱 Simplified iOS setup
- 🔧 Better error messages

### Quick Migration Checklist

- [ ] Update dependency version to `1.0.2`
- [ ] **Android**: Remove manual `ActivityHolder` code
- [ ] **iOS**: (Optional) Use new bridge template
- [ ] **Desktop**: Add if needed
- [ ] Test authentication flows
- [ ] Update documentation references

---

## Android Migration

### What Changed

**v1.0.2 introduces automatic initialization via ContentProvider** - you no longer need to manually manage `ActivityHolder.current`!

### Step 1: Update Dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.2")  // Update version
}
```

### Step 2: Remove Manual Initialization

**BEFORE (v1.0.0/v1.0.1):**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.current = this  // ❌ Remove this

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.current = null  // ❌ Remove this
    }
}
```

**AFTER (v1.0.2):**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ No ActivityHolder code needed!

        setContent {
            App()
        }
    }

    // ✅ No onDestroy override needed!
}
```

### Step 3: Verify

1. Run your app
2. Test Google Sign-In (if you use it)
3. Verify no crashes or initialization errors

**That's it!** Your Android app is now using auto-initialization.

---

## iOS Migration

### What Changed

v1.0.2 provides a **ready-to-use bridge template** that's easier to set up and maintain.

### Option A: Keep Existing Bridge (No Changes Needed)

If your existing `FirebaseAuthBridge.swift` works fine, **you don't need to change anything**. v1.0.2 is fully compatible with your existing bridge.

### Option B: Update to New Template (Recommended)

The new template includes:
- Better documentation
- Improved error handling
- Consistent code style
- Future-proof structure

#### Steps to Update:

1. **Backup your current bridge** (just in case)

2. **Get the new template:**
   - Download from: `firebase-auth-kmp/FirebaseAuthBridge.swift.template`
   - Or copy from the library package

3. **Replace your bridge:**
   ```bash
   # In your iOS app
   rm iosApp/iosApp/FirebaseAuthBridge.swift
   cp firebase-auth-kmp/FirebaseAuthBridge.swift.template iosApp/iosApp/FirebaseAuthBridge.swift
   ```

4. **Verify initialization** in AppDelegate:
   ```swift
   func application(_ application: UIApplication,
                    didFinishLaunchingWithOptions...) -> Bool {
       FirebaseApp.configure()
       FirebaseAuthBridge.shared.start()  // ✅ Same as before
       return true
   }
   ```

5. **Test:**
   - Run your iOS app
   - Test all auth flows
   - Verify Google/Apple Sign-In works

**No code changes needed in your Kotlin code!**

---

## Desktop Support (New in v1.0.2)

### Adding Desktop Target

If you want to add desktop support to your app:

#### Step 1: Add JVM Target

```kotlin
// composeApp/build.gradle.kts
kotlin {
    androidTarget { ... }

    // Add this:
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()
}
```

#### Step 2: Create Desktop Main

```kotlin
// composeApp/src/desktopMain/kotlin/YourPackage/main.kt
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication, title = "Your App") {
            App()
        }
    }
}
```

#### Step 3: Configure Firebase

Create `firebase-config.json` in project root:

```json
{
  "apiKey": "YOUR_FIREBASE_API_KEY",
  "projectId": "your-project-id"
}
```

#### Step 4: Run Desktop App

```bash
./gradlew :composeApp:run
```

See [agents/desktop-setup.md](agents/desktop-setup.md) for full desktop documentation.

---

## API Changes

### No Breaking Changes! 🎉

v1.0.2 is **fully backward compatible**. All existing APIs work exactly the same:

```kotlin
// These APIs are unchanged:
authRepository.signInWithEmailAndPassword(email, password)
authRepository.signInWithGoogle(idToken)
authRepository.signInAnonymously()
authRepository.signOut()
authRepository.authState.collect { ... }
```

### New Platform: Desktop

```kotlin
// Desktop uses the same API:
val authRepository = AuthRepository(platformAuthBackend())

// Works on all platforms (Android, iOS, Desktop)
val result = authRepository.signInWithEmailAndPassword(email, password)
```

---

## Common Migration Issues

### Issue 1: Android - Google Sign-In Not Working After Update

**Problem:** Google Sign-In fails after removing `ActivityHolder` code.

**Solution:** The auto-initializer handles this automatically, but if you see issues:

1. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew :composeApp:assembleDebug
   ```

2. Verify your `google-services.json` is present

3. Check SHA-1 fingerprint is registered in Firebase Console

### Issue 2: iOS - Bridge Not Found

**Problem:** `FirebaseAuthBridge.swift.template` file not found.

**Solution:**

The template is in the library package:
```
firebase-auth-kmp/FirebaseAuthBridge.swift.template
```

Or get it from the [GitHub repository](https://github.com/com3run/firebase-auth-kmp/blob/main/firebase-auth-kmp/FirebaseAuthBridge.swift.template).

### Issue 3: Desktop - API Key Not Found

**Problem:** Desktop app crashes with "Firebase API key not found".

**Solution:**

1. Create `firebase-config.json` in your **project root** (not in src/)
2. Or set environment variable:
   ```bash
   export FIREBASE_API_KEY="your-key"
   ```

---

## Testing Your Migration

### Android Testing Checklist

- [ ] App launches successfully
- [ ] Email/password sign-in works
- [ ] Google Sign-In works (if applicable)
- [ ] Anonymous sign-in works
- [ ] Sign-out works
- [ ] Auth state updates correctly
- [ ] No `ActivityHolder` references in your code

### iOS Testing Checklist

- [ ] App launches successfully
- [ ] Bridge initializes (check console for "✅ Firebase Auth KMP Bridge initialized")
- [ ] Email/password sign-in works
- [ ] Google Sign-In works (if applicable)
- [ ] Apple Sign-In works (if applicable)
- [ ] Sign-out works
- [ ] Auth state updates correctly

### Desktop Testing Checklist (if added)

- [ ] App launches successfully
- [ ] Firebase config loads correctly
- [ ] Email/password sign-in works
- [ ] Anonymous sign-in works
- [ ] Sign-out works
- [ ] Auth state updates correctly

---

## Rollback Instructions

If you need to rollback to v1.0.0/v1.0.1:

### Android

1. Downgrade dependency:
   ```kotlin
   implementation("dev.com3run:firebase-auth-kmp:1.0.1")
   ```

2. Re-add `ActivityHolder` code:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       ActivityHolder.current = this
       // ...
   }

   override fun onDestroy() {
       super.onDestroy()
       ActivityHolder.current = null
   }
   ```

3. Clean and rebuild

### iOS

1. Downgrade dependency:
   ```kotlin
   implementation("dev.com3run:firebase-auth-kmp:1.0.1")
   ```

2. Keep your existing bridge (it's compatible)

3. Rebuild

---

## Getting Help

If you encounter issues during migration:

1. **Check the documentation:**
   - [EASY-INTEGRATION.md](EASY-INTEGRATION.md)
   - [agents/firebase-setup.md](agents/firebase-setup.md)
   - [agents/desktop-setup.md](agents/desktop-setup.md)

2. **Review common issues:**
   - [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

3. **Get support:**
   - [GitHub Issues](https://github.com/com3run/firebase-auth-kmp/issues)
   - [GitHub Discussions](https://github.com/com3run/firebase-auth-kmp/discussions)

---

## Benefits of Upgrading

### Android

- ✅ **Zero boilerplate** - No manual initialization
- ✅ **Fewer bugs** - Can't forget to set/clear ActivityHolder
- ✅ **Cleaner code** - Less code to maintain
- ✅ **Automatic lifecycle** - Library handles everything

### iOS

- ✅ **Better template** - Well-documented, ready to use
- ✅ **Easier setup** - Copy-paste instead of writing from scratch
- ✅ **Future-proof** - Stays updated with library changes

### Desktop

- ✅ **New platform** - Reach desktop users
- ✅ **Unified API** - Same code works everywhere
- ✅ **Easy config** - Simple JSON file

---

**Happy migrating! 🚀**

If you find this guide helpful, please ⭐ star the repo!
