# PureTube — Minimal Material 3 Ad-Free YouTube Player

**PureTube** (`v0.3.005`) is a lightweight, 100% ad-free native Android video player for YouTube links. Built with **Kotlin**, **Jetpack Compose (Material 3 / Dynamic Color)**, **NewPipeExtractor**, **AndroidX Media3 (ExoPlayer)**, and **Room SQLite**.

---

## ✨ Key Features

### 1. Minimalist Watch Screen & Android Share Integration
- **Clean Home Screen ([`WatchHomeScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):** Minimal **YouTube Red Shield + White Play Symbol** logo, single rounded Material 3 link input bar, a **"Paste from clipboard"** quick chip, and a **"Watch"** button.
- **Direct Share Target ([`MainActivity.kt`](app/src/main/java/com/puretube/player/MainActivity.kt)):** Share any video or Short directly from the official YouTube app or browser (`Share -> PureTube`) to immediately launch ad-free playback.

### 2. 100% Ad-Free Stream Extraction & Zero-Stall Startup
- **Local Stream Extraction ([`YoutubeStreamExtractor.kt`](app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt)):** Powered by `NewPipeExtractor v0.26.5` and `OkHttp` with HTTP/2 connection pooling ([`OkHttpDownloader.kt`](app/src/main/java/com/puretube/player/extractor/OkHttpDownloader.kt)) to fetch direct video, audio, subtitle streams, and video metadata/descriptions with zero ads or tracking.
- **Native DASH Segment Playback & Anti-Throttling DataSource ([`YoutubeHttpDataSource.kt`](app/src/main/java/com/puretube/player/extractor/YoutubeHttpDataSource.kt)):** Generates in-memory DASH MPD manifests (`YoutubeProgressiveDashManifestCreator` + `DashMediaSource`) and pairs them with a custom `YoutubeHttpDataSource` that matches the stream's YouTube client `User-Agent` and uses bounded `&range=start-end` requests to eliminate `googlevideo.com` CDN throttling.
- **Parallel Startup RAM Preloader (`YoutubeStreamPreloader`):** Prefetches the initial `640 KB` video and `192 KB` audio byte ranges (covering MP4 initialization headers, `sidx` segment index, and the first few seconds of media) into RAM in parallel as soon as extraction completes for instant, buffer-free playback during the opening seconds.

### 3. Battery & Thermal Optimizations
- **Zero-Recomposition Hidden Overlay ([`VideoPlayerScreen.kt`](app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):** Stops UI position/buffer polling completely whenever player controls auto-hide (`controlsVisible == false`), eliminating background Compose recompositions while watching a video. When controls are visible, position updates tick once per second (`1000ms`).
- **Hardware-Accelerated 30fps `AVC1 (H.264)` Priority ([`YoutubeStreamExtractor.kt`](app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt)):** Strictly prioritizes hardware-decoded `H.264 (avc1)` 30fps streams over CPU-heavy `AV1` software decoding or 60fps streams by default, keeping the SoC cool.
- **Burst Network Buffering & Smart Wake Lock:** Uses balanced `12s–30s` `DefaultLoadControl` segment bursts so Wi-Fi/cellular radios sleep between chunks, and automatically releases `FLAG_KEEP_SCREEN_ON` when playback is paused or ended.
- **Low-Frequency Database Sync:** Persists watch history progress to SQLite every 15 seconds during active playback (and immediately on pause/exit) instead of constant disk writes.

### 4. Original YouTube Player UI & Expandable Video Description ([`VideoPlayerScreen.kt`](app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt))
- **Portrait Watch Page Layout:**
  - **16:9 Top Video Player Frame** positioned cleanly below the Android status bar inset.
  - **Watch Details Panel (`#0F0F0F`):** Displays the video title, active stream summary (`1080p · Audio: Original · CC · Ad-Free Stream`), channel row with avatar and **`Options`** button, and horizontally scrollable **YouTube Action Chips** (`Quality`, `Audio`, `Captions`, `Speed`, `Fullscreen`, `Share`, `Copy link`).
  - **Expandable Video Description Card (`#1A1A1A`):** Displays view count, upload date, and like count at the top of the card with a 3-line description preview (`More` / `Show less`) that expands smoothly to show the full selectable video description.
- **Authentic YouTube Overlay Controls:**
  - **Top Bar:** Collapse chevron (`KeyboardArrowDown`) on the left (plus video title & channel name in fullscreen), and top-right quick icons for **Audio Track**, **Closed Captions (`CC`)**, **Fit/Fill toggle** (in fullscreen), and the **Settings Gear (`⚙️`)**.
  - **Center Controls:** Translucent dark circular buttons (`-10s`, `Play / Pause / Replay`, `+10s`) with crisp white icons.
  - **Bottom Bar & YouTube Red (`#FF0000`) Seekbar:** Timestamp (`0:12 / 4:20`) on the left and `Fullscreen` toggle on the right, placed above a custom **YouTube Red Seekbar** showing unplayed, white buffered progress, and active red progress tracks with a scrubbing preview bubble.
  - **Unified YouTube Settings (`⚙️`) Bottom Sheet:** Access **Quality**, **Playback speed** (`0.25x`–`2.0x`), **Captions**, **Audio track**, **Zoom to fill screen**, and **Lock screen** from one menu.

### 5. Fullscreen Pinch-to-Zoom (Fit & Fill), Multi-Audio, Captions (`CC`), & Watch History
- **Two-Finger Pinch in Fullscreen:** Pinch out for `Zoomed to fill` (`RESIZE_MODE_ZOOM`) or pinch in for `Original` (`RESIZE_MODE_FIT`). Pinching out in portrait smoothly enters fullscreen.
- **Multi-Audio Track Switcher:** Automatically defaults to the **`Original`** audio track and supports switching to dubbed or descriptive tracks at any resolution.
- **Closed Captions (`CC`):** Extracts manual and auto-generated YouTube subtitle tracks (`WebVTT` / `TTML`).
- **Local Watch History ([`WatchHistoryDatabase.kt`](app/src/main/java/com/puretube/player/data/WatchHistoryDatabase.kt) & [`HistoryScreen.kt`](app/src/main/java/com/puretube/player/ui/screens/HistoryScreen.kt)):** Saves watched videos and last playback positions locally in Room SQLite.

---

## 📦 Tech Stack & Dependencies

All dependencies and versions are managed centrally in [`gradle/libs.versions.toml`](gradle/libs.versions.toml) and [`app/build.gradle.kts`](app/build.gradle.kts):

| Category | Library / Artifact | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language & Build** | Kotlin (`org.jetbrains.kotlin.android`) | `2.0.21` | Core language & Compose compiler plugin |
| **Build Plugin** | Android Gradle Plugin (`com.android.application`) | `8.7.2` | Android app packaging (`compileSdk = 35`, `minSdk = 24`) |
| **Annotation Processing** | KSP (`com.google.devtools.ksp`) | `2.0.21-1.0.27` | Room SQLite compile-time code generation |
| **UI Framework** | Jetpack Compose BOM (`androidx.compose:compose-bom`) | `2024.11.00` | Material 3 UI, animations, gestures, and extended icons |
| **Core & Lifecycle** | `androidx.core:core-ktx`, `lifecycle-runtime-ktx`, `activity-compose`, `navigation-compose` | `1.15.0` / `2.8.7` / `1.9.3` / `2.8.4` | AndroidX lifecycle, ViewModel, and screen navigation |
| **Video Player** | AndroidX Media3 (`media3-exoplayer`, `media3-exoplayer-dash`, `media3-exoplayer-hls`, `media3-ui`) | `1.5.0` | Hardware-accelerated DASH/HLS/Progressive video & subtitle rendering |
| **YouTube Extraction** | TeamNewPipe `NewPipeExtractor` (`com.github.TeamNewPipe:NewPipeExtractor`) | `v0.26.5` | Local ad-free YouTube video, audio, caption & description extraction |
| **Networking** | Square `OkHttp` (`com.squareup.okhttp3:okhttp`) | `4.12.0` | Shared HTTP/2 connection pool & ranged stream fetching |
| **Local Database** | AndroidX Room (`room-runtime`, `room-ktx`, `room-compiler`) | `2.6.1` | Local watch history & resume position SQLite persistence |
| **Image Loading** | Coil Compose (`io.coil-kt:coil-compose`) | `2.7.0` | Async video thumbnail rendering in Watch History |
| **Concurrency & Java 8+** | `kotlinx-coroutines-android` / `desugar_jdk_libs_nio` | `1.9.0` / `2.1.3` | Async stream extraction & Java NIO/time API desugaring for NewPipe |

---

## 🛠️ Building & Installing the APK

### Pre-Built APK Files
Ready-to-install APKs in the project root:
- [`PureTube-v0.3.005-debug.apk`](PureTube-v0.3.005-debug.apk) (`v0.3.005`, `versionCode = 8`)
- [`PureTube-debug.apk`](PureTube-debug.apk) (latest build — `v0.3.005`, `versionCode = 8`)

> **In-Place Upgrades:** All builds are signed with [`app/keystore/puretube.keystore`](app/keystore/puretube.keystore), ensuring future builds update directly over the installed app without uninstalling.

### Option 1: Build via Local Toolchain Script (Windows)
```cmd
.build-tools\build-apk.bat
```
Compiles the project and automatically exports `PureTube-v<versionName>-debug.apk` and `PureTube-debug.apk`.

### Option 2: GitHub Actions Automated Build & Releases
The workflow in [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) automatically:
1. Reads `versionName` and `versionCode` from [`app/build.gradle.kts`](app/build.gradle.kts).
2. Builds `PureTube-v<versionName>-debug.apk` on every push to `main`/`master` and uploads it as a downloadable workflow artifact.
3. **Automatic GitHub Releases:** Whenever you push a tag starting with `v` (e.g., `git tag v0.3.005 && git push origin v0.3.005`), GitHub Actions automatically creates a **GitHub Release** titled `PureTube v0.3.005` with version details and attaches `PureTube-v0.3.005-debug.apk` for 1-click download.

---

## 🚀 Pushing to Your GitHub Repository

1. **Initialize Git & Commit (run in project root):**
   ```powershell
   git init
   git add .
   git commit -m "Initial commit: PureTube v0.3.005"
   ```
   *(Note: [`.gitignore`](.gitignore) is already configured to exclude `.build-tools/`, `.gradle/`, `build/`, `local.properties`, `.idea/`, and local `*.apk` binaries so your repository stays lightweight and clean.)*

2. **Create a New Repository on GitHub & Push:**
   ```powershell
   git branch -M main
   git remote add origin https://github.com/<your-username>/PureTube.git
   git push -u origin main
   ```

3. **Trigger an Official Versioned GitHub Release (with attached APK):**
   ```powershell
   git tag v0.3.005
   git push origin v0.3.005
   ```

---

## 📝 Changelog
See [`PROJECT_LOG.md`](PROJECT_LOG.md) for the detailed version-by-version development log (`v0.1` -> `v0.3.005`).
