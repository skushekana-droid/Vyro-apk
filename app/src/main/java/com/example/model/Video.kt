package com.example.model

enum class ContentVisibility {
    PUBLIC,
    UNLISTED,
    PRIVATE
}

enum class ContentCategory(val displayName: String) {
    ALL("All"),
    TRENDING("Trending"),
    TECH_AI("Tech & AI"),
    GAMING("Gaming"),
    MUSIC("Music"),
    ECONOMY("Economy & Business"),
    CREATIVE("Art & Cinema"),
    EDUCATION("Education"),
    FITNESS("Fitness & Health"),
    PODCASTS("Podcasts")
}

data class LinkedProduct(
    val id: String,
    val title: String,
    val price: Double,
    val imageUrl: String = "",
    val description: String = "",
    val storeName: String = "VYRO Direct",
    val salesCount: Int = 0
)

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatar: String = "",
    val isVerifiedCreator: Boolean = true,
    val thumbnailUrl: String = "",
    val thumbnailDrawableName: String = "",
    val videoUrl: String = "",
    val durationSeconds: Int = 180,
    val isShort: Boolean = false,
    val category: ContentCategory = ContentCategory.TRENDING,
    val tags: List<String> = emptyList(),
    val visibility: ContentVisibility = ContentVisibility.PUBLIC,
    val viewCount: Long = 0,
    val likeCount: Long = 0,
    val dislikeCount: Long = 0,
    val commentCount: Long = 0,
    val shareCount: Long = 0,
    val timeAgo: String = "Just now",
    val tipsEnabled: Boolean = true,
    val tipsTotalEarned: Double = 0.0,
    val linkedProduct: LinkedProduct? = null,
    val soundTrackTitle: String = "Original Sound - VYRO Media",
    val audienceTarget: String = "General Audience",
    val isAiGeneratedMetadata: Boolean = false
)
