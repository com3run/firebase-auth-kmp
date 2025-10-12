# Koin Dependency Injection Setup

This project uses [Koin](https://insert-koin.io/) for dependency injection, providing a clean and simple way to manage dependencies across the Firebase Auth library.

## Why Koin?

- **Kotlin-first**: Built specifically for Kotlin
- **Multiplatform support**: Works seamlessly with KMP
- **Lightweight**: No code generation, pure Kotlin DSL
- **Easy to test**: Simple mocking and testing capabilities
- **Compose integration**: Native support for Jetpack Compose

## Dependencies

The following Koin dependencies are included in `gradle/libs.versions.toml`:

```toml
[versions]
koin = "4.0.0"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
```

## Module Structure

### AppModule (`di/AppModule.kt`)

```kotlin
val appModule = module {
    // Platform-specific AuthBackend (singleton)
    single<AuthBackend> { platformAuthBackend() }

    // AuthRepository (singleton)
    singleOf(::AuthRepository)

    // AuthViewModel (ViewModel lifecycle)
    viewModelOf(::AuthViewModel)
}
```

**Provided Dependencies:**
- `AuthBackend` - Platform-specific implementation (Android/iOS)
- `AuthRepository` - Main auth API with validation
- `AuthViewModel` - ViewModel for UI state management

## Integration

### 1. App Initialization

In `App.kt`, Koin is initialized using `KoinApplication`:

```kotlin
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(appModule)
        }
    ) {
        MaterialTheme {
            SampleAuthUi()
        }
    }
}
```

### 2. ViewModel Injection in Compose

Use `koinViewModel()` to inject ViewModels in your composables:

```kotlin
@Composable
fun SampleAuthUi() {
    val viewModel = koinViewModel<AuthViewModel>()
    AuthScreen(viewModel)
}
```

### 3. Manual Injection (if needed)

For non-Compose code, use `get()` to retrieve dependencies:

```kotlin
class SomeClass(private val authRepository: AuthRepository = get())
```

Or inject via constructor:

```kotlin
class SomeClass(private val authRepository: AuthRepository)

// In Koin module
single { SomeClass(get()) }
```

## Usage in Your Own Projects

### Basic Setup

1. **Add Koin dependencies** to your `build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation("io.insert-koin:koin-core:4.0.0")
    implementation("io.insert-koin:koin-compose:4.0.0")
    implementation("io.insert-koin:koin-compose-viewmodel:4.0.0")
}

androidMain.dependencies {
    implementation("io.insert-koin:koin-android:4.0.0")
}
```

2. **Copy the auth module** to your project

3. **Create or update your Koin module**:
```kotlin
val yourAppModule = module {
    // Include auth dependencies
    single<AuthBackend> { platformAuthBackend() }
    singleOf(::AuthRepository)
    viewModelOf(::AuthViewModel)

    // Add your own dependencies
    single { YourService() }
    viewModelOf(::YourViewModel)
}
```

4. **Initialize Koin in your App**:
```kotlin
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(yourAppModule)
        }
    ) {
        // Your app content
    }
}
```

### Testing with Koin

Create test modules with fake implementations:

```kotlin
val testModule = module {
    single<AuthBackend> { FakeAuthBackend() }
    singleOf(::AuthRepository)
}

class AuthTest {
    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    fun testAuth() {
        val repo = get<AuthRepository>()
        // Test your auth logic
    }
}
```

## Advanced Usage

### Scoped Dependencies

For dependencies that should be recreated with a specific scope:

```kotlin
val appModule = module {
    // Activity-scoped dependency (Android)
    scope<ComponentActivity> {
        scoped { SomeScopedDependency() }
    }

    // Custom scope
    scope(named("UserSession")) {
        scoped { UserSessionData() }
    }
}
```

### Named Dependencies

When you need multiple instances of the same type:

```kotlin
val appModule = module {
    single(named("production")) { ProductionAuthBackend() }
    single(named("staging")) { StagingAuthBackend() }
}

// Usage
val prodBackend: AuthBackend = get(named("production"))
```

### Lazy Injection

```kotlin
class MyClass {
    private val authRepo: AuthRepository by inject()
}
```

### Parameters

Pass parameters when resolving:

```kotlin
val appModule = module {
    factory { (userId: String) -> UserProfile(userId, get()) }
}

// Usage
val profile: UserProfile = get { parametersOf("user123") }
```

## Platform-Specific Setup

### Android

No additional setup required. Koin Android module is automatically initialized.

### iOS

For iOS native code (Swift), you'll need to expose Koin-managed instances through Kotlin/Native:

```kotlin
// In iosMain
object KoinHelper {
    fun getAuthRepository(): AuthRepository = KoinPlatform.getKoin().get()
}
```

## Best Practices

1. **Use `singleOf` and `viewModelOf`** - More concise syntax
2. **Keep modules focused** - Separate modules by feature
3. **Avoid circular dependencies** - Use interfaces when needed
4. **Use `single` for stateful objects** - Repository, DataStore, etc.
5. **Use `factory` for stateless objects** - Use cases, mappers, etc.
6. **Test your modules** - Verify all dependencies can be resolved

## Troubleshooting

### "No definition found for..."

Make sure:
- The dependency is declared in a module
- The module is loaded: `modules(yourModule)`
- You're using the correct type/name

### ViewModel not recreating on navigation

Use `koinViewModel()` in Compose, not `koinNavViewModel()` unless you need specific nav behavior.

### Circular dependency

Refactor to use interfaces or split the dependency.

## Resources

- [Koin Documentation](https://insert-koin.io/)
- [Koin Compose](https://insert-koin.io/docs/reference/koin-compose/compose)
- [Koin Multiplatform](https://insert-koin.io/docs/reference/koin-mp/kmp)

## Migration Guide

### From Manual DI

**Before:**
```kotlin
@Composable
fun MyScreen() {
    val repo = AuthRepository(platformAuthBackend())
    val viewModel = remember { AuthViewModel(repo) }
}
```

**After:**
```kotlin
@Composable
fun MyScreen() {
    val viewModel = koinViewModel<AuthViewModel>()
}
```

### From Hilt/Dagger

Key differences:
- No annotation processing
- Runtime dependency resolution
- Simple DSL-based configuration
- No need for `@Inject`, `@HiltViewModel`, etc.

## Summary

Koin provides a lightweight, Kotlin-friendly DI solution for your Firebase Auth library:

✅ Easy setup with minimal boilerplate
✅ Perfect for Kotlin Multiplatform
✅ Native Compose integration
✅ Simple testing
✅ No code generation

Your auth library is now ready to be used in any KMP project with clean dependency injection!
