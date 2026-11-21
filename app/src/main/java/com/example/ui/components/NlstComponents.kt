package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalNlstStrings
import com.example.media.VoiceEffect
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
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NlstTopBar(
    language: AppLanguage,
    isCapturing: Boolean,
    measuredFps: Int,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalNlstStrings.current
    Surface(
        color = ObsidianSurface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(NeonGreen, ElectricPurple)),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = strings.appTitle,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.tagline,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isCapturing) {
                    NeonStatusPill(
                        text = "REC ${measuredFps}fps",
                        accent = LiveCrimson,
                        background = LiveCrimsonSoft,
                        showDot = true,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = ObsidianElevated,
                    border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleLanguage)
                        .testTag("language_toggle_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = strings.languageLabel,
                            tint = ElectricPurple,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (language == AppLanguage.ENGLISH) "தமிழ்" else "EN",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NlstNeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color = ObsidianBorder,
    containerColor: Color = ObsidianCard,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun NeonStatusPill(
    text: String,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showDot: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accent),
                )
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

enum class CapabilityTier { ON_DEVICE_REAL, PERMISSION_REQUIRED, EXTERNAL_SERVICE }

@Composable
fun CapabilityRow(
    title: String,
    detail: String,
    tier: CapabilityTier,
    modifier: Modifier = Modifier,
) {
    val strings = LocalNlstStrings.current
    val (badgeText, badgeColor, badgeBg, icon) = when (tier) {
        CapabilityTier.ON_DEVICE_REAL -> Quadruple(
            strings.readyOnDeviceLabel,
            NeonGreen,
            NeonGreenSoft,
            Icons.Filled.VerifiedUser,
        )
        CapabilityTier.PERMISSION_REQUIRED -> Quadruple(
            strings.requiresPermissionLabel,
            AmberAlert,
            AmberAlertSoft,
            Icons.Filled.VerifiedUser,
        )
        CapabilityTier.EXTERNAL_SERVICE -> Quadruple(
            strings.requiresExternalLabel,
            ElectricPurple,
            ElectricPurpleSoft,
            Icons.Filled.CloudOff,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianElevated.copy(alpha = 0.7f))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        NeonStatusPill(
            text = badgeText,
            accent = badgeColor,
            background = badgeBg,
            icon = icon,
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun LiveWaveformVisualizer(
    bars: List<Float>,
    activeEffect: VoiceEffect,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryBarColor by animateColorAsState(
        targetValue = when {
            !isRunning -> ObsidianBorder
            activeEffect == VoiceEffect.BOY -> NeonGreen
            activeEffect == VoiceEffect.DEEP -> ElectricPurple
            activeEffect == VoiceEffect.ROBOT -> Color(0xFF06B6D4)
            else -> NeonGreen
        },
        label = "waveformColor",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBlack)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        val count = bars.size.coerceAtLeast(1)
        val gap = 4.dp.toPx()
        val totalGap = gap * (count - 1)
        val barWidth = ((size.width - totalGap) / count).coerceAtLeast(2f)

        bars.forEachIndexed { index, amp ->
            val clamped = amp.coerceIn(0.06f, 1.0f)
            val barHeight = size.height * clamped
            val x = index * (barWidth + gap)
            val y = (size.height - barHeight) / 2f
            val barColor = if (index % 2 == 0) primaryBarColor else ElectricPurple.copy(
                alpha = if (isRunning) 0.9f else 0.35f,
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        "%02d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }
}
