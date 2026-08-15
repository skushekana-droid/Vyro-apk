package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    onExplore: () -> Unit,
    onGetStarted: () -> Unit,
    onQuickLogin: (UserRole) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("landing_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vyro_hero_banner),
                    contentDescription = "VYRO Platform",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    VyroBackground.copy(alpha = 0.85f),
                                    VyroBackground
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroBrandGradient)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "VYRO",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Where Content Becomes an Economy.",
                        color = VyroCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Watch. Create. Connect. Learn. Earn.",
                        color = VyroTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onGetStarted,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("landing_get_started_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = onExplore,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("landing_explore_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroCyanLight),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = VyroBrandGradient),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Explore VYRO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Value Proposition Pillars
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "THE REVOLUTIONARY ECOSYSTEM",
                    color = VyroGoldTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Built for Next-Generation Media",
                    color = VyroTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                PillarCard(
                    icon = "👁️",
                    title = "For Viewers",
                    desc = "Discover personalized entertainment, immersive 4K HDR shorts, live streams, and directly support your favorite storytellers with zero friction.",
                    accentColor = VyroCyanSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                PillarCard(
                    icon = "🚀",
                    title = "For Creators",
                    desc = "Build a devoted audience, sell digital merchandise directly in videos, receive live tips, and unlock multi-stream revenue shares.",
                    accentColor = VyroVioletPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                PillarCard(
                    icon = "💼",
                    title = "For Businesses",
                    desc = "Reach engaged niche communities through native creator collaborations, interactive in-video commerce, and targeted sponsorships.",
                    accentColor = VyroGoldTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                PillarCard(
                    icon = "🤖",
                    title = "AI-Powered Intelligence",
                    desc = "Integrated Gemini AI studio helps creators generate viral titles, SEO tags, video scripts, visual concepts, and automated content calendars in seconds.",
                    accentColor = VyroEmerald
                )
            }
        }

        // Quick Demo Role Switcher Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "⚡ QUICK DEMO PROFILES",
                    color = VyroCyanLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Test VYRO through different platform roles:",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                DemoRolePill(
                    role = UserRole.CREATOR,
                    name = "Kael Orion (Creator)",
                    desc = "Full Creator Studio, Video Upload, AI Assistant & Analytics",
                    onClick = { onQuickLogin(UserRole.CREATOR) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DemoRolePill(
                    role = UserRole.VIEWER,
                    name = "Alex Reed (Viewer)",
                    desc = "Watch feeds, tip creators, comment, save bookmarks",
                    onClick = { onQuickLogin(UserRole.VIEWER) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DemoRolePill(
                    role = UserRole.BUSINESS,
                    name = "Synthetix Labs (Business)",
                    desc = "Creator store goods, digital audio plugins, commerce",
                    onClick = { onQuickLogin(UserRole.BUSINESS) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DemoRolePill(
                    role = UserRole.ADMIN,
                    name = "VYRO Safety HQ (Admin)",
                    desc = "Platform moderation, report handling & ecosystem health",
                    onClick = { onQuickLogin(UserRole.ADMIN) }
                )
            }
        }
    }
}

@Composable
private fun PillarCard(
    icon: String,
    title: String,
    desc: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VyroSurfaceElevated)
            .border(1.dp, VyroBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = VyroTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DemoRolePill(
    role: UserRole,
    name: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VyroSurface)
            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = VyroTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = desc,
                color = VyroTextMuted,
                fontSize = 11.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Select",
            tint = VyroVioletPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
