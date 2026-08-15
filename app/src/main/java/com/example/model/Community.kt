package com.example.model

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String = "",
    val isAuthorVerified: Boolean = false,
    val content: String,
    val timeAgo: String = "1h ago",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val pollOptions: List<String> = emptyList(),
    val pollVotes: List<Int> = emptyList(),
    val userVotedOptionIndex: Int? = null,
    val isLiked: Boolean = false
)

data class Community(
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val category: String,
    val membersCount: Long,
    val bannerDrawable: String = "",
    val isJoined: Boolean = false,
    val recentPosts: List<CommunityPost> = emptyList()
)
