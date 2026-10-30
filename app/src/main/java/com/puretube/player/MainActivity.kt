package com.puretube.player

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.puretube.player.data.AppThemeMode
import com.puretube.player.extractor.YoutubeStreamExtractor
import com.puretube.player.ui.MainViewModel
import com.puretube.player.ui.player.MediaPlaybackNotificationManager
import com.puretube.player.ui.player.VideoPlayerScreen
import com.puretube.player.ui.screens.HistoryScreen
import com.puretube.player.ui.screens.OnboardingScreen
import com.puretube.player.ui.screens.SettingsScreen
import com.puretube.player.ui.screens.WatchHomeScreen
import com.puretube.player.ui.theme.PureTubeTheme
import com.puretube.player.updater.WhatsNewReleaseNotesDialog
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isInPipModeState by mutableStateOf(false)
    private var isPlayerCurrentlyPlaying: Boolean = true

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Notification banner shown automatically if granted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }

        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (appSettings.themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            PureTubeTheme(
                darkTheme = useDarkTheme,
                dynamicColor = appSettings.dynamicColorEnabled
            ) {
                PureTubeApp(
                    viewModel = viewModel,
                    isInPipMode = isInPipModeState,
                    onRequestPipMode = ::enterPipIfSupported,
                    onPlayerPlayingStateChanged = ::updatePipRemoteActions
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                runCatching {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter Picture-in-Picture (PiP) mode when leaving the app while watching a video
        if (viewModel.uiState.value.activeVideo != null && viewModel.appSettings.value.autoPipOnHome) {
            enterPipIfSupported()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState = isInPictureInPictureMode
    }

    fun updatePipRemoteActions(isPlaying: Boolean) {
        isPlayerCurrentlyPlaying = isPlaying
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) return

        runCatching {
            val paramsBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setActions(MediaPlaybackNotificationManager.buildPipRemoteActions(this, isPlaying))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                paramsBuilder.setSeamlessResizeEnabled(true)
                val autoEnter = viewModel.uiState.value.activeVideo != null &&
                    viewModel.appSettings.value.autoPipOnHome &&
                    isPlaying
                paramsBuilder.setAutoEnterEnabled(autoEnter)
            }
            setPictureInPictureParams(paramsBuilder.build())
        }
    }

    fun enterPipIfSupported(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val hasPip = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!hasPip) return false

        return runCatching {
            val paramsBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setActions(
                    MediaPlaybackNotificationManager.buildPipRemoteActions(
                        this,
                        isPlayerCurrentlyPlaying
                    )
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                paramsBuilder.setSeamlessResizeEnabled(true)
            }
            enterPictureInPictureMode(paramsBuilder.build())
        }.getOrDefault(false)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val sharedOrViewedUrl = when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_VIEW -> intent.dataString
            else -> null
        }

        if (!sharedOrViewedUrl.isNullOrBlank()) {
            val cleanUrl = YoutubeStreamExtractor.extractYoutubeOrPlaylistUrl(sharedOrViewedUrl) ?: sharedOrViewedUrl
            viewModel.onUrlChanged(cleanUrl)
            viewModel.loadAndPlayUrl(cleanUrl)
        }
    }
}

@Composable
fun PureTubeApp(
    viewModel: MainViewModel,
    isInPipMode: Boolean = false,
    onRequestPipMode: () -> Boolean = { false },
    onPlayerPlayingStateChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val historyItems by viewModel.watchHistory.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // If a video is currently active, render the full-screen ad-free player directly
    val activeVideo = uiState.activeVideo
    if (activeVideo != null) {
        VideoPlayerScreen(
            video = activeVideo,
            selectedQuality = uiState.selectedQuality,
            selectedAudioTrack = uiState.selectedAudioTrack,
            selectedCaption = uiState.selectedCaption,
            playbackSpeed = uiState.playbackSpeed,
            backgroundPlayEnabled = uiState.backgroundPlayEnabled,
            isInPipMode = isInPipMode,
            initialSeekPositionMs = uiState.initialSeekPositionMs,
            initialFullscreenLandscape = appSettings.autoFullscreenOnPlay,
            initialControlsLocked = appSettings.autoStartKidsLocked,
            kidsUnlockMode = appSettings.kidsUnlockMode,
            hideVideoDescription = appSettings.hideVideoDescription,
            activePlaylist = uiState.activePlaylist,
            activePlaylistIndex = uiState.activePlaylistIndex,
            onSelectPlaylistVideo = { index, currentPos ->
                viewModel.playPlaylistVideoAt(index, currentPos)
            },
            onPlayNextInPlaylist = { currentPos ->
                viewModel.playNextInPlaylist(currentPos)
            },
            onPlayPreviousInPlaylist = { currentPos ->
                viewModel.playPreviousInPlaylist(currentPos)
            },
            onPlayerPlayingStateChanged = onPlayerPlayingStateChanged,
            onSelectQuality = { quality, currentPos ->
                viewModel.selectQuality(quality, currentPos)
            },
            onSelectAudioTrack = { audioTrack, currentPos ->
                viewModel.selectAudioTrack(audioTrack, currentPos)
            },
            onSelectCaption = { caption, currentPos ->
                viewModel.selectCaption(caption, currentPos)
            },
            onSelectSpeed = { speed ->
                viewModel.setPlaybackSpeed(speed)
            },
            onToggleBackgroundPlay = { enabled ->
                viewModel.setBackgroundPlayEnabled(enabled)
            },
            onRequestPipMode = onRequestPipMode,
            onProgressUpdate = { currentPos ->
                viewModel.savePlaybackProgress(activeVideo.videoId, currentPos)
            },
            onClosePlayer = { finalPos ->
                onPlayerPlayingStateChanged(false)
                viewModel.closePlayer(finalPos)
            }
        )
        return
    }

    // First-Install Welcome Onboarding Screen (or when replayed from Settings)
    if (uiState.showOnboarding && !uiState.isLoading) {
        OnboardingScreen(
            settings = appSettings,
            onUpdateAutoStartKidsLocked = viewModel::updateAutoStartKidsLocked,
            onUpdateAutoFullscreen = viewModel::updateAutoFullscreenOnPlay,
            onUpdateHideDescription = viewModel::updateHideVideoDescription,
            onUpdateBackgroundPlay = viewModel::setBackgroundPlayEnabled,
            onFinishOnboarding = {
                viewModel.completeOnboarding()
            }
        )
        return
    }

    // Post-Update In-App "What's New" Release Notes Dialog
    if (uiState.showReleaseNotesDialog) {
        val cachedNotes = if (appSettings.cachedReleaseNotesVersion == viewModel.currentVersionName) {
            appSettings.cachedReleaseNotesBody.takeIf { it.isNotBlank() }
        } else {
            null
        }
        WhatsNewReleaseNotesDialog(
            currentVersion = viewModel.currentVersionName,
            cachedRemoteNotes = cachedNotes,
            onDismiss = {
                viewModel.dismissReleaseNotesDialog()
            }
        )
    }

    // In-App Update Notification Dialog
    val availableUpdate = uiState.availableUpdate
    if (uiState.showUpdateDialog && availableUpdate != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDownloadingUpdate) {
                    viewModel.dismissUpdateDialog(skipThisVersion = false)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.NewReleases,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Update Available (v${availableUpdate.versionName})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = availableUpdate.releaseTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (availableUpdate.releaseNotes.isNotBlank()) {
                        Text(
                            text = availableUpdate.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "A new version of PureTube is ready to download and install directly over your current app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.isDownloadingUpdate) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val pct = (uiState.updateDownloadProgress * 100f).roundToInt().coerceIn(0, 100)
                        LinearProgressIndicator(
                            progress = { uiState.updateDownloadProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Downloading update... $pct%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.downloadAndInstallUpdate() },
                    enabled = !uiState.isDownloadingUpdate
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.isDownloadingUpdate) "Downloading..." else "Download & Update")
                }
            },
            dismissButton = {
                if (!uiState.isDownloadingUpdate) {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog(skipThisVersion = false) }
                    ) {
                        Text("Later")
                    }
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Rounded.PlayCircle else Icons.Outlined.PlayCircle,
                            contentDescription = "Watch"
                        )
                    },
                    label = { Text("Watch") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Rounded.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (availableUpdate != null) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Rounded.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> WatchHomeScreen(
                inputUrl = uiState.inputUrl,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                availableUpdate = availableUpdate,
                onUpdateBannerClick = { viewModel.openUpdateDialog() },
                onReleaseNotesClick = { viewModel.openReleaseNotesDialog() },
                onUrlChange = viewModel::onUrlChanged,
                onWatchClick = { url -> viewModel.loadAndPlayUrl(url) },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> HistoryScreen(
                historyItems = historyItems,
                onPlayHistoryItem = { item ->
                    viewModel.loadAndPlayUrl(
                        rawUrl = item.url,
                        resumePositionMs = item.lastPositionMs
                    )
                },
                onDeleteHistoryItem = viewModel::deleteHistoryItem,
                onClearAllHistory = viewModel::clearAllHistory,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> SettingsScreen(
                settings = appSettings,
                historyCount = historyItems.size,
                availableUpdate = availableUpdate,
                isCheckingForUpdate = uiState.isCheckingForUpdate,
                isDownloadingUpdate = uiState.isDownloadingUpdate,
                updateDownloadProgress = uiState.updateDownloadProgress,
                updateStatusMessage = uiState.updateStatusMessage,
                onCheckForUpdate = { viewModel.checkForAppUpdates(manual = true) },
                onDownloadAndInstallUpdate = { viewModel.downloadAndInstallUpdate() },
                onUpdateThemeMode = viewModel::updateThemeMode,
                onUpdateDynamicColor = viewModel::updateDynamicColor,
                onUpdateDefaultQuality = viewModel::updateDefaultQualityHeight,
                onUpdateDefaultSpeed = viewModel::updateDefaultPlaybackSpeed,
                onUpdateBackgroundPlay = viewModel::setBackgroundPlayEnabled,
                onUpdateAutoPip = viewModel::updateAutoPipOnHome,
                onUpdateAutoStartKidsLocked = viewModel::updateAutoStartKidsLocked,
                onUpdateKidsUnlockMode = viewModel::updateKidsUnlockMode,
                onUpdateAutoFullscreen = viewModel::updateAutoFullscreenOnPlay,
                onUpdateHideDescription = viewModel::updateHideVideoDescription,
                onUpdateSaveHistory = viewModel::updateSaveWatchHistory,
                onUpdateAutoCheckUpdates = viewModel::updateAutoCheckUpdates,
                onClearHistory = viewModel::clearAllHistory,
                onOpenReleaseNotes = { viewModel.openReleaseNotesDialog() },
                onReopenOnboarding = { viewModel.reopenOnboarding() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
