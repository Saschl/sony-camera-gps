# GitHub Actions Release Setup

This document explains how to set up the GitHub Action for building and releasing signed APKs.

## Required GitHub Secrets

You need to add the following secrets to your GitHub repository:

### 1. KEYSTORE_BASE64
- Generate a keystore file if you don't have one:
  ```bash
  keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
  ```
- Convert your keystore to base64:
  ```bash
  # On Linux/macOS
  base64 -i release-key.jks
  
  # On Windows (PowerShell)
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks"))
  ```
- Copy the base64 output and add it as a GitHub secret named `KEYSTORE_BASE64`

### 2. SIGNING_KEY_ALIAS
- The alias you used when creating the keystore (e.g., "my-alias")

### 3. SIGNING_KEY_PASSWORD
- The password for the key alias

### 4. SIGNING_STORE_PASSWORD
- The password for the keystore file

## How to Add GitHub Secrets

1. Go to your GitHub repository
2. Navigate to Settings → Secrets and variables → Actions
3. Click "New repository secret"
4. Add each of the secrets listed above

## Triggering a Release

The GitHub Action will trigger automatically when you:

1. **Push a version tag:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **Manually trigger the workflow:**
   - Go to the Actions tab in your GitHub repository
   - Select the "Build and Release APK" workflow
   - Click "Run workflow"

## What the Action Does

1. Sets up the build environment (JDK 17, Android SDK)
2. Caches Gradle dependencies for faster builds
3. Decodes the keystore from the base64 secret
4. Builds a signed release APK
5. Creates a GitHub release
6. Uploads the signed APK to the release

The released APK will be named `sony-gps-{version}.apk` where `{version}` is the git tag.
