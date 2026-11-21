package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.LocalNlstStrings
import com.example.ui.components.NlstNeonCard
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleSoft
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    initialGamerTag: String,
    initialDisplayName: String,
    initialFreeFireUid: String,
    onToggleLanguage: () -> Unit,
    onEnterArena: (gamerTag: String, displayName: String, freeFireUid: String) -> Unit,
) {
    val strings = LocalNlstStrings.current
    var gamerTag by rememberSaveable { mutableStateOf(initialGamerTag.ifBlank { "NLST_Striker" }) }
    var displayName by rememberSaveable { mutableStateOf(initialDisplayName.ifBlank { "Arjun" }) }
    var freeFireUid by rememberSaveable { mutableStateOf(initialFreeFireUid) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianBlack, ObsidianCard, ObsidianBlack),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(NeonGreen, ElectricPurple))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = strings.appTitle,
                            tint = ObsidianBlack,
                        )
                    }
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                }

                OutlinedButton(
                    onClick = onToggleLanguage,
                    border = BorderStroke(1.dp, ElectricPurple),
                    modifier = Modifier.testTag("auth_language_toggle"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = strings.languageLabel,
                        tint = ElectricPurple,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.ENGLISH) "தமிழ்" else "English",
                        color = TextPrimary,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.45f)),
                color = ObsidianCard,
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_battle_royale),
                        contentDescription = strings.welcomeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ObsidianBlack.copy(alpha = 0.15f),
                                        ObsidianBlack.copy(alpha = 0.88f),
                                    ),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = strings.welcomeTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = NeonGreen,
                        )
                        Text(
                            text = strings.welcomeSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                    }
                }
            }

            NlstNeonCard(
                borderColor = ElectricPurple.copy(alpha = 0.55f),
                containerColor = ElectricPurpleSoft,
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = strings.localAuthNoticeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = strings.localAuthNoticeBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }

            NlstNeonCard(borderColor = ObsidianBorder) {
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

                OutlinedTextField(
                    value = gamerTag,
                    onValueChange = { gamerTag = it },
                    label = { Text(strings.gamerTagLabel) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_gamer_tag_input"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text(strings.displayNameLabel) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_display_name_input"),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = freeFireUid,
                    onValueChange = { freeFireUid = it },
                    label = { Text(strings.freeFireUidLabel) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_ff_uid_input"),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { onEnterArena(gamerTag, displayName, freeFireUid) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = ObsidianBlack,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enter_arena_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = strings.enterArenaButton,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
