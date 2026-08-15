package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Comment
import com.example.model.User
import com.example.model.Video
import com.example.ui.components.InteractivePlayer
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.*

@Composable
fun WatchScreen(
    video: Video,
    relatedVideos: List<Video>,
    currentUser: User,
    comments: List<Comment>,
    onBack: () -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onTipClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onSelectRelatedVideo: (Video) -> Unit,
    onAddComment: (String) -> Unit,
    onToggleCommentLike: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("watch_screen")
    ) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Desktop / Tablet Two-Column Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Player, Video Info, Description, Comments
                LazyColumn(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        InteractivePlayer(video = video)
                    }

                    item {
                        WatchHeaderAndDetails(
                            video = video,
                            currentUser = currentUser,
                            isDescriptionExpanded = isDescriptionExpanded,
                            onToggleDescription = { isDescriptionExpanded = !isDescriptionExpanded },
                            onCreatorClick = onCreatorClick,
                            onLikeClick = onLikeClick,
                            onBookmarkClick = onBookmarkClick,
                            onTipClick = onTipClick,
                            onShareClick = onShareClick,
                            onFollowClick = onFollowClick
                        )
                    }

                    item {
                        WatchCommentsSection(
                            comments = comments,
                            newCommentText = newCommentText,
                            onCommentTextChange = { newCommentText = it },
                            onPostComment = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            onToggleCommentLike = onToggleCommentLike
                        )
                    }
                }

                // Right Column: Related Videos
                LazyColumn(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Related Videos (${relatedVideos.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    items(relatedVideos, key = { it.id }) { relVideo ->
                        VideoFeedCard(
                            video = relVideo,
                            isLiked = currentUser.likedVideoIds.contains(relVideo.id),
                            isBookmarked = currentUser.bookmarkedVideoIds.contains(relVideo.id),
                            onVideoClick = { onSelectRelatedVideo(relVideo) },
                            onCreatorClick = { onCreatorClick(relVideo.creatorId) },
                            onLikeClick = { onLikeClick() },
                            onBookmarkClick = { onBookmarkClick() },
                            onShareClick = { onShareClick() },
                            onTipClick = { onTipClick() }
                        )
                    }
                }
            }
        } else {
            // Mobile Stacked Layout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    InteractivePlayer(video = video)
                }

                item {
                    WatchHeaderAndDetails(
                        video = video,
                        currentUser = currentUser,
                        isDescriptionExpanded = isDescriptionExpanded,
                        onToggleDescription = { isDescriptionExpanded = !isDescriptionExpanded },
                        onCreatorClick = onCreatorClick,
                        onLikeClick = onLikeClick,
                        onBookmarkClick = onBookmarkClick,
                        onTipClick = onTipClick,
                        onShareClick = onShareClick,
                        onFollowClick = onFollowClick
                    )
                }

                item {
                    WatchCommentsSection(
                        comments = comments,
                        newCommentText = newCommentText,
                        onCommentTextChange = { newCommentText = it },
                        onPostComment = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        onToggleCommentLike = onToggleCommentLike
                    )
                }

                item {
                    Text(
                        text = "Related Videos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(relatedVideos, key = { it.id }) { relVideo ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        VideoFeedCard(
                            video = relVideo,
                            isLiked = currentUser.likedVideoIds.contains(relVideo.id),
                            isBookmarked = currentUser.bookmarkedVideoIds.contains(relVideo.id),
                            onVideoClick = { onSelectRelatedVideo(relVideo) },
                            onCreatorClick = { onCreatorClick(relVideo.creatorId) },
                            onLikeClick = { onLikeClick() },
                            onBookmarkClick = { onBookmarkClick() },
                            onShareClick = { onShareClick() },
                            onTipClick = { onTipClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchHeaderAndDetails(
    video: Video,
    currentUser: User,
    isDescriptionExpanded: Boolean,
    onToggleDescription: () -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onTipClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Title & Category
        Text(
            text = video.title,
            color = VyroTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${"%,d".format(video.viewCount)} views • ${video.timeAgo}",
                color = VyroTextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VyroVioletDark)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.category.displayName,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Creator Bar
        val isFollowed = currentUser.followedCreatorIds.contains(video.creatorId)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCreatorClick(video.creatorId) }
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VyroBrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.creatorName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = video.creatorName,
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (video.isVerifiedCreator) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = VyroCyanSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "48.2K subscribers",
                        color = VyroTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) VyroSurfaceElevated else VyroVioletPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("watch_subscribe_btn")
            ) {
                Text(
                    text = if (isFollowed) "Following" else "Follow",
                    color = if (isFollowed) VyroTextSecondary else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row (Like, Tip, Share, Save)
        val isLiked = currentUser.likedVideoIds.contains(video.id)
        val isSaved = currentUser.bookmarkedVideoIds.contains(video.id)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(20.dp))
                    .clickable(onClick = onLikeClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = if (isLiked) VyroCyanSecondary else VyroTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${video.likeCount}",
                    color = if (isLiked) VyroCyanSecondary else VyroTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Direct Tip Creator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroGoldTertiary)
                    .clickable(onClick = onTipClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("watch_tip_creator_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚡", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tip",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Share
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VyroSurfaceElevated)
                    .size(36.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = VyroTextPrimary, modifier = Modifier.size(16.dp))
            }

            // Save to library
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VyroSurfaceElevated)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) VyroCyanSecondary else VyroTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Linked Commerce Product Banner
        video.linkedProduct?.let { product ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroCyanDark.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🛍️ Featured Product", color = VyroCyanLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(product.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("$${"%.2f".format(product.price)}", color = VyroGoldTertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onTipClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VyroCyanSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buy Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Expandable Description & Tags
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggleDescription),
            colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = video.description,
                    color = VyroTextSecondary,
                    fontSize = 12.sp,
                    maxLines = if (isDescriptionExpanded) 100 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (isDescriptionExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        video.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VyroSurfaceHighlight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#$tag", color = VyroCyanLight, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isDescriptionExpanded) "Show Less" else "...more",
                    color = VyroVioletLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun WatchCommentsSection(
    comments: List<Comment>,
    newCommentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit,
    onToggleCommentLike: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Comments (${comments.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Add Comment Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = onCommentTextChange,
                    placeholder = { Text("Add a comment...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onPostComment,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VyroVioletPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Post Comment", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comments List
            comments.forEach { comment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VyroBrandGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.userDisplayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(comment.userDisplayName, color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(comment.timeAgo, color = VyroTextMuted, fontSize = 10.sp)
                        }
                        Text(comment.text, color = VyroTextPrimary, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.clickable { onToggleCommentLike(comment.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like comment",
                            tint = if (comment.isLiked) VyroRose else VyroTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        if (comment.likeCount > 0) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${comment.likeCount}", color = VyroTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
