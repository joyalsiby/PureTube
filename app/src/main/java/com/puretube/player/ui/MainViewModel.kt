package com.puretube.player.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.puretube.player.PureTubeApplication
import com.puretube.player.data.AppSettings
import com.puretube.player.data.AppThemeMode
import com.puretube.player.data.KidsUnlockMode
import com.puretube.player.data.WatchHistoryEntity
import com.puretube.player.extractor.AudioTrackOption
import com.puretube.player.extractor.CaptionOption
import com.puretube.player.extractor.ExtractedPlaylist
import com.puretube.player.extractor.ExtractedVideo
import com.puretube.player.extractor.VideoQualityOption
import com.puretube.player.extractor.YoutubeStreamExtractor
import com.puretube.player.extractor.YoutubeStreamPreloader
import com.puretube.player.updater.AppUpdateManager
import com.puretube.player.updater.ReleaseUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class MainUiState(
    val inputUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeVideo: ExtractedVideo? = null,
    val selectedQuality: VideoQualityOption? = null,
    val selectedAudioTrack: AudioTrackOption? = null,
    val selectedCaption: CaptionOption? = null,
    val playbackSpeed: Float = 1.0f,
    val backgroundPlayEnabled: Boolean = false,
    val initialSeekPositionMs: Long = 0L,
    // Playlist Queue State
    val activePlaylist: ExtractedPlaylist? = null,
    val activePlaylistIndex: Int = 0,
    // Update Manager State
    val availableUpdate: ReleaseUpdateInfo? = null,
    val showUpdateDialog: Boolean = false,
    val isCheckingForUpdate: Boolean = false,
    val updateStatusMessage: String? = null,
    val isDownloadingUpdate: Boolean = false,
    val updateDownloadProgress: Float = 0f,
    val downloadedApkFile: File? = null,
    // Onboarding & Post-Update Release Notes State
    val showOnboarding: Boolean = false,
    val showReleaseNotesDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PureTubeApplication
    private val historyDao = app.database.watchHistoryDao()
    private val preferences = app.preferences
    val currentVersionName: String = AppUpdateManager.getCurrentVersionName(app)

    val appSettings: StateFlow<AppSettings> = preferences.settings

    private val _uiState = MutableStateFlow(
        run {
            val initialSettings = preferences.settings.value
            val needsOnboarding = !initialSettings.onboardingCompleted
            val needsReleaseNotes = initialSettings.onboardingCompleted &&
                initialSettings.lastSeenVersion != currentVersionName

            MainUiState(
                playbackSpeed = initialSettings.defaultPlaybackSpeed,
                backgroundPlayEnabled = initialSettings.backgroundPlayEnabled,
                showOnboarding = needsOnboarding,
                showReleaseNotesDialog = needsReleaseNotes
            )
        }
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = historyDao
        .observeAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (preferences.settings.value.autoCheckUpdates) {
            checkForAppUpdates(manual = false)
        }
    }

    fun onUrlChanged(newUrl: String) {
        _uiState.update { it.copy(inputUrl = newUrl, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadAndPlayUrl(
        rawUrl: String,
        resumePositionMs: Long = 0L,
        preserveCurrentPlaylist: Boolean = false
    ) {
        val target = rawUrl.ifBlank { _uiState.value.inputUrl }.trim()
        if (target.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Paste a YouTube video or playlist link first") }
            return
        }

        val cleanVideoUrl = YoutubeStreamExtractor.extractYoutubeUrl(target)
        val playlistId = YoutubeStreamExtractor.extractPlaylistId(target)

        if (cleanVideoUrl == null && playlistId == null) {
            _uiState.update {
                it.copy(errorMessage = "Please enter a valid YouTube video or playlist link.")
            }
            return
        }

        _uiState.update {
            it.copy(
                inputUrl = target,
                isLoading = true,
                errorMessage = null,
                activePlaylist = if (preserveCurrentPlaylist) it.activePlaylist else null,
                activePlaylistIndex = if (preserveCurrentPlaylist) it.activePlaylistIndex else 0
            )
        }

        viewModelScope.launch {
            // Case 1: Pure playlist URL (no v= parameter) -> extract playlist first, then play item 0
            if (cleanVideoUrl == null && playlistId != null) {
                val playlistRes = YoutubeStreamExtractor.extractPlaylist(playlistId)
                playlistRes.fold(
                    onSuccess = { playlist ->
                        val firstVideo = playlist.videos.first()
                        _uiState.update {
                            it.copy(
                                activePlaylist = playlist,
                                activePlaylistIndex = 0
                            )
                        }
                        extractAndStartSingleVideo(
                            videoUrl = firstVideo.url,
                            resumePositionMs = 0L
                        )
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = err.localizedMessage ?: "Failed to load YouTube playlist."
                            )
                        }
                    }
                )
                return@launch
            }

            // Case 2: Video URL that ALSO has a &list= parameter -> start playing video immediately,
            // and fetch the playlist queue in parallel!
            if (cleanVideoUrl != null && playlistId != null && !preserveCurrentPlaylist) {
                launch {
                    YoutubeStreamExtractor.extractPlaylist(playlistId).onSuccess { playlist ->
                        val activeVidId = _uiState.value.activeVideo?.videoId
                            ?: cleanVideoUrl.substringAfter("v=").substringBefore("&")
                        val matchIdx = playlist.videos.indexOfFirst { it.videoId == activeVidId }.coerceAtLeast(0)
                        _uiState.update {
                            it.copy(
                                activePlaylist = playlist,
                                activePlaylistIndex = matchIdx
                            )
                        }
                    }
                }
            }

            extractAndStartSingleVideo(
                videoUrl = cleanVideoUrl ?: target,
                resumePositionMs = resumePositionMs
            )
        }
    }

    private suspend fun extractAndStartSingleVideo(
        videoUrl: String,
        resumePositionMs: Long
    ) {
        val result = YoutubeStreamExtractor.extractStream(videoUrl)
        result.fold(
            onSuccess = { extracted ->
                val currentSettings = preferences.settings.value

                // Pick optimal hardware-accelerated stream matching user's preferred resolution
                val defaultQuality = YoutubeStreamExtractor.selectDefaultStartupQuality(
                    qualities = extracted.qualities,
                    preferredHeight = currentSettings.defaultQualityHeight
                )

                // Always default to Original audio track first
                val defaultAudioTrack = extracted.audioTracks.firstOrNull {
                    it.isOriginal
                } ?: extracted.audioTracks.firstOrNull()

                val existing = historyDao.getByVideoId(extracted.videoId)
                val startPosition = if (resumePositionMs > 0L) {
                    resumePositionMs
                } else {
                    existing?.lastPositionMs?.takeIf {
                        extracted.durationSeconds > 0 && it < (extracted.durationSeconds * 950L)
                    } ?: 0L
                }

                if (currentSettings.saveWatchHistory) {
                    historyDao.upsert(
                        WatchHistoryEntity(
                            videoId = extracted.videoId,
                            url = extracted.originalUrl,
                            title = extracted.title,
                            uploaderName = extracted.uploaderName,
                            thumbnailUrl = extracted.thumbnailUrl,
                            durationSeconds = extracted.durationSeconds,
                            lastPositionMs = startPosition,
                            watchedAtTimestamp = System.currentTimeMillis()
                        )
                    )
                }

                _uiState.update { state ->
                    val matchedIndex = state.activePlaylist?.videos
                        ?.indexOfFirst { it.videoId == extracted.videoId }
                        ?.takeIf { it >= 0 }
                        ?: state.activePlaylistIndex

                    state.copy(
                        isLoading = false,
                        activeVideo = extracted,
                        activePlaylistIndex = matchedIndex,
                        selectedQuality = defaultQuality,
                        selectedAudioTrack = defaultAudioTrack,
                        selectedCaption = null,
                        playbackSpeed = currentSettings.defaultPlaybackSpeed,
                        backgroundPlayEnabled = currentSettings.backgroundPlayEnabled,
                        initialSeekPositionMs = startPosition
                    )
                }
            },
            onFailure = { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to extract video stream."
                    )
                }
            }
        )
    }

    fun playPlaylistVideoAt(index: Int, currentPositionMs: Long = 0L) {
        val playlist = _uiState.value.activePlaylist ?: return
        val targetItem = playlist.videos.getOrNull(index) ?: return
        val currentVideo = _uiState.value.activeVideo
        if (currentVideo != null && currentPositionMs > 0L) {
            savePlaybackProgress(currentVideo.videoId, currentPositionMs)
        }
        _uiState.update {
            it.copy(
                activePlaylistIndex = index,
                isLoading = true
            )
        }
        viewModelScope.launch {
            extractAndStartSingleVideo(
                videoUrl = targetItem.url,
                resumePositionMs = 0L
            )
        }
    }

    fun playNextInPlaylist(currentPositionMs: Long = 0L): Boolean {
        val playlist = _uiState.value.activePlaylist ?: return false
        val nextIndex = _uiState.value.activePlaylistIndex + 1
        if (nextIndex !in playlist.videos.indices) return false
        playPlaylistVideoAt(nextIndex, currentPositionMs)
        return true
    }

    fun playPreviousInPlaylist(currentPositionMs: Long = 0L): Boolean {
        val playlist = _uiState.value.activePlaylist ?: return false
        val prevIndex = _uiState.value.activePlaylistIndex - 1
        if (prevIndex !in playlist.videos.indices) return false
        playPlaylistVideoAt(prevIndex, currentPositionMs)
        return true
    }

    fun selectQuality(quality: VideoQualityOption, currentPositionMs: Long) {
        if (currentPositionMs <= 2_000L) {
            YoutubeStreamPreloader.preloadStreams(
                videoUrl = quality.videoUrl,
                audioUrl = _uiState.value.selectedAudioTrack?.audioUrl ?: quality.audioUrl
            )
        }
        _uiState.update {
            it.copy(
                selectedQuality = quality,
                initialSeekPositionMs = currentPositionMs
            )
        }
    }

    fun selectAudioTrack(audioTrack: AudioTrackOption, currentPositionMs: Long) {
        if (currentPositionMs <= 2_000L) {
            YoutubeStreamPreloader.preloadStreams(
                videoUrl = null,
                audioUrl = audioTrack.audioUrl
            )
        }
        _uiState.update {
            it.copy(
                selectedAudioTrack = audioTrack,
                initialSeekPositionMs = currentPositionMs
            )
        }
    }

    fun selectCaption(caption: CaptionOption?, currentPositionMs: Long) {
        _uiState.update {
            it.copy(
                selectedCaption = caption,
                initialSeekPositionMs = currentPositionMs
            )
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setBackgroundPlayEnabled(enabled: Boolean) {
        preferences.setBackgroundPlayEnabled(enabled)
        _uiState.update { it.copy(backgroundPlayEnabled = enabled) }
    }

    fun savePlaybackProgress(videoId: String, positionMs: Long) {
        if (!preferences.settings.value.saveWatchHistory) return
        viewModelScope.launch {
            historyDao.updateProgress(videoId, positionMs)
        }
    }

    fun closePlayer(finalPositionMs: Long) {
        val currentVideo = _uiState.value.activeVideo
        if (currentVideo != null && finalPositionMs >= 0L) {
            savePlaybackProgress(currentVideo.videoId, finalPositionMs)
        }
        _uiState.update {
            it.copy(
                activeVideo = null,
                activePlaylist = null,
                activePlaylistIndex = 0,
                selectedQuality = null,
                selectedAudioTrack = null,
                selectedCaption = null,
                initialSeekPositionMs = 0L
            )
        }
    }

    fun deleteHistoryItem(videoId: String) {
        viewModelScope.launch {
            historyDao.deleteById(videoId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    // =========================================================================
    // APP SETTINGS ACTIONS
    // =========================================================================

    fun updateThemeMode(mode: AppThemeMode) = preferences.setThemeMode(mode)
    fun updateDynamicColor(enabled: Boolean) = preferences.setDynamicColorEnabled(enabled)
    fun updateDefaultQualityHeight(height: Int) = preferences.setDefaultQualityHeight(height)
    fun updateDefaultPlaybackSpeed(speed: Float) {
        preferences.setDefaultPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }
    fun updateAutoPipOnHome(enabled: Boolean) = preferences.setAutoPipOnHome(enabled)
    fun updateAutoStartKidsLocked(enabled: Boolean) = preferences.setAutoStartKidsLocked(enabled)
    fun updateKidsUnlockMode(mode: KidsUnlockMode) = preferences.setKidsUnlockMode(mode)
    fun updateAutoFullscreenOnPlay(enabled: Boolean) = preferences.setAutoFullscreenOnPlay(enabled)
    fun updateHideVideoDescription(enabled: Boolean) = preferences.setHideVideoDescription(enabled)
    fun updateSaveWatchHistory(enabled: Boolean) = preferences.setSaveWatchHistory(enabled)
    fun updateAutoCheckUpdates(enabled: Boolean) = preferences.setAutoCheckUpdates(enabled)

    // =========================================================================
    // ONBOARDING & POST-UPDATE RELEASE NOTES
    // =========================================================================

    fun completeOnboarding() {
        val lastSeen = preferences.settings.value.lastSeenVersion
        preferences.setOnboardingCompleted(true)
        // If this version's release notes haven't been seen yet (e.g. after an update), show them next
        val shouldShowReleaseNotes = lastSeen != currentVersionName
        _uiState.update {
            it.copy(
                showOnboarding = false,
                showReleaseNotesDialog = shouldShowReleaseNotes
            )
        }
    }

    fun reopenOnboarding() {
        _uiState.update { it.copy(showOnboarding = true) }
    }

    fun openReleaseNotesDialog() {
        _uiState.update { it.copy(showReleaseNotesDialog = true) }
    }

    fun dismissReleaseNotesDialog() {
        preferences.setLastSeenVersion(currentVersionName)
        _uiState.update { it.copy(showReleaseNotesDialog = false) }
    }

    // =========================================================================
    // IN-APP UPDATE CHECKER & DOWNLOADER
    // =========================================================================

    fun checkForAppUpdates(manual: Boolean = true) {
        if (_uiState.value.isCheckingForUpdate) return

        _uiState.update {
            it.copy(
                isCheckingForUpdate = true,
                updateStatusMessage = if (manual) "Checking GitHub Releases..." else null
            )
        }

        viewModelScope.launch {
            val result = AppUpdateManager.checkForUpdate(app)
            result.fold(
                onSuccess = { updateInfo ->
                    if (updateInfo != null) {
                        val skipped = preferences.settings.value.skippedUpdateVersion
                        val shouldShowDialog = manual || updateInfo.versionName != skipped
                        _uiState.update {
                            it.copy(
                                isCheckingForUpdate = false,
                                availableUpdate = updateInfo,
                                showUpdateDialog = shouldShowDialog,
                                updateStatusMessage = "New update available: v${updateInfo.versionName}"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCheckingForUpdate = false,
                                availableUpdate = null,
                                updateStatusMessage = if (manual) {
                                    "You're on the latest version (v${AppUpdateManager.getCurrentVersionName(app)})"
                                } else null
                            )
                        }
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isCheckingForUpdate = false,
                            updateStatusMessage = if (manual) {
                                "Update check failed: ${err.localizedMessage ?: "Network error"}"
                            } else null
                        )
                    }
                }
            )
        }
    }

    fun openUpdateDialog() {
        if (_uiState.value.availableUpdate != null) {
            _uiState.update { it.copy(showUpdateDialog = true) }
        }
    }

    fun dismissUpdateDialog(skipThisVersion: Boolean = false) {
        val ver = _uiState.value.availableUpdate?.versionName
        if (skipThisVersion && !ver.isNullOrEmpty()) {
            preferences.setSkippedUpdateVersion(ver)
        }
        _uiState.update { it.copy(showUpdateDialog = false) }
    }

    fun downloadAndInstallUpdate() {
        val updateInfo = _uiState.value.availableUpdate ?: return
        if (_uiState.value.isDownloadingUpdate) return

        // If already downloaded for this version, install immediately
        val existingFile = _uiState.value.downloadedApkFile
        if (existingFile != null && existingFile.exists() && existingFile.name == updateInfo.apkFileName) {
            if (updateInfo.releaseNotes.isNotBlank()) {
                preferences.setCachedReleaseNotes(updateInfo.versionName, updateInfo.releaseNotes)
            }
            AppUpdateManager.installApk(app, existingFile)
            return
        }

        if (updateInfo.releaseNotes.isNotBlank()) {
            preferences.setCachedReleaseNotes(updateInfo.versionName, updateInfo.releaseNotes)
        }

        _uiState.update {
            it.copy(
                isDownloadingUpdate = true,
                updateDownloadProgress = 0f,
                updateStatusMessage = "Downloading ${updateInfo.apkFileName}..."
            )
        }

        viewModelScope.launch {
            val result = AppUpdateManager.downloadUpdateApk(
                context = app,
                updateInfo = updateInfo,
                onProgress = { fraction ->
                    _uiState.update { state ->
                        state.copy(updateDownloadProgress = fraction)
                    }
                }
            )

            result.fold(
                onSuccess = { apkFile ->
                    _uiState.update {
                        it.copy(
                            isDownloadingUpdate = false,
                            updateDownloadProgress = 1f,
                            downloadedApkFile = apkFile,
                            updateStatusMessage = "Ready to install v${updateInfo.versionName}"
                        )
                    }
                    AppUpdateManager.installApk(app, apkFile)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDownloadingUpdate = false,
                            updateStatusMessage = "Download failed: ${err.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            )
        }
    }
}
