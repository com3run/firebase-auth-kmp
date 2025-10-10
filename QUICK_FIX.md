# Quick Fix Guide

## Sign In Not Working? ❌ → ✅

### The Problem
Getting "User not found" error when clicking Sign In

### The Solution
**You need to Sign Up first!**

```
1. Enter email: test@example.com
2. Enter password: password123
3. Click "Sign Up" (not Sign In)
4. Confirm in dialog
5. Now you're signed in!
6. Sign Out
7. Now "Sign In" will work
```

### Why?
Firebase doesn't have the user account yet. Sign Up creates it.

---

## Google Sign In Not Working? ❌ → ✅

### Quick Check
Run this command to get your SHA-1:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Add SHA-1 to Firebase
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. ⚙️ Project Settings
4. Scroll down to "Your apps"
5. Click your Android app
6. Click "+ Add fingerprint"
7. Paste SHA-1
8. Download new `google-services.json`
9. Replace in `composeApp/` folder
10. Clean & rebuild:
    ```bash
    ./gradlew clean
    ./gradlew :composeApp:assembleDebug
    ```

### Enable Google Sign-In
1. Firebase Console > Authentication
2. Sign-in method tab
3. Enable "Google"
4. Save

---

## Working Test Flow ✅

### 1. Anonymous (Easiest - Always Works)
```
Click "Continue as Guest"
→ "✓ Signed in as: Unknown"
```

### 2. Sign Up (Create Account)
```
Email: test@example.com
Password: password123
Click "Sign Up"
→ Confirm in dialog
→ "✓ Signed in as: test@example.com"
```

### 3. Sign Out
```
Click "Sign Out"
→ "Not signed in"
```

### 4. Sign In (Now it works!)
```
Email: test@example.com
Password: password123
Click "Sign In"
→ "✓ Signed in as: test@example.com"
```

### 5. Password Reset
```
Click "Forgot Password?"
Enter email
Click "Send Reset Link"
→ "✓ Password reset email sent!"
```

### 6. Google Sign In (Requires SHA-1 setup)
```
Click "Sign in with Google"
→ Select Google account
→ "✓ Signed in as: [Your Name]"
```

---

## Common Errors & Fixes

| Error | Fix |
|-------|-----|
| "User not found" | Sign Up first, then Sign In |
| "Wrong password" | Use correct password (case-sensitive) |
| "Email already in use" | Account exists, use Sign In instead |
| "Weak password" | Use 6+ characters |
| Google returns null | Add SHA-1 to Firebase, enable Google auth |
| Firebase not initialized | Add `google-services.json` to `composeApp/` |

---

## Firebase Setup Checklist

### Required for Email/Password:
- [x] `google-services.json` in `composeApp/` folder
- [x] Email/Password enabled in Firebase Console > Authentication

### Required for Anonymous:
- [x] Anonymous enabled in Firebase Console > Authentication

### Required for Google Sign In:
- [x] SHA-1 certificate added to Firebase Console
- [x] Google sign-in enabled in Firebase Console
- [x] New `google-services.json` downloaded after adding SHA-1
- [x] Clean & rebuild project

---

## Emergency Reset

If nothing works:
```bash
# 1. Clean everything
./gradlew clean
rm -rf build composeApp/build

# 2. Get fresh google-services.json from Firebase

# 3. Rebuild
./gradlew :composeApp:assembleDebug

# 4. Reinstall
adb uninstall az.random.testauth
./gradlew :composeApp:installDebug
```

---

## Get SHA-1 Fast

### Method 1: Keytool
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

### Method 2: Gradle
```bash
./gradlew signingReport
```

### Method 3: Android Studio
```
Gradle panel → Tasks → android → signingReport
```

---

## Verify Firebase is Working

Add to MainActivity temporarily:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Test Firebase connection
    Log.d("TEST", "Firebase Project: ${FirebaseApp.getInstance().options.projectId}")
    Log.d("TEST", "Current User: ${FirebaseAuth.getInstance().currentUser?.email ?: "none"}")
}
```

Check Logcat for output.

---

## Most Common Issue

**95% of problems are:**
1. Trying to Sign In before Signing Up
2. Missing SHA-1 for Google Sign In

**Fix:**
- Always Sign Up first to create the account
- Add SHA-1 fingerprint to Firebase Console

---

## Success Indicators

When working correctly:
- ✅ Anonymous sign in: ~1 second
- ✅ Sign up: ~2 seconds
- ✅ Sign in: ~1-2 seconds
- ✅ Google sign in: ~3-5 seconds (includes Google picker)
- ✅ Sign out: Instant

---

## Still Not Working?

1. Check **TROUBLESHOOTING.md** for detailed solutions
2. Check Logcat for Firebase errors
3. Verify Firebase Console shows authentication enabled
4. Ensure `google-services.json` is valid JSON
5. Check package name matches Firebase Console

---

## Quick Test Commands

```bash
# Check if google-services.json exists
ls -la composeApp/google-services.json

# Check if Firebase is processing it
ls -la composeApp/build/generated/res/processDebugGoogleServices/values/values.xml

# Get your package name
grep "applicationId" composeApp/build.gradle.kts

# Check if app is installed
adb shell pm list packages | grep testauth

# View real-time logs
adb logcat | grep -E "FirebaseAuth|GoogleSignIn"
```

---

Need more help? See **TROUBLESHOOTING.md** for detailed debugging steps.
