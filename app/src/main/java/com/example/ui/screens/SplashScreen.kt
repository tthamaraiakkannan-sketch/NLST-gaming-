package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.LocalNlstStrings
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleSoft
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun NlstSplashScreen(
    onFinished: () -> Unit,
) {
    val strings = LocalNlstStrings.current
    var animateIn by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.84f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "splashScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "splashAlpha",
    )

    LaunchedEffect(Unit) {
        animateIn = true
        delay(1400L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ElectricPurpleSoft.copy(alpha = 0.38f),
                        ObsidianBlack,
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("nlst_splash_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(30.dp))
                    .background(ObsidianCard)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(NeonGreen, ElectricPurple)),
                        shape = RoundedCornerShape(30.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nlst_foreground),
                    contentDescription = strings.appTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                )
                Text(
                    text = strings.tagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SplashIconBadge(Icons.Filled.SportsEsports, NeonGreen, NeonGreenSoft)
                SplashIconBadge(Icons.Filled.Sensors, ElectricPurple, ElectricPurpleSoft)
                SplashIconBadge(Icons.Filled.HeadsetMic, NeonGreen, NeonGreenSoft)
                SplashIconBadge(Icons.Filled.Bolt, ElectricPurple, ElectricPurpleSoft)
            }

            LinearProgressIndicator(
                color = NeonGreen,
                trackColor = ObsidianElevated,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(5.dp)
                    .clip(CircleShape),
            )

            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = ObsidianBlack,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("splash_continue_button"),
            ) {
                Icon(Icons.Filled.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
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

@Composable
private fun SplashIconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    bg: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.6f)),
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
