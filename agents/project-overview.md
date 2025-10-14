# Project Overview

## Project Identity

**Name**: Firebase Auth KMP Library
**Package**: `dev.com3run.testauth`
**Platform**: Kotlin Multiplatform (Android + iOS + Desktop/JVM)
**Purpose**: Production-ready Firebase Authentication library with comprehensive feature support

## Architecture

### Design Pattern: Clean Architecture

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
       ┌───────┴────────┬─────────────┐
       ▼                ▼              ▼
┌──────────────┐  ┌─────────────┐  ┌──────────────┐
│  Android     │  │     iOS     │  │   Desktop    │
│  Firebase    │  │  Firebase   │  │  Firebase    │
│  SDK         │  │  Bridge     │  │  REST API    │
│(androidMain) │  │ (iosMain)   │  │  (jvmMain)   │
└──────────────┘  └─────────────┘  └──────────────┘
```

### Core Components

#### 1. AuthBackend (Interface)
- **Location**: `composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthBackend.kt`
- **Purpose**: Platform-agnostic contract for Firebase implementations
- **Key Features**:
  - Exposes `StateFlow<AuthUser?>` for reactive auth state
  - Defines all auth operations (sign in, sign up, password reset, profile management)
  - Platform-specific implementations via expect/actual pattern

#### 2. AuthRepository
- **Location**: `composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthRepository.kt`
- **Purpose**: High-level API consumed by the app
- **Responsibilities**:
  - Input validation before delegating to AuthBackend
  - Returns `AuthResult` (Success/Failure) for all operations
  - Business logic layer

#### 3. AuthModels
- **Location**: `composeApp/src/commonMain/kotlin/az/random/testauth/auth/AuthModels.kt`
- **Models**:
  - `AuthUser`: Platform-agnostic user model
  - `AuthError`: Comprehensive error types (InvalidCredential, EmailAlreadyInUse, WeakPassword, RequiresRecentLogin, etc.)
  - `AuthResult`: Sealed class for Success/Failure outcomes

#### 4. Platform Implementations

**Android**: `AndroidFirebaseAuthBackend`
- Location: `composeApp/src/androidMain/kotlin/az/random/testauth/auth/`
- Uses Firebase Auth SDK (`firebase-auth-ktx`)
- Requires `google-services.json` in composeApp module
- Direct Firebase API integration

**iOS**: `IosFirebaseAuthBackend`
- Location: `composeApp/src/iosMain/kotlin/az/random/testauth/auth/`
- Uses notification-based bridge to Swift Firebase SDK
- Requires `GoogleService-Info.plist` and Swift bridge implementation
- NSNotificationCenter-based Kotlin-Swift communication

**Desktop/JVM**: `DesktopFirebaseAuthBackend`
- Location: `firebase-auth-kmp/src/jvmMain/kotlin/dev/com3run/firebaseauthkmp/`
- Uses Firebase Authentication REST API
- Requires `firebase-config.json` with API key and project ID
- HTTP-based implementation using Ktor Client
- Supports core auth features; OAuth requires external browser flow

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

## Dependency Injection

**Framework**: Koin

**AppModule** (`composeApp/src/commonMain/kotlin/az/random/testauth/di/AppModule.kt`)
- Provides singleton `AuthBackend` via `platformAuthBackend()` expect/actual function
- Provides singleton `AuthRepository`
- Provides factory `AuthViewModel`

**Platform Setup**:
- **Android**: Initialize Koin in `MainActivity.onCreate()` with `startKoin { modules(appModule) }`
- **iOS**: Initialize Koin in `MainViewController.kt` before creating compose view
- **Desktop**: Initialize Koin in `main()` function before creating window

## Supported Features

- Email/Password authentication
- OAuth providers (Google, Apple, Facebook)
- Anonymous authentication
- Password reset
- Email verification
- Profile updates (display name, photo URL)
- Account linking/unlinking
- Account deletion
- Reauthentication
- Reactive auth state (`StateFlow`)

## OAuth Provider IDs

When working with account linking/unlinking operations:
- Google: `"google.com"`
- Apple: `"apple.com"`
- Facebook: `"facebook.com"`
- Email/Password: `"password"`

## Package Namespace

All code resides in: `dev.com3run.testauth`
Auth library namespace: `dev.com3run.testauth.auth`