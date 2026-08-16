package com.example.model

enum class UserRole {
    VIEWER,
    CREATOR,
    BUSINESS,
    ADMIN
}

enum class MembershipTier {
    FREE,
    VYRO_PLUS,
    CREATOR_FOUNDER,
    CREATOR_VIP
}

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val bio: String = "",
    val country: String = "Global",
    val websiteUrl: String = "https://vyro.media/@creator",
    val dateJoined: String = "August 2026",
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val totalViews: Long = 0,
    val totalLikes: Long = 0,
    val isCreator: Boolean = false,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.VIEWER,
    val membershipTier: MembershipTier = MembershipTier.FREE,
    val walletBalance: Double = 0.0,
    val pendingEarnings: Double = 0.0,
    val subscribedCreatorIds: Set<String> = emptySet(),
    val bookmarkedVideoIds: Set<String> = emptySet(),
    val likedVideoIds: Set<String> = emptySet(),
    val followedCreatorIds: Set<String> = emptySet(),
    val categoryTags: List<String> = listOf("Cinema", "AI", "VFX", "Tech")
)
