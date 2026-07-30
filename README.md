# Needle Gap Android

Native Android wrapper for the `needle_gap.html` canvas game.

## Project

- App name: Needle Gap
- Package name: `com.finan.needlegap`
- Version: `1.0.0`
- Entry point: `app/src/main/assets/index.html`
- Android shell: `app/src/main/java/com/finan/needlegap/MainActivity.java`

## Build in Android Studio

1. Open this folder in Android Studio: `needle-gap-android`
2. Let Android Studio install/sync the Android Gradle Plugin and SDK if prompted.
3. Run the app on an emulator or phone.
4. For Play Store, use `Build > Generate Signed App Bundle / APK`.
5. Choose `Android App Bundle`, create/select a signing key, then build release.

## Command Line Build

After installing Android Studio, JDK, and Android SDK:

```powershell
cd needle-gap-android
gradle :app:bundleRelease
```

The release bundle will be created under:

```text
app/build/outputs/bundle/release/
```

## Codemagic Build

This project includes `codemagic.yaml` with two workflows:

- `android-debug` builds a debug APK for phone testing.
- `android-release-aab` builds a release Android App Bundle.

If the repository does not include `gradlew`, Codemagic creates it during the build using Gradle `8.9`.

For a Play Store upload-ready release, configure signing in Codemagic or Android Studio before publishing.

## Play Store Notes

- The current in-game ad is a simulated countdown. Real Play Store monetization needs AdMob or another real ad SDK.
- Because the page loads Google Fonts, the app declares `INTERNET`. If you want a fully offline app, bundle local fonts and remove the permission.
- Replace the generated vector launcher icon with final store artwork before publishing.
