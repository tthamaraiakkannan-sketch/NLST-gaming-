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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.prefs.GamerProfile
import com.example.i18n.AppLanguage
import com.example.i18n.LocalNlstStrings
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
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    profile: GamerProfile,
    hasMicPermission: Boolean,
    hasNotificationPermission: Boolean,
    isFreeFireInstalled: Boolean,
    onRequestPermissions: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSaveProfile: (gamerTag: String, displayName: String, freeFireUid: String, freeFireRank: String) -> Unit,
    onSignOut: () -> Unit,
) {
    val strings = LocalNlstStrings.current
    var gamerTag by rememberSaveable(profile.gamerTag) { mutableStateOf(profile.gamerTag) }
    var displayName by rememberSaveable(profile.displayName) { mutableStateOf(profile.displayName) }
    var freeFireUid by rememberSaveable(profile.freeFireUid) { mutableStateOf(profile.freeFireUid) }
    var freeFireRank by rememberSaveable(profile.freeFireRank) { mutableStateOf(profile.freeFireRank) }

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
                    text = strings.profileTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
            }

            item {
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.55f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_gamer_avatar),
                            contentDescription = profile.gamerTag,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(2.dp, NeonGreen, CircleShape),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName.ifBlank { profile.gamerTag },
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                            )
                            Text(
                                text = "@${profile.gamerTag}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGreen,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            NeonStatusPill(
                                text = "Local Profile • ${profile.freeFireRank}",
                                accent = ElectricPurple,
                                background = ElectricPurpleSoft,
                            )
                        }
                    }
                }
            }

            item {
                // Bilingual Tamil / English Switcher
                NlstNeonCard {
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val selected = profile.language == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) NeonGreenSoft else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (selected) NeonGreen else ObsidianBorder,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable { onSetLanguage(lang) }
                                    .padding(vertical = 12.dp)
                                    .testTag("profile_lang_${lang.code}"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.ENGLISH) "English" else "தமிழ் (Tamil)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) NeonGreen else TextPrimary,
                                )
                            }
                        }
                    }
                }
            }

            item {
                NlstNeonCard {
                    Text(
                        text = strings.editProfileTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gamerTag,
                        onValueChange = { gamerTag = it },
                        label = { Text(strings.gamerTagLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text(strings.displayNameLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = freeFireUid,
                        onValueChange = { freeFireUid = it },
                        label = { Text(strings.freeFireUidLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = freeFireRank,
                        onValueChange = { freeFireRank = it },
                        label = { Text(strings.freeFireRankLabel) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onSaveProfile(gamerTag, displayName, freeFireUid, freeFireRank) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = ObsidianBlack,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button"),
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(strings.saveProfile, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                NlstNeonCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.permissionsAuditTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        if (!hasMicPermission || !hasNotificationPermission) {
                            OutlinedButton(
                                onClick = onRequestPermissions,
                                border = BorderStroke(1.dp, AmberAlert),
                            ) {
                                Text(strings.grantPermission, color = AmberAlert)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionAuditLine("RECORD_AUDIO (Voice FX DSP)", hasMicPermission)
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionAuditLine("POST_NOTIFICATIONS (Stream Service)", hasNotificationPermission)
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionAuditLine("Free Fire Package Detected", isFreeFireInstalled)
                }
            }

            item {
                NlstNeonCard(borderColor = ElectricPurple.copy(alpha = 0.45f)) {
                    Text(
                        text = strings.externalChecklistTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricPurple,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CapabilityRow(
                            title = "Cloud Auth & Squad Sync",
                            detail = "Requires Firebase Auth + Firestore (declined in setup)",
                            tier = CapabilityTier.EXTERNAL_SERVICE,
                        )
                        CapabilityRow(
                            title = "Multi-User Squad Voice SFU",
                            detail = "Requires WebRTC / Agora / LiveKit audio server",
                            tier = CapabilityTier.EXTERNAL_SERVICE,
                        )
                        CapabilityRow(
                            title = "YouTube Live Video & Chat API",
                            detail = "Requires H.264 RTMP Stream Key + YouTube Data API v3 OAuth",
                            tier = CapabilityTier.EXTERNAL_SERVICE,
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onSignOut,
                    border = BorderStroke(1.dp, LiveCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = strings.signOut,
                        tint = LiveCrimson,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(strings.signOut, color = LiveCrimson)
                }
            }
        }
    }
}

@Composable
private fun PermissionAuditLine(label: String, granted: Boolean) {
    val strings = LocalNlstStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        NeonStatusPill(
            text = if (granted) strings.permissionGranted else strings.permissionMissing,
            accent = if (granted) NeonGreen else AmberAlert,
            background = if (granted) NeonGreenSoft else AmberAlertSoft,
        )
    }
}
