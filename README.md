<div align="center">

<img src="app/src/main/res/drawable/ic_launcher_legacy.xml" width="0" height="0" />

# 🛡️ PureTube
### **The Distraction-Free, 100% Ad-Free YouTube Player for Kids, Parents & Deep Focus**

*Keep the official YouTube app locked. Share or paste only the exact video link you want your child—or yourself—to watch in a clean Material You (MUI3) interface with **Zero Ads**, **No Search**, **No Shorts**, and **No Algorithmic Rabbit Holes**.*

[![Latest Release](https://img.shields.io/github/v/release/joyalsiby/PureTube?style=for-the-badge&logo=github&color=E50914&label=Latest%20Release)](https://github.com/joyalsiby/PureTube/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/joyalsiby/PureTube/build-apk.yml?style=for-the-badge&logo=githubactions&label=APK%20Build)](https://github.com/joyalsiby/PureTube/actions)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/joyalsiby/PureTube/releases)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](LICENSE)

<br />

[**⬇️ Download Latest APK (`v0.3.007`)**](https://github.com/joyalsiby/PureTube/releases/latest) · [**📦 Install via Obtainium / F-Droid**](#-f-droid--obtainium-installation) · [**🛡️ Bypass Play Protect Warning**](#-fixing-the-google-play-protect-scan-warning) · [**📝 Changelog**](PROJECT_LOG.md)

</div>

---

## 👨‍👩‍👧 Why PureTube? (Built for Parents, Kids & Mindful Focus)

When you hand a phone or tablet to a child—or open YouTube yourself to watch a single tutorial or lecture—the official YouTube app immediately bombards you with **unskippable ads**, **endless Shorts feeds**, **auto-playing "Up Next" suggestions**, and a **Search bar** that leads straight into an algorithmic rabbit hole.

**PureTube flips the model completely:**
1. **🔒 Lock the Official YouTube App:** Parents can keep the main YouTube app locked (using App Lock / Parental Controls) or disabled on the child's device.
2. **📲 Share or Paste 1 Link:** Simply tap **`Share -> PureTube`** from a parent's link, WhatsApp, or browser—or paste the link directly into PureTube's single rounded Material 3 input bar.
3. **🎯 Watch That Video—Nothing Else:** PureTube plays **only** that video with **100% zero ads**, **no search bar**, **no home feed**, **no comments**, and **no recommended video sidebar**. When the video finishes, it stops.

| Feature | Official YouTube / YouTube Kids | 🛡️ **PureTube** |
| :--- | :---: | :---: |
| **Pre-roll / Mid-roll Video Ads** | ❌ Frequent ads & sponsored cards | ✅ **100% Ad-Free** |
| **Algorithmic Home Feed & Shorts** | ❌ Endless scrolling rabbit holes | ✅ **None — Link-Only Focus** |
| **Search Bar & Related Videos** | ❌ Easy for kids to wander off | ✅ **Zero Search / Zero Suggestions** |
| **Kids Screen Lock on Startup** | ❌ Manual or premium-only | ✅ **Auto Screen Lock for Toddlers** |
| **Background Play & PiP** | 💰 Requires YouTube Premium | ✅ **100% Free & Built-In** |
| **In-App One-Tap Auto Updater** | — | ✅ **Built-In GitHub Release Updater** |

---

## ✨ Core Features

### 🧒 1. Kids & Focus Mode (Parental Controls)
- **Zero-Distraction Watch Flow ([`WatchHomeScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):** Features only the minimal **PureTube Red Shield** logo, a single rounded Material 3 URL bar, a **Paste from clipboard** chip, and a **Watch** button.
- **Auto-Start with Kids Screen Lock ([`SettingsScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/SettingsScreen.kt)):** Enable **"Start videos with Kids Screen Lock"** in Settings so playback immediately locks touch controls—preventing toddlers from pausing, scrubbing, or exiting accidentally.
- **Auto-Fullscreen & Distraction-Free Toggle:** Option to launch videos directly into landscape fullscreen and hide the expandable video description/stats card underneath.
- **Direct Android Share Target & Default Link Handler ([`MainActivity.kt`](app/src/main/java/com/puretube/player/MainActivity.kt)):** Share any YouTube watch, Short, or `youtu.be` link directly to **PureTube** (`Share -> PureTube`) for instant playback.

### 🎬 2. Authentic YouTube Player UI in Material 3 ([`VideoPlayerScreen.kt`](app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt))
- **Up to 4K / 1080p60 Adaptive Playback:** Merges high-bitrate adaptive video streams with crystal-clear audio using **AndroidX Media3 (`ExoPlayer`)** and in-memory DASH manifests.
- **Multi-Audio Track Switcher (Defaults to `Original`):** Automatically selects the **Original** language audio track first (never a random AI dub!) and lets you switch between Original, Dubbed, and Descriptive audio tracks at any resolution.
- **Closed Captions (`CC`) & Subtitles:** Supports manual and auto-generated YouTube subtitles (`WebVTT` / `TTML`) with a quick top-bar toggle.
- **Video Chapters & Clickable Timestamps:** Renders chapter markers directly on the **YouTube Red (`#FF0000`) Seekbar**, shows active chapter titles while scrubbing, includes a **Chapters Bottom Sheet**, and turns all `01:23` timestamps in the description into clickable jump links.
- **Fullscreen Touch Gestures:**
  - **Left Vertical Swipe:** Smoothly adjust screen brightness (`0%–100%`) with top-center HUD.
  - **Right Vertical Swipe:** Smoothly adjust media volume (`0%–100%`) with top-center HUD.
  - **Two-Finger Pinch:** Pinch out for **Zoomed to fill** (`RESIZE_MODE_ZOOM`) or pinch in for **Original** (`RESIZE_MODE_FIT`).
  - **Double-Tap Left/Right:** Instant `-10s` / `+10s` seek with ripple feedback.
- **Background Audio Play & Picture-in-Picture (PiP):** Keep audio playing with the screen off, or press Home to float the video in a 16:9 Picture-in-Picture window.

### 🔄 3. Built-In In-App Release Notifier & 1-Tap Updater ([`AppUpdateManager.kt`](app/src/main/java/com/puretube/player/updater/AppUpdateManager.kt))
- **Automatic Update Notifications:** On launch, PureTube quietly checks [GitHub Releases](https://github.com/joyalsiby/PureTube/releases) for a newer `vX.X.XXX` release.
- **In-App Download & Seamless Upgrade:** When a new release is out, an **Update Dialog** (and a badge on the **Settings** tab + home banner) lets you view the changelog, download the APK with a live progress bar right inside PureTube, and trigger the Android package installer to update in-place without losing your watch history or settings.

### ⚡ 4. Zero-Stall Startup & Battery / Thermal Engineering
- **Parallel Startup RAM Preloader ([`YoutubeHttpDataSource.kt`](app/src/main/java/com/puretube/player/extractor/YoutubeHttpDataSource.kt)):** Prefetches the first `640 KB` of video and `192 KB` of audio into RAM in parallel during stream extraction so playback starts in `<1.2s` with zero initial stutter.
- **Anti-Throttling DASH DataSource:** Matches the `User-Agent` to the stream's YouTube client (`IOS`, `ANDROID`, `ANDROID_VR`) and uses bounded `&range=start-end` requests over a shared HTTP/2 connection pool to eliminate `googlevideo.com` CDN throttling.
- **Cool & Battery-Friendly:** Zero Compose UI recompositions while player controls are hidden, hardware-decoded `AVC1 (H.264)` 30fps priority, smart wake-lock release on pause, and low-frequency (15s) SQLite watch history writes.

---

## 🧭 App Navigation (Material 3 Bottom Bar)

1. **▶️ Watch ([`WatchHomeScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):** Minimal link input, clipboard quick-paste, update notification banner, and instant player launch.
2. **🕒 History ([`HistoryScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/HistoryScreen.kt)):** Local Room SQLite watch history with thumbnails, progress bars, 1-tap resume from your last position, individual delete, or clear all.
3. **⚙️ Settings ([`SettingsScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/SettingsScreen.kt)):**
   - **App Updates:** Check for updates, view current version, 1-tap download & install, and auto-check toggle.
   - **Kids & Focus Mode (Parental Controls):** Auto-start with Kids Screen Lock, Auto-Fullscreen on play, Hide video description/stats, and shortcut to set PureTube as Android's default YouTube link handler.
   - **Playback & Quality:** Default startup resolution (`4K` / `1440p` / `1080p` / `720p` / `480p` / `360p`), default playback speed, Background Play toggle, and Auto-PiP toggle.
   - **Appearance:** System / Dark / Light theme selector + Material You Dynamic Color toggle.
   - **Privacy & History:** Pause/enable watch history saving & clear history.
   - **About & Links:** Direct links to GitHub Repository, Releases & Changelog, Issue Tracker, and Share PureTube.

---

## 🛡️ Fixing the Google Play Protect "Scan / Unknown App" Warning

Because apps that extract ad-free YouTube streams cannot be hosted on the Google Play Store (due to Google's terms regarding YouTube ad-blocking and background play), Android's **Google Play Protect** may show an *"App scan recommended"* or *"Unrecognized app developer"* prompt when installing for the first time.

### Why Does This Warning Appear?
1. **New Self-Signed Certificate Reputation:** Google Play Protect tracks the SHA-256 signing certificate of every APK across Android devices. Until an open-source certificate has been installed by hundreds of devices cleanly, Play Protect prompts the user to "Scan app".
2. **`CN=Android Debug` Certificate Subject:** APKs signed with a standard debug-style certificate (`CN=Android Debug`) or debuggable flag are flagged more aggressively by Play Protect heuristics than production release APKs.

### ✅ How PureTube Minimizes & Bypasses It
- **Persistent Keystore Across All Releases:** Every build of PureTube (local and GitHub Actions) is signed with the exact same [`app/keystore/puretube.keystore`](app/keystore/puretube.keystore) (`SHA-256: 02:BC:9E:CC:A9:05:89:34:C5:1D:FE:EC:F6:52:97:68:49:3D:C9:B4:85:69:3C:38:AA:33:85:92:E7:5A:68:41`).
- **Once Scanned, Updates Never Warn Again:** When installing PureTube for the first time:
  1. If Play Protect asks **"Scan app before installing?"**, tap **"Scan app"**. Because PureTube uses zero obfuscation, zero trackers, and clean AndroidX/NewPipe open-source code, the scan passes as **Safe** in ~2 seconds.
  2. Or tap **"More details" -> "Install anyway"**.
  3. **All future updates installed via PureTube's built-in Updater will install seamlessly without repeating the warning** because the signing key matches the already-trusted app on your phone!
- **Developer Tip — Appeal to Google Play Protect (Optional):** You can submit your release APK directly to Google's [Play Protect Appeals Form](https://support.google.com/googleplay/android-developer/contact/protectappeals) so Google whitelists your signing certificate's SHA-256 fingerprint globally across all Android devices.

---

## 📦 F-Droid & Obtainium Installation

Since ad-free YouTube players cannot be published on the Google Play Store, here are the best ways to distribute and auto-update PureTube on Android:

### Option A: Instant F-Droid Experience via **Obtainium** (No Waiting!)
[**Obtainium**](https://github.com/ImranR98/Obtainium) lets anyone track and auto-update PureTube directly from GitHub Releases (just like an F-Droid store client):
1. Install **Obtainium** from F-Droid or GitHub.
2. Tap **"Add App"** -> Paste:
   ```text
   https://github.com/joyalsiby/PureTube
   ```
3. Obtainium will automatically track new releases and update PureTube in the background. *(Or just use PureTube's built-in **Settings -> Check for Updates** button!)*

### Option B: Publishing to **F-Droid** & **IzzyOnDroid**
PureTube is already configured to be **100% F-Droid compliant**:
- ✅ **100% Free & Open Source Dependencies** (Kotlin, Jetpack Compose, AndroidX Media3, Room, NewPipeExtractor, OkHttp, Coil — no proprietary Google Play Services or Firebase SDKs).
- ✅ **Google Dependency Metadata Blob Disabled:** `dependenciesInfo { includeInApk = false; includeInBundle = false }` is configured in [`app/build.gradle.kts`](app/build.gradle.kts) (required by F-Droid's scanner).
- ✅ **Fastlane Metadata Included:** Title, short description, and full description are ready in [`fastlane/metadata/android/en-US/`](fastlane/metadata/android/en-US/).
- ✅ **MIT License Included:** [`LICENSE`](LICENSE) is in the repository root.

#### Steps to Get Listed on **IzzyOnDroid** (Fastest F-Droid Repo — Takes 1–2 Days)
1. Push a tagged release on GitHub (`git tag v0.3.007 && git push origin v0.3.007`) so the APK is attached to your GitHub Release.
2. Open a new issue at [**gitlab.com/IzzyOnDroid/repo/-/issues**](https://gitlab.com/IzzyOnDroid/repo/-/issues) with your repo URL (`https://github.com/joyalsiby/PureTube`).
3. Once added, anyone with the **F-Droid**, **Droid-ify**, or **Neo Store** app (with the IzzyOnDroid repository enabled) can search for and install **PureTube**!

#### Steps to Get Listed on the **Official F-Droid Repository**
1. Fork [`gitlab.com/fdroid/rfp`](https://gitlab.com/fdroid/rfp) (Request for Packaging) or [`gitlab.com/fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata).
2. Submit an RFP issue with `https://github.com/joyalsiby/PureTube`, Category: `Multimedia`, License: `MIT`.
3. F-Droid's build servers will compile `PureTube` from source from your `v*` Git tags and publish it to the main F-Droid catalog.

---

## 🛠️ Tech Stack & Architecture

| Category | Library / Artifact | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language & UI** | Kotlin + Jetpack Compose (Material 3) | `2.0.21` / BOM `2024.11.00` | Native declarative Material You UI, gestures & animations |
| **Video Engine** | AndroidX Media3 (`ExoPlayer`, `Dash`, `Hls`, `Ui`) | `1.5.0` | Hardware-accelerated adaptive DASH/HLS/Progressive playback |
| **Stream Extraction** | TeamNewPipe `NewPipeExtractor` | `v0.26.5` | Local ad-free YouTube video, audio, chapter & subtitle extraction |
| **Networking & Updater** | Square `OkHttp` | `4.12.0` | HTTP/2 connection pooling, anti-throttling range requests & APK updater |
| **Local Persistence** | AndroidX Room SQLite + `SharedPreferences` | `2.6.1` | Local watch history, resume positions & parental/focus settings |
| **Image Loading** | Coil Compose | `2.7.0` | Async video thumbnail rendering in Watch History |

---

## 🚀 Building Locally & Releasing

### 1. Build APK Locally (Windows)
```cmd
.build-tools\build-apk.bat
```
Outputs [`PureTube-v0.3.007-debug.apk`](PureTube-v0.3.007-debug.apk) and [`PureTube-debug.apk`](PureTube-debug.apk) in the project root.

### 2. Automated GitHub Actions Release
Whenever you push a version tag starting with `v`, [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) automatically builds the APK and publishes a **GitHub Release** (which immediately notifies users via PureTube's in-app updater):
```powershell
git tag v0.3.007
git push origin v0.3.007
```

---

## 📄 License
Licensed under the [MIT License](LICENSE). Built with ❤️ for distraction-free viewing.
