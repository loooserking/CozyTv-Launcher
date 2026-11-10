# TVLauncher v2.0.0 — White Frosted Glass Edition

This major release introduces the TVLauncher application identity (`com.tvlauncher.app`) and a redesigned Android TV home screen.

## Highlights

- New TVLauncher name, original red-and-white TV logo, adaptive icon, and legacy density icons.
- White interface with frosted-style cards, rounded app tiles, subtle shadows, and minimal red focus accents.
- Live flip clock and dynamically discovered Android TV app activities.
- Wi-Fi/network and system Settings shortcuts.
- Recently Used Apps removed.
- D-pad navigation and lightweight focus/clock animations.
- Android 9 (API 28) minimum compatibility.

## Install

Build the release APK with `./gradlew assembleRelease`, then install it over ADB:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

The application ID changed, so Android installs this release separately from earlier app identities; data and Home settings are not migrated.

## Known limitations

- No in-launcher preferences screen, UI sound effects, mute control, or sound settings are implemented.
- Available apps and system settings destinations depend on TV firmware.
- The release must be tested on a physical Android TV before claiming device-specific compatibility.
- This build uses Gradle's default Android debug signing key because no production release keystore is configured. A production distribution should be signed with a maintainer-controlled release key.
