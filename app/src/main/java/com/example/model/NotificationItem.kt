package com.example.model

enum class NotificationType {
    NEW_FOLLOWER,
    NEW_COMMENT,
    COMMENT_REPLY,
    CREATOR_UPLOAD,
    LIVE_STREAM,
    TIP_RECEIVED,
    SYSTEM_ANNOUNCEMENT
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val actorAvatar: String = "",
    val targetVideoId: String? = null,
    val isRead: Boolean = false
)
