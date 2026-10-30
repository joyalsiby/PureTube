package com.puretube.player.ui.player

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ClosedCaptionDisabled
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.puretube.player.extractor.AudioTrackOption
import com.puretube.player.extractor.CaptionOption
import com.puretube.player.extractor.ExtractedVideo
import com.puretube.player.extractor.VideoQualityOption
import com.puretube.player.extractor.YoutubeHttpDataSource
import com.puretube.player.extractor.YoutubeStreamExtractor
import com.puretube.player.ui.screens.formatDurationSeconds
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import kotlin.math.abs
import kotlin.math.roundToInt

private val YouTubeRed = Color(0xFFFF0000)
private val YouTubeBlueLink = Color(0xFF3EA6FF)
private val YouTubeDarkBackground = Color(0xFF0F0F0F)
private val YouTubeChipSurface = Color(0xFF272727)
private val YouTubeCardSurface = Color(0xFF1A1A1A)

private enum class SeekSide {
    LEFT, RIGHT
}

private enum class GestureHudType {
    BRIGHTNESS, VOLUME
}

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    video: ExtractedVideo,
    selectedQuality: VideoQualityOption?,
    selectedAudioTrack: AudioTrackOption?,
    selectedCaption: CaptionOption?,
    playbackSpeed: Float,
    backgroundPlayEnabled: Boolean = true,
    isInPipMode: Boolean = false,
    initialSeekPositionMs: Long,
    initialFullscreenLandscape: Boolean = false,
    initialControlsLocked: Boolean = false,
    hideVideoDescription: Boolean = false,
    onSelectQuality: (VideoQualityOption, Long) -> Unit,
    onSelectAudioTrack: (AudioTrackOption, Long) -> Unit,
    onSelectCaption: (CaptionOption?, Long) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleBackgroundPlay: (Boolean) -> Unit = {},
    onRequestPipMode: () -> Boolean = { false },
    onProgressUpdate: (Long) -> Unit,
    onClosePlayer: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    var isPlaying by remember { mutableStateOf(true) }
    var playbackState by remember { mutableStateOf(Player.STATE_BUFFERING) }
    var currentPositionMs by remember { mutableLongStateOf(initialSeekPositionMs) }
    var bufferedPositionMs by remember { mutableLongStateOf(initialSeekPositionMs) }
    var durationMs by remember { mutableLongStateOf(video.durationSeconds * 1000L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionFraction by remember { mutableFloatStateOf(0f) }

    var controlsVisible by remember { mutableStateOf(true) }
    var isFullscreenLandscape by remember { mutableStateOf(initialFullscreenLandscape) }
    var isZoomedToFill by remember { mutableStateOf(false) }
    var isControlsLocked by remember { mutableStateOf(initialControlsLocked) }

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showCaptionSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    var seekFeedbackSide by remember { mutableStateOf<SeekSide?>(null) }
    var zoomFeedbackText by remember { mutableStateOf<String?>(null) }
    var gestureHudType by remember { mutableStateOf<GestureHudType?>(null) }
    var gestureHudFraction by remember { mutableFloatStateOf(0.5f) }
    var isGestureDragging by remember { mutableStateOf(false) }

    val dataSourceFactory = remember {
        YoutubeHttpDataSource.Factory()
    }

    val exoPlayer = remember {
        // Battery-optimized LoadControl:
        // - Fast 1.2s startup (backed by YoutubeStreamPreloader RAM cache)
        // - 12s..30s burst window with size/time balance so Wi-Fi/cellular radio sleeps between segment bursts
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 12_000,
                /* maxBufferMs = */ 30_000,
                /* bufferForPlaybackMs = */ 1_200,
                /* bufferForPlaybackAfterRebufferMs = */ 2_500
            )
            .setBackBuffer(10_000, true)
            .setPrioritizeTimeOverSizeThresholds(false)
            .build()

        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .setSeekBackIncrementMs(10_000L)
            .setSeekForwardIncrementMs(10_000L)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    // Manage immersive system bars & screen orientation
    DisposableEffect(activity, isFullscreenLandscape) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isFullscreenLandscape) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Battery optimization: only hold FLAG_KEEP_SCREEN_ON while video is actively playing
    val keepScreenAwake = isPlaying && playbackState != Player.STATE_ENDED
    DisposableEffect(activity, keepScreenAwake) {
        val window = activity?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Load or switch quality, audio track, or caption stream seamlessly
    LaunchedEffect(
        video.videoId,
        selectedQuality?.videoUrl,
        selectedAudioTrack?.id,
        selectedCaption?.id
    ) {
        val activeAudioTrack = selectedAudioTrack
            ?: video.audioTracks.firstOrNull { it.isOriginal }
            ?: video.audioTracks.firstOrNull()

        val activeAudioUrl = activeAudioTrack?.audioUrl
            ?: selectedQuality?.audioUrl
            ?: video.bestAudioUrl

        val sourcesToMerge = mutableListOf<MediaSource>()

        when {
            selectedQuality != null && selectedQuality.isVideoOnly && !activeAudioUrl.isNullOrEmpty() -> {
                val videoSource = createAdaptiveOrProgressiveSource(
                    dataSourceFactory = dataSourceFactory,
                    streamUrl = selectedQuality.videoUrl,
                    dashManifestXml = selectedQuality.dashManifestXml
                )
                val audioSource = createAdaptiveOrProgressiveSource(
                    dataSourceFactory = dataSourceFactory,
                    streamUrl = activeAudioUrl,
                    dashManifestXml = activeAudioTrack?.dashManifestXml
                )
                sourcesToMerge.add(videoSource)
                sourcesToMerge.add(audioSource)
            }
            selectedQuality != null -> {
                val videoSource = createAdaptiveOrProgressiveSource(
                    dataSourceFactory = dataSourceFactory,
                    streamUrl = selectedQuality.videoUrl,
                    dashManifestXml = selectedQuality.dashManifestXml
                )
                sourcesToMerge.add(videoSource)
            }
            !video.hlsUrl.isNullOrEmpty() -> {
                val hlsSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(video.hlsUrl))
                sourcesToMerge.add(hlsSource)
            }
        }

        // Attach selected caption track if enabled
        if (selectedCaption != null && sourcesToMerge.isNotEmpty()) {
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(selectedCaption.url))
                .setMimeType(selectedCaption.mimeType)
                .setLanguage(selectedCaption.languageCode)
                .setLabel(selectedCaption.label)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
                .build()

            val subtitleSource = SingleSampleMediaSource.Factory(dataSourceFactory)
                .setTreatLoadErrorsAsEndOfStream(true)
                .createMediaSource(subtitleConfig, C.TIME_UNSET)

            sourcesToMerge.add(subtitleSource)
        }

        val finalMediaSource = when {
            sourcesToMerge.isEmpty() -> null
            sourcesToMerge.size == 1 -> sourcesToMerge.first()
            else -> MergingMediaSource(true, *sourcesToMerge.toTypedArray())
        }

        if (finalMediaSource != null) {
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, selectedCaption == null)
                .setPreferredTextLanguage(selectedCaption?.languageCode)
                .build()

            val resumePos = if (exoPlayer.currentPosition > 0L) {
                exoPlayer.currentPosition
            } else {
                initialSeekPositionMs
            }
            if (resumePos > 0L) {
                exoPlayer.setMediaSource(finalMediaSource, resumePos)
            } else {
                exoPlayer.setMediaSource(finalMediaSource, /* resetPosition = */ true)
            }
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Apply playback speed updates
    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    // Listen to ExoPlayer events
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                currentPositionMs = pos
                bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(pos)
                if (!playing && pos > 0L) {
                    onProgressUpdate(pos)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                if (exoPlayer.duration > 0) {
                    durationMs = exoPlayer.duration
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Handle background audio & Picture-in-Picture (PiP) when app goes to background
    DisposableEffect(lifecycleOwner, backgroundPlayEnabled, isInPipMode) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                onProgressUpdate(exoPlayer.currentPosition)
                val inPipNow = isInPipMode || (activity?.isInPictureInPictureMode == true)
                if (!inPipNow && !backgroundPlayEnabled) {
                    exoPlayer.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Battery Optimization: ONLY poll position for UI seekbar when controls are actually visible!
    // When controls auto-hide during playback, zero UI state mutations / recompositions occur.
    LaunchedEffect(exoPlayer, isPlaying, controlsVisible, isScrubbing) {
        if (controlsVisible || isScrubbing) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(currentPositionMs)
            if (exoPlayer.duration > 0) {
                durationMs = exoPlayer.duration
            }
        }
        while (isPlaying && (controlsVisible || isScrubbing)) {
            delay(1000L)
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(currentPositionMs)
            if (exoPlayer.duration > 0) {
                durationMs = exoPlayer.duration
            }
        }
    }

    // Low-frequency Watch History persistence (every 15s while playing, avoiding frequent disk I/O)
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            delay(15_000L)
            val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (pos > 0L) {
                onProgressUpdate(pos)
            }
        }
    }

    // Auto-hide controls after 3.5 seconds of playing
    val anySheetOpen = showSettingsSheet || showQualitySheet || showAudioSheet ||
        showCaptionSheet || showSpeedSheet || showChaptersSheet
    LaunchedEffect(controlsVisible, isPlaying, isScrubbing, anySheetOpen) {
        if (controlsVisible && isPlaying && !isScrubbing && !anySheetOpen) {
            delay(3500L)
            controlsVisible = false
        }
    }

    // Clear double-tap seek indicator after 700ms
    LaunchedEffect(seekFeedbackSide) {
        if (seekFeedbackSide != null) {
            delay(700L)
            seekFeedbackSide = null
        }
    }

    // Clear pinch zoom indicator after 1.1s
    LaunchedEffect(zoomFeedbackText) {
        if (zoomFeedbackText != null) {
            delay(1100L)
            zoomFeedbackText = null
        }
    }

    // Clear brightness / volume HUD 850ms after vertical drag ends
    LaunchedEffect(isGestureDragging, gestureHudType) {
        if (!isGestureDragging && gestureHudType != null) {
            delay(850L)
            gestureHudType = null
        }
    }

    BackHandler {
        when {
            isControlsLocked -> {
                controlsVisible = true
            }
            isFullscreenLandscape -> {
                isFullscreenLandscape = false
            }
            else -> {
                onClosePlayer(exoPlayer.currentPosition)
            }
        }
    }

    val currentAudioLabel = remember(selectedAudioTrack) {
        when {
            selectedAudioTrack == null -> "Original"
            selectedAudioTrack.isOriginal -> "Original"
            else -> selectedAudioTrack.label
                .substringBefore("(")
                .trim()
                .ifEmpty { selectedAudioTrack.label }
        }
    }

    val currentSpeedLabel = remember(playbackSpeed) {
        if (playbackSpeed == 1.0f) "Normal" else "${playbackSpeed}x"
    }

    val currentCaptionLabel = remember(selectedCaption, video.captions) {
        when {
            selectedCaption != null -> selectedCaption.label
            video.captions.isEmpty() -> "Unavailable"
            else -> "Off"
        }
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isFullscreenLandscape || isInPipMode) Color.Black else YouTubeDarkBackground)
    ) {
        // Status bar safe inset in portrait mode so the top of the 16:9 player is never covered by clock/battery
        if (!isFullscreenLandscape && !isInPipMode) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarTopPadding.coerceAtLeast(24.dp))
                    .background(Color.Black)
            )
        }

        // VIDEO SURFACE CONTAINER (16:9 at top in Portrait, full-screen in Landscape or PiP)
        val playerContainerModifier = if (isFullscreenLandscape || isInPipMode) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        }

        Box(
            modifier = playerContainerModifier
                // Combined Multi-Touch Pinch-to-Zoom + Fullscreen Vertical Swipe Brightness/Volume Gestures:
                // 1) Two fingers: Pinch Out -> "Zoomed to fill" (or enter fullscreen from portrait), Pinch In -> "Original"
                // 2) Single finger vertical drag in Fullscreen: Left half -> Brightness, Right half -> Volume
                .pointerInput(isFullscreenLandscape, isControlsLocked, isInPipMode) {
                    if (isInPipMode) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startX = down.position.x
                        val startY = down.position.y
                        val viewW = size.width.toFloat().coerceAtLeast(1f)
                        val viewH = size.height.toFloat().coerceAtLeast(1f)
                        val touchSlop = viewConfiguration.touchSlop

                        var zoomAccumulator = 1f
                        var isMultiTouch = false
                        var isVerticalSwipe = false
                        var totalDx = 0f
                        var totalDy = 0f
                        val isLeftSide = startX < viewW * 0.5f
                        // Avoid triggering vertical swipe if gesture starts at very top/bottom system bar / seekbar edges
                        val inVerticalSwipeZone = isFullscreenLandscape &&
                            !isControlsLocked &&
                            startY in (viewH * 0.12f)..(viewH * 0.82f)

                        var swipeValueFraction = 0.5f

                        do {
                            val event = awaitPointerEvent()
                            val pressedCount = event.changes.count { it.pressed }

                            if (pressedCount >= 2 && !isControlsLocked) {
                                isMultiTouch = true
                                if (isVerticalSwipe) {
                                    isVerticalSwipe = false
                                    isGestureDragging = false
                                }
                                val zoomChange = event.calculateZoom()
                                if (zoomChange != 1f) {
                                    zoomAccumulator *= zoomChange
                                    if (isFullscreenLandscape) {
                                        if (zoomAccumulator > 1.06f) {
                                            isZoomedToFill = true
                                            zoomFeedbackText = "Zoomed to fill"
                                            zoomAccumulator = 1f
                                        } else if (zoomAccumulator < 0.94f) {
                                            isZoomedToFill = false
                                            zoomFeedbackText = "Original"
                                            zoomAccumulator = 1f
                                        }
                                    } else {
                                        // Pinching out on the portrait player expands into fullscreen
                                        if (zoomAccumulator > 1.12f) {
                                            isFullscreenLandscape = true
                                            zoomAccumulator = 1f
                                        }
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                            } else if (pressedCount == 1 && !isMultiTouch && inVerticalSwipeZone) {
                                val change = event.changes.firstOrNull()
                                if (change != null && change.pressed) {
                                    val delta = change.positionChange()
                                    totalDx += delta.x
                                    totalDy += delta.y

                                    if (!isVerticalSwipe) {
                                        if (abs(totalDy) > touchSlop && abs(totalDy) > abs(totalDx) * 1.35f) {
                                            isVerticalSwipe = true
                                            isGestureDragging = true
                                            if (isLeftSide) {
                                                val currentBright = activity?.window?.attributes?.screenBrightness ?: -1f
                                                swipeValueFraction = if (currentBright in 0.01f..1.0f) {
                                                    currentBright
                                                } else {
                                                    0.5f
                                                }
                                                gestureHudType = GestureHudType.BRIGHTNESS
                                                gestureHudFraction = swipeValueFraction
                                            } else {
                                                val maxVol = (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1)
                                                val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: (maxVol / 2)
                                                swipeValueFraction = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
                                                gestureHudType = GestureHudType.VOLUME
                                                gestureHudFraction = swipeValueFraction
                                            }
                                            change.consume()
                                        }
                                    } else {
                                        // Dragging up (negative delta.y) increases brightness/volume
                                        val sensitivity = viewH * 0.65f
                                        val deltaFraction = (-delta.y / sensitivity)
                                        swipeValueFraction = (swipeValueFraction + deltaFraction).coerceIn(0f, 1f)
                                        gestureHudFraction = swipeValueFraction

                                        if (isLeftSide) {
                                            gestureHudType = GestureHudType.BRIGHTNESS
                                            activity?.window?.let { win ->
                                                val lp = win.attributes
                                                lp.screenBrightness = swipeValueFraction.coerceIn(0.01f, 1.0f)
                                                win.attributes = lp
                                            }
                                        } else {
                                            gestureHudType = GestureHudType.VOLUME
                                            audioManager?.let { am ->
                                                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                                val targetVol = (swipeValueFraction * maxVol).roundToInt().coerceIn(0, maxVol)
                                                am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                            }
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (isVerticalSwipe) {
                            isGestureDragging = false
                        }
                    }
                }
                // Single-tap (toggle controls) & Double-tap (-10s / +10s seek)
                .pointerInput(isControlsLocked) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible = !controlsVisible
                        },
                        onDoubleTap = { offset ->
                            if (isControlsLocked) {
                                controlsVisible = true
                                return@detectTapGestures
                            }
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.4f) {
                                val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                                seekFeedbackSide = SeekSide.LEFT
                            } else if (offset.x > screenWidth * 0.6f) {
                                val maxDur = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                                val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                                seekFeedbackSide = SeekSide.RIGHT
                            } else {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                        }
                    )
                }
        ) {
            val targetResizeMode = if (isFullscreenLandscape && isZoomedToFill) {
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }

            // ExoPlayer Surface (also renders closed captions via SubtitleView inside PlayerView)
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = targetResizeMode
                        player = exoPlayer
                        subtitleView?.setApplyEmbeddedStyles(true)
                        subtitleView?.setApplyEmbeddedFontSizes(true)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    if (view.resizeMode != targetResizeMode) {
                        view.resizeMode = targetResizeMode
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Double-Tap Seek Feedback (Left / Right side YouTube ripple pill)
            androidx.compose.animation.AnimatedVisibility(
                visible = seekFeedbackSide == SeekSide.LEFT,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = if (isFullscreenLandscape) 48.dp else 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "10 seconds",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = seekFeedbackSide == SeekSide.RIGHT,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = if (isFullscreenLandscape) 48.dp else 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "10 seconds",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }

            // Pinch-to-Zoom Feedback Pill ("Zoomed to fill" / "Original" at top-center, just like YouTube)
            androidx.compose.animation.AnimatedVisibility(
                visible = zoomFeedbackText != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.78f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isZoomedToFill) Icons.Rounded.CropFree else Icons.Rounded.FitScreen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = zoomFeedbackText.orEmpty(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Vertical Swipe Brightness / Volume HUD Pill (Fullscreen Gesture Feedback)
            androidx.compose.animation.AnimatedVisibility(
                visible = gestureHudType != null && !isInPipMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                val hudPercent = (gestureHudFraction * 100f).roundToInt().coerceIn(0, 100)
                val hudIcon = when (gestureHudType) {
                    GestureHudType.BRIGHTNESS -> if (hudPercent < 45) Icons.Rounded.BrightnessLow else Icons.Rounded.BrightnessHigh
                    GestureHudType.VOLUME -> if (hudPercent == 0) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp
                    null -> Icons.Rounded.VolumeUp
                }
                val hudTitle = if (gestureHudType == GestureHudType.BRIGHTNESS) "Brightness" else "Volume"

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.82f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = hudIcon,
                            contentDescription = hudTitle,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(5.dp)
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(gestureHudFraction.coerceIn(0f, 1f))
                                    .height(5.dp)
                                    .background(YouTubeRed, RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$hudPercent%",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Buffering Spinner (White ring like YouTube player)
            if (playbackState == Player.STATE_BUFFERING) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(if (isInPipMode) 28.dp else 48.dp)
                        .align(Alignment.Center)
                )
            }

            // Locked Screen Overlay (When user locks screen from YouTube Settings menu)
            if (isControlsLocked && !isInPipMode) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                ) {
                    Surface(
                        onClick = {
                            isControlsLocked = false
                            controlsVisible = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.78f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = "Unlock screen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Screen locked · Tap to unlock",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            } else if (!isInPipMode) {
                // ORIGINAL YOUTUBE PLAYER CONTROLS OVERLAY
                androidx.compose.animation.AnimatedVisibility(
                    visible = controlsVisible || playbackState == Player.STATE_ENDED,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.72f),
                                        Color.Black.copy(alpha = 0.22f),
                                        Color.Black.copy(alpha = 0.78f)
                                    )
                                )
                            )
                    ) {
                        // 1. TOP BAR: Left (Collapse Chevron + Fullscreen Title/Channel) | Right (Audio, CC, Fit/Fill, Settings Gear)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(
                                    horizontal = if (isFullscreenLandscape) 16.dp else 6.dp,
                                    vertical = if (isFullscreenLandscape) 10.dp else 4.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (isFullscreenLandscape) {
                                        isFullscreenLandscape = false
                                    } else {
                                        onClosePlayer(exoPlayer.currentPosition)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Minimize or Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // In Fullscreen, show Video Title + Channel Name in the top bar (just like YouTube!)
                            if (isFullscreenLandscape) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = video.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = video.uploaderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Top-Right Action Icons (Original YouTube Layout: Audio, CC, Zoom/Fit in Fullscreen, Settings Gear)
                            if (video.audioTracks.size > 1) {
                                IconButton(onClick = { showAudioSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = "Audio track",
                                        tint = if (selectedAudioTrack != null && !selectedAudioTrack.isOriginal) {
                                            YouTubeRed
                                        } else {
                                            Color.White
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Closed Captions (CC) Icon Button with YouTube-style red underline when active
                            val isCaptionActive = selectedCaption != null
                            IconButton(onClick = { showCaptionSheet = true }) {
                                Box(contentAlignment = Alignment.BottomCenter) {
                                    Icon(
                                        imageVector = if (isCaptionActive) {
                                            Icons.Rounded.ClosedCaption
                                        } else {
                                            Icons.Rounded.ClosedCaptionDisabled
                                        },
                                        contentDescription = "Subtitles / closed captions",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (isCaptionActive) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(2.5.dp)
                                                .background(YouTubeRed, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }

                            // Quick Fit / Fill button in Fullscreen top bar
                            if (isFullscreenLandscape) {
                                IconButton(
                                    onClick = {
                                        isZoomedToFill = !isZoomedToFill
                                        zoomFeedbackText = if (isZoomedToFill) "Zoomed to fill" else "Original"
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isZoomedToFill) {
                                            Icons.Rounded.CropFree
                                        } else {
                                            Icons.Rounded.FitScreen
                                        },
                                        contentDescription = "Toggle Fit or Zoom to fill",
                                        tint = if (isZoomedToFill) YouTubeRed else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Picture-in-Picture (PiP) Top Bar Icon Button
                            IconButton(
                                onClick = {
                                    onRequestPipMode()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PictureInPictureAlt,
                                    contentDescription = "Picture-in-Picture (PiP)",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // YouTube Settings Gear Icon Button
                            IconButton(onClick = { showSettingsSheet = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }

                        // 2. CENTER CONTROLS: Translucent dark circles with crisp white icons (-10s, Play/Pause, +10s)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(if (isFullscreenLandscape) 48.dp else 36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    currentPositionMs = newPos
                                    seekFeedbackSide = SeekSide.LEFT
                                },
                                modifier = Modifier
                                    .size(if (isFullscreenLandscape) 52.dp else 44.dp)
                                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay10,
                                    contentDescription = "Rewind 10 seconds",
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isFullscreenLandscape) 30.dp else 26.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (playbackState == Player.STATE_ENDED) {
                                        exoPlayer.seekTo(0L)
                                        exoPlayer.play()
                                    } else if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                                modifier = Modifier
                                    .size(if (isFullscreenLandscape) 68.dp else 58.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            ) {
                                val centerIcon = when {
                                    playbackState == Player.STATE_ENDED -> Icons.Rounded.Replay
                                    isPlaying -> Icons.Rounded.Pause
                                    else -> Icons.Rounded.PlayArrow
                                }
                                Icon(
                                    imageVector = centerIcon,
                                    contentDescription = "Play or Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isFullscreenLandscape) 40.dp else 34.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val maxDur = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                                    val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                                    exoPlayer.seekTo(newPos)
                                    currentPositionMs = newPos
                                    seekFeedbackSide = SeekSide.RIGHT
                                },
                                modifier = Modifier
                                    .size(if (isFullscreenLandscape) 52.dp else 44.dp)
                                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Forward10,
                                    contentDescription = "Forward 10 seconds",
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isFullscreenLandscape) 30.dp else 26.dp)
                                )
                            }
                        }

                        // 3. BOTTOM BAR: Timestamp + Fullscreen Toggle (above) + YouTube Red Seekbar + Fullscreen Quick Actions (below)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(
                                    start = if (isFullscreenLandscape) 20.dp else 12.dp,
                                    end = if (isFullscreenLandscape) 20.dp else 8.dp,
                                    bottom = if (isFullscreenLandscape) 12.dp else 2.dp
                                )
                        ) {
                            val safeDuration = durationMs.coerceAtLeast(1L)
                            val sliderFraction = if (isScrubbing) {
                                scrubPositionFraction
                            } else {
                                (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
                            }
                            val bufferedFraction = (bufferedPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

                            val displayCurrentSeconds = if (isScrubbing) {
                                ((scrubPositionFraction * safeDuration) / 1000L).toLong()
                            } else {
                                currentPositionMs / 1000L
                            }
                            val displayTotalSeconds = (durationMs / 1000L).coerceAtLeast(0L)

                            val activeChapter = remember(video.chapters, displayCurrentSeconds) {
                                video.chapters.lastOrNull { it.startTimeSeconds <= displayCurrentSeconds }
                            }
                            val chapterFractions = remember(video.chapters, displayTotalSeconds) {
                                if (displayTotalSeconds <= 0L || video.chapters.isEmpty()) {
                                    emptyList()
                                } else {
                                    video.chapters
                                        .map { it.startTimeSeconds.toFloat() / displayTotalSeconds.toFloat() }
                                        .filter { it in 0.01f..0.99f }
                                }
                            }

                            // Scrubbing time + chapter preview bubble above seekbar
                            AnimatedVisibility(
                                visible = isScrubbing,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Black.copy(alpha = 0.85f)
                                ) {
                                    val previewLabel = if (activeChapter != null) {
                                        "${formatDurationSeconds(displayCurrentSeconds)}  •  ${activeChapter.title}"
                                    } else {
                                        formatDurationSeconds(displayCurrentSeconds)
                                    }
                                    Text(
                                        text = previewLabel,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Row 1: Timestamp (0:12 / 4:20 • Chapter >) on left + Quality badge & Fullscreen icon on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .clickable(enabled = video.chapters.isNotEmpty()) {
                                            showChaptersSheet = true
                                        }
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) {
                                                append(formatDurationSeconds(displayCurrentSeconds))
                                            }
                                            withStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                                                append(" / ${formatDurationSeconds(displayTotalSeconds)}")
                                            }
                                            if (activeChapter != null) {
                                                withStyle(SpanStyle(color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)) {
                                                    append("  •  ${activeChapter.title}")
                                                }
                                            }
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (activeChapter != null) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = "View chapters",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Subtle current resolution badge next to fullscreen icon
                                    if (selectedQuality != null && !isFullscreenLandscape) {
                                        Surface(
                                            onClick = { showQualitySheet = true },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.55f)
                                        ) {
                                            Text(
                                                text = selectedQuality.label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    IconButton(
                                        onClick = { isFullscreenLandscape = !isFullscreenLandscape },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFullscreenLandscape) {
                                                Icons.Rounded.FullscreenExit
                                            } else {
                                                Icons.Rounded.Fullscreen
                                            },
                                            contentDescription = "Toggle Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }

                            // Row 2: Authentic YouTube Red Seekbar with Buffered Progress Track & Chapter Markers
                            YouTubeSeekBar(
                                value = sliderFraction,
                                bufferedFraction = bufferedFraction,
                                chapterFractions = chapterFractions,
                                isScrubbing = isScrubbing,
                                onValueChange = { fraction ->
                                    isScrubbing = true
                                    scrubPositionFraction = fraction
                                },
                                onValueChangeFinished = {
                                    val seekTarget = (scrubPositionFraction * safeDuration).toLong()
                                    exoPlayer.seekTo(seekTarget)
                                    currentPositionMs = seekTarget
                                    isScrubbing = false
                                }
                            )

                            // Row 3 (Fullscreen Only): YouTube Fullscreen Bottom Quick-Actions Strip
                            if (isFullscreenLandscape) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (video.qualities.isNotEmpty()) {
                                            FullscreenQuickPill(
                                                icon = Icons.Rounded.HighQuality,
                                                label = selectedQuality?.label ?: "Auto",
                                                onClick = { showQualitySheet = true }
                                            )
                                        }
                                        if (video.audioTracks.isNotEmpty()) {
                                            FullscreenQuickPill(
                                                icon = Icons.Rounded.GraphicEq,
                                                label = "Audio: $currentAudioLabel",
                                                onClick = { showAudioSheet = true }
                                            )
                                        }
                                        FullscreenQuickPill(
                                            icon = Icons.Rounded.Speed,
                                            label = currentSpeedLabel,
                                            onClick = { showSpeedSheet = true }
                                        )
                                        FullscreenQuickPill(
                                            icon = if (selectedCaption != null) {
                                                Icons.Rounded.ClosedCaption
                                            } else {
                                                Icons.Rounded.ClosedCaptionDisabled
                                            },
                                            label = selectedCaption?.languageCode?.uppercase() ?: "CC",
                                            isActive = selectedCaption != null,
                                            onClick = { showCaptionSheet = true }
                                        )
                                        if (video.chapters.isNotEmpty()) {
                                            FullscreenQuickPill(
                                                icon = Icons.Rounded.FormatListBulleted,
                                                label = "Chapters (${video.chapters.size})",
                                                onClick = { showChaptersSheet = true }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FullscreenQuickPill(
                                            icon = if (isZoomedToFill) Icons.Rounded.CropFree else Icons.Rounded.FitScreen,
                                            label = if (isZoomedToFill) "Zoomed to fill" else "Original (Fit)",
                                            isActive = isZoomedToFill,
                                            onClick = {
                                                isZoomedToFill = !isZoomedToFill
                                                zoomFeedbackText = if (isZoomedToFill) "Zoomed to fill" else "Original"
                                            }
                                        )
                                        FullscreenQuickPill(
                                            icon = Icons.Rounded.Lock,
                                            label = "Lock",
                                            onClick = {
                                                isControlsLocked = true
                                                controlsVisible = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PORTRAIT WATCH PAGE DETAILS BELOW THE 16:9 VIDEO PLAYER (Original YouTube Watch UI Layout)
        if (!isFullscreenLandscape && !isInPipMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Video Title
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle metadata line (Resolution · Audio · Ad-free)
                Text(
                    text = buildString {
                        append(selectedQuality?.label ?: "Auto")
                        append("  ·  Audio: ")
                        append(currentAudioLabel)
                        if (selectedCaption != null) {
                            append("  ·  CC: ")
                            append(selectedCaption.languageCode.uppercase())
                        }
                        if (backgroundPlayEnabled) {
                            append("  ·  Bg Play")
                        }
                        append("  ·  Ad-Free Stream")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // YouTube Channel Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Channel Avatar Circle
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF303030),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = video.uploaderName.firstOrNull()?.uppercase() ?: "Y",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.uploaderName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "PureTube Viewer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }

                    // Settings button pill on right of channel row
                    Surface(
                        onClick = { showSettingsSheet = true },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Options",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // YouTube-style Horizontal Action Pills Row (Quality, Audio, Captions, Chapters, Speed, Bg Play, PiP, Fullscreen, Share, Copy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (video.qualities.isNotEmpty()) {
                        YouTubeActionChip(
                            icon = Icons.Rounded.HighQuality,
                            label = selectedQuality?.label ?: "Quality",
                            onClick = { showQualitySheet = true }
                        )
                    }

                    if (video.audioTracks.isNotEmpty()) {
                        YouTubeActionChip(
                            icon = Icons.Rounded.GraphicEq,
                            label = "Audio: $currentAudioLabel",
                            onClick = { showAudioSheet = true }
                        )
                    }

                    YouTubeActionChip(
                        icon = if (selectedCaption != null) {
                            Icons.Rounded.ClosedCaption
                        } else {
                            Icons.Rounded.ClosedCaptionDisabled
                        },
                        label = if (selectedCaption != null) {
                            "CC: ${selectedCaption.languageCode.uppercase()}"
                        } else {
                            "Captions"
                        },
                        isHighlighted = selectedCaption != null,
                        onClick = { showCaptionSheet = true }
                    )

                    if (video.chapters.isNotEmpty()) {
                        YouTubeActionChip(
                            icon = Icons.Rounded.FormatListBulleted,
                            label = "Chapters (${video.chapters.size})",
                            onClick = { showChaptersSheet = true }
                        )
                    }

                    YouTubeActionChip(
                        icon = Icons.Rounded.Speed,
                        label = "Speed: $currentSpeedLabel",
                        onClick = { showSpeedSheet = true }
                    )

                    YouTubeActionChip(
                        icon = Icons.Rounded.Headphones,
                        label = if (backgroundPlayEnabled) "Bg Play: On" else "Bg Play: Off",
                        isHighlighted = backgroundPlayEnabled,
                        onClick = { onToggleBackgroundPlay(!backgroundPlayEnabled) }
                    )

                    YouTubeActionChip(
                        icon = Icons.Rounded.PictureInPictureAlt,
                        label = "PiP",
                        onClick = { onRequestPipMode() }
                    )

                    YouTubeActionChip(
                        icon = Icons.Rounded.Fullscreen,
                        label = "Fullscreen",
                        onClick = { isFullscreenLandscape = true }
                    )

                    YouTubeActionChip(
                        icon = Icons.Rounded.Share,
                        label = "Share",
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, video.title)
                                putExtra(Intent.EXTRA_TEXT, video.originalUrl)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share video link"))
                        }
                    )

                    YouTubeActionChip(
                        icon = Icons.Rounded.ContentCopy,
                        label = "Copy link",
                        onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("YouTube URL", video.originalUrl))
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // YouTube-style Expandable Video Description Card with Clickable Timestamps
                if (!hideVideoDescription) {
                Surface(
                    onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                    shape = RoundedCornerShape(12.dp),
                    color = YouTubeCardSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statsHeadline = remember(video.viewCount, video.uploadDate, video.likeCount) {
                                buildList {
                                    if (video.viewCount >= 0L) {
                                        add("${formatCompactCount(video.viewCount)} views")
                                    }
                                    if (video.uploadDate.isNotBlank()) {
                                        add(video.uploadDate)
                                    }
                                    if (video.likeCount > 0L) {
                                        add("${formatCompactCount(video.likeCount)} likes")
                                    }
                                }.joinToString("  •  ").ifBlank { "Description" }
                            }

                            Text(
                                text = statsHeadline,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDescriptionExpanded) "Show less" else "More",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (isDescriptionExpanded) {
                                        Icons.Rounded.ExpandLess
                                    } else {
                                        Icons.Rounded.ExpandMore
                                    },
                                    contentDescription = if (isDescriptionExpanded) "Collapse description" else "Expand description",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val bodyText = video.description.ifBlank { "No description provided for this video." }
                        val annotatedDescription = remember(bodyText, video.durationSeconds) {
                            buildClickableDescriptionAnnotatedString(
                                text = bodyText,
                                maxDurationSeconds = video.durationSeconds,
                                onTimestampClick = { targetSeconds ->
                                    val seekMs = (targetSeconds * 1000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(seekMs)
                                    exoPlayer.play()
                                    currentPositionMs = seekMs
                                    Toast.makeText(
                                        context,
                                        "Jumped to ${formatDurationSeconds(targetSeconds)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        if (isDescriptionExpanded) {
                            SelectionContainer {
                                Text(
                                    text = annotatedDescription,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                                    color = Color.White.copy(alpha = 0.86f)
                                )
                            }
                        } else {
                            Text(
                                text = annotatedDescription,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                }
            }
        }
    }

    // =========================================================================
    // BOTTOM SHEETS (YouTube Unified Settings Menu + Individual Option Sheets)
    // =========================================================================

    // 0. Unified YouTube Player Settings Bottom Sheet (Opened via top-right ⚙️ Gear icon)
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = Color(0xFF212121),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // 1. Quality
                if (video.qualities.isNotEmpty()) {
                    YouTubeSettingsRow(
                        icon = Icons.Rounded.HighQuality,
                        title = "Quality",
                        valueText = selectedQuality?.label ?: "Auto",
                        onClick = {
                            showSettingsSheet = false
                            showQualitySheet = true
                        }
                    )
                }

                // 2. Playback speed
                YouTubeSettingsRow(
                    icon = Icons.Rounded.Speed,
                    title = "Playback speed",
                    valueText = currentSpeedLabel,
                    onClick = {
                        showSettingsSheet = false
                        showSpeedSheet = true
                    }
                )

                // 3. Captions
                YouTubeSettingsRow(
                    icon = if (selectedCaption != null) {
                        Icons.Rounded.ClosedCaption
                    } else {
                        Icons.Rounded.ClosedCaptionDisabled
                    },
                    title = "Captions",
                    valueText = currentCaptionLabel,
                    onClick = {
                        showSettingsSheet = false
                        showCaptionSheet = true
                    }
                )

                // 4. Audio track
                if (video.audioTracks.isNotEmpty()) {
                    YouTubeSettingsRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Audio track",
                        valueText = currentAudioLabel,
                        onClick = {
                            showSettingsSheet = false
                            showAudioSheet = true
                        }
                    )
                }

                // 5. Video Chapters
                if (video.chapters.isNotEmpty()) {
                    YouTubeSettingsRow(
                        icon = Icons.Rounded.FormatListBulleted,
                        title = "Chapters",
                        valueText = "${video.chapters.size} chapters",
                        onClick = {
                            showSettingsSheet = false
                            showChaptersSheet = true
                        }
                    )
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // 6. Background audio play toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleBackgroundPlay(!backgroundPlayEnabled)
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Headphones,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background play",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Keep audio playing when screen is off or in another app",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = backgroundPlayEnabled,
                        onCheckedChange = { checked ->
                            onToggleBackgroundPlay(checked)
                        }
                    )
                }

                // 7. Picture-in-Picture (PiP) mode
                YouTubeSettingsRow(
                    icon = Icons.Rounded.PictureInPictureAlt,
                    title = "Picture-in-Picture (PiP)",
                    valueText = "Floating window",
                    onClick = {
                        showSettingsSheet = false
                        onRequestPipMode()
                    }
                )

                // 8. Zoom to fill screen (Pinch fit / fill toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isFullscreenLandscape) {
                                isFullscreenLandscape = true
                                isZoomedToFill = true
                                zoomFeedbackText = "Zoomed to fill"
                            } else {
                                isZoomedToFill = !isZoomedToFill
                                zoomFeedbackText = if (isZoomedToFill) "Zoomed to fill" else "Original"
                            }
                            showSettingsSheet = false
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isZoomedToFill) Icons.Rounded.CropFree else Icons.Rounded.FitScreen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Zoom to fill screen",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = if (isZoomedToFill) "Zoomed to fill (Pinch in for Original)" else "Original fit (Pinch out in fullscreen to fill)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = isZoomedToFill && isFullscreenLandscape,
                        onCheckedChange = { checked ->
                            if (checked && !isFullscreenLandscape) {
                                isFullscreenLandscape = true
                            }
                            isZoomedToFill = checked
                            zoomFeedbackText = if (checked) "Zoomed to fill" else "Original"
                            showSettingsSheet = false
                        }
                    )
                }

                // 9. Lock screen
                YouTubeSettingsRow(
                    icon = Icons.Rounded.Lock,
                    title = "Lock screen",
                    valueText = "Prevent accidental touches",
                    onClick = {
                        showSettingsSheet = false
                        isControlsLocked = true
                        controlsVisible = true
                    }
                )
            }
        }
    }

    // 0b. Video Chapters Bottom Sheet
    if (showChaptersSheet && video.chapters.isNotEmpty()) {
        val currentSec = (currentPositionMs / 1000L).coerceAtLeast(0L)
        val currentChap = video.chapters.lastOrNull { it.startTimeSeconds <= currentSec }

        ModalBottomSheet(
            onDismissRequest = { showChaptersSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Video Chapters (${video.chapters.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )

                video.chapters.forEach { chapter ->
                    val isCurrent = currentChap?.startTimeSeconds == chapter.startTimeSeconds
                    ListItem(
                        leadingContent = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) {
                                    YouTubeRed.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            ) {
                                Text(
                                    text = formatDurationSeconds(chapter.startTimeSeconds),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCurrent) YouTubeRed else YouTubeBlueLink,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = chapter.title,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Playing",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            val targetMs = chapter.startTimeSeconds * 1000L
                            exoPlayer.seekTo(targetMs)
                            exoPlayer.play()
                            currentPositionMs = targetMs
                            showChaptersSheet = false
                        }
                    )
                }
            }
        }
    }

    // 1. Captions / Subtitles Bottom Sheet
    if (showCaptionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCaptionSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Captions (Subtitles)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )

                val isOffSelected = selectedCaption == null
                ListItem(
                    headlineContent = {
                        Text(
                            text = if (video.captions.isEmpty()) "Off (No captions available for this video)" else "Off",
                            fontWeight = if (isOffSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingContent = {
                        if (isOffSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        onSelectCaption(null, exoPlayer.currentPosition)
                        showCaptionSheet = false
                    }
                )

                video.captions.forEach { caption ->
                    val isSelected = caption.id == selectedCaption?.id
                    ListItem(
                        headlineContent = {
                            Text(
                                text = caption.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onSelectCaption(caption, exoPlayer.currentPosition)
                            showCaptionSheet = false
                        }
                    )
                }
            }
        }
    }

    // 2. Audio Track Switch Bottom Sheet (Original / Dubbed / Translated)
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Audio Track",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                Text(
                    text = "Defaults to Original audio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                video.audioTracks.forEach { track ->
                    val isSelected = track.id == selectedAudioTrack?.id
                    ListItem(
                        headlineContent = {
                            Text(
                                text = track.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = if (track.isOriginal) {
                            { Text("Default original track") }
                        } else null,
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onSelectAudioTrack(track, exoPlayer.currentPosition)
                            showAudioSheet = false
                        }
                    )
                }
            }
        }
    }

    // 3. Quality Selection Bottom Sheet
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Video Quality",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                video.qualities.forEach { option ->
                    val isSelected = option.label == selectedQuality?.label
                    ListItem(
                        headlineContent = {
                            Text(
                                text = option.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectQuality(option, exoPlayer.currentPosition)
                            showQualitySheet = false
                        }
                    )
                }
            }
        }
    }

    // 4. Playback Speed Bottom Sheet
    if (showSpeedSheet) {
        val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Playback Speed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                speeds.forEach { speed ->
                    val isSelected = speed == playbackSpeed
                    ListItem(
                        headlineContent = {
                            Text(
                                text = if (speed == 1.0f) "Normal" else "${speed}x",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onSelectSpeed(speed)
                            showSpeedSheet = false
                        }
                    )
                }
            }
        }
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YouTubeSeekBar(
    value: Float,
    bufferedFraction: Float,
    chapterFractions: List<Float> = emptyList(),
    isScrubbing: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(if (isScrubbing) 18.dp else 12.dp)
                    .background(YouTubeRed, CircleShape)
            )
        },
        track = {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isScrubbing) 5.dp else 3.dp)
            ) {
                val strokeWidth = size.height
                val centerY = size.height / 2f

                // 1. Unplayed background track
                drawLine(
                    color = Color.White.copy(alpha = 0.28f),
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // 2. Buffered progress track
                val bufferedEnd = (size.width * bufferedFraction.coerceIn(0f, 1f))
                if (bufferedEnd > 0f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.62f),
                        start = Offset(0f, centerY),
                        end = Offset(bufferedEnd, centerY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 3. Active YouTube Red progress track
                val activeEnd = (size.width * value.coerceIn(0f, 1f))
                if (activeEnd > 0f) {
                    drawLine(
                        color = YouTubeRed,
                        start = Offset(0f, centerY),
                        end = Offset(activeEnd, centerY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 4. Video Chapter Boundary Tick Marks (YouTube-style dark gap ticks)
                if (chapterFractions.isNotEmpty()) {
                    val gapWidth = 2.5.dp.toPx()
                    chapterFractions.forEach { frac ->
                        val tickX = size.width * frac.coerceIn(0f, 1f)
                        drawLine(
                            color = Color.Black.copy(alpha = 0.9f),
                            start = Offset(tickX, 0f),
                            end = Offset(tickX, size.height),
                            strokeWidth = gapWidth
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun YouTubeActionChip(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isHighlighted) Color.White.copy(alpha = 0.22f) else YouTubeChipSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FullscreenQuickPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) {
            YouTubeRed.copy(alpha = 0.85f)
        } else {
            Color.White.copy(alpha = 0.16f)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun YouTubeSettingsRow(
    icon: ImageVector,
    title: String,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(UnstableApi::class)
private fun createAdaptiveOrProgressiveSource(
    dataSourceFactory: DataSource.Factory,
    streamUrl: String,
    dashManifestXml: String?
): MediaSource {
    if (!dashManifestXml.isNullOrBlank()) {
        val dashSource = runCatching {
            val manifest = ByteArrayInputStream(dashManifestXml.toByteArray(Charsets.UTF_8)).use { input ->
                DashManifestParser().parse(Uri.parse(streamUrl), input)
            }
            DashMediaSource.Factory(dataSourceFactory).createMediaSource(manifest)
        }.getOrNull()
        if (dashSource != null) {
            return dashSource
        }
    }
    return ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(streamUrl))
}

private fun formatCompactCount(count: Long): String {
    return when {
        count >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.1fB", count / 1_000_000_000.0).removeSuffix(".0B") + if (String.format(java.util.Locale.US, "%.1fB", count / 1_000_000_000.0).endsWith(".0B")) "B" else ""
        count >= 1_000_000L -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0).replace(".0M", "M")
        count >= 10_000L -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0).replace(".0K", "K")
        count >= 1_000L -> java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(count)
        else -> count.toString()
    }
}

private val DescriptionTimestampRegex = Regex("""\b(\d{1,2}:\d{2}(?::\d{2})?)\b""")

private fun buildClickableDescriptionAnnotatedString(
    text: String,
    maxDurationSeconds: Long,
    onTimestampClick: (Long) -> Unit
): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in DescriptionTimestampRegex.findAll(text)) {
            val start = match.range.first
            val endExclusive = match.range.last + 1
            val tsText = match.groupValues[1]
            val parsedSeconds = YoutubeStreamExtractor.parseTimestampToSeconds(tsText)

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            if (parsedSeconds != null && (maxDurationSeconds <= 0L || parsedSeconds <= maxDurationSeconds)) {
                val link = LinkAnnotation.Clickable(
                    tag = "ts_$parsedSeconds",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = YouTubeBlueLink,
                            fontWeight = FontWeight.SemiBold
                        )
                    ),
                    linkInteractionListener = {
                        onTimestampClick(parsedSeconds)
                    }
                )
                withLink(link) {
                    append(tsText)
                }
            } else {
                append(tsText)
            }
            lastIndex = endExclusive
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
