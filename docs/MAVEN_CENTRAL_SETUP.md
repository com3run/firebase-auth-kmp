# Maven Central Publishing Setup Guide

This guide will walk you through publishing your Firebase Auth KMP library to Maven Central, the official repository for Java/Kotlin libraries.

## Why Maven Central?

- ✅ **Official Repository**: Industry standard for JVM libraries
- ✅ **Better Discoverability**: Appears in IDE autocomplete
- ✅ **No Extra Configuration**: Users don't need to add special repositories
- ✅ **Professional**: Shows your library is production-ready
- ✅ **Permanent**: Once published, versions are immutable

## Prerequisites Overview

1. Sonatype JIRA account
2. Domain ownership verification (`dev.com3run`)
3. GPG key for signing artifacts
4. Gradle configuration

**Estimated Time**: 2-3 days (mostly waiting for approvals)

---

## Step 1: Create Sonatype Account (15 minutes)

### 1.1 Sign Up

1. Go to: https://issues.sonatype.org/secure/Signup!default.jspa
2. Fill in the registration form
3. Verify your email
4. Log in to Sonatype JIRA

### 1.2 Create a New Project Ticket

1. Click "Create" button in top menu
2. Fill in the form:
   - **Project**: Community Support - Open Source Project Repository Hosting (OSSRH)
   - **Issue Type**: New Project
   - **Summary**: `Request for dev.com3run group ID`
   - **Description**:
     ```
     Hello,

     I would like to publish my Kotlin Multiplatform library to Maven Central.

     Project Details:
     - Group ID: dev.com3run
     - Project URL: https://github.com/com3run/testauth
     - SCM URL: https://github.com/com3run/testauth.git
     - Domain: com3run.dev

     I am the owner of the domain com3run.dev and can verify ownership.

     Thank you!
     ```
   - **Group Id**: `dev.com3run`
   - **Project URL**: `https://github.com/com3run/testauth`
   - **SCM URL**: `https://github.com/com3run/testauth.git`

3. Click "Create"

### 1.3 Verify Domain Ownership

Sonatype will ask you to verify you own `com3run.dev`. You have two options:

**Option A: DNS TXT Record** (Recommended)
1. Add a TXT record to your domain's DNS:
   - Name: `@` or root domain
   - Type: `TXT`
   - Value: Your JIRA ticket URL, e.g., `OSSRH-12345`
2. Wait for DNS propagation (5-30 minutes)
3. Reply to the ticket saying "DNS TXT record added"

**Option B: GitHub Repository**
1. Create a public GitHub repo at: `https://github.com/com3run/dev.com3run` or `https://github.com/com3run/OSSRH-XXXXX`
2. Reply to the ticket with the repo URL

### 1.4 Wait for Approval

- You'll receive comments on your ticket
- Usually approved within 1-2 business days
- Once approved, you'll see: "Configuration has been prepared, now you can publish"

---

## Step 2: Generate GPG Key (10 minutes)

Maven Central requires all artifacts to be signed with GPG.

### 2.1 Install GPG

**macOS:**
```bash
brew install gnupg
```

**Linux:**
```bash
sudo apt-get install gnupg
# or
sudo yum install gnupg
```

**Windows:**
Download from: https://www.gnupg.org/download/

### 2.2 Generate Key

```bash
# Generate a new GPG key
gpg --gen-key

# Follow the prompts:
# - Real name: Kamran Mammadov
# - Email: your-email@example.com
# - Passphrase: Choose a strong password (remember this!)
```

### 2.3 Find Your Key ID

```bash
# List your keys
gpg --list-keys

# Output will look like:
# pub   rsa3072 2025-01-10 [SC] [expires: 2027-01-10]
#       ABCD1234ABCD1234ABCD1234ABCD1234ABCD1234
# uid           [ultimate] Kamran Mammadov <your-email@example.com>

# Your key ID is the last 8 characters: ABCD1234
```

### 2.4 Upload Public Key to Keyserver

```bash
# Replace YOUR_KEY_ID with your actual key ID
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Also upload to other servers for redundancy:
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID
```

### 2.5 Export Private Key

```bash
# Export to secring.gpg file
gpg --export-secret-keys YOUR_KEY_ID > ~/.gnupg/secring.gpg
```

---

## Step 3: Configure Gradle (5 minutes)

### 3.1 Create ~/.gradle/gradle.properties

```bash
# Create or edit the file
nano ~/.gradle/gradle.properties
```

Add your credentials (use the template from `gradle.properties.template`):

```properties
# Sonatype OSSRH Credentials
ossrhUsername=your-sonatype-username
ossrhPassword=your-sonatype-password

# GPG Signing
signing.keyId=YOUR_KEY_ID
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

**IMPORTANT**: Never commit this file to Git!

### 3.2 Update Developer Email

Edit `firebase-auth-kmp/build.gradle.kts` and update:

```kotlin
developers {
    developer {
        id.set("com3run")
        name.set("Kamran Mammadov")
        email.set("your-actual-email@example.com")  // Update this!
    }
}
```

---

## Step 4: Test Signing Locally (5 minutes)

Before publishing to Maven Central, test that signing works:

```bash
# Test signing
./gradlew :firebase-auth-kmp:publishToMavenLocal

# Check for .asc files (signatures)
ls ~/.m2/repository/dev/com3run/firebase-auth-kmp/1.0.0/

# You should see:
# firebase-auth-kmp-1.0.0.jar
# firebase-auth-kmp-1.0.0.jar.asc  <-- This means signing works!
# firebase-auth-kmp-1.0.0.pom
# firebase-auth-kmp-1.0.0.pom.asc  <-- Signatures
```

---

## Step 5: Publish to Maven Central (30 minutes)

### 5.1 Update Version (if needed)

If you want to publish a new version:

```kotlin
// firebase-auth-kmp/build.gradle.kts
version = "1.0.1"  // or keep 1.0.0
```

### 5.2 Publish to Staging

```bash
# Publish all artifacts to Maven Central staging repository
./gradlew :firebase-auth-kmp:publishAllPublicationsToOSSRHRepository

# This will:
# - Build all variants (Android, iOS)
# - Sign all artifacts with GPG
# - Upload to Sonatype OSSRH
```

**If this fails**, check:
- Are credentials correct in `~/.gradle/gradle.properties`?
- Is GPG key uploaded to keyservers?
- Run with `--info` for more details: `./gradlew :firebase-auth-kmp:publishAllPublicationsToOSSRHRepository --info`

### 5.3 Release from Staging Repository

1. **Log in to Sonatype OSSRH**:
   - Go to: https://s01.oss.sonatype.org/
   - Log in with your JIRA credentials

2. **Find Your Staging Repository**:
   - Click "Staging Repositories" in left menu
   - Look for `devcom3run-XXXX` (your staging repository)
   - It might take a minute to appear after publish

3. **Close the Repository**:
   - Select your repository
   - Click "Close" button
   - Wait 2-5 minutes for validation
   - Refresh to see validation results
   - If validation fails, read the error messages and fix issues

4. **Release the Repository**:
   - Once closed successfully, click "Release" button
   - Confirm the release
   - Your artifacts will be published!

### 5.4 Wait for Sync

- **Central Portal**: Available immediately at https://central.sonatype.com/
- **Maven Central Search**: 15-30 minutes
- **Maven Central Repository**: 1-2 hours
- **IDE Autocomplete**: 2-4 hours

---

## Step 6: Verify Publication (5 minutes)

### 6.1 Check Central Portal

1. Go to: https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp
2. You should see your version listed

### 6.2 Check Maven Central

After 1-2 hours, check:
https://repo1.maven.org/maven2/dev/com3run/firebase-auth-kmp/

### 6.3 Test in a Project

Create a test project:

```kotlin
// No special repository needed!
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.0")
}
```

---

## Step 7: Update Documentation

Once published, update your README.md:

```markdown
## Installation

```kotlin
// Just add the dependency - no special repository needed!
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.0")
}
```

Maven Central: https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp
```

---

## Publishing Future Versions

Once you're approved, publishing new versions is easy:

```bash
# 1. Update version in build.gradle.kts
version = "1.1.0"

# 2. Publish to staging
./gradlew :firebase-auth-kmp:publishAllPublicationsToOSSRHRepository

# 3. Release from staging repository (same as before)
```

---

## Troubleshooting

### Common Issues

**Issue**: `Could not find signing.keyId`
- **Solution**: Check `~/.gradle/gradle.properties` exists and has correct properties

**Issue**: `gpg: signing failed: No secret key`
- **Solution**: Make sure `secring.gpg` path is correct in gradle.properties

**Issue**: `401 Unauthorized`
- **Solution**: Verify OSSRH credentials are correct

**Issue**: `Validation failed: no public key`
- **Solution**: Upload your GPG public key to keyservers again

**Issue**: `Group ID not allowed`
- **Solution**: Wait for Sonatype ticket approval

### Getting Help

- **Sonatype Guide**: https://central.sonatype.org/publish/
- **Community Support**: Comment on your JIRA ticket
- **Gradle Docs**: https://docs.gradle.org/current/userguide/signing_plugin.html

---

## Summary Checklist

Before publishing, ensure:

- [ ] Sonatype JIRA account created
- [ ] Group ID `dev.com3run` approved
- [ ] GPG key generated and uploaded
- [ ] `~/.gradle/gradle.properties` configured
- [ ] Email added to build.gradle.kts
- [ ] Signing tested locally
- [ ] Published to staging successfully
- [ ] Released from staging repository
- [ ] Verified on Maven Central
- [ ] Documentation updated

---

## Benefits After Publishing

Once on Maven Central:

✅ Users can use your library without adding special repositories
✅ Better IDE autocomplete and discovery
✅ Appears in Maven Central search
✅ Professional credibility
✅ Compatible with all build tools (Gradle, Maven, etc.)

---

## Next Steps

After your first Maven Central release:

1. **Update JitPack README**: Mention Maven Central is now available
2. **Add Maven Central Badge**:
   ```markdown
   [![Maven Central](https://img.shields.io/maven-central/v/dev.com3run/firebase-auth-kmp.svg)](https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp)
   ```
3. **Announce**: Share on social media, Kotlin Slack, etc.

Good luck! 🚀
