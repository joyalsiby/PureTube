package com.puretube.player.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.puretube.player.data.AppSettings
import com.puretube.player.data.AppThemeMode
import com.puretube.player.data.KidsUnlockMode
import com.puretube.player.updater.AppUpdateManager
import com.puretube.player.updater.ReleaseUpdateInfo
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    historyCount: Int,
    availableUpdate: ReleaseUpdateInfo?,
    isCheckingForUpdate: Boolean,
    isDownloadingUpdate: Boolean,
    updateDownloadProgress: Float,
    updateStatusMessage: String?,
    onCheckForUpdate: () -> Unit,
    onDownloadAndInstallUpdate: () -> Unit,
    onUpdateThemeMode: (AppThemeMode) -> Unit,
    onUpdateDynamicColor: (Boolean) -> Unit,
    onUpdateDefaultQuality: (Int) -> Unit,
    onUpdateDefaultSpeed: (Float) -> Unit,
    onUpdateBackgroundPlay: (Boolean) -> Unit,
    onUpdateAutoPip: (Boolean) -> Unit,
    onUpdateAutoStartKidsLocked: (Boolean) -> Unit,
    onUpdateKidsUnlockMode: (KidsUnlockMode) -> Unit = {},
    onUpdateAutoFullscreen: (Boolean) -> Unit,
    onUpdateHideDescription: (Boolean) -> Unit,
    onUpdateSaveHistory: (Boolean) -> Unit,
    onUpdateAutoCheckUpdates: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onOpenReleaseNotes: () -> Unit = {},
    onReopenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentVersion = remember { AppUpdateManager.getCurrentVersionName(context) }

    var showThemeSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showUnlockModeSheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear watch history?") },
            text = { Text("All saved videos and playback positions on this device will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "Watch history cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear all")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header Row directly below status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                onClick = onOpenReleaseNotes,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "v$currentVersion · What's New",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO CARD: Kids & Focus Mode Philosophy + App Identity
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PureTubeShieldLogo(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PureTube — Focus & Kids Player",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "100% Ad-Free · No Search · No Recommendations",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Designed for parents & mindful viewers. Keep the official YouTube app locked and share or paste only the exact video link you want your kids to watch—with zero ads, zero Shorts rabbit holes, and zero algorithmic distractions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. IN-APP UPDATER CARD
            item {
                SettingsSectionCard(title = "App Updates") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (availableUpdate != null) {
                                    Icons.Rounded.NewReleases
                                } else {
                                    Icons.Rounded.SystemUpdate
                                },
                                contentDescription = null,
                                tint = if (availableUpdate != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (availableUpdate != null) {
                                        "Update Available: v${availableUpdate.versionName}"
                                    } else {
                                        "Installed Version: v$currentVersion"
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = updateStatusMessage
                                        ?: "Directly check, download & install new releases in-app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isDownloadingUpdate) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val pct = (updateDownloadProgress * 100f).roundToInt().coerceIn(0, 100)
                            LinearProgressIndicator(
                                progress = { updateDownloadProgress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Downloading APK... $pct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCheckForUpdate,
                                enabled = !isCheckingForUpdate && !isDownloadingUpdate,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isCheckingForUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking...")
                                } else {
                                    Text("Check for Updates")
                                }
                            }

                            if (availableUpdate != null) {
                                Button(
                                    onClick = onDownloadAndInstallUpdate,
                                    enabled = !isDownloadingUpdate,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Update Now")
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsSwitchItem(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Auto-check updates on startup",
                        subtitle = "Notify automatically when a new PureTube version is released on GitHub",
                        checked = settings.autoCheckUpdates,
                        onCheckedChange = onUpdateAutoCheckUpdates
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.NewReleases,
                        title = "What's New in v$currentVersion (Release Notes)",
                        subtitle = "View in-app release notes & changelog history",
                        onClick = onOpenReleaseNotes
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.RocketLaunch,
                        title = "Replay Welcome Onboarding",
                        subtitle = "Take the PureTube quick-start tour & Kids Mode setup again",
                        onClick = onReopenOnboarding
                    )
                }
            }

            // 3. KIDS & FOCUS MODE (PARENTAL CONTROLS)
            item {
                SettingsSectionCard(title = "Kids & Focus Mode (Parental Controls)") {
                    SettingsSwitchItem(
                        icon = Icons.Rounded.Lock,
                        title = "Start videos with Kids Screen Lock",
                        subtitle = "Automatically lock player controls when playback starts to prevent accidental toddler touches",
                        checked = settings.autoStartKidsLocked,
                        onCheckedChange = onUpdateAutoStartKidsLocked
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Lock,
                        title = "Kids Screen Unlock Method",
                        subtitle = settings.kidsUnlockMode.label,
                        onClick = { showUnlockModeSheet = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsSwitchItem(
                        icon = Icons.Rounded.Fullscreen,
                        title = "Auto-enter Fullscreen on play",
                        subtitle = "Open videos directly in full-screen landscape mode",
                        checked = settings.autoFullscreenOnPlay,
                        onCheckedChange = onUpdateAutoFullscreen
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsSwitchItem(
                        icon = Icons.Rounded.VisibilityOff,
                        title = "Minimal Distraction-Free Watch Page",
                        subtitle = "Hide video description & view counts below the player",
                        checked = settings.hideVideoDescription,
                        onCheckedChange = onUpdateHideDescription
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.ChildCare,
                        title = "Set PureTube as Default Link Handler",
                        subtitle = "Open Android App Info -> 'Open by default' to launch YouTube links directly in PureTube",
                        onClick = {
                            runCatching {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    Intent(
                                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                } else {
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                }
                                context.startActivity(intent)
                            }.onFailure {
                                val fallback = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(fallback)
                            }
                        }
                    )
                }
            }

            // 4. PLAYBACK & STREAM PREFERENCES
            item {
                SettingsSectionCard(title = "Playback & Quality") {
                    val qualityLabel = when (settings.defaultQualityHeight) {
                        2160 -> "4K (2160p)"
                        1440 -> "QHD (1440p)"
                        1080 -> "Full HD (1080p - Recommended)"
                        720 -> "HD (720p - Battery Saver)"
                        480 -> "SD (480p - Data Saver)"
                        360 -> "Low (360p)"
                        else -> "${settings.defaultQualityHeight}p"
                    }

                    SettingsActionItem(
                        icon = Icons.Rounded.HighQuality,
                        title = "Default Video Resolution",
                        subtitle = qualityLabel,
                        onClick = { showQualitySheet = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Speed,
                        title = "Default Playback Speed",
                        subtitle = if (settings.defaultPlaybackSpeed == 1.0f) "Normal (1.0x)" else "${settings.defaultPlaybackSpeed}x",
                        onClick = { showSpeedSheet = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsSwitchItem(
                        icon = Icons.Rounded.Headphones,
                        title = "Background Audio Playback",
                        subtitle = "Off by default · Keep audio playing when screen is off or PiP is closed",
                        checked = settings.backgroundPlayEnabled,
                        onCheckedChange = onUpdateBackgroundPlay
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsSwitchItem(
                        icon = Icons.Rounded.PictureInPictureAlt,
                        title = "Auto Picture-in-Picture (PiP)",
                        subtitle = "Float video in a closeable window when leaving app while playing",
                        checked = settings.autoPipOnHome,
                        onCheckedChange = onUpdateAutoPip
                    )
                }
            }

            // 5. APPEARANCE (MATERIAL 3)
            item {
                SettingsSectionCard(title = "Appearance (Material You)") {
                    SettingsActionItem(
                        icon = Icons.Rounded.DarkMode,
                        title = "App Theme",
                        subtitle = settings.themeMode.label,
                        onClick = { showThemeSheet = true }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingsSwitchItem(
                            icon = Icons.Rounded.Palette,
                            title = "Material You Dynamic Color",
                            subtitle = "Adapt app colors to your Android 12+ wallpaper palette",
                            checked = settings.dynamicColorEnabled,
                            onCheckedChange = onUpdateDynamicColor
                        )
                    }
                }
            }

            // 6. PRIVACY & LOCAL HISTORY
            item {
                SettingsSectionCard(title = "Privacy & Watch History") {
                    SettingsSwitchItem(
                        icon = Icons.Rounded.History,
                        title = "Save Local Watch History",
                        subtitle = "Remember watched videos and resume positions on this device",
                        checked = settings.saveWatchHistory,
                        onCheckedChange = onUpdateSaveHistory
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.DeleteSweep,
                        title = "Clear Watch History",
                        subtitle = "$historyCount saved video(s) stored locally",
                        onClick = {
                            if (historyCount > 0) {
                                showClearHistoryDialog = true
                            } else {
                                Toast.makeText(context, "Watch history is already empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // 7. ABOUT & LINKS
            item {
                SettingsSectionCard(title = "About & Developer") {
                    SettingsActionItem(
                        icon = Icons.Rounded.Person,
                        title = "Designed & Built by Joyal Siby",
                        subtitle = "Product Designer & Creative Developer · github.com/joyalsiby",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUpdateManager.AUTHOR_GITHUB_URL))
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Language,
                        title = "Developer Website & Portfolio",
                        subtitle = "joyalsiby.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUpdateManager.AUTHOR_WEBSITE_URL))
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Code,
                        title = "GitHub Repository",
                        subtitle = "github.com/${AppUpdateManager.GITHUB_OWNER}/${AppUpdateManager.GITHUB_REPO}",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUpdateManager.GITHUB_REPO_URL))
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.OpenInBrowser,
                        title = "All Releases & Changelog",
                        subtitle = "View release notes and download APKs from GitHub",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUpdateManager.GITHUB_RELEASES_URL))
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.BugReport,
                        title = "Report an Issue / Request Feature",
                        subtitle = "Open an issue on GitHub",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUpdateManager.GITHUB_ISSUES_URL))
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Share,
                        title = "Share PureTube with Parents & Friends",
                        subtitle = "Send the PureTube download link",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "PureTube — Ad-Free Focus & Kids YouTube Player")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out PureTube — a 100% ad-free, distraction-free YouTube video player for kids & focus with no search or recommendations: ${AppUpdateManager.GITHUB_RELEASES_URL}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PureTube"))
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SettingsActionItem(
                        icon = Icons.Rounded.Info,
                        title = "Open-Source Engine",
                        subtitle = "Built with Kotlin, Jetpack Compose (M3), AndroidX Media3 ExoPlayer & NewPipeExtractor",
                        showChevron = false,
                        onClick = {}
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Bottom Sheet: Theme Mode
    if (showThemeSheet) {
        ModalBottomSheet(onDismissRequest = { showThemeSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Choose App Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                AppThemeMode.entries.forEach { mode ->
                    val isSelected = settings.themeMode == mode
                    ListItem(
                        headlineContent = {
                            Text(
                                text = mode.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onUpdateThemeMode(mode)
                            showThemeSheet = false
                        }
                    )
                }
            }
        }
    }

    // Bottom Sheet: Default Quality
    if (showQualitySheet) {
        val qualities = listOf(
            2160 to "4K (2160p)",
            1440 to "QHD (1440p)",
            1080 to "Full HD (1080p - Recommended)",
            720 to "HD (720p - Battery Saver)",
            480 to "SD (480p - Data Saver)",
            360 to "Low (360p)"
        )
        ModalBottomSheet(onDismissRequest = { showQualitySheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Default Startup Video Quality",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                qualities.forEach { (height, label) ->
                    val isSelected = settings.defaultQualityHeight == height
                    ListItem(
                        headlineContent = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onUpdateDefaultQuality(height)
                            showQualitySheet = false
                        }
                    )
                }
            }
        }
    }

    // Bottom Sheet: Default Playback Speed
    if (showSpeedSheet) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        ModalBottomSheet(onDismissRequest = { showSpeedSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Default Playback Speed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                speeds.forEach { speed ->
                    val isSelected = settings.defaultPlaybackSpeed == speed
                    ListItem(
                        headlineContent = {
                            Text(
                                text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onUpdateDefaultSpeed(speed)
                            showSpeedSheet = false
                        }
                    )
                }
            }
        }
    }

    // Bottom Sheet: Kids Screen Unlock Method
    if (showUnlockModeSheet) {
        ModalBottomSheet(onDismissRequest = { showUnlockModeSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Kids Screen Unlock Method",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                KidsUnlockMode.entries.forEach { mode ->
                    val isSelected = settings.kidsUnlockMode == mode
                    ListItem(
                        headlineContent = {
                            Text(
                                text = mode.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = {
                            Text(text = mode.description)
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onUpdateKidsUnlockMode(mode)
                            showUnlockModeSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = showChevron, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
