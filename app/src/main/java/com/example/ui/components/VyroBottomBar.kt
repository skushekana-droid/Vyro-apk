package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun VyroBottomBar(
    currentScreen: Screen,
    activeTab: Screen = Screen.HOME,
    onNavigate: (Screen) -> Unit,
    onSwitchTab: (Screen) -> Unit = onNavigate
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = VyroSurface.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            val isHomeSelected = currentScreen == Screen.HOME || (currentScreen == Screen.WATCH && activeTab == Screen.HOME)
            BottomNavItem(
                icon = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                label = "HOME",
                isSelected = isHomeSelected,
                onClick = { onSwitchTab(Screen.HOME) },
                testTag = "nav_home"
            )

            // Discover
            val isDiscoverSelected = currentScreen == Screen.DISCOVER
            BottomNavItem(
                icon = if (isDiscoverSelected) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = "DISCOVER",
                isSelected = isDiscoverSelected,
                onClick = { onSwitchTab(Screen.DISCOVER) },
                testTag = "nav_discover"
            )

            // Minimalist Diamond Squircle Create Action
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(50.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = VyroVioletPrimary.copy(alpha = 0.6f))
                .rotate(45f)
                .clip(RoundedCornerShape(16.dp))
                .background(VyroBrandGradient)
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .clickable { onNavigate(Screen.CREATE) }
                .testTag("nav_create_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.rotate(-45f)) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Content",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Shorts
            val isShortsSelected = currentScreen == Screen.SHORTS
            BottomNavItem(
                icon = if (isShortsSelected) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                label = "SHORTS",
                isSelected = isShortsSelected,
                onClick = { onSwitchTab(Screen.SHORTS) },
                testTag = "nav_shorts"
            )

            // Creator Studio / Profile
            val isProfileSelected = currentScreen == Screen.CREATOR_STUDIO || currentScreen == Screen.CREATOR_PROFILE
            BottomNavItem(
                icon = if (isProfileSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                label = "PROFILE",
                isSelected = isProfileSelected,
                onClick = { onSwitchTab(Screen.CREATOR_STUDIO) },
                testTag = "nav_studio"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VyroVioletPrimary else VyroTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = if (isSelected) VyroVioletLight else VyroTextMuted
        )
    }
}
