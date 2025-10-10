# Apple Sign-In Implementation Guide

This guide explains how Apple Sign-In works in this Kotlin Multiplatform app.

## Overview

Apple Sign-In is **fully implemented** on iOS and provides a **stub (no-op) implementation** on Android (since Apple Sign-In is iOS-only).

## Architecture

```
┌─────────────────────────────────────┐
│         UI Layer                    │
│  (SampleAuthUi.kt)                 │
│  User taps "Sign in with Apple"   │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      AuthViewModel                  │
│  signInWithApple(idToken: String)  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      AuthRepository                 │
│  signInWithApple(idToken: String)  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      AuthBackend (Interface)        │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌──────────────┐  ┌─────────────────┐
│  iOS         │  │  Android        │
│  (Native)    │  │  (Stub/No-op)   │
└──────────────┘  └─────────────────┘
```

## How It Works

### iOS Flow

1. **User taps "Sign in with Apple"** button in UI
2. **UI calls** `requestAppleIdToken()` - This triggers the native Apple Sign-In flow
3. **Swift Bridge** handles the native flow:
   - Receives `AppleSignInRequest` notification from Kotlin
   - Launches `ASAuthorizationController` (Apple's native UI)
   - User authenticates with Face ID/Touch ID/Password
   - Receives Apple ID token from Apple
   - Posts `AppleSignInCompleted` notification back to Kotlin with the token
4. **Kotlin receives** the ID token
5. **ViewModel** calls `authRepository.signInWithApple(idToken)`
6. **AuthRepository** validates input and delegates to iOS backend
7. **iOS Backend** sends auth request to Swift bridge with the token
8. **Swift Bridge** signs in with Firebase using the Apple credential
9. **Firebase returns** auth result
10. **UI updates** based on auth state

### Android Flow

On Android, the button is visible but tapping it does nothing:
- `requestAppleIdToken()` immediately returns `null`
- This is expected since Apple Sign-In is iOS-only

## Code Structure

### Common Code (Shared)

**`composeApp/src/commonMain/kotlin/az/random/testauth/auth/PlatformAuth.kt`**
```kotlin
// Expect function - platform-specific implementations
expect suspend fun requestAppleIdToken(): String?
```

**`composeApp/src/commonMain/kotlin/az/random/testauth/ui/SampleAuthUi.kt`**
```kotlin
Button(
    onClick = {
        scope.launch {
            val idToken = requestAppleIdToken()
            if (idToken != null) {
                viewModel.signInWithApple(idToken)
            } else {
                // Handle cancellation or error
            }
        }
    }
) {
    Text("Sign in with Apple")
}
```

**`composeApp/src/commonMain/kotlin/az/random/testauth/ui/AuthViewModel.kt`**
```kotlin
fun signInWithApple(idToken: String) {
    viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        when (val result = authRepository.signInWithApple(idToken)) {
            is AuthResult.Success -> {
                // State will be updated by the authState collector
            }
            is AuthResult.Failure -> {
                _uiState.value = AuthUiState.Error(mapError(result.error))
            }
        }
    }
}
```

### iOS Implementation

**`composeApp/src/iosMain/kotlin/az/random/testauth/auth/IosFirebaseAuthBackend.kt`**
```kotlin
actual suspend fun requestAppleIdToken(): String? = suspendCancellableCoroutine { continuation ->
    val center = NSNotificationCenter.defaultCenter
    var observer: Any? = null

    logDebug("AppleSignIn", "Requesting Apple ID token")

    // Listen for completion
    observer = center.addObserverForName(
        name = "AppleSignInCompleted",
        `object` = null,
        queue = NSOperationQueue.mainQueue
    ) { notification: NSNotification? ->
        val userInfo = notification?.userInfo as? Map<*, *>
        val idToken = userInfo?.get("idToken") as? String

        if (idToken != null) {
            logDebug("AppleSignIn", "Successfully got Apple ID token")
        } else {
            logError("AppleSignIn", "Failed to get Apple ID token (null)")
        }

        // Clean up
        observer?.let { center.removeObserver(it) }

        // Resume with result
        if (continuation.isActive) {
            continuation.resume(idToken)
        }
    }

    // Trigger the sign-in flow
    logDebug("AppleSignIn", "Sending AppleSignInRequest notification")
    center.postNotificationName(
        aName = "AppleSignInRequest",
        `object` = null,
        userInfo = null
    )

    // Clean up on cancellation
    continuation.invokeOnCancellation {
        observer?.let { center.removeObserver(it) }
    }
}
```

**`iosApp/iosApp/FirebaseAuthBridge.swift`** (Swift side)
```swift
@objc private func handleAppleSignInRequest(_ notification: Notification) {
    if #available(iOS 13.0, *) {
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]

        let authorizationController = ASAuthorizationController(authorizationRequests: [request])

        // Create a coordinator to handle the result
        let coordinator = AppleSignInCoordinator { [weak self] idToken in
            self?.postAppleSignInResult(idToken: idToken)
        }

        authorizationController.delegate = coordinator
        authorizationController.presentationContextProvider = coordinator

        // Store coordinator to prevent deallocation
        self.appleSignInCoordinator = coordinator

        authorizationController.performRequests()
    } else {
        print("❌ Apple Sign-In requires iOS 13.0 or later")
        postAppleSignInResult(idToken: nil)
    }
}

private func postAppleSignInResult(idToken: String?) {
    let userInfo: [String: Any] = [
        "idToken": idToken as Any
    ]

    center.post(
        name: NSNotification.Name("AppleSignInCompleted"),
        object: nil,
        userInfo: userInfo
    )
}
```

**AppleSignInCoordinator:**
```swift
@available(iOS 13.0, *)
private class AppleSignInCoordinator: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    private let completion: (String?) -> Void

    init(completion: @escaping (String?) -> Void) {
        self.completion = completion
        super.init()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let identityTokenData = appleIDCredential.identityToken,
              let idToken = String(data: identityTokenData, encoding: .utf8) else {
            print("❌ Failed to get identity token from Apple Sign-In")
            completion(nil)
            return
        }

        print("✅ Apple sign-in successful")
        completion(idToken)
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        print("❌ Apple sign-in error: \\(error.localizedDescription)")
        completion(nil)
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        return UIApplication.shared
        .connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow } ?? UIWindow()
    }
}
```

### Android Implementation

**`composeApp/src/androidMain/kotlin/az/random/testauth/auth/PlatformAuth.android.kt`**
```kotlin
actual suspend fun requestAppleIdToken(): String? {
    // Apple Sign-In is iOS-only, return null on Android
    return null
}
```

## Testing

### iOS Testing

1. **Run the app** on iOS Simulator or real device
2. **Tap "Sign in with Apple"**
3. **Expected behavior:**
   - Native Apple Sign-In UI appears
   - User can authenticate with Face ID/Touch ID/Password
   - On success: User is signed in and UI shows signed-in state
   - On cancel: Nothing happens, user stays on sign-in screen

### Android Testing

1. **Run the app** on Android emulator or real device
2. **Tap "Sign in with Apple"**
3. **Expected behavior:**
   - Nothing happens (button does nothing)
   - This is correct since Apple Sign-In is iOS-only

## Firebase Configuration

### iOS Setup

1. **Enable Apple Sign-In** in Firebase Console:
   - Go to Authentication → Sign-in method
   - Enable "Apple" provider
   - No additional configuration needed for iOS

2. **Enable in Xcode**:
   - Open `iosApp.xcodeproj`
   - Select your target → Signing & Capabilities
   - Click "+ Capability"
   - Add "Sign in with Apple"

3. **Required imports** in Swift:
   ```swift
   import AuthenticationServices  // For ASAuthorization
   import FirebaseAuth            // For OAuthProvider
   ```

### Android Setup

No setup required - Apple Sign-In is not available on Android.

## Logs

### iOS Logs (Xcode Console)

**Successful Sign-In:**
```
[AppleSignIn] Requesting Apple ID token
[AppleSignIn] Sending AppleSignInRequest notification
✅ Apple sign-in successful
[AppleSignIn] Successfully got Apple ID token
[FirebaseAuth] Attempting Apple sign in with idToken
[FirebaseAuth] Sending auth request: action=apple, requestId=...
[FirebaseAuth] Received auth response for requestId=...
[FirebaseAuth] Auth request successful
```

**User Cancelled:**
```
[AppleSignIn] Requesting Apple ID token
[AppleSignIn] Sending AppleSignInRequest notification
❌ Apple sign-in error: The operation couldn't be completed...
[ERROR][AppleSignIn] Failed to get Apple ID token (null)
```

**Error:**
```
[AppleSignIn] Requesting Apple ID token
[AppleSignIn] Sending AppleSignInRequest notification
❌ Apple sign-in error: [error message]
[ERROR][AppleSignIn] Failed to get Apple ID token (null)
```

## Advantages of Apple Sign-In

1. **Privacy-focused** - Users can hide their email
2. **Native iOS integration** - Face ID/Touch ID support
3. **Required by Apple** - Apps must offer Apple Sign-In if they offer other social logins
4. **Fast and convenient** - One tap to sign in
5. **Secure** - Apple handles authentication

## Common Issues

### Issue: Apple Sign-In button does nothing on iOS

**Possible causes:**
1. Apple Sign-In not enabled in Firebase Console
2. "Sign in with Apple" capability not added in Xcode
3. Swift bridge not initialized (`FirebaseAuthBridge.shared.start()` not called)
4. Running on iOS < 13.0

**Solution:**
- Check Firebase Console Authentication settings
- Check Xcode Signing & Capabilities
- Check AppDelegate initialization
- Check iOS version (must be 13.0+)

### Issue: "Failed to get identity token from Apple Sign-In"

**Possible cause:**
Apple returned a credential without an identity token (rare)

**Solution:**
- This is usually a temporary Apple service issue
- Try again
- Check Apple System Status: https://www.apple.com/support/systemstatus/

### Issue: Button visible but doesn't work on Android

**This is expected behavior** - Apple Sign-In is iOS-only. You can either:
1. Hide the button on Android
2. Keep it visible but disabled with a message
3. Show a toast/snackbar explaining it's iOS-only

## Best Practices

1. **Handle cancellation gracefully** - Don't show errors when user cancels
2. **Test on real device** - Face ID/Touch ID don't work in simulator
3. **Provide alternative sign-in methods** - Don't rely only on Apple Sign-In
4. **Handle privacy options** - Some users may hide their email
5. **Keep UI responsive** - Use proper loading states

## Security Notes

1. **Never store the ID token** - It expires after 10 minutes
2. **Validate tokens on backend** - Don't trust client-side validation
3. **Use HTTPS only** - For all API calls
4. **Keep Firebase SDK updated** - For latest security patches
5. **Follow Apple's guidelines** - For proper implementation

## Summary

✅ **iOS**: Fully implemented with native Apple Sign-In flow
❌ **Android**: Stub implementation (returns null immediately)

The implementation is production-ready and follows best practices for both security and user experience.
