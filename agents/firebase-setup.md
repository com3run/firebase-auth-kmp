# Firebase Configuration

## Overview

This project uses Firebase Authentication with platform-specific implementations for Android and iOS. Each platform requires proper configuration to enable Firebase services.

## Android Setup

### 1. Firebase Project Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create or select your Firebase project
3. Add Android app to project
4. Download `google-services.json`

### 2. Place Configuration File

```bash
# Place google-services.json in the composeApp directory
composeApp/google-services.json
```

### 3. Dependencies

Already configured in `build.gradle.kts`:
- `firebase-auth-ktx` - Firebase Authentication SDK
- `play-services-auth:21.2.0` - Required for Google Sign-In

### 4. Google Sign-In Setup

**Important**: Google Sign-In requires an Activity reference

In `MainActivity.kt`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Set activity reference for Google Sign-In
    ActivityHolder.current = this

    // ... rest of setup
}

override fun onDestroy() {
    super.onDestroy()
    // Clear activity reference
    ActivityHolder.current = null
}
```

### 5. Enable Authentication Methods

In Firebase Console:
1. Navigate to Authentication → Sign-in method
2. Enable desired methods:
   - Email/Password
   - Google
   - Apple (requires additional iOS setup)
   - Facebook (requires Facebook App ID)
   - Anonymous

### 6. SHA-1 Configuration (Required for Google Sign-In)

```bash
# Get debug SHA-1
./gradlew signingReport

# Add SHA-1 to Firebase Console:
# Project Settings → Your apps → Android app → Add fingerprint
```

## iOS Setup

### 1. Firebase Project Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Add iOS app to your Firebase project
3. Download `GoogleService-Info.plist`

### 2. Place Configuration File

```bash
# Place GoogleService-Info.plist in iosApp directory
iosApp/GoogleService-Info.plist
```

### 3. Add Firebase SDK

Choose one of the following methods:

#### Option A: CocoaPods (Recommended)
```ruby
# In iosApp/Podfile
pod 'Firebase/Auth'
```

#### Option B: Swift Package Manager
1. Open iosApp project in Xcode
2. File → Add Packages
3. Enter: `https://github.com/firebase/firebase-ios-sdk`
4. Select `FirebaseAuth` package

### 4. Swift Bridge Implementation

iOS uses a notification-based bridge for Kotlin-Swift communication.

**Required Components**:

1. **FirebaseAuthBridge.swift** - Listens to Kotlin requests and performs Firebase operations
2. **Notification Observers** - Monitor auth state changes
3. **Request/Response Flow** - Each request gets unique `requestId` for matching responses

**Bridge Responsibilities**:
- Listen to `AuthRequest` notifications from Kotlin
- Perform Firebase operations in Swift
- Post `AuthResponse` notifications back to Kotlin
- Monitor auth state changes and post `AuthState` notifications

**Example Bridge Structure**:
```swift
class FirebaseAuthBridge {
    init() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleAuthRequest),
            name: NSNotification.Name("AuthRequest"),
            object: nil
        )

        // Monitor auth state
        Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.notifyAuthStateChange(user)
        }
    }

    @objc func handleAuthRequest(_ notification: Notification) {
        // Extract request details
        // Perform Firebase operation
        // Post response notification
    }
}
```

### 5. Initialize Bridge

In your iOS app entry point:
```swift
@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        _ = FirebaseAuthBridge.shared // Initialize bridge
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 6. Enable Authentication Methods

Same as Android - configure in Firebase Console:
- Authentication → Sign-in method
- Enable Email/Password, Google, Apple, Facebook, Anonymous

### 7. Apple Sign-In Configuration

1. Enable in Xcode: Signing & Capabilities → + Capability → Sign in with Apple
2. Register Service ID in Apple Developer Portal
3. Add OAuth redirect URI in Firebase Console
4. Update Firebase Console with Apple Service ID

### 8. Google Sign-In Configuration

1. Add URL scheme to Info.plist:
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>YOUR_REVERSED_CLIENT_ID</string>
        </array>
    </dict>
</array>
```

2. Find `REVERSED_CLIENT_ID` in `GoogleService-Info.plist`

## Common Configuration Issues

### Android

**Issue**: Google Sign-In fails with DEVELOPER_ERROR
- **Solution**: Add SHA-1 fingerprint to Firebase Console
- **Verify**: Check package name matches Firebase configuration

**Issue**: Firebase not initialized
- **Solution**: Ensure `google-services.json` is in `composeApp/` directory
- **Verify**: Check `google-services` plugin is applied in build.gradle.kts

**Issue**: ActivityHolder.current is null
- **Solution**: Set `ActivityHolder.current = this` in MainActivity.onCreate()
- **Verify**: Check Activity lifecycle management

### iOS

**Issue**: Firebase not initialized
- **Solution**: Verify `FirebaseApp.configure()` called before any Firebase usage
- **Verify**: Check `GoogleService-Info.plist` is added to Xcode project

**Issue**: Bridge not responding
- **Solution**: Verify FirebaseAuthBridge is initialized
- **Verify**: Check notification names match between Kotlin and Swift
- **Debug**: Log notification posting/receiving in both layers

**Issue**: Auth state not updating
- **Solution**: Verify `addStateDidChangeListener` is properly set up
- **Verify**: Check `AuthState` notifications are being posted

## Testing Firebase Configuration

### Android
```bash
# Enable Firebase debug logging
adb shell setprop log.tag.FirebaseAuth DEBUG
adb logcat -s FirebaseAuth
```

### iOS
```bash
# Enable Firebase debug logging
# Add to scheme: -FIRDebugEnabled

# View logs in Xcode Console
# Filter: "Firebase"
```

## OAuth Provider IDs Reference

When implementing account linking/unlinking:
- Google: `"google.com"`
- Apple: `"apple.com"`
- Facebook: `"facebook.com"`
- Email/Password: `"password"`

## Security Considerations

1. **Never commit configuration files to public repositories**
   - Add to `.gitignore`:
     ```
     google-services.json
     GoogleService-Info.plist
     ```

2. **Use different Firebase projects for dev/staging/prod**

3. **Enable App Check** for production apps to prevent abuse

4. **Configure authorized domains** in Firebase Console

5. **Set up proper security rules** for Firestore/Realtime Database if used

## Resources

- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase iOS Setup](https://firebase.google.com/docs/ios/setup)
- [Firebase Auth Documentation](https://firebase.google.com/docs/auth)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Apple Sign-In](https://developer.apple.com/sign-in-with-apple/)