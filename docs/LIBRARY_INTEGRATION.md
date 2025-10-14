# Firebase Auth KMP - Library Integration Guide

This guide explains how to integrate the Firebase Auth KMP library into your Kotlin Multiplatform project.

## Overview

**Firebase Auth KMP** is a Kotlin Multiplatform library that provides Firebase Authentication for Android and iOS with a unified API.

**Group ID:** `dev.com3run`
**Artifact ID:** `firebase-auth-kmp`
**Version:** `1.0.0`

## Installation

### Option 1: From Maven Local (Testing)

If you've published the library to Maven Local for testing:

#### Step 1: Add Maven Local Repository

In your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()  // Add this
        google()
        mavenCentral()
    }
}
```

#### Step 2: Add Dependency

In your module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.com3run:firebase-auth-kmp:1.0.0")
        }
    }
}
```

### Option 2: From Maven Central (When Published)

Once published to Maven Central, no need to add `mavenLocal()`, just add the dependency:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.com3run:firebase-auth-kmp:1.0.0")
        }
    }
}
```

## Platform-Specific Setup

### Android Setup

#### 1. Add Firebase to Your Android App

Download `google-services.json` from Firebase Console and place it in your `composeApp/` or Android module directory.

Add the Google Services plugin in `build.gradle.kts`:

```kotlin
plugins {
    // ... other plugins
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

In your app module `build.gradle.kts`:

```kotlin
plugins {
    // ... other plugins
    id("com.google.gms.google-services")
}
```

#### 2. Set Up Activity Reference

The library needs a reference to the current Activity for Google Sign-In. In your `MainActivity`:

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.com3run.firebaseauthkmp.ActivityHolder
import dev.com3run.firebaseauthkmp.GoogleSignInInterop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set activity reference for library
        ActivityHolder.current = this

        setContent {
            // Your app content
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.current = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GoogleSignInInterop.onActivityResult(requestCode, resultCode, data)
    }
}
```

### iOS Setup

#### 1. Add Firebase to Your iOS App

Download `GoogleService-Info.plist` from Firebase Console and add it to your iOS app in Xcode.

#### 2. Install Firebase iOS SDK

Add Firebase SDK using CocoaPods or Swift Package Manager.

**Using CocoaPods** (Recommended):

Create `Podfile` in `iosApp/` directory:

```ruby
platform :ios, '13.0'

target 'iosApp' do
  use_frameworks!

  pod 'FirebaseAuth', '~> 10.0'
end
```

Then run:
```bash
cd iosApp
pod install
```

**Using Swift Package Manager:**

In Xcode, go to File → Add Packages → Enter Firebase URL:
`https://github.com/firebase/firebase-ios-sdk`

Select `FirebaseAuth` and add to your target.

#### 3. Create Swift Bridge

Create `FirebaseAuthBridge.swift` in your iOS app:

```swift
import Foundation
import FirebaseAuth
import AuthenticationServices

class FirebaseAuthBridge {
    static let shared = FirebaseAuthBridge()
    private let center = NotificationCenter.default
    private var appleSignInCoordinator: AppleSignInCoordinator?

    private init() {}

    func start() {
        // Listen for auth requests from Kotlin
        center.addObserver(
            forName: NSNotification.Name("AuthRequest"),
            object: nil,
            queue: .main
        ) { [weak self] notification in
            self?.handleAuthRequest(notification)
        }

        // Listen for Apple Sign-In requests
        center.addObserver(
            forName: NSNotification.Name("AppleSignInRequest"),
            object: nil,
            queue: .main
        ) { [weak self] notification in
            self?.handleAppleSignInRequest(notification)
        }

        // Monitor auth state changes
        Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.postAuthState(user: user)
        }
    }

    private func handleAuthRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let requestId = userInfo["requestId"] as? String,
              let action = userInfo["action"] as? String else {
            return
        }

        Task {
            do {
                let user: User?

                switch action {
                case "anonymous":
                    let result = try await Auth.auth().signInAnonymously()
                    user = result.user

                case "signUpWithEmailAndPassword":
                    guard let email = userInfo["email"] as? String,
                          let password = userInfo["password"] as? String else {
                        postAuthError(requestId: requestId, code: "invalid-params", message: "Missing email or password")
                        return
                    }
                    let result = try await Auth.auth().createUser(withEmail: email, password: password)
                    user = result.user

                case "signInWithEmailAndPassword":
                    guard let email = userInfo["email"] as? String,
                          let password = userInfo["password"] as? String else {
                        postAuthError(requestId: requestId, code: "invalid-params", message: "Missing email or password")
                        return
                    }
                    let result = try await Auth.auth().signIn(withEmail: email, password: password)
                    user = result.user

                case "google":
                    guard let idToken = userInfo["idToken"] as? String else {
                        postAuthError(requestId: requestId, code: "invalid-params", message: "Missing ID token")
                        return
                    }
                    let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: nil)
                    let result = try await Auth.auth().signIn(with: credential)
                    user = result.user

                case "apple":
                    guard let idToken = userInfo["idToken"] as? String else {
                        postAuthError(requestId: requestId, code: "invalid-params", message: "Missing ID token")
                        return
                    }
                    let credential = OAuthProvider.credential(
                        withProviderID: "apple.com",
                        idToken: idToken,
                        rawNonce: nil
                    )
                    let result = try await Auth.auth().signIn(with: credential)
                    user = result.user

                case "signOut":
                    try Auth.auth().signOut()
                    user = nil

                default:
                    postAuthError(requestId: requestId, code: "unsupported-action", message: "Action not supported: \\(action)")
                    return
                }

                postAuthSuccess(requestId: requestId, user: user)

            } catch {
                postAuthError(requestId: requestId, code: "auth-error", message: error.localizedDescription)
            }
        }
    }

    @objc private func handleAppleSignInRequest(_ notification: Notification) {
        if #available(iOS 13.0, *) {
            let appleIDProvider = ASAuthorizationAppleIDProvider()
            let request = appleIDProvider.createRequest()
            request.requestedScopes = [.fullName, .email]

            let authorizationController = ASAuthorizationController(authorizationRequests: [request])

            let coordinator = AppleSignInCoordinator { [weak self] idToken in
                self?.postAppleSignInResult(idToken: idToken)
            }

            authorizationController.delegate = coordinator
            authorizationController.presentationContextProvider = coordinator
            self.appleSignInCoordinator = coordinator

            authorizationController.performRequests()
        } else {
            print("❌ Apple Sign-In requires iOS 13.0 or later")
            postAppleSignInResult(idToken: nil)
        }
    }

    private func postAuthSuccess(requestId: String, user: User?) {
        var responseInfo: [String: Any] = [
            "requestId": requestId,
            "status": "success"
        ]

        if let user = user {
            responseInfo["uid"] = user.uid
            responseInfo["email"] = user.email ?? ""
            responseInfo["displayName"] = user.displayName ?? ""
            responseInfo["photoUrl"] = user.photoURL?.absoluteString ?? ""
            responseInfo["isAnonymous"] = user.isAnonymous
            responseInfo["isEmailVerified"] = user.isEmailVerified
        }

        center.post(name: NSNotification.Name("AuthResponse"), object: nil, userInfo: responseInfo)
    }

    private func postAuthError(requestId: String, code: String, message: String) {
        let responseInfo: [String: Any] = [
            "requestId": requestId,
            "status": "error",
            "errorCode": code,
            "errorMessage": message
        ]
        center.post(name: NSNotification.Name("AuthResponse"), object: nil, userInfo: responseInfo)
    }

    private func postAuthState(user: User?) {
        var userInfo: [String: Any] = [:]

        if let user = user {
            userInfo["uid"] = user.uid
            userInfo["email"] = user.email ?? ""
            userInfo["displayName"] = user.displayName ?? ""
            userInfo["photoUrl"] = user.photoURL?.absoluteString ?? ""
            userInfo["isAnonymous"] = user.isAnonymous
            userInfo["isEmailVerified"] = user.isEmailVerified
        } else {
            userInfo["uid"] = ""
        }

        center.post(name: NSNotification.Name("AuthState"), object: nil, userInfo: userInfo)
    }

    private func postAppleSignInResult(idToken: String?) {
        let userInfo: [String: Any] = ["idToken": idToken as Any]
        center.post(name: NSNotification.Name("AppleSignInCompleted"), object: nil, userInfo: userInfo)
    }
}

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

#### 4. Initialize Bridge in App Delegate

In your `AppDelegate.swift` or app entry point:

```swift
import Firebase
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        FirebaseAuthBridge.shared.start()
        return true
    }
}

@main
struct YourApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

#### 5. Enable Sign in with Apple Capability

In Xcode:
1. Select your target
2. Go to "Signing & Capabilities"
3. Click "+ Capability"
4. Add "Sign in with Apple"

## Usage

### Basic Setup

```kotlin
import dev.com3run.firebaseauthkmp.AuthRepository
import dev.com3run.firebaseauthkmp.platformAuthBackend

// Create auth repository
val authRepository = AuthRepository(platformAuthBackend())

// Listen to auth state
authRepository.authState.collect { user ->
    if (user != null) {
        println("Signed in as: ${user.displayName ?: user.uid}")
    } else {
        println("Signed out")
    }
}
```

### Sign Up with Email/Password

```kotlin
val result = authRepository.signUpWithEmailAndPassword(
    email = "user@example.com",
    password = "securePassword123"
)

when (result) {
    is AuthResult.Success -> println("Account created!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Sign In with Email/Password

```kotlin
val result = authRepository.signInWithEmailAndPassword(
    email = "user@example.com",
    password = "securePassword123"
)

when (result) {
    is AuthResult.Success -> println("Signed in!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Sign In Anonymously

```kotlin
val result = authRepository.signInAnonymously()

when (result) {
    is AuthResult.Success -> println("Signed in as guest!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

### Sign In with Google

```kotlin
// Request ID token using platform-specific flow
val idToken = requestGoogleIdToken()

if (idToken != null) {
    val result = authRepository.signInWithGoogle(idToken)
    when (result) {
        is AuthResult.Success -> println("Signed in with Google!")
        is AuthResult.Failure -> println("Error: ${result.error}")
    }
}
```

### Sign In with Apple (iOS Only)

```kotlin
// Check if available (iOS only)
if (isAppleSignInAvailable()) {
    val idToken = requestAppleIdToken()

    if (idToken != null) {
        val result = authRepository.signInWithApple(idToken)
        when (result) {
            is AuthResult.Success -> println("Signed in with Apple!")
            is AuthResult.Failure -> println("Error: ${result.error}")
        }
    }
}
```

### Sign Out

```kotlin
authRepository.signOut()
```

### Password Reset

```kotlin
val result = authRepository.sendPasswordResetEmail("user@example.com")

when (result) {
    is AuthResult.Success -> println("Reset email sent!")
    is AuthResult.Failure -> println("Error: ${result.error}")
}
```

## Dependency Injection (Optional)

Using Koin:

```kotlin
import org.koin.dsl.module
import dev.com3run.firebaseauthkmp.*

val authModule = module {
    single<AuthBackend> { platformAuthBackend() }
    single { AuthRepository(get()) }
}
```

## Error Handling

The library provides detailed error types:

```kotlin
when (result) {
    is AuthResult.Success -> {
        // Handle success
    }
    is AuthResult.Failure -> {
        when (result.error) {
            AuthError.InvalidCredential -> println("Invalid credentials")
            AuthError.UserNotFound -> println("User not found")
            AuthError.WrongPassword -> println("Wrong password")
            AuthError.EmailAlreadyInUse -> println("Email already in use")
            AuthError.WeakPassword -> println("Password too weak")
            AuthError.InvalidEmail -> println("Invalid email format")
            is AuthError.Network -> println("Network error: ${result.error.message}")
            is AuthError.Unknown -> println("Unknown error: ${result.error.message}")
            else -> println("Error: ${result.error}")
        }
    }
}
```

## Testing

The library includes a `FakeAuthBackend` for testing:

```kotlin
import dev.com3run.firebaseauthkmp.FakeAuthBackend
import dev.com3run.firebaseauthkmp.AuthRepository

val fakeBackend = FakeAuthBackend()
val authRepository = AuthRepository(fakeBackend)

// Use in tests
```

## Troubleshooting

### Android: Google Sign-In Not Working

1. Make sure `google-services.json` is in the correct location
2. Verify SHA-1 fingerprint is added to Firebase Console
3. Check that `ActivityHolder.current` is set in MainActivity
4. Ensure Google Sign-In is enabled in Firebase Console

### iOS: Apple Sign-In Not Working

1. Verify "Sign in with Apple" capability is added in Xcode
2. Check that `FirebaseAuthBridge.shared.start()` is called
3. Ensure `GoogleService-Info.plist` is added to the project
4. Verify Apple Sign-In is enabled in Firebase Console

### Build Errors

If you get "cannot access" errors, make sure you've added the library dependency in the correct sourceSet (commonMain).

## Sample App

Check out the `composeApp` module in this repository for a complete working example.

## Support

For issues and questions:
- GitHub Issues: https://github.com/com3run/firebase-auth-kmp/issues
- Documentation: See LIBRARY_DOCUMENTATION.md

## License

MIT License
