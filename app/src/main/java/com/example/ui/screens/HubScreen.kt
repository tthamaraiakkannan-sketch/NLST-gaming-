package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FriendEntity
import com.example.data.local.VoiceRoomEntity
import com.example.data.prefs.GamerProfile
import com.example.i18n.LocalNlstStrings
import com.example.media.FreeFireInstallStatus
import com.example.media.FreeFireLauncher
import com.example.ui.components.CapabilityRow
import com.example.ui.components.CapabilityTier
import com.example.ui.components.NeonStatusPill
import com.example.ui.components.NlstNeonCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberAlertSoft
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleSoft
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HubScreen(
    profile: GamerProfile,
    freeFireStatus: FreeFireInstallStatus,
    rooms: List<VoiceRoomEntity>,
    friends: List<FriendEntity>,
    onStartLiveClick: () -> Unit,
    onVoiceChatClick: () -> Unit,
    onYouTubeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateRoomAndOpen: (title: String, gameTitle: String, maxSlots: Int) -> Unit,
    onJoinRoomCodeAndOpen: (code: String) -> Unit,
    onOpenRoom: (VoiceRoomEntity) -> Unit,
    onAddFriend: (gamerTag: String, gameTitle: String, freeFireUid: String) -> Unit,
    onDeleteFriend: (Long) -> Unit,
    onInviteFriendToVoiceChat: (FriendEntity) -> Unit,
) {
    val strings = LocalNlstStrings.current
    val context = LocalContext.current

    var showCreateRoomDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinRoomDialog by rememberSaveable { mutableStateOf(false) }
    var showAddFriendDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // Hero Gaming Banner with Start Live & Voice Chat
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(NeonGreen, ElectricPurple)),
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_battle_royale),
                            contentDescription = strings.heroBannerTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            ObsidianBlack.copy(alpha = 0.25f),
                                            ObsidianBlack.copy(alpha = 0.93f),
                                        ),
                                    ),
                                ),
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            NeonStatusPill(
                                text = "ARENA READY • ${profile.gamerTag.uppercase()}",
                                accent = NeonGreen,
                                background = NeonGreenSoft,
                                showDot = true,
                            )
                            Text(
                                text = strings.heroBannerTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = strings.heroBannerSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Button(
                                    onClick = onStartLiveClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonGreen,
                                        contentColor = ObsidianBlack,
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("hub_start_live_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Sensors,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(strings.startLiveCta, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onVoiceChatClick,
                                    border = BorderStroke(1.dp, ElectricPurple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("hub_voice_chat_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.HeadsetMic,
                                        contentDescription = null,
                                        tint = ElectricPurple,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(strings.joinSquadCta, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Quick Actions Grid: Create Room, Join Room, YouTube Live, Profile
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { showCreateRoomDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianCard,
                                contentColor = NeonGreen,
                            ),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("hub_create_room_button"),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.createRoom, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showJoinRoomDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianCard,
                                contentColor = ElectricPurple,
                            ),
                            border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("hub_join_room_button"),
                        ) {
                            Icon(Icons.Filled.MeetingRoom, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.joinByCode, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = onYouTubeClick,
                            border = BorderStroke(1.dp, LiveCrimson.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_youtube_button"),
                        ) {
                            Icon(
                                Icons.Filled.PlayCircleFilled,
                                contentDescription = null,
                                tint = LiveCrimson,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.navYouTube, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = onProfileClick,
                            border = BorderStroke(1.dp, ObsidianBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_profile_button"),
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.navProfile, color = TextPrimary)
                        }
                    }
                }
            }

            item {
                // Friends List Card
                NlstNeonCard(borderColor = NeonGreen.copy(alpha = 0.4f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.friendsListTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        OutlinedButton(
                            onClick = { showAddFriendDialog = true },
                            border = BorderStroke(1.dp, NeonGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_friend_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GroupAdd,
                                contentDescription = strings.addFriend,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(strings.addFriend, color = NeonGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.friendsLocalNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (friends.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                                .padding(14.dp),
                        ) {
                            Text(
                                text = strings.noFriendsYet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            friends.forEach { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ObsidianElevated)
                                        .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(ElectricPurpleSoft)
                                                .border(1.dp, ElectricPurple, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = friend.gamerTag.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = ElectricPurple,
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = friend.gamerTag,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary,
                                            )
                                            val uidText = if (friend.freeFireUid.isNotBlank()) {
                                                " • UID ${friend.freeFireUid}"
                                            } else {
                                                ""
                                            }
                                            Text(
                                                text = "${friend.gameTitle}$uidText",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { onInviteFriendToVoiceChat(friend) },
                                            border = BorderStroke(1.dp, NeonGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.HeadsetMic,
                                                contentDescription = strings.inviteVoiceChat,
                                                tint = NeonGreen,
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Spacer(modifier = Modifier.size(4.dp))
                                            Text(
                                                text = strings.inviteVoiceChat,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonGreen,
                                            )
                                        }
                                        IconButton(onClick = { onDeleteFriend(friend.id) }) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Remove Friend",
                                                tint = TextMuted,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Free Fire Integration Card
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.45f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_free_fire_card),
                            contentDescription = strings.freeFireCardTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(86.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = strings.freeFireCardTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                            )
                            NeonStatusPill(
                                text = if (freeFireStatus.isAnyInstalled) {
                                    strings.freeFireInstalled
                                } else {
                                    strings.freeFireNotInstalled
                                },
                                accent = if (freeFireStatus.isAnyInstalled) NeonGreen else AmberAlert,
                                background = if (freeFireStatus.isAnyInstalled) NeonGreenSoft else AmberAlertSoft,
                            )
                            if (profile.freeFireUid.isNotBlank()) {
                                Text(
                                    text = "UID: ${profile.freeFireUid} • ${profile.freeFireRank}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricPurple,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (freeFireStatus.isAnyInstalled) {
                        Button(
                            onClick = {
                                FreeFireLauncher.launchFreeFire(context, freeFireStatus.preferredPackage)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple,
                                contentColor = TextPrimary,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launch_free_fire_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsEsports,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(strings.launchFreeFire)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { FreeFireLauncher.openPlayStore(context) },
                            border = BorderStroke(1.dp, ObsidianBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("free_fire_playstore_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Launch,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(strings.openPlayStore, color = TextPrimary)
                        }
                    }
                }
            }

            item {
                // Transparent External Services & Permissions Readiness Matrix
                NlstNeonCard(borderColor = ObsidianBorder) {
                    Text(
                        text = strings.readinessTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Text(
                        text = strings.readinessSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CapabilityRow(
                            title = "Screen Capture (MediaProjection + FGS)",
                            detail = "Captures real frames & FPS via Android OS consent",
                            tier = CapabilityTier.PERMISSION_REQUIRED,
                        )
                        CapabilityRow(
                            title = "Voice FX DSP (Boy / Deep / Robot)",
                            detail = "Real-time AudioRecord + PCM DSP processing",
                            tier = CapabilityTier.PERMISSION_REQUIRED,
                        )
                        CapabilityRow(
                            title = "YouTube oEmbed & RTMP TCP Probe",
                            detail = "Real YouTube metadata lookup & port 1935 handshake",
                            tier = CapabilityTier.ON_DEVICE_REAL,
                        )
                        CapabilityRow(
                            title = "Multi-User Squad Voice & Live Viewers",
                            detail = "Requires WebRTC SFU, Firebase Cloud & YouTube OAuth API",
                            tier = CapabilityTier.EXTERNAL_SERVICE,
                        )
                    }
                }
            }

            item {
                Text(
                    text = strings.recentRoomsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
            }

            if (rooms.isEmpty()) {
                item {
                    NlstNeonCard(containerColor = ObsidianElevated.copy(alpha = 0.55f)) {
                        Text(
                            text = strings.noRoomsYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            } else {
                items(rooms.take(4), key = { it.id }) { room ->
                    NlstNeonCard(
                        borderColor = NeonGreen.copy(alpha = 0.35f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onOpenRoom(room) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = "${room.gameTitle} • ${room.roomCode} • ${room.voiceEffect}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = strings.joinButton,
                                tint = ElectricPurple,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        var roomTitle by rememberSaveable { mutableStateOf("Free Fire Rank Push") }
        var gameName by rememberSaveable { mutableStateOf("Free Fire") }
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            containerColor = ObsidianCard,
            title = { Text(strings.createRoom, color = NeonGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        label = { Text(strings.roomNameLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                    OutlinedTextField(
                        value = gameName,
                        onValueChange = { gameName = it },
                        label = { Text(strings.gameSelectLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricPurple,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreateRoomDialog = false
                        onCreateRoomAndOpen(roomTitle, gameName, 4)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = ObsidianBlack,
                    ),
                ) {
                    Text(strings.createButton, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
        )
    }

    if (showJoinRoomDialog) {
        var codeInput by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinRoomDialog = false },
            containerColor = ObsidianCard,
            title = { Text(strings.joinByCode, color = ElectricPurple) },
            text = {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text(strings.roomCodeHint) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showJoinRoomDialog = false
                        onJoinRoomCodeAndOpen(codeInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple,
                        contentColor = TextPrimary,
                    ),
                ) {
                    Text(strings.joinButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinRoomDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
        )
    }

    if (showAddFriendDialog) {
        var friendTag by rememberSaveable { mutableStateOf("") }
        var friendUid by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            containerColor = ObsidianCard,
            title = { Text(strings.addFriend, color = NeonGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = friendTag,
                        onValueChange = { friendTag = it },
                        label = { Text(strings.friendTagHint) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        modifier = Modifier.testTag("dialog_friend_tag_input"),
                    )
                    OutlinedTextField(
                        value = friendUid,
                        onValueChange = { friendUid = it },
                        label = { Text(strings.freeFireUidLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricPurple,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendTag.isNotBlank()) {
                            onAddFriend(friendTag, "Free Fire", friendUid)
                        }
                        showAddFriendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = ObsidianBlack,
                    ),
                    modifier = Modifier.testTag("dialog_add_friend_confirm"),
                ) {
                    Text(strings.addFriend, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
        )
    }
}
