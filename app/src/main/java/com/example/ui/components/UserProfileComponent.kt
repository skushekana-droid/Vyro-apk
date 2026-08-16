package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MembershipTier
import com.example.model.User
import com.example.model.Video
import com.example.ui.theme.*

enum class ProfileGridMode {
    TWO_COLUMN,
    THREE_COLUMN,
    LIST_VIEW
}

enum class ProfileContentFilter(val label: String, val icon: String) {
    ALL("All", "🎬"),
    LONG_FORM("Videos", "📽️"),
    SHORTS("Shorts", "⚡"),
    POPULAR("Popular", "🔥"),
    LIKED("Top Liked", "❤️")
}

/**
 * Production-ready User Profile Component displaying creator stats, biography,
 * and a responsive content grid, backed by Room local database caching.
 */
@Composable
fun UserProfileComponent(
    user: User,
    videos: List<Video>,
    isOwnProfile: Boolean,
    isFollowed: Boolean,
    modifier: Modifier = Modifier,
    onFollowClick: () -> Unit = {},
    onTipClick: () -> Unit = {},
    onOpenStudio: () -> Unit = {},
    onVideoClick: (Video) -> Unit = {},
    onLikeVideo: (String) -> Unit = {},
    onBookmarkVideo: (String) -> Unit = {},
    onShareProfile: () -> Unit = {},
    onUpdateProfile: (displayName: String, bio: String, country: String, websiteUrl: String, tags: List<String>) -> Unit = { _, _, _, _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf(ProfileContentFilter.ALL) }
    var gridMode by remember { mutableStateOf(ProfileGridMode.TWO_COLUMN) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAnalyticsDetails by remember { mutableStateOf(false) }
    var isBioExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    // Computed / Dynamic creator stats
    val totalViewsCount = remember(user.totalViews, videos) {
        if (user.totalViews > 0) user.totalViews else videos.sumOf { it.viewCount.toLong() }
    }
    val totalLikesCount = remember(user.totalLikes, videos) {
        if (user.totalLikes > 0) user.totalLikes else videos.sumOf { it.likeCount.toLong() }
    }
    val totalVideosCount = remember(videos) { videos.size }

    // Filtered videos based on search, tab, and tag filters
    val filteredContent = remember(videos, selectedFilter, searchQuery, selectedTagFilter) {
        var list = when (selectedFilter) {
            ProfileContentFilter.ALL -> videos
            ProfileContentFilter.LONG_FORM -> videos.filter { !it.isShort }
            ProfileContentFilter.SHORTS -> videos.filter { it.isShort }
            ProfileContentFilter.POPULAR -> videos.sortedByDescending { it.viewCount }
            ProfileContentFilter.LIKED -> videos.sortedByDescending { it.likeCount }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase().trim()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.tags.any { t -> t.lowercase().contains(q) }
            }
        }
        if (selectedTagFilter != null) {
            list = list.filter { it.tags.any { t -> t.equals(selectedTagFilter, ignoreCase = true) } }
        }
        list
    }

    val gridColumns = when (gridMode) {
        ProfileGridMode.TWO_COLUMN -> GridCells.Fixed(2)
        ProfileGridMode.THREE_COLUMN -> GridCells.Fixed(3)
        ProfileGridMode.LIST_VIEW -> GridCells.Fixed(1)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("user_profile_component")
    ) {
        LazyVerticalGrid(
            columns = gridColumns,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header & Banner (Spans full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                UserProfileHeader(
                    user = user,
                    isOwnProfile = isOwnProfile,
                    isFollowed = isFollowed,
                    onFollowClick = onFollowClick,
                    onTipClick = onTipClick,
                    onOpenStudio = onOpenStudio,
                    onEditProfileClick = { showEditProfileDialog = true },
                    onShareProfile = onShareProfile
                )
            }

            // Biography & Tags (Spans full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                UserProfileBioSection(
                    user = user,
                    isBioExpanded = isBioExpanded,
                    onToggleBioExpand = { isBioExpanded = !isBioExpanded },
                    selectedTag = selectedTagFilter,
                    onSelectTag = { tag ->
                        selectedTagFilter = if (selectedTagFilter == tag) null else tag
                    }
                )
            }

            // Creator Stats Row (Spans full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                CreatorStatsSection(
                    followersCount = user.followersCount,
                    totalViews = totalViewsCount,
                    totalLikes = totalLikesCount,
                    videoCount = totalVideosCount,
                    membershipTier = user.membershipTier,
                    isVerified = user.isVerified,
                    showDetails = showAnalyticsDetails,
                    onToggleDetails = { showAnalyticsDetails = !showAnalyticsDetails }
                )
            }

            // Filter Tabs & Search / Layout Controls (Spans full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                ContentGridControls(
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it },
                    gridMode = gridMode,
                    onChangeGridMode = { gridMode = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    totalItemCount = filteredContent.size
                )
            }

            // Empty state if no videos match
            if (filteredContent.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyContentState(
                        isOwnProfile = isOwnProfile,
                        onUploadClick = onOpenStudio,
                        hasSearch = searchQuery.isNotBlank() || selectedTagFilter != null,
                        onClearSearch = {
                            searchQuery = ""
                            selectedTagFilter = null
                            selectedFilter = ProfileContentFilter.ALL
                        }
                    )
                }
            } else {
                // Content Grid Items
                items(filteredContent, key = { it.id }) { video ->
                    when (gridMode) {
                        ProfileGridMode.TWO_COLUMN -> {
                            TwoColumnContentGridCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onLikeClick = { onLikeVideo(video.id) },
                                onBookmarkClick = { onBookmarkVideo(video.id) }
                            )
                        }
                        ProfileGridMode.THREE_COLUMN -> {
                            ThreeColumnThumbnailCard(
                                video = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                        ProfileGridMode.LIST_VIEW -> {
                            ListModeContentCard(
                                video = video,
                                onClick = { onVideoClick(video) },
                                onLikeClick = { onLikeVideo(video.id) },
                                onBookmarkClick = { onBookmarkVideo(video.id) }
                            )
                        }
                    }
                }
            }
        }

        // Edit Profile Modal
        if (showEditProfileDialog) {
            EditProfileDialog(
                user = user,
                onDismiss = { showEditProfileDialog = false },
                onSave = { displayName, bio, country, websiteUrl, tags ->
                    onUpdateProfile(displayName, bio, country, websiteUrl, tags)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

// ----------------------------------------------------------------------------
// HEADER COMPONENT (Banner, Avatar, Action Buttons)
// ----------------------------------------------------------------------------
@Composable
private fun UserProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    onTipClick: () -> Unit,
    onOpenStudio: () -> Unit,
    onEditProfileClick: () -> Unit,
    onShareProfile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Banner with Gradient and Room Cache Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
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
                            listOf(Color.Transparent, Color(0x990A0B10), VyroBackground)
                        )
                    )
            )

            // Local Room Cache Status Pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC0E111A))
                    .border(1.dp, VyroVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(VyroCyanSecondary)
                )
                Text(
                    text = "Room SQLite Cached",
                    color = VyroCyanLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Avatar & Interaction Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-36).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(VyroBrandGradient)
                    .border(3.dp, VyroBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 34.sp
                )
                if (user.isVerified) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VyroSurface)
                            .border(1.5.dp, VyroCyanSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Creator",
                            tint = VyroCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Action Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOwnProfile) {
                    Button(
                        onClick = onEditProfileClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorder),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("profile_open_studio_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Studio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowed) VyroSurfaceElevated else VyroVioletPrimary
                        ),
                        border = if (isFollowed) androidx.compose.foundation.BorderStroke(1.dp, VyroBorder) else null,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("follow_creator_btn")
                    ) {
                        Icon(
                            imageVector = if (isFollowed) Icons.Filled.Check else Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = if (isFollowed) VyroCyanLight else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFollowed) "Subscribed" else "Subscribe",
                            color = if (isFollowed) VyroCyanLight else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onTipClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VyroGoldTertiary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("tip_creator_btn")
                    ) {
                        Text("Tip ⚡", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = onShareProfile,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Profile",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// BIOGRAPHY SECTION (Name, Handle, Country, Bio Text, Links, Tags)
// ----------------------------------------------------------------------------
@Composable
private fun UserProfileBioSection(
    user: User,
    isBioExpanded: Boolean,
    onToggleBioExpand: () -> Unit,
    selectedTag: String?,
    onSelectTag: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-20).dp)
            .testTag("creator_bio_section")
    ) {
        // Name & Verification Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = user.displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            if (user.isVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified",
                    tint = VyroCyanSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (user.membershipTier == MembershipTier.CREATOR_VIP) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(VyroGoldTertiary.copy(alpha = 0.2f))
                        .border(1.dp, VyroGoldTertiary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("VIP CREATOR", color = VyroGoldTertiary, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Username, Location, and Join Date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = user.username,
                color = VyroCyanLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(text = "•", color = VyroTextMuted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = VyroTextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = user.country, color = VyroTextSecondary, fontSize = 12.sp)
            }
            Text(text = "•", color = VyroTextMuted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = VyroTextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = "Joined ${user.dateJoined}", color = VyroTextMuted, fontSize = 11.sp)
            }
        }

        // Bio Text with Expandable "Read More"
        if (user.bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = user.bio,
                color = Color(0xFFE2E8F0),
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                maxLines = if (isBioExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            if (user.bio.length > 100) {
                Text(
                    text = if (isBioExpanded) "Show Less" else "...more",
                    color = VyroVioletLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onToggleBioExpand() }
                        .padding(top = 2.dp)
                )
            }
        }

        // External Link / Portfolio
        if (user.websiteUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = "Website",
                    tint = VyroCyanSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.websiteUrl.removePrefix("https://").removePrefix("http://"),
                    color = VyroCyanSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Specialty Tag Cloud
        if (user.categoryTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                user.categoryTags.take(4).forEach { tag ->
                    val isSelected = selectedTag.equals(tag, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VyroVioletPrimary else VyroSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) VyroCyanSecondary else VyroBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTag(tag) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = if (isSelected) Color.White else VyroTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// CREATOR STATS ROW (Followers, Total Views, Likes, Videos, Analytics)
// ----------------------------------------------------------------------------
@Composable
private fun CreatorStatsSection(
    followersCount: Long,
    totalViews: Long,
    totalLikes: Long,
    videoCount: Int,
    membershipTier: MembershipTier,
    isVerified: Boolean,
    showDetails: Boolean,
    onToggleDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-10).dp)
            .testTag("creator_stats_row")
    ) {
        // Main 4-Column Stat Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(VyroSurfaceElevated, Color(0xFF141724))
                    )
                )
                .border(1.dp, VyroBorder, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subscribers
            CreatorStatItem(
                icon = Icons.Outlined.People,
                value = formatMetricNumber(followersCount),
                label = "Subscribers",
                badge = "+14%",
                badgeColor = VyroGreenSuccess
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(VyroBorder)
            )

            // Total Views
            CreatorStatItem(
                icon = Icons.Outlined.Visibility,
                value = formatMetricNumber(totalViews),
                label = "Total Views",
                badge = "⚡ Active",
                badgeColor = VyroCyanSecondary
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(VyroBorder)
            )

            // Total Likes
            CreatorStatItem(
                icon = Icons.Outlined.FavoriteBorder,
                value = formatMetricNumber(totalLikes),
                label = "Likes",
                badge = "98% +",
                badgeColor = VyroVioletLight
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(VyroBorder)
            )

            // Uploads
            CreatorStatItem(
                icon = Icons.Outlined.VideoLibrary,
                value = "$videoCount",
                label = "Videos",
                badge = "4K HD",
                badgeColor = VyroGoldTertiary
            )
        }

        // Expandable Deep Creator Performance Analytics
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleDetails() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showDetails) "Hide Analytics Details" else "View Creator Engagement & Room Cache Stats",
                color = VyroCyanLight,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (showDetails) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = VyroCyanLight,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroSurface)
                    .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 CREATOR METRICS BREAKDOWN",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Cached in Room SQLite",
                        color = VyroGreenSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Engagement Rate",
                        value = "12.4%",
                        subtitle = "Top 5% in category",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Avg. Watch Duration",
                        value = "6m 42s",
                        subtitle = "74% completion",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Creator Monetization",
                        value = "Active",
                        subtitle = "VIP Tier",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    badge: String,
    badgeColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VyroTextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            color = VyroTextMuted,
            fontSize = 10.sp
        )
        Text(
            text = badge,
            color = badgeColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VyroSurfaceElevated)
            .padding(8.dp)
    ) {
        Text(text = title, color = VyroTextMuted, fontSize = 10.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = subtitle, color = VyroCyanLight, fontSize = 9.sp)
    }
}

// ----------------------------------------------------------------------------
// CONTENT GRID CONTROLS (Tabs, Search, Layout Mode Toggle)
// ----------------------------------------------------------------------------
@Composable
private fun ContentGridControls(
    selectedFilter: ProfileContentFilter,
    onSelectFilter: (ProfileContentFilter) -> Unit,
    gridMode: ProfileGridMode,
    onChangeGridMode: (ProfileGridMode) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    totalItemCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("content_filter_tabs")
    ) {
        // Filter Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedFilter.ordinal,
            containerColor = Color.Transparent,
            contentColor = VyroVioletPrimary,
            edgePadding = 0.dp,
            divider = {}
        ) {
            ProfileContentFilter.values().forEach { filter ->
                val selected = selectedFilter == filter
                Tab(
                    selected = selected,
                    onClick = { onSelectFilter(filter) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = filter.icon, fontSize = 12.sp)
                            Text(
                                text = filter.label,
                                color = if (selected) Color.White else VyroTextMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar & Grid Layout Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search in profile field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text("Search in uploads ($totalItemCount)", color = VyroTextMuted, fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = VyroTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = VyroTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroVioletPrimary,
                    unfocusedBorderColor = VyroBorder,
                    focusedContainerColor = VyroSurface,
                    unfocusedContainerColor = VyroSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            )

            // Grid Mode Switchers
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                    .padding(2.dp)
                    .testTag("grid_layout_toggle"),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { onChangeGridMode(ProfileGridMode.TWO_COLUMN) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (gridMode == ProfileGridMode.TWO_COLUMN) VyroVioletPrimary else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "2-Column Grid",
                        tint = if (gridMode == ProfileGridMode.TWO_COLUMN) Color.White else VyroTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onChangeGridMode(ProfileGridMode.THREE_COLUMN) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (gridMode == ProfileGridMode.THREE_COLUMN) VyroVioletPrimary else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Window,
                        contentDescription = "3-Column Grid",
                        tint = if (gridMode == ProfileGridMode.THREE_COLUMN) Color.White else VyroTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onChangeGridMode(ProfileGridMode.LIST_VIEW) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (gridMode == ProfileGridMode.LIST_VIEW) VyroVioletPrimary else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ViewList,
                        contentDescription = "List View",
                        tint = if (gridMode == ProfileGridMode.LIST_VIEW) Color.White else VyroTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GRID ITEM: 2-COLUMN MEDIA CARD
// ----------------------------------------------------------------------------
@Composable
private fun TwoColumnContentGridCard(
    video: Video,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VyroSurfaceElevated)
            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("content_grid_item_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Thumbnail with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                val drawableId = if (video.thumbnailDrawableName == "vyro_thumb_cyber") {
                    R.drawable.vyro_thumb_cyber
                } else {
                    R.drawable.vyro_hero_banner
                }
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xAA0A0B10))
                            )
                        )
                )

                // Duration or Short Badge (Bottom Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (video.isShort) "⚡ Short" else formatSecondsToTime(video.durationSeconds),
                        color = if (video.isShort) VyroGoldTertiary else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // View Count Badge (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = VyroCyanSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatMetricNumber(video.viewCount.toLong()),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.timeAgo,
                        color = VyroTextMuted,
                        fontSize = 10.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Likes",
                            tint = VyroPinkAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatMetricNumber(video.likeCount.toLong()),
                            color = VyroTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GRID ITEM: 3-COLUMN THUMBNAIL CARD
// ----------------------------------------------------------------------------
@Composable
private fun ThreeColumnThumbnailCard(
    video: Video,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(VyroSurfaceElevated)
            .clickable { onClick() }
            .testTag("content_grid_item_${video.id}")
    ) {
        val drawableId = if (video.thumbnailDrawableName == "vyro_thumb_cyber") {
            R.drawable.vyro_thumb_cyber
        } else {
            R.drawable.vyro_hero_banner
        }
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xBB000000))
                    )
                )
        )

        // Top right type pill
        if (video.isShort) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("⚡", fontSize = 9.sp)
            }
        }

        // Bottom views & duration
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        ) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "👁️ ${formatMetricNumber(video.viewCount.toLong())}",
                    color = VyroCyanLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// GRID ITEM: LIST VIEW MODE CARD
// ----------------------------------------------------------------------------
@Composable
private fun ListModeContentCard(
    video: Video,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VyroSurfaceElevated)
            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .testTag("content_grid_item_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 75.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val drawableId = if (video.thumbnailDrawableName == "vyro_thumb_cyber") {
                    R.drawable.vyro_thumb_cyber
                } else {
                    R.drawable.vyro_hero_banner
                }
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (video.isShort) "⚡ Short" else formatSecondsToTime(video.durationSeconds),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatMetricNumber(video.viewCount.toLong())} views",
                        color = VyroCyanLight,
                        fontSize = 11.sp
                    )
                    Text(text = "•", color = VyroTextMuted, fontSize = 11.sp)
                    Text(text = video.timeAgo, color = VyroTextMuted, fontSize = 11.sp)
                }
                if (video.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = video.tags.take(3).joinToString(" ") { "#$it" },
                        color = VyroVioletLight,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = VyroTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// EMPTY CONTENT STATE
// ----------------------------------------------------------------------------
@Composable
private fun EmptyContentState(
    isOwnProfile: Boolean,
    onUploadClick: () -> Unit,
    hasSearch: Boolean,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(VyroSurfaceElevated)
                .border(1.dp, VyroBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.VideoLibrary,
                contentDescription = null,
                tint = VyroCyanLight,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (hasSearch) "No Matching Content" else "No Content in This Category",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (hasSearch) "Try adjusting your search query or tag filters." else "This creator hasn't published content in this tab yet.",
            color = VyroTextSecondary,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (hasSearch) {
            OutlinedButton(
                onClick = onClearSearch,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroCyanLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroCyanSecondary)
            ) {
                Text("Reset Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (isOwnProfile) {
            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload First Video", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// EDIT PROFILE DIALOG (Persists to Room Database)
// ----------------------------------------------------------------------------
@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, country: String, websiteUrl: String, tags: List<String>) -> Unit
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var bio by remember { mutableStateOf(user.bio) }
    var country by remember { mutableStateOf(user.country) }
    var websiteUrl by remember { mutableStateOf(user.websiteUrl) }
    var tagsInput by remember { mutableStateOf(user.categoryTags.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = VyroVioletLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Creator Profile",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Changes will be immediately persisted to Room SQLite database cache.",
                    color = VyroGreenSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                // Display Name
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Biography
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biography") },
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Location / Country
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country / Location") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Website URL
                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Website / Portfolio URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tags
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Specialty Tags (comma-separated)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedTags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(displayName, bio, country, websiteUrl, parsedTags)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary)
            ) {
                Text("Save to Room Cache", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VyroTextMuted)
            }
        },
        containerColor = VyroSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    )
}

// ----------------------------------------------------------------------------
// UTILITY FORMATTING HELPERS
// ----------------------------------------------------------------------------
private fun formatMetricNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> "$number"
    }
}

private fun formatSecondsToTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
