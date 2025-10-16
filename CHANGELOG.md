# Changelog

All notable changes to Firebase Auth KMP will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-10-16

### Fixed
- 📦 **Maven Central JVM Module** - Ensured JVM/Desktop artifact is properly published to Maven Central
  - Verified all platform-specific modules are available
  - Fixed umbrella dependency resolution for Desktop targets
  - Users can now use `implementation("dev.com3run:firebase-auth-kmp:1.0.2")` in `commonMain` for all platforms including Desktop

### Added
- 📚 **JitPack documentation** - Added clear instructions for JitPack users
  - Documented platform-specific artifact requirements for JitPack
  - Added troubleshooting section for dependency resolution issues
  - Recommended Maven Central as primary distribution channel

### Changed
- 📖 **Updated README.md** with installation options
  - Differentiated between Maven Central (recommended) and JitPack
  - Added platform-specific dependency examples for JitPack users
  - Clarified umbrella dependency behavior across repositories

## [1.0.1] - 2025-10-14

### Added
- ✨ **Desktop/JVM platform support** - Firebase Authentication now works on Windows, macOS, and Linux
  - REST API-based implementation
  - Email/Password authentication
  - Anonymous authentication
  - OAuth provider support (requires external browser flow)
  - Automatic configuration loading from `firebase-config.json`
  - Environment variable fallback for API key
  - Multi-path config file discovery
  - Comprehensive desktop setup documentation
- 🚀 **Android auto-initialization** - Zero-code setup via ContentProvider
  - Automatic `ActivityHolder` management
  - Activity lifecycle tracking
  - No manual initialization required
  - Backward compatible with existing code
- 📱 **iOS bridge template** - Ready-to-use Swift bridge file
  - Well-documented with setup instructions
  - Simplified one-line initialization
  - Improved error messages
  - Better code organization
- 📚 **Easy Integration Guide** (`EASY-INTEGRATION.md`)
  - Platform-specific quick start guides
  - Before/After comparisons
  - 5-minute setup instructions
  - Troubleshooting tips
  - Non-Koin usage examples
- 📘 **30-Second Quick Start** (`QUICKSTART.md`)
  - Ultra-simple setup guide
  - Direct GitHub links for iOS bridge
  - Minimal example code
- 🏗️ **Comprehensive iOS Setup Guide** (`docs/IOS_SETUP_GUIDE.md`)
  - Detailed explanation of bridge pattern
  - Why manual Swift file is necessary
  - Step-by-step Xcode instructions
  - Comparison with other KMP libraries
  - Troubleshooting section
- 🔄 **Migration Guide** (`MIGRATION.md`)
  - Step-by-step upgrade instructions
  - Platform-specific migration steps
  - Rollback procedures
  - Common issues and solutions
- 🏗️ **Desktop documentation** (`agents/desktop-setup.md`)
  - Complete feature matrix
  - Usage examples
  - OAuth implementation guide
  - Platform comparison table

### Changed
- 📖 **Updated README.md** with:
  - Desktop platform badge
  - Tiered documentation approach (30s, 2min, 5min)
  - Simplified integration examples
  - Auto-initialization highlights
  - Better platform comparison table
  - Link to QUICKSTART.md
- 📝 **Enhanced agents documentation**:
  - `project-overview.md` - Added desktop architecture
  - `development-workflow.md` - Added desktop build commands
  - `firebase-setup.md` - Clarified platform-specific setup
  - `README.md` - Added desktop-setup.md reference
- 🔧 **Improved error messages**:
  - Desktop: Clear instructions when API key is missing with current working directory
  - Desktop: Helpful guidance for configuration file location
  - All platforms: More actionable error feedback
- 📚 **Better iOS documentation**:
  - Direct download links for bridge file
  - Explanation of why bridge is needed
  - Comparison with C-interop approach

### Deprecated
- ⚠️ **Android manual `ActivityHolder` setup** (still works, but no longer needed)
  - `ActivityHolder.current = this` in `onCreate()` is now optional
  - `ActivityHolder.current = null` in `onDestroy()` is now optional
  - Auto-initialization handles this automatically

### Fixed
- 🐛 Android: Activity lifecycle edge cases now handled automatically
- 🐛 Desktop: Config file discovery now checks multiple paths (current dir, parent, user.dir)
- 🐛 Desktop: Better error handling for missing or malformed configuration
- 🐛 iOS: Improved bridge initialization reliability
- 🐛 Documentation: Fixed version inconsistencies across all docs

### Internal
- Added `FirebaseAuthInitializer` ContentProvider for Android
- Added `ActivityLifecycleTracker` for automatic Activity management
- Added `DesktopFirebaseAuthBackend` with REST API implementation
- Added `FirebaseRestClient` for HTTP-based authentication
- Added `PlatformAuth.jvm.kt` for desktop platform functions
- Updated build configuration to support JVM target
- Added Ktor client dependencies for desktop HTTP requests
- Added kotlinx-serialization for JSON handling
- Improved desktop config file loading with multiple fallback paths
- Maven Central publishing setup with NMCP plugin
- Signing configuration for release artifacts

## [1.0.0] - 2025-10-08

### Added
- 🎉 **Initial release** of Firebase Auth KMP
- ✅ **Android support** with native Firebase SDK
  - Email/Password authentication
  - Google Sign-In
  - Anonymous authentication
  - Profile management
  - Password reset
  - Email verification
- ✅ **iOS support** with notification-based bridge
  - Email/Password authentication
  - Google Sign-In
  - Apple Sign-In
  - Anonymous authentication
  - Profile management
  - Complete feature parity with Android
- 🔄 **Reactive auth state** with Kotlin Flow
- 🛡️ **Type-safe error handling** with sealed classes
- 🧪 **Testing support** with FakeAuthBackend
- 📘 **Comprehensive documentation**
  - Library integration guide
  - Usage examples
  - Troubleshooting guide
  - Apple Sign-In setup guide
- 🏗️ **Clean architecture** design
  - Platform-agnostic AuthRepository
  - Platform-specific AuthBackend implementations
  - Common data models
- 📦 **Sample app** demonstrating all features

### Technical Details
- Kotlin Multiplatform 2.0+
- Android API 24+
- iOS 13.0+
- Compose Multiplatform UI
- Koin dependency injection
- Coroutines-based async API

---

## Release Notes

### v1.0.1 Highlights

This release significantly improves developer experience with:

1. **Desktop Support** - Firebase Auth now works on desktop applications (Windows, macOS, Linux)
2. **Zero-Config Android** - No manual initialization code needed
3. **Simplified iOS Setup** - Copy one file, add one line
4. **Better Documentation** - Easy integration guide gets you started in 5 minutes
5. **Tiered Documentation** - 30s quickstart, 2min setup, 5min full guide

**Breaking Changes:** None! v1.0.1 is fully backward compatible with v1.0.0.

**Migration:** See [MIGRATION.md](MIGRATION.md) for upgrade instructions.

### Platform Support Matrix

| Feature | Android | iOS | Desktop |
|---------|---------|-----|---------|
| Email/Password | ✅ | ✅ | ✅ |
| Anonymous Auth | ✅ | ✅ | ✅ |
| Google Sign-In | ✅ | ✅ | ⚠️ Manual |
| Apple Sign-In | ❌ | ✅ | ⚠️ Manual |
| Facebook Sign-In | ✅ | ✅ | ⚠️ Manual |
| Auto-Init | ✅ | ❌ | ❌ |
| Offline Support | ✅ | ✅ | ❌ |
| Account Linking | ✅ | ✅ | ❌ |

✅ = Full support | ⚠️ = Partial support | ❌ = Not supported

---

## Upgrade Guide

### From v1.0.0 to v1.0.1

**Android users:** Remove manual `ActivityHolder` code - it's automatic now!
**iOS users:** Consider using the new bridge template (optional)
**Desktop users:** Welcome! See [QUICKSTART.md](QUICKSTART.md) for 30-second setup
**All users:** Update dependency to `1.0.1`

See [MIGRATION.md](MIGRATION.md) for detailed instructions.

---

## Links

- [GitHub Repository](https://github.com/com3run/firebase-auth-kmp)
- [Maven Central](https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp)
- [Documentation](agents/)
- [Issue Tracker](https://github.com/com3run/firebase-auth-kmp/issues)

---

[1.0.1]: https://github.com/com3run/firebase-auth-kmp/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/com3run/firebase-auth-kmp/releases/tag/v1.0.0
