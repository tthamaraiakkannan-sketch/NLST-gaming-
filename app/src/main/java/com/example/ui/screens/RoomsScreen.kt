package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.VoiceRoomEntity
import com.example.data.prefs.GamerProfile
import com.example.i18n.LocalNlstStrings
import com.example.media.VoiceEffect
import com.example.media.VoiceEngineState
import com.example.ui.components.LiveWaveformVisualizer
import com.example.ui.components.NeonStatusPill
import com.example.ui.components.NlstNeonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleSoft
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoomsScreen(
    profile: GamerProfile,
    rooms: List<VoiceRoomEntity>,
    activeRoom: VoiceRoomEntity?,
    messages: List<ChatMessageEntity>,
    voiceState: VoiceEngineState,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onCreateRoom: (title: String, gameTitle: String, maxSlots: Int) -> Unit,
    onJoinByCode: (String) -> Unit,
    onSelectRoom: (VoiceRoomEntity) -> Unit,
    onDeleteRoom: (Long) -> Unit,
    onLeaveRoom: () -> Unit,
    onSelectVoiceEffect: (VoiceEffect) -> Unit,
    onToggleMicMonitor: () -> Unit,
    onSendChat: (String) -> Unit,
) {
    if (activeRoom != null) {
        BackHandler { onLeaveRoom() }
        ActiveRoomContent(
            profile = profile,
            room = activeRoom,
            messages = messages,
            voiceState = voiceState,
            hasMicPermission = hasMicPermission,
            onRequestMicPermission = onRequestMicPermission,
            onLeaveRoom = onLeaveRoom,
            onSelectVoiceEffect = onSelectVoiceEffect,
            onToggleMicMonitor = onToggleMicMonitor,
            onSendChat = onSendChat,
        )
    } else {
        RoomsLobbyContent(
            rooms = rooms,
            onCreateRoom = onCreateRoom,
            onJoinByCode = onJoinByCode,
            onSelectRoom = onSelectRoom,
            onDeleteRoom = onDeleteRoom,
        )
    }
}

@Composable
private fun RoomsLobbyContent(
    rooms: List<VoiceRoomEntity>,
    onCreateRoom: (title: String, gameTitle: String, maxSlots: Int) -> Unit,
    onJoinByCode: (String) -> Unit,
    onSelectRoom: (VoiceRoomEntity) -> Unit,
    onDeleteRoom: (Long) -> Unit,
) {
    val strings = LocalNlstStrings.current
    var roomName by rememberSaveable { mutableStateOf("Free Fire Rank Push") }
    var selectedGame by rememberSaveable { mutableStateOf("Free Fire") }
    var maxSlots by rememberSaveable { mutableIntStateOf(4) }
    var joinCode by rememberSaveable { mutableStateOf("") }

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
                Text(
                    text = strings.roomsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
            }

            item {
                // Create Room Card
                NlstNeonCard(borderColor = NeonGreen.copy(alpha = 0.45f)) {
                    Text(
                        text = strings.createRoom,
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonGreen,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text(strings.roomNameLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_room_name_input"),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.gameSelectLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Free Fire", "Free Fire MAX", "Clash Squad", "Custom Room").forEach { game ->
                            val selected = selectedGame == game
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) NeonGreenSoft else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (selected) NeonGreen else ObsidianBorder,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable { selectedGame = game }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = game,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) NeonGreen else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${strings.maxSlotsLabel}: $maxSlots",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 6, 8).forEach { count ->
                            val selected = maxSlots == count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) ElectricPurpleSoft else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (selected) ElectricPurple else ObsidianBorder,
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable { maxSlots = count }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) ElectricPurple else TextSecondary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onCreateRoom(roomName, selectedGame, maxSlots) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = ObsidianBlack,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_room_submit_button"),
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(strings.createButton, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                // Join By Room Code Card
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.45f)) {
                    Text(
                        text = strings.joinByCode,
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricPurple,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it },
                            label = { Text(strings.roomCodeHint) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_room_code_input"),
                        )
                        Button(
                            onClick = { onJoinByCode(joinCode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple,
                                contentColor = TextPrimary,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("join_room_code_button"),
                        ) {
                            Text(strings.joinButton)
                        }
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
                items(rooms, key = { it.id }) { room ->
                    NlstNeonCard(
                        borderColor = ObsidianBorder,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectRoom(room) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = "${room.gameTitle} • ${room.roomCode} • ${room.maxSlots} Slots",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { onSelectRoom(room) },
                                    border = BorderStroke(1.dp, NeonGreen),
                                ) {
                                    Text(strings.joinButton, color = NeonGreen)
                                }
                                IconButton(onClick = { onDeleteRoom(room.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete",
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
}

@Composable
private fun ActiveRoomContent(
    profile: GamerProfile,
    room: VoiceRoomEntity,
    messages: List<ChatMessageEntity>,
    voiceState: VoiceEngineState,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onLeaveRoom: () -> Unit,
    onSelectVoiceEffect: (VoiceEffect) -> Unit,
    onToggleMicMonitor: () -> Unit,
    onSendChat: (String) -> Unit,
) {
    val strings = LocalNlstStrings.current
    var chatInput by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                // Active Room Header
                NlstNeonCard(borderColor = NeonGreen.copy(alpha = 0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            NeonStatusPill(
                                text = "${room.roomCode} • ${room.gameTitle}",
                                accent = NeonGreen,
                                background = NeonGreenSoft,
                                showDot = true,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = room.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                            )
                        }
                        OutlinedButton(
                            onClick = onLeaveRoom,
                            border = BorderStroke(1.dp, LiveCrimson),
                            modifier = Modifier.testTag("leave_room_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = strings.leaveRoom,
                                tint = LiveCrimson,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(strings.leaveRoom, color = LiveCrimson)
                        }
                    }
                }
            }

            item {
                // Squad Voice Roster Slots
                NlstNeonCard {
                    Text(
                        text = strings.squadSlotsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Host slot (local user)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(ObsidianElevated)
                                .border(1.5.dp, NeonGreen, RoundedCornerShape(14.dp))
                                .padding(10.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_gamer_avatar),
                                contentDescription = profile.gamerTag,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = profile.gamerTag.ifBlank { room.hostName },
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                            )
                            Text(
                                text = "HOST • ${voiceState.activeEffect.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGreen,
                            )
                        }

                        // Remaining invite slots
                        repeat((room.maxSlots - 1).coerceAtLeast(1)) { idx ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(width = 100.dp, height = 104.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ObsidianElevated.copy(alpha = 0.55f))
                                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                                    .padding(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = strings.emptySlotLabel,
                                    tint = ElectricPurple,
                                    modifier = Modifier.size(26.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Slot ${idx + 2}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                )
                                Text(
                                    text = "Invite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.voiceRelayNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            item {
                // Real-time Gaming Voice Effects DSP Card
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.55f)) {
                    Text(
                        text = strings.voiceEffectsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricPurple,
                    )
                    Text(
                        text = strings.voiceEffectsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val effects = listOf(
                        Triple(VoiceEffect.NORMAL, strings.effectNormal, Icons.Filled.Mic),
                        Triple(VoiceEffect.BOY, strings.effectBoy, Icons.Filled.GraphicEq),
                        Triple(VoiceEffect.DEEP, strings.effectDeep, Icons.Filled.SpatialAudioOff),
                        Triple(VoiceEffect.ROBOT, strings.effectRobot, Icons.Filled.SmartToy),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        effects.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowItems.forEach { (effect, label, icon) ->
                                    val selected = voiceState.activeEffect == effect
                                    val accent = when (effect) {
                                        VoiceEffect.BOY -> NeonGreen
                                        VoiceEffect.DEEP -> ElectricPurple
                                        VoiceEffect.ROBOT -> CyberCyan
                                        VoiceEffect.NORMAL -> TextPrimary
                                    }
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (selected) accent.copy(alpha = 0.16f) else ObsidianElevated,
                                            )
                                            .border(
                                                1.2.dp,
                                                if (selected) accent else ObsidianBorder,
                                                RoundedCornerShape(12.dp),
                                            )
                                            .clickable { onSelectVoiceEffect(effect) }
                                            .padding(horizontal = 12.dp, vertical = 12.dp)
                                            .testTag("voice_effect_${effect.id.lowercase()}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (selected) accent else TextSecondary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) accent else TextPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = strings.micLevelLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Text(
                            text = "${voiceState.inputDb.toInt()} dBFS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voiceState.isRunning) NeonGreen else TextMuted,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LiveWaveformVisualizer(
                        bars = voiceState.waveformBars,
                        activeEffect = voiceState.activeEffect,
                        isRunning = voiceState.isRunning,
                    )

                    voiceState.errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiveCrimson,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!hasMicPermission) {
                                onRequestMicPermission()
                            } else {
                                onToggleMicMonitor()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (voiceState.isRunning) LiveCrimson else NeonGreen,
                            contentColor = if (voiceState.isRunning) TextPrimary else ObsidianBlack,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_voice_fx_mic_button"),
                    ) {
                        Icon(
                            imageVector = if (voiceState.isRunning) Icons.Filled.MicOff else Icons.Filled.HeadsetMic,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (!hasMicPermission) {
                                strings.grantPermission
                            } else if (voiceState.isRunning) {
                                strings.stopMicMonitor
                            } else {
                                strings.startMicMonitor
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                // Live Squad Chat Card
                NlstNeonCard {
                    Text(
                        text = strings.liveChatTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.chatLocalNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical quick callout chips (English & Tamil)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Drop Peak!", "Revive me!", "Booyah!", "சுற்றி வா!", "பூயா!").forEach { quick ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ObsidianElevated)
                                    .border(1.dp, ElectricPurple.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .clickable { onSendChat(quick) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = quick,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        messages.takeLast(12).forEach { msg ->
                            val badgeColor = when (msg.senderRole) {
                                "HOST" -> NeonGreen
                                "SYSTEM" -> CyberCyan
                                else -> ElectricPurple
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianElevated)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "[${msg.senderName}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text(strings.chatInputHint) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianElevated,
                                unfocusedContainerColor = ObsidianElevated,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("room_chat_input"),
                        )
                        Button(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendChat(chatInput)
                                    chatInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = ObsidianBlack,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("room_chat_send_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = strings.sendChat,
                            )
                        }
                    }
                }
            }
        }
    }
}
