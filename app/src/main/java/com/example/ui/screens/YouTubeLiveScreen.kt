package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.prefs.YouTubeLiveConfig
import com.example.data.remote.RtmpProbeResult
import com.example.i18n.LocalNlstStrings
import com.example.media.FreeFireLauncher
import com.example.ui.YouTubeMetadataUiState
import com.example.ui.components.NeonStatusPill
import com.example.ui.components.NlstNeonCard
import com.example.ui.theme.AmberAlert
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
fun YouTubeLiveScreen(
    config: YouTubeLiveConfig,
    rtmpProbeResult: RtmpProbeResult,
    metadataState: YouTubeMetadataUiState,
    onSaveConfig: (rtmpUrl: String, streamKey: String, broadcastUrl: String) -> Unit,
    onTestRtmpHandshake: (rtmpUrl: String, streamKey: String) -> Unit,
    onFetchMetadata: (broadcastUrl: String) -> Unit,
) {
    val strings = LocalNlstStrings.current
    val context = LocalContext.current

    var rtmpUrl by rememberSaveable { mutableStateOf(config.rtmpServerUrl) }
    var streamKey by rememberSaveable { mutableStateOf(config.streamKey) }
    var broadcastUrl by rememberSaveable { mutableStateOf(config.broadcastUrl) }
    var showKey by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(config) {
        if (rtmpUrl.isBlank()) rtmpUrl = config.rtmpServerUrl
        if (streamKey.isBlank() && config.streamKey.isNotBlank()) streamKey = config.streamKey
        if (broadcastUrl.isBlank() && config.broadcastUrl.isNotBlank()) broadcastUrl = config.broadcastUrl
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonGreen,
        unfocusedBorderColor = ObsidianBorder,
        focusedLabelColor = NeonGreen,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = NeonGreen,
        focusedContainerColor = ObsidianElevated,
        unfocusedContainerColor = ObsidianElevated,
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
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
                        text = strings.youtubeTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = strings.youtubeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            item {
                // RTMP Server Config & Real Socket Handshake Probe
                NlstNeonCard(borderColor = LiveCrimson.copy(alpha = 0.55f)) {
                    OutlinedTextField(
                        value = rtmpUrl,
                        onValueChange = { rtmpUrl = it },
                        label = { Text(strings.rtmpServerLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_rtmp_url_input"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = streamKey,
                        onValueChange = { streamKey = it },
                        label = { Text(strings.streamKeyLabel) },
                        singleLine = true,
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle key visibility",
                                    tint = TextSecondary,
                                )
                            }
                        },
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_stream_key_input"),
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { onSaveConfig(rtmpUrl, streamKey, broadcastUrl) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = ObsidianBlack,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_rtmp_config_button"),
                        ) {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.saveRtmpConfig, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onTestRtmpHandshake(rtmpUrl, streamKey) },
                            border = BorderStroke(1.dp, ElectricPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_rtmp_handshake_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NetworkCheck,
                                contentDescription = null,
                                tint = ElectricPurple,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.testRtmpHandshake, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    when (rtmpProbeResult) {
                        RtmpProbeResult.Idle -> Unit
                        RtmpProbeResult.Probing -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NeonGreen,
                                    strokeWidth = 2.dp,
                                )
                                Text(strings.rtmpTesting, color = TextSecondary)
                            }
                        }

                        is RtmpProbeResult.Success -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonGreenSoft)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonGreen)
                                Text(
                                    text = "RTMP C0/S0 Handshake OK: ${rtmpProbeResult.host}:${rtmpProbeResult.port} (${rtmpProbeResult.latencyMs} ms) • Key: ${if (rtmpProbeResult.hasStreamKey) "Set" else "Missing"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeonGreen,
                                )
                            }
                        }

                        is RtmpProbeResult.Failure -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LiveCrimsonSoft)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = LiveCrimson)
                                Text(
                                    text = rtmpProbeResult.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LiveCrimson,
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Real YouTube oEmbed Stream Metadata Verifier
                NlstNeonCard(borderColor = ObsidianBorder) {
                    OutlinedTextField(
                        value = broadcastUrl,
                        onValueChange = { broadcastUrl = it },
                        label = { Text(strings.youtubeUrlLabel) },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_broadcast_url_input"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onFetchMetadata(broadcastUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple,
                            contentColor = TextPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fetch_youtube_metadata_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.PlayCircleFilled, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(strings.fetchYoutubeInfo)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    when (metadataState) {
                        YouTubeMetadataUiState.Idle -> Unit
                        YouTubeMetadataUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ElectricPurple,
                                strokeWidth = 2.dp,
                            )
                        }

                        is YouTubeMetadataUiState.Success -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ObsidianElevated)
                                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = metadataState.data.thumbnailUrl,
                                    contentDescription = metadataState.data.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 100.dp, height = 64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    NeonStatusPill(
                                        text = "VERIFIED VIA OEMBED",
                                        accent = NeonGreen,
                                        background = NeonGreenSoft,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = metadataState.data.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                    )
                                    Text(
                                        text = metadataState.data.authorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricPurple,
                                    )
                                }
                            }
                        }

                        is YouTubeMetadataUiState.Error -> {
                            Text(
                                text = metadataState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LiveCrimson,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { FreeFireLauncher.openUrl(context, "https://studio.youtube.com") },
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Launch, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.openYoutubeStudio, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                val target = broadcastUrl.trim().ifBlank { "https://www.youtube.com/live" }
                                FreeFireLauncher.openUrl(context, target)
                            },
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Launch, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.watchOnYoutube, color = TextPrimary)
                        }
                    }
                }
            }

            item {
                NlstNeonCard(
                    borderColor = AmberAlert.copy(alpha = 0.55f),
                    containerColor = ElectricPurpleSoft,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = AmberAlert,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = strings.youtubeApiNoticeTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = strings.youtubeApiNoticeBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
