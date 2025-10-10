# Debug Guide

## Debug Logging Added

I've added comprehensive debug logging to help troubleshoot authentication issues on **both Android and iOS**.

## How to View Logs

### Android

#### Option 1: Android Studio Logcat
1. Open Android Studio
2. Run your app
3. Open **Logcat** panel (bottom of screen)
4. Filter by these tags:
   - `FirebaseAuth` - All Firebase authentication operations
   - `GoogleSignIn` - Google Sign-In flow

#### Option 2: Command Line
```bash
# View all Firebase and Google logs in real-time
adb logcat | grep -E "FirebaseAuth|GoogleSignIn"

# View only errors
adb logcat | grep -E "FirebaseAuth|GoogleSignIn" | grep -E "E/"

# Save logs to file
adb logcat | grep -E "FirebaseAuth|GoogleSignIn" > auth_logs.txt
```

### iOS

#### Option 1: Xcode Console
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Run the app (⌘R)
3. Open **Console** panel (bottom of Xcode)
4. Filter by:
   - `FirebaseAuth` - All Firebase authentication operations
   - `GoogleSignIn` - Google Sign-In flow
   - `AppleSignIn` - Apple Sign-In flow

#### Option 2: Command Line (Device)
```bash
# View logs from connected device
xcrun simctl spawn booted log stream --predicate 'eventMessage contains "FirebaseAuth" or eventMessage contains "GoogleSignIn" or eventMessage contains "AppleSignIn"' --level debug

# Or use Console.app
# Open /Applications/Utilities/Console.app
# Select your device/simulator
# Search for "FirebaseAuth", "GoogleSignIn", or "AppleSignIn"
```

#### Option 3: Print statements (Swift)
All logs use `print()` and `NSLog()` in Swift, so they appear in Xcode console:
- 🔐 = Authentication operation
- ✅ = Success
- ❌ = Error/Failure
- ⚠️ = Warning

## Log Tags and What They Show

### `FirebaseAuth` Tag

**Anonymous Sign In:**
```
D/FirebaseAuth: Attempting anonymous sign in
D/FirebaseAuth: Anonymous sign in successful: [uid]
```

**Sign Up:**
```
D/FirebaseAuth: Attempting sign up with email: test@example.com
D/FirebaseAuth: Sign up successful: test@example.com
```

**Sign In:**
```
D/FirebaseAuth: Attempting sign in with email: test@example.com
D/FirebaseAuth: Sign in successful: test@example.com
```

**Google Sign In:**
```
D/FirebaseAuth: Attempting Google sign in with idToken
D/FirebaseAuth: Google sign in successful
```

**Errors:**
```
E/FirebaseAuth: Sign in failed: [error message]
D/FirebaseAuth: Mapping error: FirebaseAuthException - [message]
D/FirebaseAuth: Firebase error code: ERROR_USER_NOT_FOUND
```

### `GoogleSignIn` Tag

**Success Flow:**
```
D/GoogleSignIn: Activity found, proceeding with Google Sign-In
D/GoogleSignIn: Requesting Google ID token
D/GoogleSignIn: Using client ID: [client_id]
D/GoogleSignIn: Launched Google Sign-In intent
D/GoogleSignIn: Successfully got Google account: [email]
D/GoogleSignIn: ID token present: true
```

**Error Cases:**
```
E/GoogleSignIn: Activity is null. Make sure to set ActivityHolder.current in your MainActivity
E/GoogleSignIn: default_web_client_id not found in resources. Make sure google-services.json is configured correctly.
E/GoogleSignIn: Failed to get Google sign-in result: [error message]
```

## Common Error Messages and Solutions

### Sign In Issues

#### Error: "User not found" (ERROR_USER_NOT_FOUND)
**Log:**
```
E/FirebaseAuth: Sign in failed: There is no user record corresponding to this identifier
D/FirebaseAuth: Firebase error code: ERROR_USER_NOT_FOUND
```

**Solution:** You need to sign up first to create the account.

**Steps:**
1. Enter email: `test@example.com`
2. Enter password: `password123`
3. Click **"Sign Up"** (not Sign In!)
4. After sign up succeeds, sign out
5. Now "Sign In" will work

---

#### Error: "Wrong password" (ERROR_WRONG_PASSWORD)
**Log:**
```
E/FirebaseAuth: Sign in failed: The password is invalid
D/FirebaseAuth: Firebase error code: ERROR_WRONG_PASSWORD
```

**Solution:** Check your password. Firebase passwords are case-sensitive and must be at least 6 characters.

---

#### Error: "Invalid email" (ERROR_INVALID_EMAIL)
**Log:**
```
E/FirebaseAuth: Sign in failed: The email address is badly formatted
D/FirebaseAuth: Firebase error code: ERROR_INVALID_EMAIL
```

**Solution:** Use a valid email format like `user@example.com`.

---

### Google Sign In Issues

#### Error: Activity is null
**Log:**
```
E/GoogleSignIn: Activity is null. Make sure to set ActivityHolder.current in your MainActivity
```

**Solution:** This should not happen as MainActivity already sets it. If you see this:
1. Verify MainActivity.kt:73-81 has the activity lifecycle methods
2. Make sure you're calling Google Sign-In from the main UI, not from a background thread

---

#### Error: default_web_client_id not found
**Log:**
```
E/GoogleSignIn: default_web_client_id not found in resources. Make sure google-services.json is configured correctly.
```

**Solution:** Your google-services.json is not being processed correctly.

**Fix:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew :composeApp:assembleDebug

# Check if the resource was generated
ls -la composeApp/build/generated/res/processDebugGoogleServices/values/values.xml

# If file doesn't exist, verify google-services.json is in composeApp/ folder
ls -la composeApp/google-services.json
```

---

#### Error: Failed to get Google sign-in result
**Log:**
```
E/GoogleSignIn: Failed to get Google sign-in result: [error message]
```

**Common Causes:**
1. **User cancelled** - Normal, just try again
2. **SHA-1 not configured** - See below
3. **Google Sign-In not enabled** - Enable in Firebase Console

**SHA-1 Configuration:**
```bash
# Get your SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

# Or use Gradle
./gradlew signingReport
```

Then add SHA-1 to Firebase Console:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project "auth-test-bfda7"
3. Project Settings (⚙️)
4. Scroll to "Your apps"
5. Click your Android app
6. Click "Add fingerprint"
7. Paste SHA-1
8. Download new `google-services.json`
9. Replace in `composeApp/` folder
10. Rebuild: `./gradlew clean && ./gradlew :composeApp:assembleDebug`

---

## Testing Workflow with Logs

### 1. Test Anonymous Sign In
```bash
# Start log monitoring
adb logcat -c && adb logcat | grep FirebaseAuth

# Click "Continue as Guest" in app
# Expected logs:
D/FirebaseAuth: Attempting anonymous sign in
D/FirebaseAuth: Anonymous sign in successful: [uid]
```

### 2. Test Sign Up
```bash
# Enter email and password, click Sign Up
# Expected logs:
D/FirebaseAuth: Attempting sign up with email: test@example.com
D/FirebaseAuth: Sign up successful: test@example.com
```

### 3. Test Sign In (after Sign Up)
```bash
# Sign out, then sign in with same credentials
# Expected logs:
D/FirebaseAuth: Attempting sign in with email: test@example.com
D/FirebaseAuth: Sign in successful: test@example.com
```

### 4. Test Google Sign In
```bash
# Start log monitoring for both tags
adb logcat -c && adb logcat | grep -E "FirebaseAuth|GoogleSignIn"

# Click "Sign in with Google" in app
# Expected logs:
D/GoogleSignIn: Activity found, proceeding with Google Sign-In
D/GoogleSignIn: Requesting Google ID token
D/GoogleSignIn: Using client ID: [...]
D/GoogleSignIn: Launched Google Sign-In intent
# (Select Google account in UI)
D/GoogleSignIn: Successfully got Google account: your.email@gmail.com
D/GoogleSignIn: ID token present: true
D/FirebaseAuth: Attempting Google sign in with idToken
D/FirebaseAuth: Google sign in successful
```

---

## Firebase Configuration Verification

Your current configuration (verified):
- ✅ Package name: `az.random.testauth`
- ✅ Firebase project: `auth-test-bfda7`
- ✅ google-services.json present
- ✅ SHA-1 configured: `7a4b3ac25d88d9be96e011f042c679d8eb3392d7`
- ✅ Web client ID available for Google Sign-In

## Quick Debug Commands

```bash
# Check if app is installed
adb shell pm list packages | grep testauth

# Clear app data (fresh start)
adb shell pm clear az.random.testauth

# Reinstall app
./gradlew :composeApp:installDebug

# View logs in real-time with filter
adb logcat -c && adb logcat | grep -E "FirebaseAuth|GoogleSignIn"

# Check Firebase resource was generated
cat composeApp/build/generated/res/processDebugGoogleServices/values/values.xml | grep default_web_client_id

# Verify package name matches
grep "applicationId" composeApp/build.gradle.kts

# Get SHA-1 (for reference)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

---

## What Changed

I added logging to these files:

1. **AndroidFirebaseAuthBackend.kt**:
   - Logs every authentication attempt (sign in, sign up, anonymous, Google)
   - Logs success with user info
   - Logs errors with exception details
   - Logs error code mapping

2. **PlatformAuth.android.kt**:
   - Logs Google Sign-In flow start
   - Logs client ID being used
   - Logs when Google picker is launched
   - Logs Google account selection result
   - Logs ID token acquisition

All logs are at appropriate levels:
- `Log.d()` for normal flow (Debug)
- `Log.e()` for errors (Error)
- `Log.w()` for warnings (Warning)

---

## Expected Behavior Summary

### ✅ Working (as per your report):
- Anonymous sign in (guest)
- Sign up with email/password

### ⚠️ Not Working (troubleshooting needed):
- Sign in with email/password
  - **Likely cause:** Need to sign up first
  - **Check logs for:** ERROR_USER_NOT_FOUND

- Google Sign In
  - **Possible causes:**
    1. SHA-1 not matching current debug keystore
    2. Google Sign-In not enabled in Firebase Console
    3. User cancelling the flow
  - **Check logs for:** Specific error messages

---

## Next Steps

1. **Run the app with logging**:
   ```bash
   ./gradlew :composeApp:installDebug
   adb logcat -c && adb logcat | grep -E "FirebaseAuth|GoogleSignIn"
   ```

2. **Try sign in** and check logs:
   - If you see ERROR_USER_NOT_FOUND, sign up first
   - If you see ERROR_WRONG_PASSWORD, check your password

3. **Try Google Sign In** and check logs:
   - If you see "default_web_client_id not found", rebuild the app
   - If you see other errors, check the specific error message

4. **Share the logs** if issues persist - the logs will show exactly what's failing

---

## Pro Tips

- Use `adb logcat -c` to clear logs before each test
- Use `grep -A 5 -B 5` to see context around errors
- Filter by log level: `grep "E/FirebaseAuth"` for errors only
- Save logs to share: `adb logcat > full_logs.txt`

---

## iOS-Specific Testing

### iOS Log Examples

**Sign Up (Swift bridge):**
```
🔐 Sign up with email: test@example.com
✅ Sign up successful: test@example.com
[FirebaseAuth] Attempting sign up with email: test@example.com
[FirebaseAuth] Sign up successful: test@example.com
```

**Sign In (Swift bridge):**
```
🔐 Sign in with email: test@example.com
✅ Sign in successful: test@example.com
[FirebaseAuth] Attempting sign in with email: test@example.com
[FirebaseAuth] Sign in successful: test@example.com
```

**Google Sign In (iOS):**
```
[GoogleSignIn] Requesting Google ID token
[GoogleSignIn] Sending GoogleSignInRequest notification
✅ Google sign-in successful
[GoogleSignIn] Successfully got Google ID token
[FirebaseAuth] Attempting Google sign in with idToken
[FirebaseAuth] Google sign in successful
```

**Apple Sign In (iOS):**
```
[AppleSignIn] Requesting Apple ID token
[AppleSignIn] Sending AppleSignInRequest notification
✅ Apple sign-in successful
[AppleSignIn] Successfully got Apple ID token
[FirebaseAuth] Attempting Apple sign in with idToken
[FirebaseAuth] Apple sign in successful
```

### iOS Quick Debug Commands

```bash
# Build iOS framework
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# Open in Xcode
open iosApp/iosApp.xcodeproj

# Run in simulator
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Debug

# View simulator logs
xcrun simctl spawn booted log stream --level debug | grep -E "FirebaseAuth|GoogleSignIn|AppleSignIn"
```

### iOS Firebase Configuration

For iOS, you need `GoogleService-Info.plist` (equivalent of Android's `google-services.json`):
1. Download from Firebase Console
2. Place in `iosApp/iosApp/` folder
3. Add to Xcode project
4. Rebuild

---

## Platform Differences

| Feature | Android | iOS |
|---------|---------|-----|
| **Logging** | Logcat (`Log.d/e`) | NSLog/print |
| **Config File** | `google-services.json` | `GoogleService-Info.plist` |
| **Google Sign-In** | SHA-1 required | Client ID from Firebase |
| **Apple Sign-In** | Not available | Native ASAuthorization |
| **Log Tags** | Filtered by tag | Filtered by content |
| **Bridge** | Direct Java/Kotlin | Notification-based |

---

## Need More Help?

If logging shows unexpected behavior, check:
1. **QUICK_FIX.md** - Quick solutions for common issues
2. **TROUBLESHOOTING.md** - Detailed troubleshooting guide
3. Firebase Console Authentication section - Verify methods are enabled
4. **Android**: Logcat output - Share relevant logs for specific help
5. **iOS**: Xcode Console output - Share relevant logs for specific help
