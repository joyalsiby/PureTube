package com.puretube.player

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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.puretube.player.extractor.YoutubeStreamExtractor
import com.puretube.player.ui.MainViewModel
import com.puretube.player.ui.player.VideoPlayerScreen
import com.puretube.player.ui.screens.HistoryScreen
import com.puretube.player.ui.screens.WatchHomeScreen
import com.puretube.player.ui.theme.PureTubeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var isInPipModeState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }

        setContent {
            PureTubeTheme {
                PureTubeApp(
                    viewModel = viewModel,
                    isInPipMode = isInPipModeState,
                    onRequestPipMode = ::enterPipIfSupported
                )
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
        // Automatically enter Picture-in-Picture (PiP) mode when pressing Home while watching a video
        if (viewModel.uiState.value.activeVideo != null) {
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

    fun enterPipIfSupported(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val hasPip = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!hasPip) return false

        return runCatching {
            val paramsBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
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
            val cleanUrl = YoutubeStreamExtractor.extractYoutubeUrl(sharedOrViewedUrl) ?: sharedOrViewedUrl
            viewModel.onUrlChanged(cleanUrl)
            viewModel.loadAndPlayUrl(cleanUrl)
        }
    }
}

@Composable
fun PureTubeApp(
    viewModel: MainViewModel,
    isInPipMode: Boolean = false,
    onRequestPipMode: () -> Boolean = { false }
) {
    val uiState by viewModel.uiState.collectAsState()
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
                viewModel.closePlayer(finalPos)
            }
        )
        return
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
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> WatchHomeScreen(
                inputUrl = uiState.inputUrl,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
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
        }
    }
}
