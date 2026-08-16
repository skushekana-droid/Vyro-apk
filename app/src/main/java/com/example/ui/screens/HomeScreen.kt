package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ContentCategory
import com.example.model.User
import com.example.model.Video
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    videos: List<Video>,
    currentUser: User,
    selectedCategory: ContentCategory,
    isFollowingOnly: Boolean,
    onSelectCategory: (ContentCategory) -> Unit,
    onToggleFollowingOnly: (Boolean) -> Unit,
    onVideoClick: (Video) -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onTipClick: (Video) -> Unit,
    onShareClick: (Video) -> Unit,
    onOpenStassen: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("home_screen")
    ) {
        // Feed Mode Switcher & Category Row (Full width header)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VyroSurface)
                .padding(vertical = 8.dp)
        ) {
            // Explore vs Following Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isFollowingOnly,
                    onClick = { onToggleFollowingOnly(false) },
                    label = { Text("⚡ Explore Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VyroVioletPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = VyroSurfaceElevated,
                        labelColor = VyroTextSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !isFollowingOnly,
                        borderColor = VyroBorderSubtle,
                        selectedBorderColor = VyroVioletPrimary
                    )
                )

                FilterChip(
                    selected = isFollowingOnly,
                    onClick = { onToggleFollowingOnly(true) },
                    label = { Text("👥 Following (${currentUser.followedCreatorIds.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VyroVioletDark,
                        selectedLabelColor = Color.White,
                        containerColor = VyroSurfaceElevated,
                        labelColor = VyroTextSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isFollowingOnly,
                        borderColor = VyroBorderSubtle,
                        selectedBorderColor = VyroVioletPrimary
                    )
                )
            }

            // Category Pills
            if (!isFollowingOnly) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ContentCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) VyroVioletPrimary else VyroSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) VyroVioletPrimary else VyroBorderSubtle,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onSelectCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("cat_pill_${category.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.displayName,
                                color = if (isSelected) Color.White else VyroTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Responsive Videos Grid (1 column on mobile, 2-4 columns on tablet/desktop)
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isFollowingOnly) "No videos from followed creators yet" else "No videos found in this category",
                        color = VyroTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Follow top creators or explore all trending content!",
                        color = VyroTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_feed_grid"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Stassen AI Assistant Banner
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onOpenStassen() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.model.StassenIdentityRegistry.PRIMARY_PORTRAIT_RES),
                                    contentDescription = "Stassen Character Identity",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .border(1.5.dp, VyroVioletPrimary, androidx.compose.foundation.shape.CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(10.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(VyroEmerald)
                                        .border(1.5.dp, VyroSurface, androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Stassen AI Companion",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• In Virtual Residence",
                                        color = VyroCyanLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "Tap to visit Stassen's office, discuss videos, or research topics",
                                    color = VyroTextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onOpenStassen,
                            colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Visit House", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                items(videos, key = { it.id }) { video ->
                    VideoFeedCard(
                        video = video,
                        isLiked = currentUser.likedVideoIds.contains(video.id),
                        isBookmarked = currentUser.bookmarkedVideoIds.contains(video.id),
                        onVideoClick = { onVideoClick(video) },
                        onCreatorClick = { onCreatorClick(video.creatorId) },
                        onLikeClick = { onLikeClick(video.id) },
                        onBookmarkClick = { onBookmarkClick(video.id) },
                        onShareClick = { onShareClick(video) },
                        onTipClick = { onTipClick(video) }
                    )
                }
            }
        }
    }
}
