# Development Workflow

## Build Commands

### Build the Project
```bash
# Full build
./gradlew build

# Clean build
./gradlew clean build

# Build Android debug APK
./gradlew :composeApp:assembleDebug

# Build Android release APK
./gradlew :composeApp:assembleRelease
```

## Testing Strategy

### Test-Driven Development (TDD)
- **Test Location**: `composeApp/src/commonTest/kotlin/az/random/testauth/auth/`
- **Test Implementation**: `FakeAuthBackend` for mocking Firebase
- **Approach**: Write tests first, then implement functionality
- **Rule**: All tests must pass before implementation is considered complete

### Running Tests

```bash
# Run all tests
./gradlew :composeApp:check

# Run specific test class
./gradlew :composeApp:testDebugUnitTest --tests "dev.com3run.testauth.auth.AuthRepositoryTest"

# Run all unit tests
./gradlew :composeApp:testDebugUnitTest

# Run with detailed output
./gradlew :composeApp:check --info
```

## Platform-Specific Commands

### Android

```bash
# Install debug APK on connected device
./gradlew :composeApp:installDebug

# Install and launch
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb shell am start -n dev.com3run.testauth/.MainActivity

# View logs
adb logcat -s FirebaseAuth

# Clear app data (useful for testing)
adb shell pm clear dev.com3run.testauth
```

### iOS

```bash
# Open project in Xcode (must be done from Xcode)
# Navigate to iosApp directory and open .xcodeproj

# Build from command line (requires Xcode)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug

# Note: iOS requires additional setup in Xcode for Firebase
# See firebase-setup.md for details
```

### Desktop

```bash
# Run desktop application
./gradlew :composeApp:run

# Build desktop distribution for current OS
./gradlew :composeApp:packageDistributionForCurrentOS

# Create platform-specific installers
./gradlew :composeApp:package

# Build specific formats
./gradlew :composeApp:packageDmg    # macOS
./gradlew :composeApp:packageMsi    # Windows
./gradlew :composeApp:packageDeb    # Linux
```

## Git Workflow

### Current Branch Information
- **Main Branch**: `main`
- **Recent Releases**:
  - v1.0.0 - Maven Central Publication
  - Initial Firebase Auth KMP Library release

### Common Git Operations

```bash
# Check status
git status

# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: add new authentication feature"

# Push to remote
git push origin main

# Create and switch to feature branch
git checkout -b feature/new-auth-flow

# View commit history
git log --oneline -10
```

## Development Best Practices

### Before Committing
1. Run all tests: `./gradlew :composeApp:check`
2. Ensure clean build: `./gradlew clean build`
3. Verify no lint errors
4. Check that both Android and iOS implementations are updated (if applicable)

### Code Review Checklist
- [ ] All tests pass
- [ ] New features have corresponding tests
- [ ] Code follows project architecture (AuthBackend → AuthRepository pattern)
- [ ] Platform-specific code properly isolated in androidMain/iosMain
- [ ] Error handling uses AuthError types consistently
- [ ] Documentation updated if public API changed

## Gradle Configuration

### Build Configuration
- **compileSdk**: Defined in version catalog
- **minSdk**: Defined in version catalog
- **targetSdk**: Defined in version catalog
- **JVM Target**: Java 11
- **Kotlin Native Cache**: Disabled (`kotlin.native.cacheKind=none`)
- **Gradle Configuration Cache**: Enabled

### Common Gradle Tasks
```bash
# List all tasks
./gradlew tasks

# List all dependencies
./gradlew :composeApp:dependencies

# Refresh dependencies
./gradlew --refresh-dependencies

# Build with stacktrace for debugging
./gradlew build --stacktrace

# Build with debug info
./gradlew build --debug
```

## Debugging Tips

### Android Debugging
- Use Android Studio's built-in debugger
- Set breakpoints in `composeApp/src/androidMain` for platform code
- Check Firebase Console for auth-related issues
- Use `Log.d()` for quick debugging (remember to remove before committing)

### iOS Debugging
- Use Xcode debugger for Swift bridge code
- Check iOS console for Firebase initialization issues
- Verify `GoogleService-Info.plist` is properly configured
- Monitor NSNotificationCenter messages between Kotlin and Swift

### Common Issues
- **Build fails**: Try `./gradlew clean build`
- **Tests fail randomly**: Check if Firebase emulator is needed
- **iOS bridge not working**: Verify notification observer setup in Swift
- **Android Google Sign-In fails**: Ensure `ActivityHolder.current` is set

## Performance Monitoring

### Build Performance
```bash
# Build with build scan
./gradlew build --scan

# Profile build performance
./gradlew build --profile
```

### Test Performance
```bash
# Run tests with timing
./gradlew test --info | grep "Test"
```