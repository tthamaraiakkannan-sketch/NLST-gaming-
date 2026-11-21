package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.prefs.YouTubeLiveConfig
import com.example.i18n.LocalNlstStrings
import com.example.media.GameCaptureTarget
import com.example.media.ScreenCaptureState
import com.example.media.StreamOrientation
import com.example.media.StreamQuality
import com.example.ui.components.NeonStatusPill
import com.example.ui.components.NlstNeonCard
import com.example.ui.components.formatDuration
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberAlertSoft
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleSoft
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCrimsonSoft
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StreamStudioScreen(
    captureState: ScreenCaptureState,
    youtubeConfig: YouTubeLiveConfig,
    hasNotificationPermission: Boolean,
    hasMicPermission: Boolean,
    localRoomCount: Int,
    onRequestPermissions: () -> Unit,
    onUpdateConfig: (GameCaptureTarget, StreamQuality, StreamOrientation) -> Unit,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit,
) {
    val strings = LocalNlstStrings.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text(
                        text = strings.streamStudioTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = strings.streamStudioSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            if (!hasNotificationPermission || !hasMicPermission) {
                item {
                    NlstNeonCard(
                        borderColor = AmberAlert.copy(alpha = 0.7f),
                        containerColor = AmberAlertSoft,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = AmberAlert,
                            )
                            Text(
                                text = strings.permissionNeededBanner,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedButton(
                                onClick = onRequestPermissions,
                                border = BorderStroke(1.dp, AmberAlert),
                                modifier = Modifier.testTag("stream_grant_permissions_button"),
                            ) {
                                Text(strings.grantPermission, color = AmberAlert)
                            }
                        }
                    }
                }
            }

            item {
                // Real MediaProjection Telemetry Card
                NlstNeonCard(
                    borderColor = if (captureState.isCapturing) LiveCrimson else ObsidianBorder,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NeonStatusPill(
                            text = if (captureState.isCapturing) {
                                strings.captureStatusActive
                            } else {
                                strings.captureStatusIdle
                            },
                            accent = if (captureState.isCapturing) LiveCrimson else TextSecondary,
                            background = if (captureState.isCapturing) LiveCrimsonSoft else ObsidianElevated,
                            showDot = captureState.isCapturing,
                        )
                        Text(
                            text = formatDuration(captureState.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (captureState.isCapturing) NeonGreen else TextSecondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TelemetryBox(
                            label = strings.fpsLabel,
                            value = if (captureState.isCapturing) "${captureState.measuredFps} FPS" else "--",
                            accent = NeonGreen,
                            modifier = Modifier.weight(1f),
                        )
                        TelemetryBox(
                            label = strings.resolutionLabel,
                            value = if (captureState.capturedWidth > 0) {
                                "${captureState.capturedWidth}x${captureState.capturedHeight}"
                            } else {
                                "${captureState.quality.width}x${captureState.quality.height}"
                            },
                            accent = ElectricPurple,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.rtmpLinkStatusLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Text(
                            text = if (youtubeConfig.streamKey.isNotBlank()) {
                                "Key Configured (${youtubeConfig.streamKey.take(4)}••••)"
                            } else {
                                strings.rtmpNotConfigured
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (youtubeConfig.streamKey.isNotBlank()) NeonGreen else AmberAlert,
                        )
                    }

                    captureState.lastError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiveCrimson,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (captureState.isCapturing) {
                        Button(
                            onClick = onStopCapture,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiveCrimson,
                                contentColor = TextPrimary,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("stop_screen_capture_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(strings.stopScreenCapture, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStartCapture,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = ObsidianBlack,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_screen_capture_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(strings.startScreenCapture, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                // Game Target & Stream Presets
                NlstNeonCard {
                    Text(
                        text = strings.selectGameTarget,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GameCaptureTarget.entries.forEach { target ->
                            val selected = captureState.targetGame == target
                            SelectableOptionRow(
                                label = target.title,
                                selected = selected,
                                accent = NeonGreen,
                                onClick = {
                                    onUpdateConfig(target, captureState.quality, captureState.orientation)
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.qualityLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StreamQuality.entries.forEach { q ->
                            val selected = captureState.quality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) NeonGreenSoft else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (selected) NeonGreen else ObsidianBorder,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable {
                                        onUpdateConfig(captureState.targetGame, q, captureState.orientation)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = q.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) NeonGreen else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.orientationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StreamOrientation.entries.forEach { orient ->
                            val selected = captureState.orientation == orient
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) ElectricPurpleSoft else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (selected) ElectricPurple else ObsidianBorder,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable {
                                        onUpdateConfig(captureState.targetGame, captureState.quality, orient)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = orient.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) ElectricPurple else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Honest Live Audience & Viewer Count Card (No Fake Numbers)
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.5f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = ElectricPurple,
                        )
                        Text(
                            text = strings.audienceCardTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TelemetryBox(
                            label = strings.localRoomAudienceLabel,
                            value = "$localRoomCount",
                            accent = NeonGreen,
                            modifier = Modifier.weight(1f),
                        )
                        TelemetryBox(
                            label = strings.externalViewersLabel,
                            value = "0 (Disconnected)",
                            accent = ElectricPurple,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricPurpleSoft)
                            .padding(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = strings.audienceCardNotice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianElevated)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SelectableOptionRow(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) NeonGreenSoft else ObsidianElevated)
            .border(
                1.dp,
                if (selected) accent else ObsidianBorder,
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) accent else TextPrimary,
        )
        if (selected) {
            NeonStatusPill(text = "ACTIVE", accent = accent, background = ObsidianBlack)
        }
    }
}
