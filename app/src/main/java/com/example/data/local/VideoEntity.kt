package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatar: String,
    val isVerifiedCreator: Boolean,
    val thumbnailUrl: String,
    val thumbnailDrawableName: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val isShort: Boolean,
    val categoryName: String,
    val tagsString: String, // Comma separated
    val visibility: String,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val timeAgo: String,
    val tipsEnabled: Boolean,
    val tipsTotalEarned: Double,
    val isSavedBookmark: Boolean = false,
    val isLikedByUser: Boolean = false,
    val isUserUploaded: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
