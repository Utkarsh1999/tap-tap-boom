# Play Store Deployment Guide 🚀

This document provides a step-by-step guide to releasing **tap-tap-boom** to the Google Play Store using the automated GitHub Actions pipeline.

---

## 1. Pre-Deployment Checklist (Status)

Below is the current status of the project configuration required for a production release.

| Item | Status | Description |
| :--- | :--- | :--- |
| **Package Name** | ✅ **DONE** | `com.taptapboom.android` is set in `build.gradle.kts`. |
| **Version Code** | ✅ **DONE** | `versionCode = 1` set. (Increment for every new release). |
| **Version Name** | ✅ **DONE** | `versionName = "1.0.0"` set. |
| **Release Signing Config** | ✅ **DONE** | `signingConfigs { release { ... } }` implemented using environment variables. |
| **ProGuard/R8** | ✅ **DONE** | Minification and resource shrinking enabled for release builds. |
| **Release Workflow** | ✅ **DONE** | `.github/workflows/release.yml` created and configured. |
| **Secrets Setup** | ⏳ **PENDING** | GitHub Repository Secrets need to be populated by the User. |
| **Play Console Setup** | ⏳ **PENDING** | App entry and internal track must be created in Google Play Console. |

---

## 2. Step-by-Step Instructions

### Step 1: Generate a Production Keystore
If you don't have one, generate it using `keytool`:
```bash
keytool -genkey -v -keystore release.jks -alias tap-tap-boom-alias -keyalg RSA -keysize 2048 -validity 10000
```
> [!WARNING]
> Keep this file (`release.jks`) and its passwords extremely secure. If lost, you cannot update your app on the Play Store.

### Step 2: Configure GitHub Repository Secrets
Navigate to your GitHub repo → **Settings** → **Secrets and variables** → **Actions** and add the following:

1.  **`RELEASE_KEYSTORE`**: The base64-encoded content of your `.jks` file.
    *   Command to get it: `cat release.jks | base64`
2.  **`RELEASE_KEYSTORE_PASSWORD`**: The master password for the keystore.
3.  **`RELEASE_KEY_ALIAS`**: The alias you chose (e.g., `tap-tap-boom-alias`).
4.  **`RELEASE_KEY_PASSWORD`**: The password for the specific key.
5.  **`PLAY_STORE_JSON`**: The JSON content of your Google Play Service Account.
    *   *How to get it:* Google Play Console → API Access → Create Service Account → Grant "Release Manager" role → Create JSON Key.

### Step 3: Trigger a Release
The pipeline is designed to trigger when you push a version tag.
1. Increment `versionCode` and `versionName` in `androidApp/build.gradle.kts` (if necessary).
2. Commit and push:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### Step 4: Verify in Play Console
1. Go to **Google Play Console** → **Internal Testing**.
2. You should see a new release containing the `.aab` file uploaded by the GitHub Action.

---

## 3. Maintenance Notes
- **Version Code**: Must be higher than the previous release.
- **Track**: The default is set to `internal`. To change this to `production`, update the `track` parameter in `.github/workflows/release.yml`.
