# Maven Publishing Guide

This guide explains how to publish the Firebase Auth KMP library to different Maven repositories.

## Overview

The library is configured to publish to Maven repositories with the following coordinates:

- **Group ID:** `az.random`
- **Artifact ID:** `firebase-auth-kmp`
- **Version:** `1.0.0`

## Publishing Options

### Option 1: Maven Local (Testing)

Perfect for testing the library in other projects on your local machine.

#### Publish Command

```bash
./gradlew :firebase-auth-kmp:publishToMavenLocal
```

#### Verify Publication

Check that the library was published:

```bash
ls ~/.m2/repository/az/random/firebase-auth-kmp/1.0.0/
```

You should see files like:
- `firebase-auth-kmp-1.0.0.jar`
- `firebase-auth-kmp-1.0.0.pom`
- `firebase-auth-kmp-1.0.0-sources.jar`
- `firebase-auth-kmp-1.0.0.module`

#### Use in Another Project

In your other project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()  // Add this
        google()
        mavenCentral()
    }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("az.random:firebase-auth-kmp:1.0.0")
}
```

---

### Option 2: Maven Central (Production)

Maven Central is the standard repository for open-source libraries. This makes your library accessible worldwide.

#### Prerequisites

1. **Create Sonatype JIRA Account**
   - Go to https://issues.sonatype.org/
   - Create an account
   - Create a ticket to request a new project
   - Example: "Request for az.random group ID"
   - Wait for approval (usually 1-2 business days)

2. **Generate GPG Keys**
   ```bash
   # Generate key
   gpg --gen-key

   # List keys
   gpg --list-keys

   # Export public key to key server
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Create gradle.properties**

   Create `~/.gradle/gradle.properties` with your credentials:

   ```properties
   # Sonatype OSSRH credentials
   ossrhUsername=your-jira-username
   ossrhPassword=your-jira-password

   # GPG signing
   signing.keyId=YOUR_GPG_KEY_ID
   signing.password=YOUR_GPG_PASSWORD
   signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
   ```

#### Update build.gradle.kts

Modify `firebase-auth-kmp/build.gradle.kts`:

```kotlin
plugins {
    id("maven-publish")
    id("signing")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "az.random"
            artifactId = "firebase-auth-kmp"
            version = "1.0.0"

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

    repositories {
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
}

signing {
    sign(publishing.publications["maven"])
}
```

#### Publish Commands

```bash
# Publish to staging
./gradlew :firebase-auth-kmp:publishAllPublicationsToOSSRHRepository

# Or publish specific variant
./gradlew :firebase-auth-kmp:publishMavenPublicationToOSSRHRepository
```

#### Release Process

1. Login to https://s01.oss.sonatype.org/
2. Go to "Staging Repositories"
3. Find your repository (usually named `azrandom-XXXX`)
4. Click "Close" and wait for validation
5. Once validation passes, click "Release"
6. Your library will be available on Maven Central within a few hours

---

### Option 3: GitHub Packages

GitHub Packages provides a simple way to host Maven packages on GitHub.

#### Update build.gradle.kts

```kotlin
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "az.random"
            artifactId = "firebase-auth-kmp"
            version = "1.0.0"

            pom {
                // ... same as before
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/com3run/testauth")

            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

#### Create GitHub Token

1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate new token with `write:packages` scope
3. Save token securely

#### Publish Command

```bash
./gradlew :firebase-auth-kmp:publishAllPublicationsToGitHubPackagesRepository
```

#### Using from GitHub Packages

Users need to authenticate to use GitHub Packages. In their `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/com3run/testauth")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
        google()
        mavenCentral()
    }
}
```

---

### Option 4: JitPack (Easiest)

JitPack builds and publishes libraries directly from GitHub repositories.

#### No Build Configuration Needed

JitPack works with your existing GitHub repository.

#### How to Publish

1. Push your code to GitHub
2. Create a GitHub Release (tag)
3. Done! JitPack will build automatically

#### Usage

Users add JitPack repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
    }
}
```

Then use:

```kotlin
dependencies {
    implementation("com.github.com3run:testauth:1.0.0")
}
```

---

## Version Management

### Semantic Versioning

Follow semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR:** Breaking changes
- **MINOR:** New features (backward compatible)
- **PATCH:** Bug fixes (backward compatible)

### Updating Version

In `firebase-auth-kmp/build.gradle.kts`:

```kotlin
group = "az.random"
version = "1.1.0"  // Update this
```

### Snapshot Versions

For development versions:

```kotlin
version = "1.1.0-SNAPSHOT"
```

Snapshots can be published to Maven Central's snapshot repository.

---

## Publishing Checklist

Before publishing a release:

- [ ] All tests pass
- [ ] Documentation is updated
- [ ] CHANGELOG is updated
- [ ] Version number is incremented
- [ ] Git tag is created
- [ ] Code is pushed to GitHub
- [ ] README includes usage examples
- [ ] POM information is complete and accurate

---

## CI/CD Automation

### GitHub Actions Example

Create `.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: macos-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'adopt'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Publish to Maven Central
      env:
        OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
        SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
        SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
        SIGNING_SECRET_KEY: ${{ secrets.SIGNING_SECRET_KEY }}
      run: ./gradlew :firebase-auth-kmp:publishToSonatype closeAndReleaseSonatypeStagingRepository
```

Store secrets in GitHub repository settings.

---

## Troubleshooting

### Common Issues

**Issue:** "Task 'publishToMavenLocal' not found"
**Solution:** Make sure you're running the task on the library module: `:firebase-auth-kmp:publishToMavenLocal`

**Issue:** "Publication has no artifacts"
**Solution:** Ensure the library module builds successfully before publishing: `./gradlew :firebase-auth-kmp:build`

**Issue:** "GPG signing failed"
**Solution:** Check that your GPG key is properly configured and `signing.secretKeyRingFile` path is correct

**Issue:** "401 Unauthorized" for Maven Central
**Solution:** Verify your OSSRH credentials are correct and your project has been approved

---

## Best Practices

1. **Test locally first** - Always publish to Maven Local and test before publishing publicly
2. **Use CI/CD** - Automate publishing to avoid manual errors
3. **Keep versions consistent** - Don't reuse version numbers
4. **Sign releases** - Always sign artifacts for Maven Central
5. **Document breaking changes** - Clearly communicate in changelog and release notes
6. **Version strategy** - Use semantic versioning consistently

---

## Resources

- [Maven Central Guide](https://central.sonatype.org/publish/publish-guide/)
- [GitHub Packages Documentation](https://docs.github.com/en/packages)
- [JitPack Documentation](https://jitpack.io/docs/)
- [Gradle Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)

---

## Quick Commands Reference

```bash
# Build library
./gradlew :firebase-auth-kmp:build

# Publish to Maven Local
./gradlew :firebase-auth-kmp:publishToMavenLocal

# Publish all publications (Kotlin Multiplatform)
./gradlew :firebase-auth-kmp:publishAllPublicationsToMavenLocal

# Clean and rebuild
./gradlew clean :firebase-auth-kmp:build

# Check publication setup
./gradlew :firebase-auth-kmp:publishToMavenLocalPublicationToMavenLocal --dry-run
```

---

## Support

For publishing issues:
- Check Gradle logs: `./gradlew :firebase-auth-kmp:publishToMavenLocal --info`
- Maven Central support: https://central.sonatype.org/
- GitHub Issues: https://github.com/com3run/testauth/issues
