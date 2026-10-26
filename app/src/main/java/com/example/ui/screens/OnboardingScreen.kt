package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UnlockedProBadge
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var page by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Real App Blocking",
            subtitle = "Lock away infinite doom-scrolling and entertainment apps during study and work sprints.",
            icon = Icons.Default.Block,
            color = PurpleAccent
        ),
        OnboardingPageData(
            title = "Unbreakable Strict Mode",
            subtitle = "Enable strict personal accountability with cooldown timers and emergency shields.",
            icon = Icons.Default.Lock,
            color = RoseStrict
        ),
        OnboardingPageData(
            title = "Unlimited Schedules",
            subtitle = "Automate your daily deep work routines, pomodoros, and evening digital detoxes effortlessly.",
            icon = Icons.Default.EventRepeat,
            color = IndigoAccent
        ),
        OnboardingPageData(
            title = "All Features Unlocked",
            subtitle = "No subscriptions. No paywalls. No account creation. 100% private local storage on your device.",
            icon = Icons.Default.CheckCircle,
            color = EmeraldSuccess
        )
    )

    val current = pages[page]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Charcoal950)
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnlockedProBadge()
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("skip_onboarding_button")
                ) {
                    Text("Skip", color = TextSecondary)
                }
            }

            // Center Graphic & Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(current.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(current.color, PurpleDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = current.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = current.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = current.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 24.sp
                )
            }

            // Bottom Navigation & Dots
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = index == page
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) PurpleAccent else Charcoal700)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (page < pages.size - 1) {
                            page++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text(
                        text = if (page == pages.size - 1) "Get Started — Free Forever" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)
