package com.puretube.player.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WatchHomeScreen(
    inputUrl: String,
    isLoading: Boolean,
    errorMessage: String?,
    onUrlChange: (String) -> Unit,
    onWatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimal YouTube Red Shield + White Play Symbol
            PureTubeShieldLogo()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PureTube",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Paste a YouTube link to watch with zero ads",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Material 3 Rounded Search / Link Input Bar
            OutlinedTextField(
                value = inputUrl,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("https://youtube.com/watch?v=...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = "Link"
                    )
                },
                trailingIcon = {
                    if (inputUrl.isNotEmpty()) {
                        IconButton(onClick = { onUrlChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear link"
                            )
                        }
                    }
                },
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        focusManager.clearFocus()
                        onWatchClick(inputUrl)
                    }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Paste from Clipboard Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clipText = clipboard?.primaryClip
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                            ?.trim()
                        if (!clipText.isNullOrEmpty()) {
                            onUrlChange(clipText)
                        }
                    },
                    enabled = !isLoading,
                    label = { Text("Paste from clipboard") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Error Banner
            AnimatedVisibility(visible = !errorMessage.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Watch Button (Material 3 Filled Large Button)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onWatchClick(inputUrl)
                },
                enabled = !isLoading && inputUrl.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading ad-free stream...",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Watch",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Subtle version indicator at the bottom of the screen (vX.X.XXX format)
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.3.006"

        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun PureTubeShieldLogo(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(84.dp)
            .semantics {
                contentDescription = "PureTube Logo"
            }
    ) {
        val scaleX = size.width / 108f
        val scaleY = size.height / 108f

        // Minimal YouTube Red (#FF0000) Shield
        val shieldPath = Path().apply {
            moveTo(54f * scaleX, 14f * scaleY)
            lineTo(83f * scaleX, 24.2f * scaleY)
            cubicTo(
                86.6f * scaleX, 25.5f * scaleY,
                89f * scaleX, 28.9f * scaleY,
                89f * scaleX, 32.7f * scaleY
            )
            lineTo(89f * scaleX, 51.5f * scaleY)
            cubicTo(
                89f * scaleX, 72.2f * scaleY,
                74f * scaleX, 88.6f * scaleY,
                54f * scaleX, 95.5f * scaleY
            )
            cubicTo(
                34f * scaleX, 88.6f * scaleY,
                19f * scaleX, 72.2f * scaleY,
                19f * scaleX, 51.5f * scaleY
            )
            lineTo(19f * scaleX, 32.7f * scaleY)
            cubicTo(
                19f * scaleX, 28.9f * scaleY,
                21.4f * scaleX, 25.5f * scaleY,
                25f * scaleX, 24.2f * scaleY
            )
            close()
        }
        drawPath(path = shieldPath, color = Color(0xFFFF0000))

        // Crisp White (#FFFFFF) Play Symbol Inside Shield
        val playPath = Path().apply {
            moveTo(44.5f * scaleX, 39f * scaleY)
            cubicTo(
                44.5f * scaleX, 37f * scaleY,
                46.7f * scaleX, 35.8f * scaleY,
                48.4f * scaleX, 36.8f * scaleY
            )
            lineTo(68.2f * scaleX, 49f * scaleY)
            cubicTo(
                69.8f * scaleX, 50f * scaleY,
                69.8f * scaleX, 52.4f * scaleY,
                68.2f * scaleX, 53.4f * scaleY
            )
            lineTo(48.4f * scaleX, 65.6f * scaleY)
            cubicTo(
                46.7f * scaleX, 66.6f * scaleY,
                44.5f * scaleX, 65.4f * scaleY,
                44.5f * scaleX, 63.4f * scaleY
            )
            close()
        }
        drawPath(path = playPath, color = Color.White)
    }
}
