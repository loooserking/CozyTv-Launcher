# TVLauncher

A lightweight, clean and minimal Android TV launcher featuring a premium white frosted-glass interface.

TVLauncher replaces the home screen with a live clock, network shortcuts, and a row of installed Android TV apps. It uses the Android TV app icons and labels already installed on the device.

## Features

- White home screen with translucent, frosted-style cards, subtle shadows, rounded app tiles, and restrained red focus accents.
- Live flip clock and date, with lightweight clock transitions.
- Automatically discovers apps that expose an Android TV (`LEANBACK_LAUNCHER`) activity and refreshes the list when apps change.
- Launches apps from the home screen; long-press an app tile to open its system app-details page.
- Network status shortcut that opens Wi-Fi settings by default, plus a separate system Settings shortcut.
- D-pad navigation between shortcuts and app tiles, with focus feedback.
- Optional clock screensaver.
- No Recently Used Apps screen.

## Screenshots

Screenshots are not included yet. No Android TV or emulator was connected in the build environment, so genuine screenshots could not be captured. They will be added here when a device is available.

To capture screenshots from a connected TV after installing and opening TVLauncher:

```bash
mkdir -p docs/screenshots
adb exec-out screencap -p > docs/screenshots/home-screen.png
```

Capture the home image while the launcher is visible. Move focus to the installed-app row and capture `app-grid.png`; return Home and open the system Settings shortcut to capture `settings.png`; return Home and open the network shortcut to capture `wifi-settings.png`. Use `adb exec-out screencap -p > docs/screenshots/<name>.png` for each screen. The installed-app image shows the launcher’s app row; the shortcut images show the TV’s system settings screens.

## Compatibility

- Minimum Android version: Android 9 (API 28).
- Compile SDK: Android 14 (API 34); target SDK: 34.
- Intended for Android TV / Google TV devices that provide Android TV launcher activities and the standard Home intent.
- The app label is **TVLauncher**.
- Application ID: `com.tvlauncher.app`.
- Current release version: `2.0.0` (version code `7`).

The app has not been tested on every TV model or manufacturer. The appearance and availability of system settings screens depend on the device.

## Install on a TV

Build the APK locally (see below), enable Developer options and USB debugging on the TV, and connect with Android Debug Bridge:

```bash
adb connect <TV_IP>:5555
adb install -r artifacts/TVLauncher.apk
```

Open TVLauncher from the TV’s apps list and select it as the Home app when prompted. On devices that support setting the Home activity over ADB, this can also be done with:

```bash
adb shell cmd package set-home-activity --user 0 com.tvlauncher.app/.LauncherHomeActivity
```

The application ID changed from earlier builds. Android treats TVLauncher as a separate app from those builds; app data and Home preferences are not migrated automatically.

## Build

Requirements: JDK 21, Android SDK Platform 34, and Android SDK Build-Tools 34.0.0.

Set `JAVA_HOME` to your JDK 21 installation and ensure the Android SDK is installed. On Linux or macOS:

```bash
export JAVA_HOME="/path/to/jdk-21"
export ANDROID_HOME="/path/to/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

./gradlew testDebugUnitTest assembleDebug
./gradlew assembleRelease
mkdir -p artifacts
cp app/build/outputs/apk/release/app-release.apk artifacts/TVLauncher.apk
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The release APK is written to `app/build/outputs/apk/release/app-release.apk`; the commands above also copy it to `artifacts/TVLauncher.apk`.

To install the release build directly:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Remote and network shortcuts

Use the remote’s D-pad to move focus between the network and Settings shortcuts and the installed-app row. Press OK/Enter to open the focused shortcut or launch the focused app. Long-press an app tile to open its system app details.

The network shortcut opens the Wi-Fi settings screen unless a VPN app is configured in the app’s resource/JSON configuration, in which case it launches that VPN app. Android TV variants may route system settings differently or may not provide a matching settings activity.

## Limitations

- There is no separate in-launcher settings/preferences screen.
- The app does not implement UI sound effects, a mute option, or sound controls; volume and sound remain controlled by Android and the TV.
- Only apps with an Android TV launcher activity are listed. Phone-only apps may not appear.
- Wi-Fi SSID visibility and system-settings navigation depend on Android version, permissions, and TV firmware.
- No emulator or physical TV was available for screenshot capture or on-device verification in this environment.

## Project structure

```text
app/
  src/main/java/com/tvlauncher/app/   Launcher activities, app discovery, and remote services
  src/main/res/                       Manifest resources, layouts, icons, and styling
  src/test/java/com/tvlauncher/app/   Unit tests
tools/                                Optional Android TV audit/apply/restore scripts
profiles/                             Optional device package profiles
docs/screenshots/                     Genuine device screenshots (to be added)
```

## Development

Run the unit tests and debug build with:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Keep the application ID, resource references, and manifest component names consistent when changing app identity. Test D-pad focus, app launch, network and Settings shortcuts, and Home behavior on an Android TV device before release.

## License and acknowledgements

TVLauncher is distributed under the MIT License; see [LICENSE](LICENSE). App icons and names displayed by the launcher belong to their respective publishers.
