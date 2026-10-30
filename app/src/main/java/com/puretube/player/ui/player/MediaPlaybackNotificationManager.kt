package com.puretube.player.ui.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.puretube.player.MainActivity
import java.util.UUID

/**
 * Manages the AndroidX Media3 [MediaSession], Quick Settings / Lock Screen Media Control Panel,
 * Notification Banner (Play/Pause, -10s, +10s, Close), and Picture-in-Picture (PiP) RemoteActions.
 */
@OptIn(UnstableApi::class)
class MediaPlaybackNotificationManager(
    private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val videoTitle: String,
    private val uploaderName: String,
    private val onStopAndClosePlayer: () -> Unit
) {

    companion object {
        const val CHANNEL_ID = "puretube_media_playback"
        const val NOTIFICATION_ID = 4201

        const val ACTION_PLAY_PAUSE = "com.puretube.player.ACTION_PLAY_PAUSE"
        const val ACTION_REWIND_10 = "com.puretube.player.ACTION_REWIND_10"
        const val ACTION_FORWARD_10 = "com.puretube.player.ACTION_FORWARD_10"
        const val ACTION_STOP_CLOSE = "com.puretube.player.ACTION_STOP_CLOSE"

        fun buildPipRemoteActions(context: Context, isPlaying: Boolean): List<RemoteAction> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            val rewindIntent = PendingIntent.getBroadcast(
                context,
                101,
                Intent(ACTION_REWIND_10).setPackage(context.packageName),
                flags
            )
            val playPauseIntent = PendingIntent.getBroadcast(
                context,
                102,
                Intent(ACTION_PLAY_PAUSE).setPackage(context.packageName),
                flags
            )
            val forwardIntent = PendingIntent.getBroadcast(
                context,
                103,
                Intent(ACTION_FORWARD_10).setPackage(context.packageName),
                flags
            )

            val rewindAction = RemoteAction(
                Icon.createWithResource(context, android.R.drawable.ic_media_rew),
                "Rewind 10s",
                "Rewind 10 seconds",
                rewindIntent
            )
            val playPauseAction = RemoteAction(
                Icon.createWithResource(
                    context,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                ),
                if (isPlaying) "Pause" else "Play",
                if (isPlaying) "Pause video" else "Play video",
                playPauseIntent
            )
            val forwardAction = RemoteAction(
                Icon.createWithResource(context, android.R.drawable.ic_media_ff),
                "Forward 10s",
                "Forward 10 seconds",
                forwardIntent
            )

            return listOf(rewindAction, playPauseAction, forwardAction)
        }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private var mediaSession: MediaSession? = null
    private var isReceiverRegistered = false

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY_PAUSE -> {
                    if (exoPlayer.playbackState == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0L)
                        exoPlayer.play()
                    } else if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    updateNotification()
                }
                ACTION_REWIND_10 -> {
                    val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                    exoPlayer.seekTo(target)
                    updateNotification()
                }
                ACTION_FORWARD_10 -> {
                    val maxDur = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                    val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                    exoPlayer.seekTo(target)
                    updateNotification()
                }
                ACTION_STOP_CLOSE -> {
                    exoPlayer.pause()
                    onStopAndClosePlayer()
                }
            }
        }
    }

    fun initialize() {
        createNotificationChannelIfNeeded()

        runCatching {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val sessionActivityPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            mediaSession = MediaSession.Builder(context, exoPlayer)
                .setId("puretube_session_${UUID.randomUUID()}")
                .setSessionActivity(sessionActivityPendingIntent)
                .build()
        }

        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_REWIND_10)
            addAction(ACTION_FORWARD_10)
            addAction(ACTION_STOP_CLOSE)
        }

        runCatching {
            ContextCompat.registerReceiver(
                context,
                actionReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            isReceiverRegistered = true
        }

        updateNotification()
    }

    fun updateNotification() {
        val nm = notificationManager ?: return
        runCatching {
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            val openAppIntent = PendingIntent.getActivity(
                context,
                200,
                Intent(context, MainActivity::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                flags
            )

            val rewindPending = PendingIntent.getBroadcast(
                context,
                201,
                Intent(ACTION_REWIND_10).setPackage(context.packageName),
                flags
            )
            val playPausePending = PendingIntent.getBroadcast(
                context,
                202,
                Intent(ACTION_PLAY_PAUSE).setPackage(context.packageName),
                flags
            )
            val forwardPending = PendingIntent.getBroadcast(
                context,
                203,
                Intent(ACTION_FORWARD_10).setPackage(context.packageName),
                flags
            )
            val stopClosePending = PendingIntent.getBroadcast(
                context,
                204,
                Intent(ACTION_STOP_CLOSE).setPackage(context.packageName),
                flags
            )

            val isPlaying = exoPlayer.isPlaying

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(videoTitle)
                .setContentText(uploaderName)
                .setSubText("PureTube")
                .setContentIntent(openAppIntent)
                .setDeleteIntent(stopClosePending)
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .addAction(
                    android.R.drawable.ic_media_rew,
                    "-10s",
                    rewindPending
                )
                .addAction(
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isPlaying) "Pause" else "Play",
                    playPausePending
                )
                .addAction(
                    android.R.drawable.ic_media_ff,
                    "+10s",
                    forwardPending
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Close",
                    stopClosePending
                )

            nm.notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun release() {
        if (isReceiverRegistered) {
            runCatching { context.unregisterReceiver(actionReceiver) }
            isReceiverRegistered = false
        }
        runCatching { notificationManager?.cancel(NOTIFICATION_ID) }
        runCatching { mediaSession?.release() }
        mediaSession = null
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PureTube Video Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls for active PureTube video"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
