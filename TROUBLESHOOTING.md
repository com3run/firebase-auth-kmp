# Troubleshooting Guide

## Sign In Not Working

### Issue: "User not found" error when trying to sign in

**Cause:** You're trying to sign in with an account that doesn't exist yet.

**Solution:**
1. First, **Sign Up** with the email/password
2. Then you can **Sign In** with those credentials

**Steps:**
1. Enter email: `test@example.com`
2. Enter password: `password123`
3. Click "Sign Up" button
4. Confirm in the dialog
5. Account is created and you're automatically signed in
6. Now click "Sign Out"
7. Click "Sign In" - should work now

### Issue: "Wrong password" error

**Solution:**
- Make sure you're using the same password you used during sign up
- Firebase passwords are case-sensitive
- Minimum 6 characters required

### Issue: Sign in button not responding

**Checklist:**
- [ ] Is Firebase configured? Check if `google-services.json` exists in `composeApp/` folder
- [ ] Is the internet connected?
- [ ] Check Logcat for errors

## Google Sign In Not Working

### Issue: Google Sign In button does nothing or returns null

**Required Setup:**

#### 1. Add SHA-1 Certificate to Firebase Console

**Get your SHA-1:**
```bash
# Debug certificate (for testing)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Or use Gradle
./gradlew signingReport
```

**Add to Firebase:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings (gear icon)
4. Scroll to "Your apps" section
5. Select your Android app
6. Click "Add fingerprint"
7. Paste your SHA-1
8. Download the new `google-services.json`
9. Replace the old one in `composeApp/` folder
10. Clean and rebuild project

#### 2. Enable Google Sign-In in Firebase

1. Go to Firebase Console > Authentication
2. Click "Sign-in method" tab
3. Enable "Google"
4. Set a support email
5. Save

#### 3. Verify google-services.json

Make sure your `google-services.json` contains:
```json
{
  "client": [
    {
      "oauth_client": [
        {
          "client_type": 3,
          "client_id": "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
        }
      ]
    }
  ]
}
```

The Web Client ID is required for Google Sign In to work.

### Issue: "default_web_client_id not found"

**Cause:** Google Services plugin didn't process `google-services.json` correctly.

**Solution:**
```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew :composeApp:assembleDebug
```

Check that `composeApp/build/generated/res/processDebugGoogleServices/values/values.xml` exists and contains `default_web_client_id`.

### Issue: Google Sign In shows error dialog

**Common causes:**
1. **SHA-1 not added** - Add it in Firebase Console
2. **OAuth consent screen not configured** - Set it up in Google Cloud Console
3. **Wrong package name** - Verify package name matches in Firebase Console
4. **API not enabled** - Enable "Google Sign-In API" in Google Cloud Console

## Anonymous Sign In Not Working

### Issue: Anonymous sign in fails

**Solution:**
1. Go to Firebase Console > Authentication
2. Click "Sign-in method" tab
3. Enable "Anonymous"
4. Save

This should work now.

## General Firebase Issues

### Issue: "FirebaseApp is not initialized"

**Solution:**
1. Ensure `google-services.json` is in the `composeApp/` folder
2. Verify the plugin is applied in `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.googleServices)
   }
   ```
3. Clean and rebuild

### Issue: Network errors

**Checklist:**
- [ ] Internet connection working?
- [ ] Firebase project created?
- [ ] `google-services.json` downloaded and added?
- [ ] Correct package name in Firebase Console?

### Issue: Build errors after adding Firebase

**Solution:**
```bash
# Invalidate caches
rm -rf .gradle build composeApp/build

# Rebuild
./gradlew clean build
```

## Debugging Tips

### Enable Firebase Debug Logging (Android)

Add to `AndroidManifest.xml`:
```xml
<application>
    <meta-data
        android:name="firebase_analytics_collection_deactivated"
        android:value="true" />
</application>
```

### Check Logcat Filters

In Android Studio Logcat:
- Filter: `FirebaseAuth`
- Filter: `GoogleSignIn`
- Filter: `AuthBackend`

### Common Error Messages

| Error | Meaning | Solution |
|-------|---------|----------|
| `ERROR_USER_NOT_FOUND` | Account doesn't exist | Sign up first |
| `ERROR_WRONG_PASSWORD` | Incorrect password | Check password |
| `ERROR_EMAIL_ALREADY_IN_USE` | Email taken | Use different email or sign in |
| `ERROR_WEAK_PASSWORD` | Password < 6 chars | Use longer password |
| `ERROR_INVALID_EMAIL` | Bad email format | Fix email format |
| `ERROR_USER_DISABLED` | Account disabled | Contact support |
| `ERROR_TOO_MANY_REQUESTS` | Rate limited | Wait a few minutes |
| `12501` (Google) | User cancelled | Normal, try again |
| `10` (Google) | SHA-1 not configured | Add SHA-1 to Firebase |

## Testing Checklist

### Before Testing

- [ ] Firebase project created
- [ ] `google-services.json` downloaded and placed in `composeApp/`
- [ ] Package name matches Firebase Console
- [ ] Authentication methods enabled in Firebase Console:
  - [ ] Email/Password
  - [ ] Anonymous
  - [ ] Google (if testing)
- [ ] SHA-1 certificate added (for Google Sign In)
- [ ] App built successfully
- [ ] Internet connection available

### Test Flow

1. **Anonymous Sign In** (easiest)
   - Click "Continue as Guest"
   - Should see: "✓ Signed in as: Unknown"

2. **Sign Up**
   - Enter unique email
   - Enter password (6+ chars)
   - Click "Sign Up"
   - Should see: "✓ Signed in as: [email]"

3. **Sign Out**
   - Click "Sign Out"
   - Should see: "Not signed in"

4. **Sign In**
   - Enter same email/password from step 2
   - Click "Sign In"
   - Should see: "✓ Signed in as: [email]"

5. **Google Sign In** (requires setup)
   - Click "Sign in with Google"
   - Select Google account
   - Should see: "✓ Signed in as: [name]"

## Getting Help

If you're still having issues:

1. **Check Firebase Console Logs**
   - Go to Firebase Console > Authentication > Users
   - Verify users are being created

2. **Check Android Logcat**
   - Look for Firebase errors
   - Look for exception stack traces

3. **Verify Configuration**
   - Package name in `build.gradle.kts` matches Firebase
   - `google-services.json` is not corrupted
   - Firebase project has billing enabled (if using certain features)

4. **Test with Default Credentials**
   - Email: `test@example.com`
   - Password: `password123`
   - These should work after sign up

## Quick Fixes

### Reset Everything

```bash
# 1. Clean project
./gradlew clean
rm -rf build composeApp/build

# 2. Re-download google-services.json from Firebase Console
# 3. Place in composeApp/ folder

# 4. Rebuild
./gradlew :composeApp:assembleDebug

# 5. Uninstall app from device
adb uninstall az.random.testauth

# 6. Install fresh
./gradlew :composeApp:installDebug
```

### Test Firebase Connection

Add this temporarily to test Firebase is working:

```kotlin
// In MainActivity.onCreate()
FirebaseAuth.getInstance().addAuthStateListener { auth ->
    Log.d("FirebaseTest", "Auth state: ${auth.currentUser?.email ?: "not signed in"}")
}
```

## Platform-Specific Issues

### Android

**Issue: App crashes on startup**
- Check if Firebase is initialized
- Verify `google-services.json` is present
- Check Logcat for stack trace

**Issue: Google Sign In stuck on loading**
- Verify SHA-1 certificate
- Check internet connection
- Look for API errors in Logcat

### iOS

**Issue: Apple Sign In required on iOS**
- Apple requires native Sign In with Apple implementation
- Follow Apple's guidelines for OAuth

## Production Checklist

Before deploying:

- [ ] Replace debug SHA-1 with release SHA-1
- [ ] Update `google-services.json` with production config
- [ ] Test all auth methods in release build
- [ ] Enable email verification in production
- [ ] Set up password recovery flows
- [ ] Add proper error messages
- [ ] Implement rate limiting
- [ ] Add analytics to track auth issues
- [ ] Test offline scenarios
- [ ] Add loading indicators
- [ ] Handle edge cases (network loss during auth, etc.)

## Contact

For library-specific issues, check:
- `LIBRARY_DOCUMENTATION.md` - API reference
- `USAGE_EXAMPLES.md` - Code examples
- `KOIN_SETUP.md` - Dependency injection
- `SAMPLE_UI_GUIDE.md` - UI reference

For Firebase issues:
- [Firebase Documentation](https://firebase.google.com/docs/auth)
- [Firebase Support](https://firebase.google.com/support)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/firebase-authentication)
