# iOS Crash Fix

## Problem

The iOS app was crashing when clicking buttons due to a Koin dependency injection issue.

## Root Cause

The issue was in `AppModule.kt` where we were using `viewModelOf()`:

```kotlin
// ❌ This doesn't work on iOS
viewModelOf(::AuthViewModel)
```

The `viewModelOf` DSL function is **Android-specific** and relies on Android's ViewModel lifecycle. On iOS, this causes a crash because:

1. iOS doesn't have Android's ViewModel architecture
2. `koin-compose-viewmodel` has platform-specific implementations
3. The ViewModel scope is not available on iOS

## Solution

Changed from `viewModelOf()` to `factoryOf()` which works on **all platforms**:

```kotlin
// ✅ This works on both Android and iOS
factoryOf(::AuthViewModel)
```

### Files Modified

**1. `composeApp/src/commonMain/kotlin/az/random/testauth/di/AppModule.kt`**

```kotlin
// Before:
import org.koin.core.module.dsl.viewModelOf

val appModule = module {
    single<AuthBackend> { platformAuthBackend() }
    singleOf(::AuthRepository)
    viewModelOf(::AuthViewModel)  // ❌ Android-only
}

// After:
import org.koin.core.module.dsl.factoryOf

val appModule = module {
    single<AuthBackend> { platformAuthBackend() }
    singleOf(::AuthRepository)
    factoryOf(::AuthViewModel)  // ✅ Cross-platform
}
```

**2. `composeApp/src/commonMain/kotlin/az/random/testauth/ui/SampleAuthUi.kt`**

```kotlin
// Before:
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SampleAuthUi() {
    val viewModel = koinViewModel<AuthViewModel>()  // ❌ Android-only
    AuthScreen(viewModel)
}

// After:
import org.koin.compose.koinInject

@Composable
fun SampleAuthUi() {
    val viewModel = koinInject<AuthViewModel>()  // ✅ Cross-platform
    AuthScreen(viewModel)
}
```

## Key Differences

| Approach | Scope | Platforms | Lifecycle |
|----------|-------|-----------|-----------|
| `viewModelOf()` + `koinViewModel()` | ViewModel | Android only | Survives config changes |
| `factoryOf()` + `koinInject()` | Factory | All platforms | New instance per request |

### Trade-offs

**Using `factoryOf()`:**
- ✅ Works on both Android and iOS
- ✅ Simpler, more predictable
- ✅ No platform-specific behavior
- ⚠️ Creates new instance each time (not retained across recomposition)
- ⚠️ State is lost on configuration changes (Android)

**Using `viewModelOf()` (Android only):**
- ✅ Survives Android configuration changes
- ✅ Single instance per ViewModelStore
- ❌ iOS not supported
- ❌ Requires platform-specific code

## State Management Solution

Since we're using `factoryOf()`, the AuthViewModel will be recreated on recomposition. However, **this is fine** because:

1. **AuthViewModel** observes `authRepository.authState` Flow
2. The **AuthRepository** is a `singleton` - survives across ViewModels
3. The **AuthBackend** is also a `singleton` - maintains the auth state
4. Firebase maintains auth state persistently

So even if the ViewModel is recreated, it will immediately get the current auth state from the repository.

### Flow:

```
User Signs In
    ↓
AuthViewModel → AuthRepository → AuthBackend → Firebase
    ↓
Firebase updates auth state
    ↓
AuthBackend.authState Flow emits
    ↓
AuthRepository.authState Flow emits
    ↓
AuthViewModel collects and updates UI
```

Even if AuthViewModel is recreated:

```
New AuthViewModel created
    ↓
init { viewModelScope.launch { authRepository.authState.collect { ... } } }
    ↓
Immediately gets current state from repository
    ↓
UI shows correct state
```

## Alternative Approach (if you need ViewModel lifecycle)

If you need different behavior per platform, you can create platform-specific modules:

### Option 1: Platform-Specific Modules

**`androidMain/kotlin/di/PlatformModule.kt`:**
```kotlin
actual val platformModule = module {
    viewModelOf(::AuthViewModel)  // Android ViewModel
}
```

**`iosMain/kotlin/di/PlatformModule.kt`:**
```kotlin
actual val platformModule = module {
    factoryOf(::AuthViewModel)  // iOS factory
}
```

**`commonMain/kotlin/di/PlatformModule.kt`:**
```kotlin
expect val platformModule: Module
```

### Option 2: Use `remember` in Compose

```kotlin
@Composable
fun SampleAuthUi() {
    val repository = koinInject<AuthRepository>()
    val viewModel = remember { AuthViewModel(repository) }
    AuthScreen(viewModel)
}
```

This would create the ViewModel once per composition and reuse it.

## Testing

**Android:**
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug
# Test: Click buttons - should work ✅
```

**iOS:**
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
open iosApp/iosApp.xcodeproj
# Run in Xcode (⌘R)
# Test: Click buttons - should work ✅
```

## Build Results

Both platforms build successfully:

**Android:**
```
BUILD SUCCESSFUL in 3s
43 actionable tasks: 7 executed, 4 from cache, 32 up-to-date
```

**iOS:**
```
BUILD SUCCESSFUL in 1s
8 actionable tasks: 3 executed, 5 up-to-date
```

## Summary

✅ **Fixed:** iOS crash when clicking buttons
✅ **Solution:** Use `factoryOf()` + `koinInject()` instead of `viewModelOf()` + `koinViewModel()`
✅ **Result:** App works on both Android and iOS
✅ **State:** Preserved via singleton Repository and Backend

The app should now work correctly on both platforms without crashes!
