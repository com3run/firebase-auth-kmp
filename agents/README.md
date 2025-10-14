# Agents Folder

This folder contains comprehensive documentation and instructions that guide AI agents (like Claude Code) when working with this Firebase Auth KMP project.

## Purpose

These documents capture the architectural decisions, development workflows, configuration requirements, and coding standards that define how this project should be built and maintained. They serve as a reference for:

- AI assistants helping with code development
- New developers onboarding to the project
- Maintaining consistency across contributions
- Documenting best practices and patterns

## Documents

### 1. [project-overview.md](./project-overview.md)
**What it covers**: High-level architecture and project structure

- Project identity and technology stack
- Clean architecture pattern (AuthBackend → AuthRepository)
- Core components and their responsibilities
- Module structure and organization
- Dependency injection with Koin
- Supported authentication features
- Package namespaces

**When to reference**: Understanding the overall system design, locating specific components, or planning new features.

### 2. [development-workflow.md](./development-workflow.md)
**What it covers**: Build, test, and development processes

- Build commands for Android and iOS
- Test-Driven Development (TDD) approach
- Platform-specific development commands
- Git workflow and branching strategy
- Code review checklist
- Gradle configuration
- Debugging tips and common issues
- Performance monitoring

**When to reference**: Building the project, running tests, debugging issues, or setting up development environment.

### 3. [firebase-setup.md](./firebase-setup.md)
**What it covers**: Firebase configuration for Android and iOS

- Android Firebase setup (`google-services.json`)
- iOS Firebase setup (`GoogleService-Info.plist`)
- OAuth provider configuration (Google, Apple, Facebook)
- Platform-specific implementation details
- iOS notification bridge architecture
- Common configuration issues and solutions
- Security considerations
- Testing Firebase integration

**When to reference**: Setting up Firebase on Android/iOS, configuring OAuth providers, troubleshooting authentication issues, or implementing platform-specific features.

### 3.5. [desktop-setup.md](./desktop-setup.md)
**What it covers**: Desktop (JVM) Firebase configuration and usage

- Desktop Firebase setup (`firebase-config.json`)
- Firebase REST API integration
- Supported features and limitations
- OAuth implementation guide for desktop
- Token management and refresh
- Build and distribution commands
- Desktop-specific troubleshooting
- Comparison with mobile platforms

**When to reference**: Setting up Firebase for desktop apps, implementing desktop authentication, handling OAuth flows on desktop, or troubleshooting desktop-specific issues.

### 4. [coding-guidelines.md](./coding-guidelines.md)
**What it covers**: Implementation patterns and best practices

- Core architectural principles
- Platform isolation patterns
- Error handling with `AuthError` and `AuthResult`
- Test-Driven Development requirements
- Dependency injection patterns
- Platform-specific guidelines (Android/iOS)
- Code style and naming conventions
- Common mistakes to avoid
- Security best practices

**When to reference**: Writing new code, reviewing pull requests, implementing features, or resolving architectural questions.

## Quick Reference

### Architecture Pattern
```
AuthRepository (validation) → AuthBackend (interface) → Platform Implementations
```

### Key Locations
- **Core Auth**: `composeApp/src/commonMain/kotlin/az/random/testauth/auth/`
- **Android Impl**: `composeApp/src/androidMain/kotlin/az/random/testauth/auth/`
- **iOS Impl**: `composeApp/src/iosMain/kotlin/az/random/testauth/auth/`
- **Tests**: `composeApp/src/commonTest/kotlin/az/random/testauth/auth/`

### Common Commands
```bash
# Run all tests
./gradlew :composeApp:check

# Build Android APK
./gradlew :composeApp:assembleDebug

# Clean build
./gradlew clean build
```

### Error Types
- `InvalidCredential` - Wrong email/password
- `EmailAlreadyInUse` - Email already registered
- `WeakPassword` - Password doesn't meet requirements
- `RequiresRecentLogin` - Sensitive operation needs re-auth
- `UserNotFound` - Account doesn't exist
- `NetworkError` - Connection issues
- `Unknown` - Unexpected errors

### OAuth Provider IDs
- Google: `"google.com"`
- Apple: `"apple.com"`
- Facebook: `"facebook.com"`
- Email/Password: `"password"`

## Usage Guidelines

### For AI Agents
1. **Read project-overview.md first** to understand the architecture
2. **Follow coding-guidelines.md** for all code changes
3. **Use development-workflow.md** for build and test commands
4. **Reference firebase-setup.md** for platform configuration

### For Developers
1. Start with **project-overview.md** to understand the system
2. Follow **development-workflow.md** to set up your environment
3. Configure Firebase using **firebase-setup.md**
4. Write code according to **coding-guidelines.md**

### For Code Reviews
- Verify architecture follows patterns in **project-overview.md**
- Check code style matches **coding-guidelines.md**
- Ensure tests follow TDD approach in **development-workflow.md**
- Validate platform setup against **firebase-setup.md**

## Maintenance

These documents should be updated when:
- Architecture patterns change
- New features are added
- Build processes evolve
- Firebase configuration requirements change
- Best practices are refined
- Common issues are discovered

## Related Files

- **/CLAUDE.md** - Original project instructions (this folder is an expanded version)
- **/.gitignore** - Includes Firebase configuration files
- **/composeApp/build.gradle.kts** - Build configuration
- **/.github/** - CI/CD workflows (if present)

## Contributing

When updating these documents:
1. Keep information current with codebase
2. Use clear, concise language
3. Include code examples for complex concepts
4. Update all related sections if patterns change
5. Test commands before documenting them
6. Maintain consistent formatting

## Questions?

If these documents don't answer your questions:
1. Check the source code for implementation details
2. Review test files for usage examples
3. Consult Firebase documentation for platform-specific issues
4. Ask in project discussions or issues

---

**Last Updated**: 2025-10-13
**Project Version**: 1.0.0
**Package**: `dev.com3run.testauth`
