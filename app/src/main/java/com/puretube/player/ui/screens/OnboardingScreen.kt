package com.puretube.player.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.puretube.player.data.AppSettings
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    settings: AppSettings,
    onUpdateAutoStartKidsLocked: (Boolean) -> Unit,
    onUpdateAutoFullscreen: (Boolean) -> Unit,
    onUpdateHideDescription: (Boolean) -> Unit,
    onUpdateBackgroundPlay: (Boolean) -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar: Step Indicator Badge + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Step ${pagerState.currentPage + 1} of 4",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (pagerState.currentPage < 3) {
                    TextButton(onClick = onFinishOnboarding) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomePage()
                    1 -> OnboardingHowToWatchPage()
                    2 -> OnboardingKidsAndFocusSetupPage(
                        settings = settings,
                        onUpdateAutoStartKidsLocked = onUpdateAutoStartKidsLocked,
                        onUpdateAutoFullscreen = onUpdateAutoFullscreen,
                        onUpdateHideDescription = onUpdateHideDescription,
                        onUpdateBackgroundPlay = onUpdateBackgroundPlay
                    )
                    3 -> OnboardingGesturesAndControlsPage()
                }
            }

            // Bottom Bar: Animated Dots + Back / Next / Get Started Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            label = "dotWidth"
                        )
                        val color by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            label = "dotColor"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 3) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinishOnboarding()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        if (pagerState.currentPage < 3) {
                            Text(
                                text = "Next",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Watching",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingWelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PureTubeShieldLogo(modifier = Modifier.size(80.dp))

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "100% Ad-Free · Zero Distractions",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to PureTube",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Keep the official YouTube app locked. PureTube plays only the exact video link you share or paste—with zero ads, no search bar, no Shorts, and no algorithmic rabbit holes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.VerifiedUser,
            title = "100% Ad-Free Stream Playback",
            description = "Watch any video without pre-roll, mid-roll, or sponsored ad interruptions."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.Block,
            title = "No Search, Shorts, or Recommendations",
            description = "Built for kids, parents & deep focus. When the video ends, it simply stops."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.HighQuality,
            title = "Up to 4K DASH & Original Audio",
            description = "Crystal-clear 4K/1080p playback that always defaults to the Original audio track."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.PictureInPictureAlt,
            title = "Background Play & Picture-in-Picture",
            description = "Listen with your screen off or float the video in a 16:9 PiP window for free."
        )
    }
}

@Composable
private fun OnboardingHowToWatchPage() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "2 Easy Ways to Watch",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Because PureTube has no distracting search feed, you control exactly which video plays:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.Share,
            title = "1. Share Directly to PureTube",
            description = "In WhatsApp, your browser, or a parent's device, tap Share on any YouTube link and pick PureTube for instant playback."
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.ContentPaste,
            title = "2. Paste Link on the Watch Screen",
            description = "Copy any YouTube video, Short, or youtu.be link, open PureTube, and tap 'Paste from clipboard' -> Watch."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ChildCare,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Optional: Open YouTube Links by Default",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You can set PureTube as Android's default handler for youtube.com & youtu.be links so tapped links open straight in PureTube.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure Default Link Handler")
                }
            }
        }
    }
}

@Composable
private fun OnboardingKidsAndFocusSetupPage(
    settings: AppSettings,
    onUpdateAutoStartKidsLocked: (Boolean) -> Unit,
    onUpdateAutoFullscreen: (Boolean) -> Unit,
    onUpdateHideDescription: (Boolean) -> Unit,
    onUpdateBackgroundPlay: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ChildCare,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kids & Focus Quick Setup",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Customize how PureTube behaves now (you can change these anytime in the Settings tab):",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OnboardingToggleRow(
                    icon = Icons.Rounded.Lock,
                    title = "Start with Kids Screen Lock",
                    subtitle = "Lock player touch controls automatically when playback starts so toddlers can't exit or scrub",
                    checked = settings.autoStartKidsLocked,
                    onCheckedChange = onUpdateAutoStartKidsLocked
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                OnboardingToggleRow(
                    icon = Icons.Rounded.Fullscreen,
                    title = "Auto-Enter Fullscreen on Play",
                    subtitle = "Launch videos directly in landscape fullscreen mode",
                    checked = settings.autoFullscreenOnPlay,
                    onCheckedChange = onUpdateAutoFullscreen
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                OnboardingToggleRow(
                    icon = Icons.Rounded.VisibilityOff,
                    title = "Minimal Watch Page",
                    subtitle = "Hide video description & view counts below the portrait player",
                    checked = settings.hideVideoDescription,
                    onCheckedChange = onUpdateHideDescription
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                OnboardingToggleRow(
                    icon = Icons.Rounded.Headphones,
                    title = "Background Audio Playback",
                    subtitle = "Keep audio playing when the screen is turned off or app is minimized",
                    checked = settings.backgroundPlayEnabled,
                    onCheckedChange = onUpdateBackgroundPlay
                )
            }
        }
    }
}

@Composable
private fun OnboardingGesturesAndControlsPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Smart Player Gestures",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PureTube packs an authentic Material 3 player with powerful touch gestures and built-in updates:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.BrightnessHigh,
            title = "Fullscreen Brightness & Volume Swipes",
            description = "In fullscreen, swipe vertically on the left half for Brightness (0%–100%) or right half for Volume (0%–100%)."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.FitScreen,
            title = "Pinch-to-Zoom & Double-Tap Seek",
            description = "Pinch with two fingers for 'Zoomed to fill' or 'Original' fit, and double-tap left/right to seek ±10 seconds."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.GraphicEq,
            title = "Multi-Audio, Captions (CC) & Chapters",
            description = "Switch between Original and Dubbed audio tracks, toggle subtitles, or jump across chapters on the seekbar."
        )
        Spacer(modifier = Modifier.height(10.dp))
        OnboardingFeatureCard(
            icon = Icons.Rounded.SystemUpdate,
            title = "1-Tap Updates & In-App Release Notes",
            description = "PureTube notifies you when a new version is out, installs it in 1 tap, and shows What's New after each update."
        )
    }
}

@Composable
private fun OnboardingFeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OnboardingToggleRow(
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
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
