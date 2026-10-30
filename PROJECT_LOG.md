# PureTube — Project Development Log

This document tracks everything built, configured, and improved in the **PureTube** Android project.

---

## v0.1 — Initial Architecture & Core App Build

1. **Minimal Material 3 Android App (`PureTube`):**
   - Built natively with **Kotlin** and **Jetpack Compose (Material 3 / Dynamic Color)**.
   - **Watch Screen ([`WatchHomeScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):** Clean single-bar layout to paste a YouTube link, a **"Paste from clipboard"** chip, and a **"Watch"** button.
   - **Android Share Integration ([`MainActivity.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/MainActivity.kt)):** Sharing a video from the official YouTube app or browser directly to PureTube immediately plays it.

2. **100% Ad-Free Stream Extraction ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt)):**
   - Integrated **`NewPipeExtractor v0.26.5`** + **`OkHttp`** ([`OkHttpDownloader.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/OkHttpDownloader.kt)) to extract raw video and audio streams locally on the phone with zero ads.

3. **Custom Material 3 / YouTube-Style Player ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - Powered by **AndroidX Media3 (`ExoPlayer`)** with `MergingMediaSource` to combine high-res `1080p60`/`4K` video streams with the best audio track.
   - Includes auto-hiding controls, double-tap `-10s` / `+10s` seek gestures, **Quality selector**, **Playback speed selector (`0.5x`–`2.0x`)**, seekbar, and fullscreen rotation toggle.

4. **Local Watch History ([`WatchHistoryDatabase.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/data/WatchHistoryDatabase.kt) & [`HistoryScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/HistoryScreen.kt)):**
   - Added a **Room SQLite database** and a **History** tab in the bottom navigation bar to save watched videos, resume progress, or clear history.

5. **Local Build Toolchain & APK:**
   - Installed **OpenJDK 17**, **Gradle 8.9**, and **Android SDK 35** inside `.build-tools/` and compiled the ready-to-install APK (`v0.1`).

---

## v0.2 — Portrait Status Bar Fix, Audio Track Switcher, Captions (CC), & Fast Playback Optimization

1. **Portrait Mode Top Controls Inset Fix ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Problem:** In portrait / non-fullscreen mode, Android's top status bar (clock, battery, notification icons) overlapped the player's top row (`Back`, `Speed`, and `Quality` buttons), making them unclickable unless switched to fullscreen.
   - **Fix:** Added dynamic status bar inset padding (`WindowInsets.statusBars.asPaddingValues().calculateTopPadding().coerceAtLeast(28.dp) + 8.dp`) in portrait mode and `navigationBarsPadding()` on the bottom controls bar. Organised the top header into two clean rows:
     - **Row 1:** Back button + Video Title & Channel Name.
     - **Row 2:** Horizontally scrollable action pills (`CC`, `Audio`, `Speed`, `Quality`).

2. **YouTube Multi-Audio Track Switcher — Defaulting to Original ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt), [`MainViewModel.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/MainViewModel.kt), [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Problem:** Previously, all audio streams were sorted purely by bitrate without checking `AudioTrackType`, which could pick a dubbed/translated track instead of the original audio and offered no way to switch audio tracks.
   - **Fix:**
     - Added [`AudioTrackOption`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt#L14-L21) extraction that inspects `audioTrackType` (`ORIGINAL`, `DUBBED`, `DESCRIPTIVE`), `audioTrackId`, `audioTrackName`, and `audioLocale`.
     - **Always defaults to the `Original` audio track** when a video loads.
     - Added an **`Audio: Original`** pill in the player top controls that opens a bottom sheet to switch between Original, English/Translated Dubs, and Descriptive audio tracks at any video resolution.

3. **Closed Captions / Subtitles (`CC`) Support ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt) & [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - Added [`CaptionOption`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt#L23-L30) extraction for both manual and auto-generated YouTube subtitle tracks (`WebVTT` / `TTML`).
   - Added a **`CC`** button in the player top controls bar that opens a Captions bottom sheet (`Off` + available languages) and renders subtitles directly on ExoPlayer's `PlayerView` via `SingleSampleMediaSource` and `TrackSelectionParameters`.

4. **Faster Video Startup & Reduced Buffering ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt), [`OkHttpDownloader.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/OkHttpDownloader.kt), [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Streamlined Extraction:** Replaced `StreamInfo.getInfo()` (which fetched extra secondary pages like related videos, channel avatars, subscriber counts, and descriptions) with direct `youtubeService.getStreamExtractor(cleanUrl)` calls for only the required stream data.
   - **Connection Pooling & Codec Priority:** Configured a persistent `ConnectionPool` in `OkHttpDownloader` and prioritized hardware-accelerated `MP4`/`AVC1 (H.264)` video streams + `M4A` audio streams.
   - **ExoPlayer Buffer Tuning:** Configured `DefaultLoadControl` with `bufferForPlaybackMs = 1000` (starts playing after just 1s of buffered media instead of 2.5s), `bufferForPlaybackAfterRebufferMs = 2000`, `maxBufferMs = 60000` (60s ahead buffer), and a `15000ms` back-buffer so seeking and playback stay smooth.

5. **Versioning & Build Configuration ([`app/build.gradle.kts`](file:///f:/Youtube%20Video%20watching%20app/app/build.gradle.kts), [`WatchHomeScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt), [`gradle.properties`](file:///f:/Youtube%20Video%20watching%20app/gradle.properties)):**
   - Set app version to **`v0.2`** (`versionCode = 2`, `versionName = "0.2"`) and displayed `v0.2` at the bottom of the home screen.
   - Set `kotlin.compiler.execution.strategy=in-process` so Kotlin compiles in-process without hanging on background daemon sockets.
   - Exported versioned APK at [`PureTube-v0.2-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-v0.2-debug.apk) (and updated [`PureTube-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-debug.apk)).


---

## v0.3 — Fullscreen Pinch-to-Zoom (Fit & Fill) & Original YouTube Player UI Layout

1. **Fullscreen Pinch-to-Zoom (`Zoomed to fill` / `Original` Fit) ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - Added two-finger multi-touch pinch gesture detection (`awaitEachGesture` + `calculateZoom`) that works alongside single-tap and double-tap seek gestures without conflict:
     - **Pinch Out (spread two fingers) in Fullscreen:** Switches `PlayerView` resize mode to `AspectRatioFrameLayout.RESIZE_MODE_ZOOM` so the video fills the entire ultrawide screen edge-to-edge and displays YouTube's centered **`Zoomed to fill`** pill toast.
     - **Pinch In (pinch two fingers together) in Fullscreen:** Switches back to `AspectRatioFrameLayout.RESIZE_MODE_FIT` and displays the **`Original`** pill toast.
     - **Pinch Out in Portrait:** Smoothly expands the player into fullscreen landscape mode (just like the official YouTube app).
   - Also added 1-tap **Fit / Fill** toggle buttons in the fullscreen overlay top bar, the fullscreen bottom quick-actions bar, and a **`Zoom to fill screen`** switch inside the unified Settings (`⚙️`) bottom sheet.

2. **Original YouTube Player UI Layout ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Portrait Watch Page Layout (16:9 Top Player + Details Below):**
     - Replaced the full-screen portrait black box with YouTube's classic **16:9 video player frame at the top** (below a safe status bar inset) and a dark (`#0F0F0F`) scrollable watch details panel underneath.
     - Below the 16:9 player: **Video Title**, metadata line (`Resolution · Audio · CC · Ad-Free Stream`), **Channel Row** with avatar & **`Options`** button, and **YouTube-style Horizontal Action Pills** (`Quality`, `Audio`, `Captions`, `Speed`, `Fullscreen`, `Share`, `Copy link`).
   - **Authentic YouTube Player Overlay Controls:**
     - **Top Bar:** Down/Collapse chevron (`KeyboardArrowDown`) on the left (+ Video Title & Channel subtitle in fullscreen), and YouTube's top-right icon bar: **Audio Track**, **Closed Captions (`CC`)** (with YouTube Red underline indicator when active), **Fit/Fill toggle** (in fullscreen), and the **Settings Gear (`⚙️`)**.
     - **Center Controls:** Translucent dark circular buttons (`Replay 10s`, large `Play / Pause / Replay`, `Forward 10s`) with crisp white icons, plus left/right side double-tap **`10 seconds`** ripple pills.
     - **Bottom Bar & YouTube Red (`#FF0000`) Seekbar:** Timestamp (`0:12 / 4:20` with bright white current time and subtle total duration) on the left and `Fullscreen` button on the right, positioned **above** a custom **YouTube Red (`#FF0000`) Seekbar** that renders unplayed, white buffered progress (`exoPlayer.bufferedPosition`), and active red progress tracks with a scrubbing time preview bubble.
     - **Fullscreen Bottom Quick-Actions Strip:** Below the seekbar in fullscreen mode, added quick-access pills for `Quality`, `Audio`, `Speed`, `CC`, `Zoomed to fill / Original`, and `Lock`.
   - **Unified YouTube Settings (`⚙️`) Bottom Sheet & Screen Lock:**
     - Tapping the top-right `⚙️` gear icon opens a unified dark YouTube Settings sheet with **Quality**, **Playback speed** (`0.25x`–`2.0x`), **Captions**, **Audio track**, **Zoom to fill screen**, and **Lock screen** (which locks player controls and shows a **`Screen locked · Tap to unlock`** pill on tap).

3. **Version Bump, Fixed Signing Keystore & Seamless In-Place APK Upgrade ([`app/build.gradle.kts`](file:///f:/Youtube%20Video%20watching%20app/app/build.gradle.kts), [`app/keystore/puretube.keystore`](file:///f:/Youtube%20Video%20watching%20app/app/keystore/puretube.keystore), [`WatchHomeScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):**
   - **Root Cause of Upgrade Conflict:** Previously, `app/build.gradle.kts` relied on Android Gradle Plugin's implicit `~/.android/debug.keystore` lookup, which resolved to different paths (`.build-tools/.android/debug.keystore` vs `%USERPROFILE%\.android\debug.keystore`) depending on environment variables, producing mismatched APK signing certificates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
   - **Fix:** Committed a persistent project keystore at [`app/keystore/puretube.keystore`](file:///f:/Youtube%20Video%20watching%20app/app/keystore/puretube.keystore) (SHA-256: `02:BC:9E:CC:A9:05:89:34:C5:1D:FE:EC:F6:52:97:68:49:3D:C9:B4:85:69:3C:38:AA:33:85:92:E7:5A:68:41`, matching the installed app) and configured explicit `signingConfigs` (`enableV1Signing`–`enableV4Signing`) for both `debug` and `release` build types in [`app/build.gradle.kts`](file:///f:/Youtube%20Video%20watching%20app/app/build.gradle.kts).
   - Bumped version to **`v0.3.1`** (`versionCode = 4`, `versionName = "0.3.1"`) and exported [`PureTube-v0.3-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-v0.3-debug.apk) and [`PureTube-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-debug.apk).

---

## v0.3.002 – v0.3.004 — Zero-Stall Startup Buffering Optimization, Minimal YouTube Red Shield + Play Logo, & Watch History Header Alignment

1. **Eliminated Initial Few-Seconds Video Buffering ([`YoutubeHttpDataSource.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeHttpDataSource.kt), [`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt), [`OkHttpDownloader.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/OkHttpDownloader.kt), [`MainViewModel.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/MainViewModel.kt), [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Root Causes Identified:**
     1. Previously, `VideoPlayerScreen` passed raw adaptive `googlevideo.com` URLs into `ProgressiveMediaSource`, which issued unbounded `Range: bytes=0-` HTTP requests. YouTube's CDN throttles open-ended requests after the initial ~64 KB burst (~1s of video), causing playback to start at `1.0s` (`bufferForPlaybackMs = 1_000`) and immediately stall/rebuffer at `1s–3s`.
     2. `DefaultHttpDataSource` used Android's legacy `HttpURLConnection` (HTTP/1.1 only, no connection reuse with `OkHttpDownloader`) and sent a hardcoded desktop Firefox `User-Agent` even when streams were signed for `c=IOS`, `c=ANDROID`, or `c=ANDROID_VR`.
     3. Calling `exoPlayer.prepare()` before `exoPlayer.seekTo(resumePos)` caused ExoPlayer to start buffering at `0ms` and abort/re-buffer on resume.
   - **Fixes Implemented:**
     - **Native DASH Segment Playback (`DashMediaSource` + `YoutubeProgressiveDashManifestCreator`):** Generates in-memory DASH MPD manifests (`Initialization` `initStart-initEnd` + `SegmentBase` `indexStart-indexEnd`) for adaptive video and audio streams so ExoPlayer fetches bounded 2–4s DASH segments instead of open-ended full-file downloads.
     - **Custom `YoutubeHttpDataSource` (Shared HTTP/2 Pool + `&range=` Anti-Throttling + Client User-Agent Matching):** Uses `OkHttpDownloader.sharedHttpClient` (`ConnectionPool(16, 5min)`, `maxRequestsPerHost = 16`), matches the `User-Agent` to the URL's `c=` client parameter (`IOS`, `ANDROID`, `ANDROID_VR`, `TVHTML5`, `WEB`), and appends bounded `&range=start-end` parameters to bypass YouTube CDN throttling.
     - **Parallel Startup RAM Preloader (`YoutubeStreamPreloader`):** Immediately prefetches the first `640 KB` of the default video stream and `192 KB` of the default audio stream into RAM in parallel as soon as extraction finishes. When `DashMediaSource` requests the MP4 initialization box, `sidx` index, and first media segment, they are served in `0ms` directly from memory.
     - **Hardware Codec Priority (`AVC1` -> `VP9` -> `AV1`) & Single-Pass `setMediaSource(source, startPos)`:** Strictly prioritizes hardware-decoded H.264 (`avc1`) streams for 30fps `1080p` startup and enables `DefaultRenderersFactory` decoder fallback.

2. **Minimal YouTube Red Shield + White Play Symbol Logo ([`ic_launcher_foreground.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/res/drawable/ic_launcher_foreground.xml), [`ic_launcher_background.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/res/drawable/ic_launcher_background.xml), [`ic_launcher_monochrome.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/res/drawable/ic_launcher_monochrome.xml), [`ic_launcher_legacy.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/res/drawable/ic_launcher_legacy.xml), [`WatchHomeScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt), [`Theme.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/theme/Theme.kt)):**
   - **Minimal & Distinct from Official YouTube Icon:** Replaced the overly complex multi-element `"P"` monogram with a clean, minimal **YouTube Red (`#FF0000`) Shield** containing a centered **White (`#FFFFFF`) Play Symbol** on a dark `#121212` background — instantly recognizable as an ad-free protective YouTube player while avoiding YouTube's horizontal rectangle box.
   - **Android 13+ Monochrome Themed Icon Support:** Updated `<monochrome android:drawable="@drawable/ic_launcher_monochrome" />` with a single-path shield + play triangle silhouette (`evenOdd` cutout) and cleaned up `WatchHomeScreen` by removing the extra toggle chip below the logo.

3. **Watch History Title Alignment & `vX.X.XXX` Versioning Scheme ([`HistoryScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/HistoryScreen.kt), [`app/build.gradle.kts`](file:///f:/Youtube%20Video%20watching%20app/app/build.gradle.kts), [`WatchHomeScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/WatchHomeScreen.kt)):**
   - **Problem:** `HistoryScreen` previously used a nested `Scaffold` + `TopAppBar` inside `MainActivity`'s outer `Scaffold`, applying duplicate status bar window insets that pushed the **"Watch History"** title far down the screen.
   - **Fix:** Replaced the nested `Scaffold` + `TopAppBar` with a top-aligned header row inside `HistoryScreen` so **"Watch History"** sits cleanly right below the status bar.
   - **Versioning Scheme (`vX.X.XXX`):** Standardized app version format to `vX.X.XXX` (where only the 3-digit patch segment increments unless the 1st or 2nd segment is explicitly requested), set version to **`v0.3.004`** (`versionCode = 7`, `versionName = "0.3.004"`), and named the versioned APK file [`PureTube-v0.3.004-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-v0.3.004-debug.apk) to match the exact version number.

---

## v0.3.005 — Battery & Thermal Optimizations, Expandable Portrait Video Description, & GitHub Release Automation

1. **Battery & Thermal Optimizations ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt) & [`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt)):**
   - **Eliminated Hidden-Controls UI Recomposition Loop:** Previously, a `while (true)` coroutine updated `currentPositionMs` and `bufferedPositionMs` every `400ms` unconditionally, causing 2.5 full-screen Compose recompositions per second even when the player overlay controls were hidden (`controlsVisible == false`) or paused. Now, UI position polling **only runs when controls are visible or scrubbing** (at a `1000ms` 1-second cadence), dropping background UI recompositions to **zero** during normal video playback.
   - **Decoupled Low-Frequency Watch History Sync:** Moved SQLite progress updates to a separate coroutine that writes every `15s` only while actively playing (plus immediately on pause/stop/exit), eliminating frequent flash storage I/O.
   - **Smart Screen Wake Lock (`FLAG_KEEP_SCREEN_ON`):** Tied `FLAG_KEEP_SCREEN_ON` strictly to `isPlaying && playbackState != Player.STATE_ENDED` so the display can dim/sleep normally when playback is paused or finishes.
   - **Hardware Decoder & 30fps Default Priority:** Updated stream sorting and `selectDefaultStartupQuality` in `YoutubeStreamExtractor.kt` to prioritize hardware-decoded `AVC1 (H.264)` 30fps streams over CPU-intensive `AV1` software decoding or 60fps streams by default, and tuned `DefaultLoadControl` (`12s–30s` burst window) so Wi-Fi/cellular radios sleep between segment downloads.

2. **Expandable Video Description Card in Portrait View ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt) & [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - Extracted `description` (converted from HTML to clean formatted text), `viewCount`, `uploadDate`, and `likeCount` directly from `StreamExtractor` with zero extra network calls.
   - Added an expandable **YouTube Description Card** in portrait view below the horizontal action chips displaying compact view count, upload date, likes, a 3-line collapsed preview (`More`), and smooth expansion (`Show less`) with selectable description text.

3. **GitHub Repository Setup, `.gitignore`, & Automated Release Workflow ([`.gitignore`](file:///f:/Youtube%20Video%20watching%20app/.gitignore), [`.github/workflows/build-apk.yml`](file:///f:/Youtube%20Video%20watching%20app/.github/workflows/build-apk.yml), [`README.md`](file:///f:/Youtube%20Video%20watching%20app/README.md)):**
   - Expanded `.gitignore` for Gradle, Android Studio, VS Code, local SDK/toolchains, and compiled binaries.
   - Upgraded `.github/workflows/build-apk.yml` to automatically parse `versionName` and `versionCode` from `app/build.gradle.kts`, name the artifact `PureTube-v<versionName>-debug.apk`, and automatically publish a **GitHub Release** with release notes and attached APK whenever a `v*` tag is pushed.
   - Bumped version to **`v0.3.005`** (`versionCode = 8`, `versionName = "0.3.005"`) and exported [`PureTube-v0.3.005-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-v0.3.005-debug.apk) and [`PureTube-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-debug.apk).

---

## v0.3.006 — Clickable Timestamps & Video Chapters, Fullscreen Brightness/Volume Swipe Gestures, Background Play, & Picture-in-Picture (PiP)

1. **Clickable Timestamps & Video Chapters ([`YoutubeStreamExtractor.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/extractor/YoutubeStreamExtractor.kt) & [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Chapter Extraction:** Extracts official YouTube chapters (`extractor.streamSegments`) and automatically falls back to parsing chapter lines (`0:00 Intro`, etc.) from the video description.
   - **Seekbar Chapter Markers & Chapter Sheet:** Renders chapter boundary gap ticks along `YouTubeSeekBar`, shows the active chapter title next to the timestamp (`0:12 / 4:20 • Chapter >`) and inside the scrubbing preview bubble, and adds a **Video Chapters Bottom Sheet** to jump directly to any chapter.
   - **Clickable Timestamps in Video Description:** Parses all `mm:ss` / `hh:mm:ss` timestamps inside the portrait description card as clickable YouTube Blue (`#3EA6FF`) links (`LinkAnnotation.Clickable`) that immediately seek the player on tap.

2. **Fullscreen Vertical Swipe Brightness & Volume Gestures ([`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - Added single-finger vertical drag gestures in fullscreen landscape mode that coexist seamlessly with 2-finger pinch-to-zoom and single/double-tap gestures:
     - **Left Half Vertical Drag:** Adjusts screen brightness (`0%–100%`) via `window.attributes.screenBrightness`.
     - **Right Half Vertical Drag:** Adjusts media stream volume (`0%–100%`) via `AudioManager.STREAM_MUSIC`.
     - Displays a sleek top-center HUD pill with icon, progress bar, and percentage.

3. **Background Audio Playback & Picture-in-Picture (PiP) Mode ([`AndroidManifest.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/AndroidManifest.xml), [`MainActivity.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/MainActivity.kt), [`MainViewModel.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/MainViewModel.kt), [`VideoPlayerScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/player/VideoPlayerScreen.kt)):**
   - **Background Audio Play:** Enabled by default (`Bg Play: On` chip & toggle in `⚙️` Settings). Configures `ExoPlayer` with `C.WAKE_MODE_NETWORK` and `AudioAttributes` (`handleAudioFocus = true`) and keeps audio playing when the screen is turned off or the app is backgrounded.
   - **Picture-in-Picture (PiP) Mode:** Enabled `android:supportsPictureInPicture="true"` on `MainActivity`, auto-enters 16:9 PiP mode in `onUserLeaveHint()` when pressing Home while watching a video (plus manual `PiP` buttons in the top bar, portrait chips, and Settings sheet), and hides overlays/details while in PiP mode.
   - Bumped version to **`v0.3.006`** (`versionCode = 9`, `versionName = "0.3.006"`) and exported [`PureTube-v0.3.006-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-v0.3.006-debug.apk) and [`PureTube-debug.apk`](file:///f:/Youtube%20Video%20watching%20app/PureTube-debug.apk).


---

## v0.3.007 — In-App Release Updater, Bottom Nav Settings Tab, Kids & Focus Parental Controls, & F-Droid Readiness

1. **In-App GitHub Release Notifier & 1-Tap APK Updater ([`AppUpdateManager.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/updater/AppUpdateManager.kt), [`file_paths.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/res/xml/file_paths.xml), [`AndroidManifest.xml`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/AndroidManifest.xml), [`MainViewModel.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/MainViewModel.kt)):**
   - Automatically checks `joyalsiby/PureTube` GitHub Releases on startup (configurable) and via manual check in Settings.
   - Displays an **Update Available** dialog with release notes, a top banner on the Watch home screen, and a notification badge on the Settings tab icon.
   - Downloads the new APK directly inside the app with a live percentage progress bar and launches Android's native package installer via `FileProvider` (`REQUEST_INSTALL_PACKAGES`) for seamless in-place upgrades.

2. **Settings Tab in Bottom Navigation Bar ([`SettingsScreen.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/ui/screens/SettingsScreen.kt), [`AppPreferences.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/data/AppPreferences.kt), [`MainActivity.kt`](file:///f:/Youtube%20Video%20watching%20app/app/src/main/java/com/puretube/player/MainActivity.kt)):**
   - Added a 3rd **Settings** tab (`Watch`, `History`, `Settings`) to the Material 3 bottom navigation bar.
   - **Kids & Focus Mode (Parental Controls):**
     - **Start videos with Kids Screen Lock:** Automatically locks player controls when a video opens so toddlers cannot accidentally tap out or scrub.
     - **Auto-enter Fullscreen on play:** Launches videos directly in landscape fullscreen.
     - **Minimal Distraction-Free Watch Page:** Option to hide video descriptions and view/like stats below the player.
     - **Set PureTube as Default Link Handler:** One-tap shortcut to Android's "Open by default" settings.
   - **Playback, Appearance, Privacy & About:** Default startup resolution (`4K` down to `360p`), default playback speed, Background Play & Auto-PiP toggles, Theme selector (`System` / `Dark` / `Light`) + Material You Dynamic Color toggle, History saving toggle, and About & GitHub links.

3. **F-Droid Compliance & Redesigned README ([`README.md`](file:///f:/Youtube%20Video%20watching%20app/README.md), [`LICENSE`](file:///f:/Youtube%20Video%20watching%20app/LICENSE), [`fastlane/`](file:///f:/Youtube%20Video%20watching%20app/fastlane/metadata/android/en-US/full_description.txt), [`app/build.gradle.kts`](file:///f:/Youtube%20Video%20watching%20app/app/build.gradle.kts)):**
   - Disabled Google's proprietary dependency metadata block (`dependenciesInfo { includeInApk = false; includeInBundle = false }`), added an `MIT` [`LICENSE`](file:///f:/Youtube%20Video%20watching%20app/LICENSE), and added Fastlane metadata for F-Droid / IzzyOnDroid submission.
   - Bumped version to **`v0.3.007`** (`versionCode = 10`, `versionName = "0.3.007"`).
