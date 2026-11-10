# TVLauncher for Android TV

<p align="center">
  <a href="https://github.com/loooserking/CozyTv-Launcher/releases/latest"><img src="https://img.shields.io/github/v/release/loooserking/CozyTv-Launcher?color=success&label=Release" alt="Release"></a>
  <a href="https://github.com/loooserking/CozyTv-Launcher/releases/latest/download/app-release.apk"><img src="https://img.shields.io/badge/Download-APK%20(~450%20KB)-brightgreen?logo=android" alt="Download APK"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License"></a>
  <a href="https://developer.android.com/about/versions/pie"><img src="https://img.shields.io/badge/Android%20TV-9.0%2B-blue.svg" alt="Android TV"></a>
  <img src="https://img.shields.io/badge/APK%20Size-~450%20KB-success.svg" alt="Size">
</p>


An ultra-lightweight Android TV launcher with a clean white home screen, frosted translucent cards, subtle gray shadows, minimal red accents, a live flip clock, automatically discovered app tiles, and quick Wi-Fi and Settings controls.

TVLauncher is an independent, open-source project and is not affiliated with any television manufacturer or streaming platform.

---

## ✨ Features

- 📺 **Premium App Home:** White surfaces, frosted translucent cards, subtle gray shadows, minimal red focus accents, and automatically discovered Android TV apps with their actual icons and labels.
- 🕰️ **Live Flip Clock:** On the home screen, plus a matching **Clock** screensaver.
- 🎛️ **Quick Controls:** Wi-Fi/network status and a Settings shortcut, navigable with a standard TV remote.
- ⚡ **Zero Volume Lag (Hardware Preservation):** Unlike accessibility-based remappers that break Android's `InputDispatcher` repeat timer, our launcher preserves 100% hardware-speed volume hold acceleration.
- 🪶 **Ultra-Lightweight (~450 KB):** Pure Java, zero analytics, zero telemetry, zero background battery drain.
- 🧹 **Safe Debloat Ready:** Compatible with ADB debloat lists to disable manufacturer bloatware (PatchWall, telemetry, recommendation rows) without root.

---

## 📺 Running On A Real TV

**Tested on:**

| | |
|---|---|
| TV | Xiaomi Mi TV (`MiTV-AXSO0`, codename `dangal`) |
| Android | Android TV 9 (API 28) |
| Chip | Amlogic quad-core (`txlx`) |
| RAM | 1 GB |
| Screen | 1280 × 720 (HD) |

Home stays on TVLauncher after setup. Tested it on another TV? Open an issue or PR and tell us the model so we can add it here.

## 🚀 1-Click Universal TV Setup

Transform any Android TV or Google TV into an ultra-clean, ad-free experience with automated debloating, SmartTube installation, and the ~450 KB TVLauncher.

### 📋 Prerequisites & Requirements

Before running the setup, ensure:
1. 📶 **Same Wi-Fi Network:** Your computer (laptop/PC) and Android TV must be connected to the exact same Wi-Fi router / network.
2. 📺 **Android TV 9.0+:** Compatible with any TV, box, or stick running Android TV 9, 10, 11, 12, or 14 (Google TV).
3. ⚙️ **Enable USB Debugging on TV:**
   - Go to **Settings ➔ Device Preferences ➔ About**.
   - Scroll down to **Build** and click OK on your remote **7 times** (until you see *"You are now a developer"*).
   - Go back, open **Developer options**, and turn **USB debugging** ON (also turn **Network debugging** ON if present).
4. 💻 **ADB Tool:** Standard Android Platform Tools (`sudo apt install adb` on Linux, `brew install android-platform-tools` on macOS, or pre-bundled in PowerShell for Windows).

---

### 🤖 Option 1: Easiest Setup with Any AI Agent (Recommended for Beginners)

Don't want to deal with terminal commands? You can give this repository to any AI coding assistant (**Claude Code, Cursor, Devin, Antigravity, Roo Code, Windsurf, ChatGPT**):

| Step | Action |
|---|---|
| **1. On your TV** | Settings ➔ Device Preferences ➔ About ➔ Press OK on **Build** 7 times. Back ➔ Developer options ➔ Turn **USB Debugging** ON. |
| **2. In your AI Agent** | Copy and paste [`PROMPT.md`](PROMPT.md) into your assistant chat. |
| **3. Review & Approve** | Your assistant runs a read-only audit (`audit.sh`), shows you the hardware report & plan, and asks for your approval before touching anything! |

---

### 💻 Option 2: Terminal Workflow (For Power Users)

**1. Run Read-Only Audit (Zero changes made to TV):**
```bash
./tools/audit.sh <TV_IP>
```
*Outputs: `reports/report_<ts>.md` (Full audit) and `reports/plan_<ts>.txt` (Editable plan).*

**2. Review & Apply Approved Plan:**
```bash
bash reports/apply_<ts>.sh
```

*(Alternatively, run the interactive 1-click suite directly: `./tools/setup.sh <TV_IP>`)*

**What the automated setup does:**
- 🔍 **Auto-detects TV hardware & manufacturer** (Sony, TCL, Xiaomi, OnePlus, Hisense, Shield, Chromecast).
- 💾 **Creates an instant full-state safety snapshot** of all currently enabled packages, default Home launcher, animation scales, and system settings.
- 🧹 **Safely disables manufacturer bloatware, sponsored recommendation rows, and telemetry** via `pm disable-user`.
- 📺 **Replaces stock ad-heavy YouTube with official SmartTube** (Ad-free, SponsorBlock, TV-optimized).
- **Installs and sets TVLauncher** as the permanent default home activity.

---

### 🛠️ Option 3: Standalone Launcher Installation

If you only want the ultra-lightweight launcher without debloating:
```bash
# 1. Build release APK (or download the latest pre-built APK from GitHub Releases)
./gradlew assembleRelease

# 2. Connect to your TV over Wi-Fi
adb connect <TV_IP>:5555

# 3. Install the launcher
adb install -r app/build/outputs/apk/release/app-release.apk

# 4. Set as permanent default Home launcher
adb shell cmd package set-home-activity --user 0 com.tvlauncher.app/.LauncherHomeActivity
```

Then on the TV, open **Settings ➔ Accessibility** and turn on **Remote keys** only if Settings/All Apps compatibility is needed. Pick **Clock** under Screensaver if you want the flip-clock screensaver.

> **Upgrade note:** Builds made with the previous application ID install separately. Android does not automatically migrate their app data or launcher preferences.

---

## 🛡️ Multi-Brand Debloat Profiles

Pre-configured, verified debloat profiles are organized in [`profiles/`](profiles/):

| Brand / Platform | Profile File | Target Capabilities Cleaned |
|---|---|---|
| **Xiaomi / Redmi** | [`profiles/xiaomi.txt`](profiles/xiaomi.txt) | PatchWall, Glance lockscreen ads, Mi Video, Mi Music, Mi Analytics |
| **Sony BRAVIA** | [`profiles/sony-bravia.txt`](profiles/sony-bravia.txt) | Samba TV ACR tracking, Sony Select promos, Sony Discover, Demo mode |
| **TCL** | [`profiles/tcl.txt`](profiles/tcl.txt) | TCL Channel, TCL TTVS tracker, TCL Assistant, recommendation feeds |
| **OnePlus TV** | [`profiles/oneplus.txt`](profiles/oneplus.txt) | OxygenPlay, OnePlus Store, HeyTap telemetry, preloaded browser |
| **Hisense** | [`profiles/hisense.txt`](profiles/hisense.txt) | Hisense telemetry, partner ad services, sponsored home rows |
| **NVIDIA SHIELD** | [`profiles/nvidia-shield.txt`](profiles/nvidia-shield.txt) | Shield recommendation rows, telemetry, partner promotions |
| **Chromecast with Google TV** | [`profiles/chromecast-gtv.txt`](profiles/chromecast-gtv.txt) | Google TV sponsored rows, partner auto-installs, feedback sender |
| **Amazon Fire TV** | [`profiles/amazon-fire-tv.txt`](profiles/amazon-fire-tv.txt) | Fire TV sponsored banners, forced promotions, metrics |
| **Universal Fallback** | [`profiles/universal-safe.txt`](profiles/universal-safe.txt) | Safe for any Android TV 9.0+ device |

> [!NOTE]
> **Safety Whitelist:** None of our profiles ever disable Chromecast built-in (`com.google.android.apps.mediashell`), Google Assistant voice search (`com.google.android.katniss`), Wi-Fi/Bluetooth stacks, or Gboard.

---

## ⏪ Instant Rollback & True Full-State Restore

Every modification is 100% reversible. A complete snapshot of your TV's original state (enabled packages, original default Home launcher, animation scales, and system settings) is preserved in `backups/<device>_original/` (Day-1 baseline) and `backups/<device>_<timestamp>/` (per-run).

> [!NOTE]
> **What is restored:** System packages, stock home launcher, animation scales, and system display settings are fully restored. (Private app data directories and saved Wi-Fi credentials cannot be accessed or modified over standard ADB without root access).

To restore your TV back to its exact untouched factory state:
```bash
./tools/restore.sh backups/<device_snapshot_directory>
```
*What the restore engine does:*
- 🔄 Re-enables all original system packages.
- 📺 Restores your original stock launcher as the default Home.
- ⚡ Reverts animation scales back to original values.
- Offers interactive clean uninstallation of TVLauncher and SmartTube.
---

## What's Inside

- White glass home screen with a flip clock, rounded app tiles, and minimal red accents
- 📺 Dynamic Android TV app tiles with package-change refresh
- ⚡ Zero-lag volume hold acceleration
- 🧹 Audit, debloat, backup and full restore scripts with per-brand profiles

## 🤝 Contributing

Contributions are welcome! Report a bug, suggest a new suit, add a debloat profile for your TV, or pick up a `good first issue`. Read the [Contributing guide](CONTRIBUTING.md) to get started, and please follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## ⚖️ Project & Legal Disclaimer

TVLauncher is an independent, non-commercial, open-source project for personal TV customization.

- **Original artwork:** The app icon and TV banner were created for this project.
- **No affiliation:** This project is not affiliated with, endorsed, sponsored or approved by any television manufacturer or streaming platform.
- **Third-party trademarks:** App names and logos shown (such as YouTube, Netflix, Prime Video, Disney+ Hotstar, SmartTube, Google Play / Android) belong to their respective owners.
- Distributed free of charge under the MIT License.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
