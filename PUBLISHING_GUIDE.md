# Publishing Guide for Firebase Auth KMP

This guide will walk you through publishing your library to JitPack and (optionally) Maven Central.

## ✅ Pre-Release Checklist

Before publishing, ensure:

- [x] Package renamed to `dev.com3run.firebaseauthkmp`
- [x] Build succeeds: `./gradlew :firebase-auth-kmp:build`
- [x] Tests pass: `./gradlew :firebase-auth-kmp:test`
- [x] Published to Maven Local successfully
- [x] README.md has installation instructions
- [x] LICENSE file exists
- [x] jitpack.yml configuration added

## Option 1: Publishing to JitPack (Recommended First)

JitPack is the easiest way to make your library publicly available. It builds directly from your GitHub repository.

### Step 1: Push to GitHub

```bash
# Add all files
git add .

# Commit changes
git commit -m "Release 1.0.0 - Firebase Auth KMP library with dev.com3run package"

# Push to GitHub
git push origin main
```

### Step 2: Create a GitHub Release

1. Go to your GitHub repository: https://github.com/com3run/testauth
2. Click on "Releases" in the right sidebar
3. Click "Create a new release"
4. Set the tag version: `1.0.0`
5. Set release title: `v1.0.0 - Initial Release`
6. Add release notes:

```markdown
## Firebase Auth KMP v1.0.0

First stable release of Firebase Auth KMP library!

### Features
- ✅ Email/Password Authentication
- ✅ Google Sign-In (Android & iOS)
- ✅ Apple Sign-In (iOS only)
- ✅ Anonymous Authentication
- ✅ Real-time auth state monitoring
- ✅ Comprehensive error handling
- ✅ Unit testing support with FakeAuthBackend

### Package Information
- Group ID: `dev.com3run`
- Artifact ID: `firebase-auth-kmp`
- Version: `1.0.0`

### Installation

Add JitPack repository:
```kotlin
maven { url = uri("https://jitpack.io") }
```

Add dependency:
```kotlin
implementation("com.github.com3run:testauth:1.0.0")
```

See [README.md](README.md) for complete setup instructions.
```

6. Click "Publish release"

### Step 3: Verify JitPack Build

1. Go to https://jitpack.io/#com3run/testauth
2. Click "Get it" next to version 1.0.0
3. Wait for the build to complete (usually 2-5 minutes)
4. Once you see a green checkmark ✓, your library is published!

### Step 4: Test the Published Library

Create a new test project and try installing your library:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.com3run:testauth:1.0.0")
        }
    }
}
```

If it works, congratulations! Your library is now public! 🎉

## Option 2: Publishing to Maven Central (Advanced)

Maven Central is the official repository for Java/Kotlin libraries. It's more complex but provides better discoverability.

### Prerequisites

1. **Create Sonatype Account**
   - Go to https://issues.sonatype.org/
   - Sign up for an account
   - Create a "New Project" ticket requesting `dev.com3run` group ID
   - Verify domain ownership (they'll ask you to add a DNS TXT record or create a GitHub repo)
   - Wait for approval (1-2 business days)

2. **Generate GPG Key**

```bash
# Generate key
gpg --gen-key

# Enter your name and email when prompted
# Remember the passphrase!

# List keys to get the key ID
gpg --list-keys

# Upload public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export private key
gpg --export-secret-keys YOUR_KEY_ID > secring.gpg
```

3. **Configure Gradle Properties**

Create or edit `~/.gradle/gradle.properties`:

```properties
# Sonatype OSSRH credentials
ossrhUsername=your-jira-username
ossrhPassword=your-jira-password

# GPG signing
signing.keyId=YOUR_GPG_KEY_ID
signing.password=YOUR_GPG_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg
```

### Update build.gradle.kts

Add signing plugin and Maven Central repository:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")  // Add this
}

// ... existing configuration ...

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }

    publications.withType<MavenPublication> {
        pom {
            name.set("Firebase Auth KMP")
            description.set("Firebase Authentication library for Kotlin Multiplatform (Android & iOS)")
            url.set("https://github.com/com3run/testauth")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("com3run")
                    name.set("Kamran Mammadov")
                    email.set("your.email@example.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/com3run/testauth.git")
                developerConnection.set("scm:git:ssh://github.com/com3run/testauth.git")
                url.set("https://github.com/com3run/testauth")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
```

### Publish to Maven Central

```bash
# Publish to staging repository
./gradlew :firebase-auth-kmp:publishAllPublicationsToOSSRHRepository

# Then go to https://s01.oss.sonatype.org/
# 1. Log in with your Sonatype credentials
# 2. Click "Staging Repositories"
# 3. Find your repository (devcom3run-XXXX)
# 4. Click "Close" and wait for validation
# 5. Once validation passes, click "Release"
# 6. Your library will be available on Maven Central within 15-30 minutes
```

### After Publishing to Maven Central

Users can use your library without JitPack:

```kotlin
// Just add the dependency - no special repository needed!
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.0")
}
```

## Releasing New Versions

### For JitPack

1. Update version in `firebase-auth-kmp/build.gradle.kts`
2. Commit and push changes
3. Create a new GitHub release with the new version tag
4. JitPack will automatically build the new version

### For Maven Central

1. Update version in `firebase-auth-kmp/build.gradle.kts`
2. Run the publish command
3. Release from staging repository

## Best Practices

1. **Semantic Versioning**: Follow semver (MAJOR.MINOR.PATCH)
   - MAJOR: Breaking changes
   - MINOR: New features (backward compatible)
   - PATCH: Bug fixes

2. **Changelog**: Keep a CHANGELOG.md file with all changes

3. **Git Tags**: Always tag releases in Git

4. **Testing**: Test locally with Maven Local before publishing

5. **Documentation**: Update README.md with new features

## Troubleshooting

### JitPack Build Failed

- Check build logs at https://jitpack.io/#com3run/testauth
- Ensure `jitpack.yml` is in the root directory
- Verify the tag exists in GitHub

### Maven Central Upload Failed

- Check credentials in `~/.gradle/gradle.properties`
- Verify your Sonatype account has permission for `dev.com3run`
- Check GPG key is properly configured

### Import Issues

- Ensure the package name is correct: `dev.com3run.firebaseauthkmp`
- Check that the repository is added (JitPack or Maven Central)
- Verify the version number is correct

## Next Steps

After publishing:

1. 🌟 Share your library on social media
2. 📝 Write a blog post about it
3. 📣 Submit to Kotlin Weekly
4. 💬 Share in Kotlin Slack/Discord communities
5. 📊 Add GitHub topics: `kotlin-multiplatform`, `firebase`, `authentication`

---

Good luck with your library! 🚀
