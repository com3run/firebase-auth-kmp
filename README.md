This is a Kotlin Multiplatform project targeting Android and iOS.

What's included for Authentication (TDD):
- Common auth domain in composeApp/src/commonMain/kotlin/az/random/testauth/auth
  - AuthRepository: high-level API for sign-in with Google, Apple, Facebook, Anonymous, plus sign-out and state flow.
  - AuthBackend: interface to be implemented per-platform (Android/iOS) using Firebase Auth SDKs.
  - Models: AuthUser, AuthResult, AuthError.
- Tests in composeApp/src/commonTest/kotlin/az/random/testauth/auth define the expected behavior first (TDD) and currently all pass using a fake backend.

How to run tests
- From IDE: run the AuthRepositoryTest class.
- From terminal: ./gradlew :composeApp:check

Sample UI for testing
- The app now includes a simple cross-platform Compose screen to try Anonymous and Google sign-in.
- It shows current auth state, and provides:
  - "Sign in Anonymously" button
  - A text field to paste a Google ID Token and a "Sign in with Google (token)" button
  - "Sign out" button
- This works on both Android and iOS. For Google sign-in, you can obtain an ID token using native platform flows (e.g., Google Sign-In on Android/iOS) and paste it into the field to test the backend wiring.

Real Firebase integration
1) Android (implemented):
   - Backend: composeApp/src/androidMain/kotlin/az/random/testauth/auth/AndroidFirebaseAuthBackend.kt implements AuthBackend using Firebase Auth (firebase-auth-ktx).
   - Dependency already added in composeApp/androidMain (libs.firebase-auth-ktx).
   - Runtime setup needed: supply google-services.json in composeApp module (or initialize FirebaseApp programmatically). Sign-in token acquisition (Google/Facebook/Apple) happens in UI; pass the token strings into AuthRepository methods.
   - Usage example:
     val repo = az.random.testauth.auth.AuthRepository(
         az.random.testauth.auth.AndroidFirebaseAuthBackend()
     )
     // Google: repo.signInWithGoogle(idToken)
     // Facebook: repo.signInWithFacebook(accessToken)
     // Apple (OIDC): repo.signInWithApple(idToken)
     // Anonymous: repo.signInAnonymously()
2) iOS (to be finalized):
   - A placeholder IosFirebaseAuthBackend exists in composeApp/iosMain. To enable real auth:
     - Add Firebase Auth to iosApp via SPM or CocoaPods and configure GoogleService-Info.plist.
     - Expose FirebaseAuth calls to Kotlin/Native and map to AuthUser in IosFirebaseAuthBackend.
   - After you share the plist and provider configs (Google/Facebook/Apple), we will wire the actual iOS implementation.
3) Keep tests green: no change needed; tests target the common contract and use a fake backend.

Folder overview

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ./gradlew :composeApp:assembleDebug
- on Windows
  .\gradlew.bat :composeApp:assembleDebug

Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about Kotlin Multiplatform: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html