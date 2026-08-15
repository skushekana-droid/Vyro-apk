package com.example.data.repository

import android.content.Context
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.local.VyroDatabase
import com.example.infrastructure.adapters.VyroIntegrationHub
import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import com.example.infrastructure.video.VyroVideoEngine
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VyroRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val database = VyroDatabase.getDatabase(context)
    private val videoDao: VideoDao = database.videoDao()
    private val userDao: UserDao = database.userDao()
    val integrationHub = VyroIntegrationHub

    private val _currentUser = MutableStateFlow(createInitialUser())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    private val _communities = MutableStateFlow(createInitialCommunities())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _walletState = MutableStateFlow(createInitialWalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _creatorAnalytics = MutableStateFlow(createInitialAnalytics())
    val creatorAnalytics: StateFlow<CreatorAnalytics> = _creatorAnalytics.asStateFlow()

    private val _moderationItems = MutableStateFlow(createInitialModerationQueue())
    val moderationItems: StateFlow<List<ModerationItem>> = _moderationItems.asStateFlow()

    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(createInitialComments())
    val commentsMap: StateFlow<Map<String, List<Comment>>> = _commentsMap.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        scope.launch {
            loadOrSeedVideos()
        }
    }

    private suspend fun loadOrSeedVideos() {
        val initialList = createInitialSeedVideos()
        val entities = initialList.map { it.toEntity() }
        videoDao.insertVideos(entities)

        videoDao.getAllVideos().collect { entityList ->
            if (entityList.isNotEmpty()) {
                val domainList = entityList.map { entity ->
                    val originalMatch = initialList.find { it.id == entity.id }
                    entity.toDomain(originalMatch?.linkedProduct)
                }
                _videos.value = domainList
            } else {
                _videos.value = initialList
            }
        }
    }

    fun signIn(email: String, role: UserRole = UserRole.CREATOR) {
        val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        _currentUser.value = _currentUser.value.copy(
            id = "user_${UUID.randomUUID().toString().take(6)}",
            username = "@${name.lowercase()}",
            displayName = name,
            role = role,
            isCreator = (role == UserRole.CREATOR || role == UserRole.ADMIN)
        )
        _isLoggedIn.value = true
    }

    fun signOut() {
        _isLoggedIn.value = false
    }

    fun switchDemoAccount(role: UserRole) {
        when (role) {
            UserRole.VIEWER -> {
                _currentUser.value = User(
                    id = "viewer_1",
                    username = "@alex_view",
                    displayName = "Alex Reed",
                    bio = "Tech enthusiast, sci-fi buff, and digital economy observer.",
                    country = "United States",
                    followersCount = 142,
                    followingCount = 89,
                    totalViews = 1200,
                    role = UserRole.VIEWER,
                    isCreator = false,
                    membershipTier = MembershipTier.FREE,
                    walletBalance = 150.00
                )
            }
            UserRole.CREATOR -> {
                _currentUser.value = User(
                    id = "creator_me",
                    username = "@kael_orion",
                    displayName = "Kael Orion",
                    bio = "Building the future of digital cinema & cybernetics on VYRO. 🚀",
                    country = "Canada",
                    followersCount = 48200,
                    followingCount = 156,
                    totalViews = 890400,
                    role = UserRole.CREATOR,
                    isCreator = true,
                    isVerified = true,
                    membershipTier = MembershipTier.CREATOR_VIP,
                    walletBalance = 3420.50,
                    pendingEarnings = 780.00
                )
            }
            UserRole.BUSINESS -> {
                _currentUser.value = User(
                    id = "biz_1",
                    username = "@synthetix_lab",
                    displayName = "Synthetix Labs",
                    bio = "Next-generation audio hardware & generative audio plugin creators.",
                    country = "Germany",
                    followersCount = 112000,
                    followingCount = 45,
                    totalViews = 2400000,
                    role = UserRole.BUSINESS,
                    isCreator = true,
                    isVerified = true,
                    membershipTier = MembershipTier.CREATOR_VIP,
                    walletBalance = 18450.00
                )
            }
            UserRole.ADMIN -> {
                _currentUser.value = User(
                    id = "admin_super",
                    username = "@vyro_admin",
                    displayName = "VYRO Safety & Governance",
                    bio = "Platform Security, Economy Oversight & Community Guardian.",
                    country = "Global HQ",
                    followersCount = 999999,
                    followingCount = 0,
                    totalViews = 10000000,
                    role = UserRole.ADMIN,
                    isCreator = true,
                    isVerified = true,
                    membershipTier = MembershipTier.CREATOR_VIP,
                    walletBalance = 50000.00
                )
            }
        }
        _isLoggedIn.value = true
    }

    fun toggleLike(videoId: String) {
        val currentLiked = _currentUser.value.likedVideoIds.contains(videoId)
        val updatedSet = if (currentLiked) {
            _currentUser.value.likedVideoIds - videoId
        } else {
            _currentUser.value.likedVideoIds + videoId
        }
        _currentUser.value = _currentUser.value.copy(likedVideoIds = updatedSet)

        _videos.value = _videos.value.map { video ->
            if (video.id == videoId) {
                val newLikeCount = if (currentLiked) (video.likeCount - 1).coerceAtLeast(0) else video.likeCount + 1
                video.copy(likeCount = newLikeCount)
            } else video
        }

        scope.launch {
            videoDao.updateLikeStatus(videoId, !currentLiked, if (currentLiked) -1 else 1)
        }
    }

    fun toggleBookmark(videoId: String) {
        val currentBookmarked = _currentUser.value.bookmarkedVideoIds.contains(videoId)
        val updatedSet = if (currentBookmarked) {
            _currentUser.value.bookmarkedVideoIds - videoId
        } else {
            _currentUser.value.bookmarkedVideoIds + videoId
        }
        _currentUser.value = _currentUser.value.copy(bookmarkedVideoIds = updatedSet)

        scope.launch {
            videoDao.updateBookmarkStatus(videoId, !currentBookmarked)
        }
    }

    fun toggleFollow(creatorId: String) {
        val isFollowed = _currentUser.value.followedCreatorIds.contains(creatorId)
        val updatedSet = if (isFollowed) {
            _currentUser.value.followedCreatorIds - creatorId
        } else {
            _currentUser.value.followedCreatorIds + creatorId
        }
        _currentUser.value = _currentUser.value.copy(
            followedCreatorIds = updatedSet,
            followingCount = if (isFollowed) (_currentUser.value.followingCount - 1).coerceAtLeast(0) else _currentUser.value.followingCount + 1
        )
    }

    fun addComment(videoId: String, text: String) {
        if (text.isBlank()) return
        val currentComments = _commentsMap.value[videoId] ?: emptyList()
        val newComment = Comment(
            id = "c_${UUID.randomUUID().toString().take(6)}",
            videoId = videoId,
            userId = _currentUser.value.id,
            username = _currentUser.value.username,
            userDisplayName = _currentUser.value.displayName,
            userAvatar = _currentUser.value.avatarUrl,
            isVerified = _currentUser.value.isVerified,
            text = text,
            timeAgo = "Just now",
            likeCount = 0,
            isLiked = false
        )
        val updatedMap = _commentsMap.value.toMutableMap()
        updatedMap[videoId] = listOf(newComment) + currentComments
        _commentsMap.value = updatedMap

        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(commentCount = v.commentCount + 1) else v
        }
    }

    fun toggleCommentLike(videoId: String, commentId: String) {
        val currentList = _commentsMap.value[videoId] ?: return
        val updated = currentList.map { c ->
            if (c.id == commentId) {
                val newLiked = !c.isLiked
                c.copy(
                    isLiked = newLiked,
                    likeCount = if (newLiked) c.likeCount + 1 else (c.likeCount - 1).coerceAtLeast(0)
                )
            } else c
        }
        val map = _commentsMap.value.toMutableMap()
        map[videoId] = updated
        _commentsMap.value = map
    }

    fun sendTip(videoId: String, creatorName: String, amount: Double, note: String): Boolean {
        if (_currentUser.value.walletBalance < amount && _currentUser.value.role == UserRole.VIEWER) {
            // Adjust balance for demo friendliness
        }
        val newBalance = (_currentUser.value.walletBalance - amount).coerceAtLeast(0.0)
        _currentUser.value = _currentUser.value.copy(walletBalance = newBalance)

        scope.launch {
            integrationHub.paymentEngine.processTip(
                fromUserId = _currentUser.value.id,
                toCreatorId = creatorName,
                amount = amount,
                note = note
            )
        }

        val tx = WalletTransaction(
            id = "tx_${UUID.randomUUID().toString().take(6)}",
            title = "Tip sent to $creatorName",
            subtitle = if (note.isNotBlank()) "\"$note\"" else "Direct creator support",
            amount = -amount,
            type = TransactionType.TIP_SENT,
            timestamp = "Just now"
        )
        _walletState.value = _walletState.value.copy(
            availableBalance = newBalance,
            transactions = listOf(tx) + _walletState.value.transactions
        )

        _videos.value = _videos.value.map { v ->
            if (v.id == videoId) v.copy(tipsTotalEarned = v.tipsTotalEarned + amount) else v
        }
        return true
    }

    fun uploadVideo(
        title: String,
        description: String,
        category: ContentCategory,
        tags: List<String>,
        isShort: Boolean,
        visibility: ContentVisibility,
        productTitle: String? = null,
        productPrice: Double? = null
    ) {
        val newId = "vid_${UUID.randomUUID().toString().take(6)}"
        val product = if (!productTitle.isNullOrBlank() && productPrice != null) {
            LinkedProduct(
                id = "prod_${UUID.randomUUID().toString().take(4)}",
                title = productTitle,
                price = productPrice,
                storeName = "${_currentUser.value.displayName} Store"
            )
        } else null

        val video = Video(
            id = newId,
            title = title,
            description = description,
            creatorId = _currentUser.value.id,
            creatorName = _currentUser.value.displayName,
            creatorUsername = _currentUser.value.username,
            isVerifiedCreator = _currentUser.value.isVerified,
            thumbnailDrawableName = if (isShort) "vyro_thumb_cyber" else "vyro_hero_banner",
            durationSeconds = if (isShort) 45 else 360,
            isShort = isShort,
            category = category,
            tags = tags,
            visibility = visibility,
            viewCount = 1,
            likeCount = 0,
            commentCount = 0,
            timeAgo = "Just now",
            linkedProduct = product
        )

        _videos.value = listOf(video) + _videos.value
        scope.launch {
            videoDao.insertVideo(video.toEntity(isUserUploaded = true))
            // Execute independent FFmpeg ladder transcoding pipeline asynchronously
            VyroVideoEngine.submitVideoJob(
                title = title,
                creatorId = _currentUser.value.id,
                fileSizeMb = if (isShort) 45.0 else 420.0,
                durationSeconds = if (isShort) 45 else 480,
                isShort = isShort
            )
        }

        // Add creator upload notification
        val notif = NotificationItem(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            title = "Video Published & Transcoded",
            message = "\"$title\" is now live across the VYRO Global Edge Network.",
            timeAgo = "Just now",
            type = NotificationType.CREATOR_UPLOAD,
            targetVideoId = newId
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun deleteVideo(videoId: String) {
        _videos.value = _videos.value.filter { it.id != videoId }
        scope.launch {
            videoDao.deleteVideo(videoId)
        }
    }

    fun joinCommunity(communityId: String) {
        _communities.value = _communities.value.map { c ->
            if (c.id == communityId) {
                val newJoined = !c.isJoined
                c.copy(
                    isJoined = newJoined,
                    membersCount = if (newJoined) c.membersCount + 1 else (c.membersCount - 1).coerceAtLeast(0)
                )
            } else c
        }
    }

    fun votePoll(communityId: String, postId: String, optionIndex: Int) {
        _communities.value = _communities.value.map { community ->
            if (community.id == communityId) {
                val updatedPosts = community.recentPosts.map { post ->
                    if (post.id == postId && post.userVotedOptionIndex == null) {
                        val currentVotes = post.pollVotes.toMutableList()
                        if (optionIndex in currentVotes.indices) {
                            currentVotes[optionIndex] = currentVotes[optionIndex] + 1
                        }
                        post.copy(
                            pollVotes = currentVotes,
                            userVotedOptionIndex = optionIndex
                        )
                    } else post
                }
                community.copy(recentPosts = updatedPosts)
            } else community
        }
    }

    fun moderateContent(itemId: String, newStatus: ModerationStatus) {
        _moderationItems.value = _moderationItems.value.map { item ->
            if (item.id == itemId) item.copy(status = newStatus) else item
        }
    }

    suspend fun generateAiAssistance(taskType: AiTaskType, input: String): String {
        return integrationHub.aiEngine.generateText(taskType, input)
    }

    // Seed Data Creators
    private fun createInitialUser(): User {
        return User(
            id = "creator_me",
            username = "@kael_orion",
            displayName = "Kael Orion",
            bio = "Building cinematic cybernetics and next-gen storytelling on VYRO.",
            country = "United States",
            followersCount = 48200,
            followingCount = 142,
            totalViews = 890400,
            isCreator = true,
            isVerified = true,
            role = UserRole.CREATOR,
            membershipTier = MembershipTier.CREATOR_VIP,
            walletBalance = 3420.50,
            pendingEarnings = 780.00,
            followedCreatorIds = setOf("creator_2", "creator_3")
        )
    }

    private fun createInitialSeedVideos(): List<Video> {
        return listOf(
            Video(
                id = "v_1",
                title = "The 2026 Creator Economy Shift: How Attention Becomes Capital",
                description = "Deep dive into the architecture of modern media platforms, decentralized creator income, direct audience commerce, and why algorithmic monopolies are fading.",
                creatorId = "creator_1",
                creatorName = "Elena Vance",
                creatorUsername = "@elenavance",
                isVerifiedCreator = true,
                thumbnailDrawableName = "vyro_hero_banner",
                durationSeconds = 840,
                isShort = false,
                category = ContentCategory.ECONOMY,
                tags = listOf("Economy", "Creators", "VYRO", "FutureTech", "Business"),
                viewCount = 142800,
                likeCount = 12400,
                commentCount = 890,
                shareCount = 3200,
                timeAgo = "3 hours ago",
                tipsTotalEarned = 340.00,
                linkedProduct = LinkedProduct(
                    id = "p_1",
                    title = "Creator Economy Master Blueprint 2026",
                    price = 29.99,
                    storeName = "Vance Media Lab",
                    salesCount = 412
                )
            ),
            Video(
                id = "v_2",
                title = "Building a Synthwave Studio with Generative AI & Hardware DSP",
                description = "Walkthrough of our hybrid analog synthesizer workflow integrated with real-time neural audio plugins.",
                creatorId = "creator_2",
                creatorName = "Synthetix Labs",
                creatorUsername = "@synthetix_lab",
                isVerifiedCreator = true,
                thumbnailDrawableName = "vyro_thumb_cyber",
                durationSeconds = 620,
                isShort = false,
                category = ContentCategory.MUSIC,
                tags = listOf("Audio", "Synthesizer", "AI", "MusicProduction"),
                viewCount = 89400,
                likeCount = 8200,
                commentCount = 412,
                shareCount = 1540,
                timeAgo = "6 hours ago",
                tipsTotalEarned = 190.00,
                linkedProduct = LinkedProduct(
                    id = "p_2",
                    title = "Cyberwave Preset Bank (VYRO Exclusive)",
                    price = 14.50,
                    storeName = "Synthetix DSP",
                    salesCount = 890
                )
            ),
            Video(
                id = "v_3",
                title = "Zero to 100K Followers in 30 Days? The Science of Retention",
                description = "We analyzed 50,000 viral shorts on VYRO. Here are the 4 micro-hook retention patterns that top creators execute.",
                creatorId = "creator_me",
                creatorName = "Kael Orion",
                creatorUsername = "@kael_orion",
                isVerifiedCreator = true,
                thumbnailDrawableName = "vyro_thumb_cyber",
                durationSeconds = 48,
                isShort = true,
                category = ContentCategory.CREATIVE,
                tags = listOf("Shorts", "Growth", "ViralStrategy", "VYRO"),
                viewCount = 312000,
                likeCount = 28900,
                commentCount = 1420,
                shareCount = 9800,
                timeAgo = "1 day ago",
                soundTrackTitle = "Kael Orion - Cyberpulse Beats",
                tipsTotalEarned = 450.00
            ),
            Video(
                id = "v_4",
                title = "Autonomous AI Agents in Production: Beyond the Hype",
                description = "Complete live walkthrough benchmarking multimodal reasoning models against real distributed systems architectures.",
                creatorId = "creator_3",
                creatorName = "Nexus AI Research",
                creatorUsername = "@nexus_ai",
                isVerifiedCreator = true,
                thumbnailDrawableName = "vyro_hero_banner",
                durationSeconds = 980,
                isShort = false,
                category = ContentCategory.TECH_AI,
                tags = listOf("AI", "Agents", "Architecture", "Engineering"),
                viewCount = 194500,
                likeCount = 16700,
                commentCount = 980,
                shareCount = 4800,
                timeAgo = "2 days ago",
                tipsTotalEarned = 520.00
            ),
            Video(
                id = "v_5",
                title = "How Game Studios Build 100-Player VR Worlds in Real-Time",
                description = "Inside the volumetric rendering pipeline and spatial network sync of Project Chimera.",
                creatorId = "creator_4",
                creatorName = "Apex Simulation",
                creatorUsername = "@apex_sim",
                isVerifiedCreator = true,
                thumbnailDrawableName = "vyro_thumb_cyber",
                durationSeconds = 54,
                isShort = true,
                category = ContentCategory.GAMING,
                tags = listOf("Gaming", "VR", "Unreal", "GameDev"),
                viewCount = 245000,
                likeCount = 21400,
                commentCount = 1100,
                shareCount = 6400,
                timeAgo = "2 days ago",
                soundTrackTitle = "Apex Sim - Neon Hyperdrive",
                tipsTotalEarned = 180.00
            )
        )
    }

    private fun createInitialCommunities(): List<Community> {
        return listOf(
            Community(
                id = "comm_1",
                name = "AI & Future Technology",
                handle = "c/future-tech",
                description = "Engineers, researchers, and creators building generative AI, neural hardware, and autonomous software.",
                category = "Technology",
                membersCount = 142500,
                isJoined = true,
                recentPosts = listOf(
                    CommunityPost(
                        id = "post_1",
                        authorName = "Elena Vance",
                        authorUsername = "@elenavance",
                        isAuthorVerified = true,
                        content = "Which AI modality will create the highest economic return for creators in 2026?",
                        timeAgo = "2h ago",
                        likesCount = 384,
                        commentsCount = 92,
                        pollOptions = listOf("Generative Video (Veo/Sora)", "Interactive Neural Voice", "Real-Time 3D Avatars", "AI-Assisted Workflow Tools"),
                        pollVotes = listOf(312, 145, 88, 520),
                        isLiked = false
                    )
                )
            ),
            Community(
                id = "comm_2",
                name = "Digital Cinema & VFX",
                handle = "c/digital-cinema",
                description = "Independent directors, colorists, 3D artists, and virtual production enthusiasts sharing workflows and gear.",
                category = "Art & Cinema",
                membersCount = 89200,
                isJoined = true,
                recentPosts = listOf(
                    CommunityPost(
                        id = "post_2",
                        authorName = "Kael Orion",
                        authorUsername = "@kael_orion",
                        isAuthorVerified = true,
                        content = "Just wrapped the color grade on the upcoming sci-fi short 'Obsidian Dawn'. Dropping the 4K premiere exclusively on VYRO this Friday! 🔥",
                        timeAgo = "5h ago",
                        likesCount = 512,
                        commentsCount = 64
                    )
                )
            ),
            Community(
                id = "comm_3",
                name = "Creator Economy & Monetization",
                handle = "c/creator-economy",
                description = "Strategies for building direct-to-consumer digital businesses, paid memberships, and high-CTR content engines.",
                category = "Business",
                membersCount = 64800,
                isJoined = false,
                recentPosts = emptyList()
            ),
            Community(
                id = "comm_4",
                name = "Cyber Gaming & Esports",
                handle = "c/cyber-gaming",
                description = "Competitive gaming tournaments, speedruns, custom hardware rigs, and game engine mods.",
                category = "Gaming",
                membersCount = 210000,
                isJoined = false,
                recentPosts = emptyList()
            )
        )
    }

    private fun createInitialNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "n_1",
                title = "New Supporter Tip!",
                message = "Alex Reed sent you a $25.00 tip on 'Zero to 100K Followers'.",
                timeAgo = "15m ago",
                type = NotificationType.TIP_RECEIVED
            ),
            NotificationItem(
                id = "n_2",
                title = "Elena Vance published a new video",
                message = "The 2026 Creator Economy Shift: How Attention Becomes Capital.",
                timeAgo = "3h ago",
                type = NotificationType.CREATOR_UPLOAD,
                targetVideoId = "v_1"
            ),
            NotificationItem(
                id = "n_3",
                title = "Trending Milestone",
                message = "Your short video crossed 300,000 views and 25,000 likes on VYRO Explore!",
                timeAgo = "1d ago",
                type = NotificationType.SYSTEM_ANNOUNCEMENT,
                targetVideoId = "v_3"
            )
        )
    }

    private fun createInitialWalletState(): WalletState {
        return WalletState(
            availableBalance = 3420.50,
            pendingEarnings = 780.00,
            totalLifetimeEarned = 14850.00,
            transactions = listOf(
                WalletTransaction(
                    id = "tx_1",
                    title = "Ad Revenue Payout (July 2026)",
                    subtitle = "VYRO Partner Program Ad Share",
                    amount = 1850.20,
                    type = TransactionType.AD_REVENUE_PAYOUT,
                    timestamp = "Aug 1, 2026"
                ),
                WalletTransaction(
                    id = "tx_2",
                    title = "Direct Creator Tips",
                    subtitle = "42 Viewer Tips on 'Retention Science'",
                    amount = 410.30,
                    type = TransactionType.TIP_RECEIVED,
                    timestamp = "Aug 10, 2026"
                ),
                WalletTransaction(
                    id = "tx_3",
                    title = "Store Item Sales (Blueprint)",
                    subtitle = "18 Digital Downloads on VYRO Store",
                    amount = 539.82,
                    type = TransactionType.MARKETPLACE_SALE,
                    timestamp = "Aug 12, 2026"
                )
            )
        )
    }

    private fun createInitialAnalytics(): CreatorAnalytics {
        return CreatorAnalytics(
            totalViews = 890400,
            watchTimeHours = 18450.0,
            totalFollowers = 48200,
            followerGrowthRate = 22.4,
            averageEngagementRate = 9.8,
            estimatedTotalRevenue = 3420.50,
            adShareRevenue = 1850.20,
            membershipRevenue = 980.00,
            tipRevenue = 410.30,
            marketplaceRevenue = 180.00,
            dailyMetrics = listOf(
                DailyMetric("Mon", 24000, 120.0, 320),
                DailyMetric("Tue", 31000, 160.0, 480),
                DailyMetric("Wed", 45000, 240.0, 690),
                DailyMetric("Thu", 38000, 190.0, 510),
                DailyMetric("Fri", 52000, 290.0, 840),
                DailyMetric("Sat", 68000, 380.0, 1100),
                DailyMetric("Sun", 74000, 420.0, 1340)
            ),
            topGeos = listOf(
                AudienceDemographic("United States", 38.5f),
                AudienceDemographic("United Kingdom", 14.2f),
                AudienceDemographic("Germany", 12.0f),
                AudienceDemographic("Canada", 9.8f),
                AudienceDemographic("Japan", 8.4f),
                AudienceDemographic("Other", 17.1f)
            ),
            trafficSources = listOf(
                TrafficSource("VYRO Discovery Feed", 46.2f),
                TrafficSource("Shorts Feed", 32.8f),
                TrafficSource("Community Hubs", 11.5f),
                TrafficSource("Direct & Search", 9.5f)
            )
        )
    }

    private fun createInitialComments(): Map<String, List<Comment>> {
        return mapOf(
            "v_1" to listOf(
                Comment(
                    id = "c_1",
                    videoId = "v_1",
                    userId = "creator_2",
                    username = "@synthetix_lab",
                    userDisplayName = "Synthetix Labs",
                    isVerified = true,
                    text = "The point on direct creator commerce at 6:40 is spot on. Selling plugins directly on VYRO eliminated 4 middleman fees for us.",
                    timeAgo = "2h ago",
                    likeCount = 142,
                    isCreatorPinned = true
                ),
                Comment(
                    id = "c_2",
                    videoId = "v_1",
                    userId = "viewer_1",
                    username = "@alex_view",
                    userDisplayName = "Alex Reed",
                    isVerified = false,
                    text = "This is the clearest explanation of why modern creator platforms need native economic rails. Subscribed!",
                    timeAgo = "1h ago",
                    likeCount = 38
                )
            ),
            "v_3" to listOf(
                Comment(
                    id = "c_3",
                    videoId = "v_3",
                    userId = "creator_1",
                    username = "@elenavance",
                    userDisplayName = "Elena Vance",
                    isVerified = true,
                    text = "Hook pattern #2 works like magic on the algorithm! 🔥",
                    timeAgo = "18h ago",
                    likeCount = 284
                )
            )
        )
    }

    private fun createInitialModerationQueue(): List<ModerationItem> {
        return listOf(
            ModerationItem(
                id = "mod_1",
                contentTitle = "Crypto Pump & Dump Fast Money Scheme",
                creatorName = "AnonymousUser99",
                contentType = "Short Video",
                reportCount = 14,
                reason = ReportReason.SPAM_OR_SCAM,
                status = ModerationStatus.PENDING,
                aiSafetyScore = 0.28f,
                timestamp = "30m ago"
            ),
            ModerationItem(
                id = "mod_2",
                contentTitle = "Unauthorized Movie Rip - CyberCity 2026",
                creatorName = "MediaPirateX",
                contentType = "Long Video",
                reportCount = 8,
                reason = ReportReason.COPYRIGHT,
                status = ModerationStatus.PENDING,
                aiSafetyScore = 0.35f,
                timestamp = "1h ago"
            )
        )
    }

    // Mapping helpers
    private fun Video.toEntity(isUserUploaded: Boolean = false): VideoEntity {
        return VideoEntity(
            id = id,
            title = title,
            description = description,
            creatorId = creatorId,
            creatorName = creatorName,
            creatorUsername = creatorUsername,
            creatorAvatar = creatorAvatar,
            isVerifiedCreator = isVerifiedCreator,
            thumbnailUrl = thumbnailUrl,
            thumbnailDrawableName = thumbnailDrawableName,
            videoUrl = videoUrl,
            durationSeconds = durationSeconds,
            isShort = isShort,
            categoryName = category.name,
            tagsString = tags.joinToString(","),
            visibility = visibility.name,
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = commentCount,
            shareCount = shareCount,
            timeAgo = timeAgo,
            tipsEnabled = tipsEnabled,
            tipsTotalEarned = tipsTotalEarned,
            isSavedBookmark = false,
            isLikedByUser = false,
            isUserUploaded = isUserUploaded
        )
    }

    private fun VideoEntity.toDomain(linkedProduct: LinkedProduct? = null): Video {
        val cat = try {
            ContentCategory.valueOf(categoryName)
        } catch (e: Exception) {
            ContentCategory.TRENDING
        }
        val vis = try {
            ContentVisibility.valueOf(visibility)
        } catch (e: Exception) {
            ContentVisibility.PUBLIC
        }
        return Video(
            id = id,
            title = title,
            description = description,
            creatorId = creatorId,
            creatorName = creatorName,
            creatorUsername = creatorUsername,
            creatorAvatar = creatorAvatar,
            isVerifiedCreator = isVerifiedCreator,
            thumbnailUrl = thumbnailUrl,
            thumbnailDrawableName = thumbnailDrawableName,
            videoUrl = videoUrl,
            durationSeconds = durationSeconds,
            isShort = isShort,
            category = cat,
            tags = if (tagsString.isNotBlank()) tagsString.split(",") else emptyList(),
            visibility = vis,
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = commentCount,
            shareCount = shareCount,
            timeAgo = timeAgo,
            tipsEnabled = tipsEnabled,
            tipsTotalEarned = tipsTotalEarned,
            linkedProduct = linkedProduct
        )
    }
}
