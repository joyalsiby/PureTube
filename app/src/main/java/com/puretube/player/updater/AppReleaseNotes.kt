package com.puretube.player.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.puretube.player.ui.screens.PureTubeShieldLogo

data class ReleaseHighlightItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

data class VersionReleaseNote(
    val versionName: String,
    val title: String,
    val summary: String,
    val highlights: List<ReleaseHighlightItem>
)

object AppReleaseNotes {

    val allReleases: List<VersionReleaseNote> = listOf(
        VersionReleaseNote(
            versionName = "0.3.011",
            title = "Media Notification & Closeable PiP, Playlists, Toddler Lock, 2x Hold & More",
            summary = "YouTube-style media notification & control panel playing indicator, closeable PiP with Background Play off by default, toddler-proof screen unlock, playlists, 2x speed hold, loop toggle, clipboard card & history search.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Media Notification Banner & Closeable PiP (Bg Play Off by Default)",
                    description = "Added Android Quick Settings Media Control Panel & Notification Banner with Play/Pause, -10s/+10s, seekbar, and Close. Background Play is now OFF by default—leaving the app opens a closeable PiP window with remote controls that pauses immediately when closed."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.Lock,
                    title = "Toddler-Proof Kids Screen Unlock (Hold 2s or Parent Math Gate)",
                    description = "Upgraded Screen Lock so toddlers can't unlock by accident! Defaults to Hold for 2 Seconds (with a live progress bar), or choose Parent Math Challenge (multiplication gate) in Settings."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.PlaylistPlay,
                    title = "YouTube Playlist Link Support & Queue Sheet",
                    description = "Share or paste any YouTube playlist link to watch its videos sequentially with Next/Previous controls and an in-player Playlist Queue sheet—still with zero algorithmic recommendations."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.FastForward,
                    title = "Long-Press for 2x Speed & Single-Video Loop Toggle",
                    description = "Press and hold anywhere on the video to temporarily play at 2.0x speed, and toggle Loop Video from the player chips or Settings (⚙️) sheet."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.ContentPaste,
                    title = "Smart Clipboard Card & Watch History Search/Filter",
                    description = "Auto-detects copied YouTube links on the Watch screen with a 1-tap Play Now card, plus search & filter chips (All, In Progress, Completed) in History."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.010",
            title = "Media Notification & Closeable PiP, Playlists, Toddler Lock, 2x Hold & More",
            summary = "Initial release of media notification, closeable PiP, toddler lock, playlist links, 2x hold, loop toggle, clipboard card, and history search.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Media Controls & Toddler Lock",
                    description = "All v0.3.010 core features integrated."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.009",
            title = "Welcome Onboarding, In-App Release Notes & Clean Player UI",
            summary = "A smoother first-install setup experience, automatic What's New release notes after every update, and a cleaner player UI.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.RocketLaunch,
                    title = "First-Install Interactive Onboarding",
                    description = "New 4-step welcome tour after first install explaining Share-to-PureTube, default link handling, and instant Kids & Focus Mode setup."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.NewReleases,
                    title = "In-App Release Notes After Every Update",
                    description = "Automatically displays What's New release notes right inside PureTube whenever you update to a new version (and anytime from Settings)."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.FitScreen,
                    title = "Cleaner Player View Mode Indicator",
                    description = "Removed the distracting red fill color from the fullscreen Fit / Zoomed-to-fill view indicator pill and top-bar icons for a distraction-free player."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.008",
            title = "Production Release Builds, Official Shield SVG & Creator Profile",
            summary = "Optimized production release APK builds (~15.5 MB), rich GitHub release notes, and developer portfolio links.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.Verified,
                    title = "Production Release APK Builds",
                    description = "Switched permanently to signed production release builds (PureTube-vX.X.XXX.apk) for smaller APK size and fewer Play Protect prompts."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.Security,
                    title = "Official PureTube Red Shield Identity",
                    description = "Added official standalone Red Shield + Play SVG branding and Joyal Siby creator profile links in Settings."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.007",
            title = "1-Tap In-App Updater, Settings Tab & Kids Parental Controls",
            summary = "Built-in GitHub release updater, dedicated Settings tab, Kids Screen Lock on startup, and F-Droid compliance.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.SystemUpdate,
                    title = "Built-In 1-Tap APK Updater",
                    description = "Automatically checks GitHub Releases and downloads + installs updates directly inside PureTube."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.ChildCare,
                    title = "Kids & Focus Parental Controls",
                    description = "Added Auto Kids Screen Lock on startup, Auto-Fullscreen on play, Minimal Distraction-Free Watch Page, and Default Link Handler shortcut."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.006",
            title = "Video Chapters, Swipe Gestures, Background Play & PiP",
            summary = "Clickable timestamps, chapter markers on seekbar, fullscreen brightness/volume gestures, and Picture-in-Picture.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.FormatListBulleted,
                    title = "Video Chapters & Clickable Timestamps",
                    description = "Chapter markers on the seekbar, Chapters bottom sheet, and clickable 01:23 timestamps in descriptions."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.TouchApp,
                    title = "Fullscreen Brightness & Volume Swipes",
                    description = "Swipe vertically on the left for Brightness (0%–100%) or on the right for Volume (0%–100%)."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.PictureInPictureAlt,
                    title = "Background Audio & Picture-in-Picture (PiP)",
                    description = "Keep audio playing with the screen off or float the video in a 16:9 PiP window when pressing Home."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.005",
            title = "Battery & Thermal Engineering + Expandable Description",
            summary = "Zero UI recompositions while controls are hidden, hardware H.264 priority, and expandable portrait descriptions.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.BatteryChargingFull,
                    title = "Zero-Recomposition Playback & Smart Wake Lock",
                    description = "Eliminated background UI polling when player controls are hidden and prioritized hardware AVC1 (H.264) decoding."
                )
            )
        ),
        VersionReleaseNote(
            versionName = "0.3.004",
            title = "Zero-Stall RAM Preloader, Multi-Audio & Closed Captions",
            summary = "Parallel RAM stream preloading (<1.2s startup), Original audio track default, CC subtitles, and pinch-to-zoom.",
            highlights = listOf(
                ReleaseHighlightItem(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "Parallel RAM Stream Preloader",
                    description = "Prefetches initial video and audio segments into RAM with anti-throttling HTTP/2 range requests for instant startup."
                ),
                ReleaseHighlightItem(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Multi-Audio Track Switcher & Closed Captions (CC)",
                    description = "Always defaults to the Original audio track first and supports WebVTT/TTML subtitles."
                )
            )
        )
    )

    fun getReleaseNoteForVersion(versionName: String): VersionReleaseNote {
        val clean = versionName.removePrefix("v").removePrefix("V").trim()
        return allReleases.firstOrNull { it.versionName == clean }
            ?: allReleases.first().copy(versionName = clean)
    }
}

@Composable
fun WhatsNewReleaseNotesDialog(
    currentVersion: String,
    cachedRemoteNotes: String? = null,
    onDismiss: () -> Unit
) {
    val currentNote = remember(currentVersion) {
        AppReleaseNotes.getReleaseNoteForVersion(currentVersion)
    }
    val olderReleases = remember(currentVersion) {
        val clean = currentVersion.removePrefix("v").removePrefix("V").trim()
        AppReleaseNotes.allReleases.filter { it.versionName != clean }
    }
    var showPreviousReleases by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with PureTube Shield Logo + Version Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PureTubeShieldLogo(modifier = Modifier.size(46.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NewReleases,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WHAT'S NEW · v${currentNote.versionName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Release Notes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentNote.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Highlights + Optional Remote Notes + Previous Releases
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    currentNote.highlights.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (!cachedRemoteNotes.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Additional Release Details",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                MarkdownReleaseNotesView(markdown = cachedRemoteNotes)
                            }
                        }
                    }

                    // Expandable Previous Releases Section
                    if (olderReleases.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPreviousReleases = !showPreviousReleases },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showPreviousReleases) {
                                        "Hide previous release notes"
                                    } else {
                                        "View previous updates (${olderReleases.size} releases)"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (showPreviousReleases) {
                                        Icons.Rounded.ExpandLess
                                    } else {
                                        Icons.Rounded.ExpandMore
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(visible = showPreviousReleases) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                olderReleases.forEach { oldRelease ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Text(
                                                text = "v${oldRelease.versionName} — ${oldRelease.title}",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            oldRelease.highlights.forEachIndexed { idx, hl ->
                                                if (idx > 0) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 6.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                    )
                                                }
                                                Text(
                                                    text = "• ${hl.title}: ${hl.description}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Got It, Let's Watch",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Renders GitHub Release Notes Markdown & HTML into structured Material 3 Compose UI
 * (Headings, Dividers, Numbered/Bulleted Lists, Bold spans, Monospace inline code, and Clickable Links).
 */
@Composable
fun MarkdownReleaseNotesView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val cleanedLines = remember(markdown) {
        normalizeReleaseMarkdownLines(markdown)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        cleanedLines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            val leadingSpaces = rawLine.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
            val isIndentedSubItem = leadingSpaces >= 2

            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                }

                trimmed.startsWith("### ") || trimmed.startsWith("## ") || trimmed.startsWith("# ") -> {
                    val headingText = trimmed.replace(Regex("""^#{1,4}\s*"""), "").trim()
                    val annotatedHeading = remember(headingText, onSurfaceColor, primaryColor, codeBgColor) {
                        buildMarkdownAnnotatedString(
                            text = headingText,
                            baseColor = primaryColor,
                            boldColor = primaryColor,
                            linkColor = primaryColor,
                            codeColor = primaryColor,
                            codeBackground = codeBgColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = annotatedHeading,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        ),
                        color = primaryColor
                    )
                }

                Regex("""^\d+\.\s+.*""").matches(trimmed) -> {
                    val numberPrefix = trimmed.substringBefore(".").trim() + "."
                    val itemContent = trimmed.substringAfter(".").trim()
                    val annotatedItem = remember(itemContent, onSurfaceColor, onSurfaceVariantColor, primaryColor, codeBgColor) {
                        buildMarkdownAnnotatedString(
                            text = itemContent,
                            baseColor = onSurfaceVariantColor,
                            boldColor = onSurfaceColor,
                            linkColor = primaryColor,
                            codeColor = onSurfaceColor,
                            codeBackground = codeBgColor
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (isIndentedSubItem) 12.dp else 0.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = numberPrefix,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp
                            ),
                            color = primaryColor,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            text = annotatedItem,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                            color = onSurfaceVariantColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val bulletContent = trimmed.drop(2).trim()
                    val annotatedBullet = remember(bulletContent, onSurfaceColor, onSurfaceVariantColor, primaryColor, codeBgColor) {
                        buildMarkdownAnnotatedString(
                            text = bulletContent,
                            baseColor = onSurfaceVariantColor,
                            boldColor = onSurfaceColor,
                            linkColor = primaryColor,
                            codeColor = onSurfaceColor,
                            codeBackground = codeBgColor
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (isIndentedSubItem) 14.dp else 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp
                            ),
                            color = if (isIndentedSubItem) onSurfaceVariantColor else primaryColor,
                            modifier = Modifier.width(14.dp)
                        )
                        Text(
                            text = annotatedBullet,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                            color = onSurfaceVariantColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                else -> {
                    val annotatedParagraph = remember(trimmed, onSurfaceColor, onSurfaceVariantColor, primaryColor, codeBgColor) {
                        buildMarkdownAnnotatedString(
                            text = trimmed,
                            baseColor = onSurfaceVariantColor,
                            boldColor = onSurfaceColor,
                            linkColor = primaryColor,
                            codeColor = onSurfaceColor,
                            codeBackground = codeBgColor
                        )
                    }
                    Text(
                        text = annotatedParagraph,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = onSurfaceVariantColor
                    )
                }
            }
        }
    }
}

private fun normalizeReleaseMarkdownLines(raw: String): List<String> {
    // 1. Strip HTML <div...>...</div> header banner blocks or convert inner tags cleanly
    val withoutHtmlBanner = raw
        .replace("\r\n", "\n")
        .replace(Regex("""<div[^>]*>[\s\S]*?</div>""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""<img[^>]*>""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""<h[1-4][^>]*>(.*?)</h[1-4]>""", RegexOption.IGNORE_CASE), "### $1")
        .replace(Regex("""<b>(.*?)</b>""", RegexOption.IGNORE_CASE), "**$1**")
        .replace(Regex("""<strong>(.*?)</strong>""", RegexOption.IGNORE_CASE), "**$1**")
        .replace(Regex("""<code>(.*?)</code>""", RegexOption.IGNORE_CASE), "`$1`")
        .replace(Regex("""</?p[^>]*>""", RegexOption.IGNORE_CASE), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    val result = mutableListOf<String>()
    var previousBlank = true
    var previousDivider = false

    for (line in withoutHtmlBanner.lines()) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            if (!previousBlank) {
                result.add("")
                previousBlank = true
            }
            continue
        }
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            if (result.isEmpty() || previousDivider) continue
            result.add("---")
            previousDivider = true
            previousBlank = false
            continue
        }
        previousBlank = false
        previousDivider = false
        result.add(line)
    }

    while (result.lastOrNull()?.let { it.isBlank() || it == "---" } == true) {
        result.removeAt(result.lastIndex)
    }
    return result
}

private val InlineMarkdownTokenRegex = Regex(
    """(\[`[^`\]]+`\]\([^)]+\)|\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|`[^`]+`)"""
)

private fun buildMarkdownAnnotatedString(
    text: String,
    baseColor: Color,
    boldColor: Color,
    linkColor: Color,
    codeColor: Color,
    codeBackground: Color
): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in InlineMarkdownTokenRegex.findAll(text)) {
            val start = match.range.first
            val endExclusive = match.range.last + 1
            if (start > lastIndex) {
                withStyle(SpanStyle(color = baseColor)) {
                    append(text.substring(lastIndex, start))
                }
            }

            val token = match.value
            when {
                // Markdown link: [label](url) or [`codeLabel`](url)
                token.startsWith("[") && token.contains("](") && token.endsWith(")") -> {
                    val rawLabel = token.substringAfter("[").substringBefore("](")
                    val cleanLabel = rawLabel.removeSurrounding("`")
                    val isCodeLabel = rawLabel.startsWith("`") && rawLabel.endsWith("`")
                    val url = token.substringAfter("](").removeSuffix(")").trim()

                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        val link = LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = if (isCodeLabel) FontFamily.Monospace else null
                                )
                            )
                        )
                        withLink(link) {
                            append(cleanLabel)
                        }
                    } else {
                        // Local file:/// reference from PROJECT_LOG.md -> render clean filename pill without URL
                        withStyle(
                            SpanStyle(
                                color = codeColor,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = if (isCodeLabel) FontFamily.Monospace else null,
                                background = if (isCodeLabel) codeBackground else Color.Unspecified
                            )
                        ) {
                            append(cleanLabel)
                        }
                    }
                }

                // Bold: **text**
                token.startsWith("**") && token.endsWith("**") && token.length > 4 -> {
                    val inner = token.removeSurrounding("**")
                    // Support nested `code` or [link](url) inside **bold**
                    val innerAnnotated = buildMarkdownAnnotatedString(
                        text = inner,
                        baseColor = boldColor,
                        boldColor = boldColor,
                        linkColor = linkColor,
                        codeColor = codeColor,
                        codeBackground = codeBackground
                    )
                    withStyle(SpanStyle(color = boldColor, fontWeight = FontWeight.Bold)) {
                        append(innerAnnotated)
                    }
                }

                // Inline code: `code`
                token.startsWith("`") && token.endsWith("`") && token.length > 2 -> {
                    val codeText = token.removeSurrounding("`")
                    withStyle(
                        SpanStyle(
                            color = codeColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            background = codeBackground
                        )
                    ) {
                        append(" $codeText ")
                    }
                }

                else -> {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(token)
                    }
                }
            }
            lastIndex = endExclusive
        }

        if (lastIndex < text.length) {
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(lastIndex))
            }
        }
    }
}
