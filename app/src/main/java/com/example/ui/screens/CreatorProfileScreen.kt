package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.User
import com.example.model.Video
import com.example.ui.components.UserProfileComponent
import com.example.ui.theme.VyroBackground

/**
 * Creator Profile Screen incorporating Room SQLite caching, stats, bio, and content grid.
 */
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
    onTipClick: () -> Unit,
    onLikeVideo: (String) -> Unit = {},
    onBookmarkVideo: (String) -> Unit = {},
    onShareProfile: () -> Unit = {},
    onUpdateProfile: (displayName: String, bio: String, country: String, websiteUrl: String, tags: List<String>) -> Unit = { _, _, _, _, _ -> }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("creator_profile_screen")
    ) {
        UserProfileComponent(
            user = user,
            videos = creatorVideos,
            isOwnProfile = isOwnProfile,
            isFollowed = isFollowed,
            onFollowClick = onFollowClick,
            onTipClick = onTipClick,
            onOpenStudio = onOpenStudio,
            onVideoClick = onVideoClick,
            onLikeVideo = onLikeVideo,
            onBookmarkVideo = onBookmarkVideo,
            onShareProfile = onShareProfile,
            onUpdateProfile = onUpdateProfile
        )
    }
}
