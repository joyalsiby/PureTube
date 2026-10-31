<div align="center">

<img src="../assets/logo.svg" width="72" height="72" alt="PureTube logo" />

# PureTube — The Complete Guide

Everything about PureTube in one place: features, settings, installation, architecture, and the build & release process.

[⬅️ Back to README](../README.md) · [📝 Changelog](../PROJECT_LOG.md) · [⬇️ Latest Release](https://github.com/joyalsiby/PureTube/releases/latest)

</div>

---

## 📑 Contents

- [Features in Depth](#-features-in-depth)
  - [Kids & Focus Mode](#-kids--focus-mode-parental-controls)
  - [The Player](#-the-player)
  - [Gestures](#-gestures)
  - [Background Play, PiP & Media Controls](#-background-play-pip--media-controls)
  - [Playlists, Clipboard & History](#-playlists-clipboard--history)
  - [Updates, Onboarding & Release Notes](#-updates-onboarding--release-notes)
  - [Performance & Battery](#-performance--battery)
- [App Navigation & Settings](#-app-navigation--settings)
- [Installation](#-installation)
  - [Direct APK](#option-a--direct-apk)
  - [Obtainium](#option-b--obtainium-auto-updates)
  - [F-Droid / IzzyOnDroid Readiness](#f-droid--izzyondroid-readiness)
  - [Google Play Protect Warning](#-google-play-protect-warning)
- [Tech Stack & Architecture](#-tech-stack--architecture)
- [Building from Source](#-building-from-source)
- [Releasing a New Version](#-releasing-a-new-version)

---

## ✨ Features in Depth

### 🧒 Kids & Focus Mode (Parental Controls)

| Feature | What it does |
| :--- | :--- |
| **Link-only watch flow** | The [Watch screen](../app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt) is just the logo, one URL bar, a *Paste from clipboard* chip, and a **Watch** button. No feed, no search, no Shorts. |
| **Share target & default link handler** | Share any YouTube watch, Shorts, `youtu.be`, or playlist link to PureTube (`Share → PureTube`), or set PureTube as Android's default handler for YouTube links ([`MainActivity.kt`](../app/src/main/java/com/puretube/player/MainActivity.kt)). |
| **Start with Kids Screen Lock** | Playback opens with touch controls locked, so toddlers can't pause, scrub, or exit by accident. |
| **Toddler-proof unlock** | Choose how the lock is released: **Hold 2s** (default, with a progress bar), a **Parent Math Challenge** (`a × b = ?`), or a classic **Single Tap**. |
| **Auto-fullscreen** | Open videos straight into landscape fullscreen. |
| **Minimal watch page** | Hide the description, view counts, and like stats below the player. |

### 🎬 The Player

Built on **AndroidX Media3 (ExoPlayer)** with a YouTube-style Material 3 UI ([`VideoPlayerScreen.kt`](../app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)).

- **Up to 4K / 60fps adaptive playback.** High-bitrate video and audio streams are merged through in-memory DASH manifests.
- **Multi-audio track switcher.** Always starts on the **Original** language track, never a random AI dub. You can switch to dubbed or descriptive tracks at any resolution.
- **Closed captions (`CC`).** Supports manual and auto-generated subtitles (`WebVTT` / `TTML`).
- **Chapters & clickable timestamps.** Chapter markers sit on the red seekbar, the active chapter title shows while you scrub, a chapters bottom sheet lets you jump around, and every `01:23` in the description is a tap-to-seek link.
- **Single-video loop.** A repeat toggle in the action chips, the fullscreen quick-actions strip, and the ⚙️ sheet.
- **Quality & speed.** Pick a resolution and a playback speed from `0.25x` to `2x` in the unified ⚙️ settings sheet.

### 👆 Gestures

| Gesture | Action |
| :--- | :--- |
| Double-tap left / right | Seek `-10s` / `+10s` |
| Long-press anywhere | Temporary `2x` speed while held |
| Swipe up/down on the left half *(fullscreen)* | Brightness `0–100%` |
| Swipe up/down on the right half *(fullscreen)* | Volume `0–100%` |
| Pinch out / in *(fullscreen)* | **Zoomed to fill** / **Original** fit |
| Pinch out *(portrait)* | Expand to fullscreen |

### 🔔 Background Play, PiP & Media Controls

- **Picture-in-Picture.** Leave the app while a video is playing and it floats into a closeable 16:9 PiP window with `-10s`, play/pause, and `+10s` controls. Closing PiP pauses playback, the same way YouTube does.
- **Background audio.** Off by default. Turn it on to keep audio playing with the screen off or after PiP is closed.
- **Media notification & lockscreen controls.** Integrates with Android's `MediaSession`, so title, channel, artwork, seekbar, `±10s`, play/pause, and close all appear in the Quick Settings media panel and on the lockscreen ([`MediaPlaybackNotificationManager.kt`](../app/src/main/java/com/puretube/player/ui/player/MediaPlaybackNotificationManager.kt)).

### 📋 Playlists, Clipboard & History

- **Playlists.** Share or paste a playlist link (`playlist?list=…` or `watch?v=…&list=…`). Videos play in order with Previous/Next controls and a queue sheet, still with no outside recommendations.
- **Smart clipboard card.** If a YouTube link is already on the clipboard, the Watch screen offers a one-tap **Play Now** / **Paste Link** card.
- **Local watch history.** Stored in Room SQLite with thumbnails, progress bars, one-tap resume, search by title or channel, and filters for `All` / `In Progress` / `Completed` ([`HistoryScreen.kt`](../app/src/main/java/com/puretube/player/ui/screens/HistoryScreen.kt)).

### 🔄 Updates, Onboarding & Release Notes

- **Built-in updater.** On launch, PureTube checks [GitHub Releases](https://github.com/joyalsiby/PureTube/releases) for a newer version. If it finds one, it shows an update dialog, a home banner, and a Settings badge. The APK downloads inside the app with a live progress bar and installs over the current version, so watch history and settings are kept ([`AppUpdateManager.kt`](../app/src/main/java/com/puretube/player/updater/AppUpdateManager.kt)).
- **What's New dialog.** After each update, a release-notes modal appears automatically. You can reopen it any time from the Watch screen or Settings ([`AppReleaseNotes.kt`](../app/src/main/java/com/puretube/player/updater/AppReleaseNotes.kt)).
- **Welcome onboarding.** A 4-step quick-start tour runs on first install and covers the share flow, Kids Mode setup, and gestures. You can replay it from Settings ([`OnboardingScreen.kt`](../app/src/main/java/com/puretube/player/ui/screens/OnboardingScreen.kt)).

### ⚡ Performance & Battery

- **Parallel startup preloader.** While the stream is being extracted, the first chunks of video and audio are prefetched into RAM, so playback starts almost instantly without an initial stutter ([`YoutubeHttpDataSource.kt`](../app/src/main/java/com/puretube/player/extractor/YoutubeHttpDataSource.kt)).
- **Anti-throttling data source.** Matches the `User-Agent` to the stream's YouTube client and uses bounded `&range=` requests over a shared HTTP/2 connection pool to avoid CDN throttling.
- **Cool & battery-friendly.** No UI recompositions while controls are hidden, hardware-decoded `AVC1 (H.264)` streams are preferred, the screen wake lock is released on pause, and watch-history writes are kept infrequent.

---

## 🧭 App Navigation & Settings

PureTube uses a Material 3 bottom bar with three tabs.

| Tab | Contents |
| :--- | :--- |
| ▶️ **Watch** | Link input, clipboard quick-paste, update banner, What's New shortcut |
| 🕒 **History** | Local watch history with resume, search, filters, delete & clear all |
| ⚙️ **Settings** | All the options below |

<details>
<summary><b>⚙️ Full Settings reference</b></summary>

<br />

| Section | Options |
| :--- | :--- |
| **App Updates** | Check for updates · auto-check on startup · What's New · replay onboarding |
| **Kids & Focus Mode** | Start with Kids Screen Lock · unlock method (Hold 2s / Math / Tap) · auto-fullscreen · minimal watch page · set as default link handler |
| **Playback & Quality** | Default resolution (`4K` → `360p`) · default speed · background audio · auto-PiP |
| **Appearance** | System / Dark / Light theme · Material You dynamic color (Android 12+) |
| **Privacy & History** | Save local watch history · clear history |
| **About & Developer** | Creator links, repository, releases, issue tracker, share PureTube |

</details>

---

## 📦 Installation

### Option A — Direct APK

1. Open the [latest release](https://github.com/joyalsiby/PureTube/releases/latest) and download the `PureTube-v*.apk` asset.
2. Open it on your phone and allow *Install unknown apps* for your browser or file manager if Android asks.
3. You only need to do this once. After that, updates come through **Settings → Check for Updates**.

### Option B — Obtainium (auto-updates)

[Obtainium](https://github.com/ImranR98/Obtainium) tracks GitHub Releases directly:

1. Install Obtainium, tap **Add App**, and paste `https://github.com/joyalsiby/PureTube`.
2. Obtainium will notify you about new releases and install them.

### F-Droid / IzzyOnDroid Readiness

PureTube is built to meet F-Droid's inclusion requirements:

- ✅ **FOSS-only dependencies:** Kotlin, Jetpack Compose, AndroidX Media3, Room, NewPipeExtractor, OkHttp, Coil.
- ✅ **Google dependency metadata blob disabled** (`dependenciesInfo { includeInApk = false; includeInBundle = false }`) in [`app/build.gradle.kts`](../app/build.gradle.kts).
- ✅ **Verified Gradle wrapper:** `distributionSha256Sum` is pinned in [`gradle-wrapper.properties`](../gradle/wrapper/gradle-wrapper.properties).
- ✅ **Fastlane metadata & MIT license:** see [`fastlane/metadata/android/en-US/`](../fastlane/metadata/android/en-US/) and [`LICENSE`](../LICENSE).

### 🔐 Google Play Protect Warning

Apps that play YouTube without ads can't be listed on the Google Play Store. Because of that, **Google Play Protect** may show *"App scan recommended"* or *"Unrecognized app developer"* the first time you install PureTube.

- **What to do:** tap **Scan app**, which normally finishes in a few seconds and reports the app as safe. Or tap **More details → Install anyway**.
- **Why it only happens once:** every PureTube build, local or CI, is signed with the same project keystore ([`app/keystore/puretube.keystore`](../app/keystore/puretube.keystore)). Android already trusts that key after the first install, so updates install without the prompt.
- **Release builds:** PureTube ships non-debuggable `assembleRelease` APKs with the dependency metadata blob disabled, which avoids debug-build heuristics.

<details>
<summary><b>🔑 Signing certificate fingerprint (SHA-256)</b></summary>

```
EE:B9:37:AA:97:5D:0E:F8:A1:0B:3C:CF:86:20:14:DC:13:E5:D8:6E:86:D5:04:B7:C1:E4:BE:4B:16:2B:C5:79
```

To confirm a downloaded APK is genuine, run `apksigner verify --print-certs PureTube-v*.apk` and compare the output with this value.

</details>

---

## 🧱 Tech Stack & Architecture

Versions below are read live from [`gradle/libs.versions.toml`](../gradle/libs.versions.toml), so they always match the code.

| Category | Library | Version | Purpose |
| :--- | :--- | :---: | :--- |
| **Language** | Kotlin | ![Kotlin](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.kotlin&label=&color=7F52FF&style=flat-square) | Native Android development |
| **UI** | Jetpack Compose (Material 3) | ![Compose BOM](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.composeBom&label=BOM&color=4285F4&style=flat-square) | Declarative Material You UI, gestures & animations |
| **Video engine** | AndroidX Media3 (ExoPlayer, DASH, HLS, Session) | ![Media3](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.media3&label=&color=3DDC84&style=flat-square) | Hardware-accelerated adaptive playback & media notification |
| **Extraction** | TeamNewPipe NewPipeExtractor | ![NewPipeExtractor](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.newpipeExtractor&label=&color=CD201F&style=flat-square) | Local, ad-free video, audio, chapter & subtitle extraction |
| **Networking** | Square OkHttp | ![OkHttp](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.okhttp&label=&color=1D9F6F&style=flat-square) | HTTP/2 pooling, range requests & APK updater |
| **Persistence** | AndroidX Room + `SharedPreferences` | ![Room](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.room&label=&color=4CAF50&style=flat-square) | Watch history, resume positions & settings |
| **Images** | Coil Compose | ![Coil](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fjoyalsiby%2FPureTube%2Fmain%2Fgradle%2Flibs.versions.toml&query=%24.versions.coil&label=&color=F59E0B&style=flat-square) | Async thumbnail loading |

### Project layout

```text
app/src/main/java/com/puretube/player/
├── MainActivity.kt            # Entry point, share/intent handling, bottom navigation, PiP
├── PureTubeApplication.kt
├── data/                      # AppPreferences (settings) & Room watch-history database
├── extractor/                 # NewPipeExtractor wrapper, OkHttp downloader, anti-throttle DataSource
├── ui/
│   ├── MainViewModel.kt
│   ├── player/                # Video player screen & media notification manager
│   ├── screens/               # Watch, History, Settings & Onboarding screens
│   └── theme/                 # Material 3 / Material You theme
└── updater/                   # GitHub release updater & in-app release notes
```

---

## 🔨 Building from Source

**Requirements:** JDK 17 and the Android SDK (the `compileSdk` platform set in [`app/build.gradle.kts`](../app/build.gradle.kts)). The Gradle wrapper downloads the correct Gradle version for you.

```bash
# macOS / Linux
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

The signed APK is written to `app/build/outputs/apk/release/app-release.apk`.

---

## 🚀 Releasing a New Version

Releases are fully automated by [`.github/workflows/build-apk.yml`](../.github/workflows/build-apk.yml):

1. Bump `versionCode` and `versionName` in [`app/build.gradle.kts`](../app/build.gradle.kts). The version format is `X.X.XXX`, and only the 3-digit patch normally increments.
2. Add a `## vX.X.XXX — Title` section to [`PROJECT_LOG.md`](../PROJECT_LOG.md). That section becomes the GitHub release notes.
3. Commit, then push a matching tag (for example `git tag vX.X.XXX && git push origin vX.X.XXX`).

The workflow builds the release APK, names it `PureTube-v<versionName>.apk`, takes that version's changelog from `PROJECT_LOG.md`, and publishes a GitHub Release marked **Latest**. Every push and pull request to `main` also builds an APK and uploads it as a CI artifact.

---

<div align="center">

[⬅️ Back to README](../README.md)

</div>
