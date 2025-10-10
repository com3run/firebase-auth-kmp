# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Kotlin Multiplatform (KMP)** Firebase Authentication library targeting **Android** and **iOS**. The library provides a clean, production-ready authentication solution with comprehensive feature support including email/password, OAuth providers (Google, Apple, Facebook), anonymous auth, and advanced account management.

## Commands

### Build & Test
```bash
# Run all tests
./gradlew :composeApp:check

# Run specific test class
./gradlew :composeApp:testDebugUnitTest --tests "az.random.testauth.auth.AuthRepositoryTest"

# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Clean build
./gradlew clean
```

### Platform-Specific
```bash
# Android: Install and run on connected device
./gradlew :composeApp:installDebug

# iOS: Open project in Xcode (must be done from Xcode or IDE run configuration)
# Navigate to iosApp directory and open .xcodeproj
```

## Architecture

### Core Design Pattern

The library follows **clean architecture** with platform-agnostic interfaces:

```
┌─────────────────────────────────────┐
│      AuthRepository                 │  ← App layer (validation + high-level API)
│  (composeApp/commonMain/auth/)     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      AuthBackend (Interface)        │  ← Platform-agnostic contract
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌──────────────┐  ┌─────────────────┐
│  Android     │  │     iOS         │
│  Firebase    │  │   Firebase      │
│  (androidMain)  │  (iosMain)      │
└──────────────┘  └─────────────────┘
```

### Key Components

**AuthBackend** (`composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthBackend.kt`)
- Interface defining the contract for platform-specific Firebase implementations
- Exposes auth state as `StateFlow<AuthUser?>`
- Methods for all auth operations (sign in, sign up, password reset, profile management, etc.)

**AuthRepository** (`composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthRepository.kt`)
- High-level API consumed by the app
- Performs input validation before delegating to AuthBackend
- Returns `AuthResult` (Success/Failure) for all operations

**AuthModels** (`composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthModels.kt`)
- `AuthUser`: Platform-agnostic user model
- `AuthError`: Comprehensive error types (InvalidCredential, EmailAlreadyInUse, WeakPassword, etc.)
- `AuthResult`: Sealed class for Success/Failure outcomes

**Platform Implementations**
- Android: `AndroidFirebaseAuthBackend` (composeApp/src/androidMain/kotlin/az/random/testauth/auth/)
  - Uses Firebase Auth SDK (`firebase-auth-ktx`)
  - Requires `google-services.json` in composeApp module
- iOS: `IosFirebaseAuthBackend` (composeApp/src/iosMain/kotlin/az/random/testauth/auth/)
  - Uses notification-based bridge to Swift Firebase SDK
  - Requires `GoogleService-Info.plist` and Swift bridge implementation

### Dependency Injection

The project uses **Koin** for DI across all platforms:

**AppModule** (`composeApp/src/commonMain/kotlin/az/random/testauth/di/AppModule.kt`)
- Provides singleton `AuthBackend` via `platformAuthBackend()` expect/actual function
- Provides singleton `AuthRepository`
- Provides factory `AuthViewModel`

**Platform Setup**
- Android: Initialize Koin in `MainActivity.onCreate()` with `startKoin { modules(appModule) }`
- iOS: Initialize Koin in `MainViewController.kt` before creating compose view

### Testing Strategy

**Test-Driven Development (TDD)**
- Tests are in `composeApp/src/commonTest/kotlin/az/random/testauth/auth/`
- Use `FakeAuthBackend` implementation for testing
- Tests define expected behavior first, then implementation follows
- All tests must pass before implementation is considered complete

## Module Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/az/random/testauth/
│   │   ├── auth/           # Core auth library (AuthRepository, AuthBackend, models)
│   │   ├── ui/             # UI screens and ViewModels (SignIn, SignUp, Home)
│   │   ├── di/             # Koin dependency injection module
│   │   └── App.kt          # Main Compose app entry point
│   ├── androidMain/kotlin/az/random/testauth/
│   │   ├── auth/           # Android Firebase implementation
│   │   └── MainActivity.kt # Android entry point
│   ├── iosMain/kotlin/az/random/testauth/
│   │   ├── auth/           # iOS Firebase implementation (notification bridge)
│   │   └── MainViewController.kt # iOS entry point
│   └── commonTest/kotlin/az/random/testauth/
│       └── auth/           # Auth tests with FakeAuthBackend
iosApp/                     # iOS app wrapper (Swift/SwiftUI)
```

## Firebase Configuration

### Android
1. Place `google-services.json` in `composeApp/` directory
2. Firebase Auth dependency already configured in build.gradle.kts
3. Google Sign-In requires `play-services-auth:21.2.0`
4. Set `ActivityHolder.current` in MainActivity for Google Sign-In flow

### iOS
1. Add Firebase SDK via CocoaPods or Swift Package Manager to `iosApp`
2. Place `GoogleService-Info.plist` in iosApp directory
3. Implement Swift bridge (`FirebaseAuthBridge`) that:
   - Listens to `AuthRequest` notifications from Kotlin
   - Performs Firebase operations in Swift
   - Posts `AuthResponse` notifications back to Kotlin
   - Monitors auth state changes and posts `AuthState` notifications
4. Initialize bridge in iOS app entry point

## OAuth Provider IDs

When working with account linking/unlinking operations, use these provider IDs:
- Google: `"google.com"`
- Apple: `"apple.com"`
- Facebook: `"facebook.com"`
- Email/Password: `"password"`

## Important Implementation Notes

1. **Token Acquisition**: The library expects tokens (Google ID token, Apple ID token, Facebook access token) to be obtained using platform-specific UI flows. AuthRepository receives these tokens as strings.

2. **Auth State Flow**: Use `authRepository.authState` (StateFlow) for reactive UI updates. It automatically emits current user state or null when signed out.

3. **Error Handling**: Always pattern match on `AuthError` types to provide specific user feedback. Some operations may require re-authentication (`RequiresRecentLogin`).

4. **Activity Reference (Android)**: Google Sign-In requires `ActivityHolder.current` to be set in MainActivity. Don't forget to clear it in `onDestroy()`.

5. **iOS Notification Bridge**: iOS implementation relies on NSNotificationCenter for Kotlin-Swift communication. Each request gets a unique `requestId` to match responses.

## Package Structure

All code resides in `az.random.testauth` package. The auth library is specifically in `az.random.testauth.auth` namespace.

## Build Configuration

- **compileSdk**: Defined in version catalog
- **minSdk**: Defined in version catalog
- **targetSdk**: Defined in version catalog
- **JVM Target**: Java 11
- **Kotlin Native Cache**: Disabled (`kotlin.native.cacheKind=none`)
- **Gradle Configuration Cache**: Enabled
