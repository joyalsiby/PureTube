package com.puretube.player.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.puretube.player.PureTubeApplication
import com.puretube.player.data.WatchHistoryEntity
import com.puretube.player.extractor.AudioTrackOption
import com.puretube.player.extractor.CaptionOption
import com.puretube.player.extractor.ExtractedVideo
import com.puretube.player.extractor.VideoQualityOption
import com.puretube.player.extractor.YoutubeStreamExtractor
import com.puretube.player.extractor.YoutubeStreamPreloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val inputUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeVideo: ExtractedVideo? = null,
    val selectedQuality: VideoQualityOption? = null,
    val selectedAudioTrack: AudioTrackOption? = null,
    val selectedCaption: CaptionOption? = null,
    val playbackSpeed: Float = 1.0f,
    val initialSeekPositionMs: Long = 0L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val historyDao = (application as PureTubeApplication).database.watchHistoryDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = historyDao
        .observeAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUrlChanged(newUrl: String) {
        _uiState.update { it.copy(inputUrl = newUrl, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadAndPlayUrl(rawUrl: String, resumePositionMs: Long = 0L) {
        val target = rawUrl.ifBlank { _uiState.value.inputUrl }.trim()
        if (target.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Paste a YouTube link first") }
            return
        }

        _uiState.update {
            it.copy(
                inputUrl = target,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = YoutubeStreamExtractor.extractStream(target)
            result.fold(
                onSuccess = { extracted ->
                    // Pick optimal hardware-accelerated 1080p stream by default if available
                    val defaultQuality = YoutubeStreamExtractor.selectDefaultStartupQuality(extracted.qualities)

                    // Always default to Original audio track first
                    val defaultAudioTrack = extracted.audioTracks.firstOrNull {
                        it.isOriginal
                    } ?: extracted.audioTracks.firstOrNull()

                    // Save to Watch History immediately
                    val existing = historyDao.getByVideoId(extracted.videoId)
                    val startPosition = if (resumePositionMs > 0L) {
                        resumePositionMs
                    } else {
                        existing?.lastPositionMs?.takeIf {
                            // Resume if previous position wasn't at the very end (< 95% duration)
                            extracted.durationSeconds > 0 && it < (extracted.durationSeconds * 950L)
                        } ?: 0L
                    }

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

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeVideo = extracted,
                            selectedQuality = defaultQuality,
                            selectedAudioTrack = defaultAudioTrack,
                            selectedCaption = null,
                            playbackSpeed = 1.0f,
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

    fun savePlaybackProgress(videoId: String, positionMs: Long) {
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
}
