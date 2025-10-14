# 📦 Publishing Guide - Firebase Auth KMP v1.0.1

This guide walks you through publishing version 1.0.1 to both GitHub and Maven Central.

## Pre-requisites Checklist

- ✅ Version updated to 1.0.1 in `firebase-auth-kmp/build.gradle.kts`
- ✅ CHANGELOG.md updated with v1.0.1 release notes
- ✅ All documentation updated with correct version
- ✅ Library builds successfully (`./gradlew :firebase-auth-kmp:build`)
- ⏳ GitHub CLI authenticated
- ⏳ Maven Central credentials configured
- ⏳ GPG signing key set up

---

## Step 1: Commit All Changes

```bash
# Review what will be committed
git status

# Add all changes
git add .

# Create commit
git commit -m "Release v1.0.1

- Add Desktop/JVM platform support
- Add Android auto-initialization
- Add iOS bridge template
- Add comprehensive documentation (QUICKSTART.md, IOS_SETUP_GUIDE.md)
- Improve desktop config file discovery
- Fix version inconsistencies across docs

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"

# Push to main
git push origin main
```

---

## Step 2: Create GitHub Release

### Option A: Using GitHub CLI (Recommended)

1. **Authenticate GitHub CLI:**
```bash
gh auth login
# Follow the prompts to authenticate
```

2. **Create and push git tag:**
```bash
# Create annotated tag
git tag -a v1.0.1 -m "Release v1.0.1 - Desktop Support, Auto-Init Android, Improved Docs"

# Push tag to GitHub
git push origin v1.0.1
```

3. **Create GitHub release:**
```bash
gh release create v1.0.1 \
  --title "v1.0.1 - Desktop Support & Enhanced Integration" \
  --notes-file RELEASE_NOTES_v1.0.1.md \
  --draft

# Or use the simplified version:
gh release create v1.0.1 \
  --title "v1.0.1 - Desktop Support & Enhanced Integration" \
  --notes "See CHANGELOG.md for full details" \
  --draft
```

### Option B: Using GitHub Web Interface

1. Go to: https://github.com/com3run/firebase-auth-kmp/releases/new
2. Create a new tag: `v1.0.1`
3. Release title: `v1.0.1 - Desktop Support & Enhanced Integration`
4. Copy release notes from CHANGELOG.md v1.0.1 section
5. Save as draft
6. Review and publish

---

## Step 3: Build Release Artifacts

```bash
# Clean build
./gradlew :firebase-auth-kmp:clean

# Build all artifacts
./gradlew :firebase-auth-kmp:assemble

# Run tests
./gradlew :firebase-auth-kmp:test

# Verify artifacts
ls -lh firebase-auth-kmp/build/outputs/
```

---

## Step 4: Publish to Maven Central

### Setup Signing (One-time)

1. **Generate GPG key (if you don't have one):**
```bash
# Check if you have a key
gpg --list-keys

# If not, generate one
gpg --gen-key
# Use your name and email (info@com3run.dev)
# Choose a strong passphrase

# Get your key ID
gpg --list-secret-keys --keyid-format=long
# Look for: sec   rsa3072/YOUR_KEY_ID
```

2. **Export public key to key servers:**
```bash
# Export to Ubuntu keyserver
gpg --keyserver hkps://keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Also export to MIT keyserver
gpg --keyserver hkps://pgp.mit.edu --send-keys YOUR_KEY_ID
```

3. **Configure signing in `~/.gradle/gradle.properties`:**
```properties
# Signing configuration
signing.keyId=YOUR_LAST_8_CHARS_OF_KEY_ID
signing.password=YOUR_GPG_PASSPHRASE
signing.secretKeyRingFile=/Users/kamranmammadov/.gnupg/secring.gpg

# Maven Central credentials (from Sonatype)
mavenCentralUsername=YOUR_SONATYPE_USERNAME
mavenCentralPassword=YOUR_SONATYPE_PASSWORD
```

4. **Export secret key ring (for older Gradle):**
```bash
gpg --export-secret-keys -o ~/.gnupg/secring.gpg
```

### Publish to Maven Central

1. **Publish to staging:**
```bash
# This will build, sign, and upload to Maven Central staging
./gradlew :firebase-auth-kmp:publishAllPublicationsToMavenCentral
```

2. **Verify on Maven Central:**
   - Go to: https://central.sonatype.com/
   - Login with your credentials
   - Navigate to "Publishing" → "Deployments"
   - You should see your staged artifacts

3. **Close and Release (via Sonatype):**
   - In Sonatype, click "Close" on your staging repository
   - Wait for validation to complete (5-10 minutes)
   - Once validated, click "Release"
   - The artifacts will sync to Maven Central in 15-30 minutes

### Alternative: Using NMCP Plugin (Automatic)

If the NMCP plugin is properly configured:

```bash
# This automatically closes and releases
./gradlew :firebase-auth-kmp:publishAllPublicationsToMavenCentral --no-configuration-cache
```

---

## Step 5: Verify Publication

### Verify Maven Central

Wait 15-30 minutes after release, then check:

```bash
# Check Maven Central search
curl -s "https://search.maven.org/solrsearch/select?q=g:dev.com3run+AND+a:firebase-auth-kmp&rows=5&wt=json" | jq .
```

Or visit:
- https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp/1.0.1
- https://repo1.maven.org/maven2/dev/com3run/firebase-auth-kmp/1.0.1/

### Verify GitHub Release

- Visit: https://github.com/com3run/firebase-auth-kmp/releases/tag/v1.0.1
- Ensure release notes are correct
- Verify tag is pushed

---

## Step 6: Announce the Release

### Update GitHub Release Description

Add usage instructions to the release:

```markdown
## 🚀 Quick Start

### Installation
\`\`\`kotlin
dependencies {
    implementation("dev.com3run:firebase-auth-kmp:1.0.1")
}
\`\`\`

### What's New
- ✨ Desktop/JVM platform support
- 🚀 Android auto-initialization (zero-code setup)
- 📱 Simplified iOS setup with ready-to-use bridge
- 📚 Tiered documentation (30s, 2min, 5min guides)

### Documentation
- [30-Second Quick Start](https://github.com/com3run/firebase-auth-kmp/blob/main/QUICKSTART.md)
- [Easy Integration Guide](https://github.com/com3run/firebase-auth-kmp/blob/main/EASY-INTEGRATION.md)
- [Full Changelog](https://github.com/com3run/firebase-auth-kmp/blob/main/CHANGELOG.md)

**Maven Central:** https://central.sonatype.com/artifact/dev.com3run/firebase-auth-kmp/1.0.1
```

### Social Media (Optional)

Share on:
- Twitter/X
- LinkedIn
- Reddit (r/Kotlin, r/KotlinMultiplatform)
- Kotlin Slack

Example post:
```
🚀 Firebase Auth KMP v1.0.1 is live!

✨ New: Desktop/JVM support
🤖 Android: Zero-code setup (auto-init)
📱 iOS: One-line setup
📚 Easy docs: 30s quickstart

Maven: dev.com3run:firebase-auth-kmp:1.0.1

#Kotlin #KMP #Firebase #Android #iOS #Desktop
```

---

## Troubleshooting

### GPG Signing Issues

**Error: "No value has been specified for property 'signatory.keyId'"**

Solution:
```bash
# Make sure gradle.properties has signing config
cat ~/.gradle/gradle.properties | grep signing
```

**Error: "gpg: signing failed: Inappropriate ioctl for device"**

Solution:
```bash
export GPG_TTY=$(tty)
echo 'export GPG_TTY=$(tty)' >> ~/.zshrc
```

### Maven Central Issues

**Error: "401 Unauthorized"**

Solution: Check your credentials in `~/.gradle/gradle.properties`

**Artifacts not showing up:**

- Wait 15-30 minutes for sync
- Check staging repository is released (not just closed)
- Verify at https://repo1.maven.org/maven2/dev/com3run/firebase-auth-kmp/

### GitHub Release Issues

**Tag already exists:**

```bash
# Delete local tag
git tag -d v1.0.1

# Delete remote tag
git push origin :refs/tags/v1.0.1

# Recreate
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

---

## Rollback Procedure

If something goes wrong after publishing:

### Maven Central
❌ **Cannot unpublish from Maven Central** - releases are permanent!

Solution:
- Publish a hotfix version (v1.0.2)
- Update documentation with known issues

### GitHub Release

```bash
# Delete release
gh release delete v1.0.1

# Delete tag
git tag -d v1.0.1
git push origin :refs/tags/v1.0.1
```

---

## Post-Release Checklist

- ✅ Version 1.0.1 available on Maven Central
- ✅ GitHub release published
- ✅ Documentation updated
- ✅ Release announced
- ✅ Update README.md badge versions (if any)
- ✅ Start planning v1.0.2 features

---

## Next Steps

After successful release:

1. **Monitor Issues:** Watch GitHub for user feedback
2. **Update Samples:** Ensure sample apps use v1.0.1
3. **Plan Next Release:** Start v1.0.2 roadmap
4. **Gather Metrics:** Track downloads on Maven Central

---

## Useful Commands

```bash
# Check current version
grep "version = " firebase-auth-kmp/build.gradle.kts

# List all tags
git tag -l

# Check Maven Central status
curl -s https://repo1.maven.org/maven2/dev/com3run/firebase-auth-kmp/maven-metadata.xml | grep latest

# View Gradle publications
./gradlew :firebase-auth-kmp:tasks --group=publishing
```

---

## Support

- 📖 Documentation: [README.md](README.md)
- 🐛 Issues: https://github.com/com3run/firebase-auth-kmp/issues
- 💬 Discussions: https://github.com/com3run/firebase-auth-kmp/discussions

---

**Good luck with your release! 🎉**
