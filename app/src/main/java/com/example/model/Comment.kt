package com.example.model

data class Comment(
    val id: String,
    val videoId: String,
    val userId: String,
    val username: String,
    val userDisplayName: String,
    val userAvatar: String = "",
    val isVerified: Boolean = false,
    val text: String,
    val timeAgo: String = "Just now",
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isCreatorPinned: Boolean = false,
    val replies: List<Comment> = emptyList()
)
