package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
import com.example.model.Video
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.*

@Composable
fun CreatorProfileScreen(
    user: User,
    creatorVideos: List<Video>,
    isOwnProfile: Boolean,
    isFollowed: Boolean,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onFollowClick: () -> Unit,
    onOpenStudio: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onTipClick: () -> Unit
) {
    val tabs = listOf("Videos", "Shorts", "Store 🛍️", "About")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("creator_profile_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Banner & Profile Card Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vyro_hero_banner),
                    contentDescription = "Channel Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, VyroBackground)
                            )
                        )
                )
            }
        }

        // Avatar & Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(VyroBrandGradient)
                            .border(3.dp, VyroBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isOwnProfile) {
                            Button(
                                onClick = onOpenStudio,
                                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("profile_open_studio_btn")
                            ) {
                                Text("Creator Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = onFollowClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowed) VyroSurfaceElevated else VyroVioletPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isFollowed) "Subscribed" else "Subscribe",
                                    color = if (isFollowed) VyroTextSecondary else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Button(
                                onClick = onTipClick,
                                colors = ButtonDefaults.buttonColors(containerColor = VyroGoldTertiary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Tip ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = VyroCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "${user.username} • ${user.country}",
                    color = VyroCyanLight,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = user.bio,
                    color = VyroTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Counters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${"%,d".format(user.followersCount)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(text = "Subscribers", color = VyroTextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${"%,d".format(user.totalViews)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(text = "Total Views", color = VyroTextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${creatorVideos.size}",
                            color = VyroCyanLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(text = "Videos", color = VyroTextMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = VyroSurface,
                    contentColor = VyroVioletPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { onTabSelected(index) },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) VyroVioletLight else VyroTextMuted,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> { // Long Videos
                val longVideos = creatorVideos.filter { !it.isShort }
                if (longVideos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No long-form videos published yet", color = VyroTextMuted)
                        }
                    }
                } else {
                    items(longVideos) { video ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            VideoFeedCard(
                                video = video,
                                isLiked = false,
                                isBookmarked = false,
                                onVideoClick = { onVideoClick(video) },
                                onCreatorClick = { },
                                onLikeClick = { },
                                onBookmarkClick = { },
                                onShareClick = { },
                                onTipClick = { }
                            )
                        }
                    }
                }
            }
            1 -> { // Shorts
                val shorts = creatorVideos.filter { it.isShort }
                if (shorts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No VYRO shorts published yet", color = VyroTextMuted)
                        }
                    }
                } else {
                    items(shorts) { short ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            VideoFeedCard(
                                video = short,
                                isLiked = false,
                                isBookmarked = false,
                                onVideoClick = { onVideoClick(short) },
                                onCreatorClick = { },
                                onLikeClick = { },
                                onBookmarkClick = { },
                                onShareClick = { },
                                onTipClick = { }
                            )
                        }
                    }
                }
            }
            2 -> { // Store
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Creator Digital Store",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Product cards
                        val products = creatorVideos.mapNotNull { it.linkedProduct }
                        if (products.isEmpty()) {
                            Text(
                                text = "No merchandise or digital products listed in this store.",
                                color = VyroTextMuted,
                                fontSize = 13.sp
                            )
                        } else {
                            products.forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VyroSurfaceElevated)
                                        .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.title,
                                            color = VyroTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${prod.salesCount} purchases",
                                            color = VyroTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = VyroCyanSecondary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Buy $${"%.2f".format(prod.price)}", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            3 -> { // About
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VyroSurfaceElevated)
                            .padding(16.dp)
                    ) {
                        Text(text = "About ${user.displayName}", color = VyroTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = user.bio, color = VyroTextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "📍 Location: ${user.country}", color = VyroTextMuted, fontSize = 12.sp)
                        Text(text = "🛡️ Verified Creator Partner since 2026", color = VyroCyanLight, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
