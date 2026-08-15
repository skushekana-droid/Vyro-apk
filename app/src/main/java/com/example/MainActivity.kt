package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UserRole
import com.example.ui.Screen
import com.example.ui.VyroViewModel
import com.example.ui.components.*
import com.example.ui.navigation.LocalVyroNavController
import com.example.ui.screens.*
import com.example.ui.theme.VyroBackground
import com.example.ui.theme.VyroTheme

class MainActivity : ComponentActivity() {
    private val viewModel: VyroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VyroTheme {
                CompositionLocalProvider(LocalVyroNavController provides viewModel.navController) {
                    VyroApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VyroApp(viewModel: VyroViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val allVideos by viewModel.videos.collectAsStateWithLifecycle()
    val shortsList by viewModel.shortsList.collectAsStateWithLifecycle()
    val communities by viewModel.communities.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    val creatorAnalytics by viewModel.creatorAnalytics.collectAsStateWithLifecycle()
    val moderationItems by viewModel.moderationItems.collectAsStateWithLifecycle()
    val commentsMap by viewModel.commentsMap.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Hardware back handler using centralized controller backstack
    BackHandler(enabled = viewModel.navController.canGoBack) {
        viewModel.popBackStack()
    }

    MainScaffold(
        navigationManager = viewModel.navController,
        currentUser = currentUser,
        unreadNotificationsCount = notifications.size,
        snackbarHostState = snackbarHostState,
        onOpenSearch = { viewModel.switchTab(Screen.DISCOVER) },
        onOpenNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
        onOpenWallet = { viewModel.navigateTo(Screen.WALLET) },
        onOpenVyroPlus = { viewModel.openVyroPlusModal() },
        onSwitchDemoRole = { viewModel.switchDemoRole(it) },
        onSignOut = { viewModel.signOut() }
    ) { currentScreen ->
        when (currentScreen) {
                Screen.LANDING -> {
                    LandingScreen(
                        onExplore = {
                            viewModel.signIn("guest@vyro.media", UserRole.VIEWER)
                        },
                        onGetStarted = {
                            viewModel.openAuthDialog(isSignUp = true)
                        },
                        onQuickLogin = { role ->
                            viewModel.switchDemoRole(role)
                            viewModel.switchTab(Screen.HOME)
                        }
                    )
                }

                Screen.HOME -> {
                    HomeScreen(
                        videos = filteredVideos,
                        currentUser = currentUser,
                        selectedCategory = uiState.selectedCategory,
                        isFollowingOnly = uiState.isFollowingFeedOnly,
                        onSelectCategory = { viewModel.setCategory(it) },
                        onToggleFollowingOnly = { viewModel.setFollowingOnly(it) },
                        onVideoClick = { viewModel.openVideo(it) },
                        onCreatorClick = { viewModel.openCreatorProfile(it) },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        onTipClick = { viewModel.openTipDialog(it) },
                        onShareClick = {
                            viewModel.showSnackbar("Video link copied to clipboard!")
                        }
                    )
                }

                Screen.DISCOVER -> {
                    DiscoverSearchScreen(
                        searchQuery = uiState.searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        videos = allVideos,
                        onVideoClick = { viewModel.openVideo(it) },
                        onCreatorClick = { viewModel.openCreatorProfile(it) },
                        onSelectTag = { tag ->
                            viewModel.setSearchQuery(tag)
                        }
                    )
                }

                Screen.SHORTS -> {
                    ShortsScreen(
                        shortsList = shortsList,
                        currentUser = currentUser,
                        commentsMap = commentsMap,
                        isMuted = uiState.isShortsMuted,
                        initialIndex = uiState.shortsCurrentIndex,
                        onIndexChanged = { viewModel.setShortsIndex(it) },
                        onToggleMute = { viewModel.toggleShortsMuted() },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onFollowClick = { viewModel.toggleFollow(it) },
                        onCreatorClick = { viewModel.openCreatorProfile(it) },
                        onTipClick = { viewModel.openTipDialog(it) },
                        onShareClick = { viewModel.showSnackbar("Short link copied!") },
                        onAddComment = { videoId, text -> viewModel.addComment(videoId, text) },
                        onToggleCommentLike = { videoId, commentId -> viewModel.toggleCommentLike(videoId, commentId) }
                    )
                }

                Screen.WATCH -> {
                    uiState.selectedVideo?.let { selected ->
                        WatchScreen(
                            video = selected,
                            relatedVideos = allVideos,
                            currentUser = currentUser,
                            comments = commentsMap[selected.id] ?: emptyList(),
                            onBack = { viewModel.popBackStack() },
                            onCreatorClick = { viewModel.openCreatorProfile(it) },
                            onLikeClick = { viewModel.toggleLike(selected.id) },
                            onBookmarkClick = { viewModel.toggleBookmark(selected.id) },
                            onTipClick = { viewModel.openTipDialog(selected) },
                            onShareClick = { viewModel.showSnackbar("Share link copied!") },
                            onFollowClick = { viewModel.toggleFollow(selected.creatorId) },
                            onSelectRelatedVideo = { viewModel.openVideo(it) },
                            onAddComment = { text -> viewModel.addComment(selected.id, text) },
                            onToggleCommentLike = { commentId -> viewModel.toggleCommentLike(selected.id, commentId) }
                        )
                    } ?: run {
                        viewModel.switchTab(Screen.HOME)
                    }
                }

                Screen.CREATE -> {
                    UploadScreen(
                        isAiGenerating = uiState.isAiGenerating,
                        aiResult = uiState.aiGeneratedResult,
                        aiCurrentTask = uiState.aiCurrentTask,
                        onGenerateAiHelp = { taskType, input ->
                            viewModel.generateAiCreatorHelp(taskType, input)
                        },
                        onClearAiResult = { viewModel.clearAiResult() },
                        onUploadSubmit = { title, desc, cat, tags, isShort, vis, prodTitle, prodPrice ->
                            viewModel.uploadVideo(title, desc, cat, tags, isShort, vis, prodTitle, prodPrice)
                        }
                    )
                }

                Screen.CREATOR_STUDIO -> {
                    CreatorStudioScreen(
                        user = currentUser,
                        analytics = creatorAnalytics,
                        myVideos = allVideos.filter { it.creatorId == currentUser.id },
                        isAiGenerating = uiState.isAiGenerating,
                        aiResult = uiState.aiGeneratedResult,
                        aiCurrentTask = uiState.aiCurrentTask,
                        onGenerateAiHelp = { taskType, input ->
                            viewModel.generateAiCreatorHelp(taskType, input)
                        },
                        onClearAiResult = { viewModel.clearAiResult() },
                        onDeleteVideo = { viewModel.deleteVideo(it) },
                        onOpenUpload = { viewModel.navigateTo(Screen.CREATE) }
                    )
                }

                Screen.CREATOR_PROFILE -> {
                    val targetCreatorId = uiState.selectedCreatorId ?: currentUser.id
                    val isOwn = targetCreatorId == currentUser.id
                    val profileUser = if (isOwn) currentUser else {
                        val creatorVideo = allVideos.find { it.creatorId == targetCreatorId }
                        currentUser.copy(
                            id = targetCreatorId,
                            displayName = creatorVideo?.creatorName ?: "Featured Creator",
                            username = creatorVideo?.creatorUsername ?: "@creator",
                            bio = "Next-generation storyteller building on the VYRO content economy."
                        )
                    }
                    val creatorVideos = allVideos.filter { it.creatorId == targetCreatorId }

                    CreatorProfileScreen(
                        user = profileUser,
                        creatorVideos = creatorVideos,
                        isOwnProfile = isOwn,
                        isFollowed = currentUser.followedCreatorIds.contains(targetCreatorId),
                        selectedTab = uiState.profileSelectedTab,
                        onTabSelected = { viewModel.setProfileTab(it) },
                        onFollowClick = { viewModel.toggleFollow(targetCreatorId) },
                        onOpenStudio = { viewModel.navigateTo(Screen.CREATOR_STUDIO) },
                        onVideoClick = { viewModel.openVideo(it) },
                        onTipClick = {
                            creatorVideos.firstOrNull()?.let { viewModel.openTipDialog(it) }
                        }
                    )
                }

                Screen.COMMUNITIES -> {
                    CommunitiesScreen(
                        communities = communities,
                        onToggleJoin = { viewModel.joinCommunity(it) },
                        onVotePoll = { commId, postId, optIndex ->
                            viewModel.votePoll(commId, postId, optIndex)
                        }
                    )
                }

                Screen.WALLET -> {
                    WalletScreen(
                        walletState = walletState,
                        onRequestPayout = {
                            viewModel.showSnackbar("Withdrawal processed to linked account.")
                        }
                    )
                }

                Screen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onNotificationClick = { notif ->
                            notif.targetVideoId?.let { vidId ->
                                val target = allVideos.find { it.id == vidId }
                                if (target != null) viewModel.openVideo(target)
                            }
                        }
                    )
                }

                Screen.ADMIN -> {
                    AdminDashboardScreen(
                        queue = moderationItems,
                        onModerateAction = { itemId, status ->
                            viewModel.moderateContent(itemId, status)
                        }
                    )
                }

                Screen.LIVE -> {
                    LiveScreen(
                        currentUser = currentUser,
                        onWatchStream = { stream ->
                            viewModel.showSnackbar("Joined stream: ${stream.title}")
                        },
                        onOpenTipDialog = { streamer, amount ->
                            viewModel.sendTip(amount.toDoubleOrNull() ?: 10.0, "Live tip to $streamer")
                        },
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }

                Screen.PURCHASES -> {
                    PurchasesScreen(
                        currentUser = currentUser,
                        onOpenVyroPlusModal = { viewModel.openVyroPlusModal() },
                        onShowSnackbar = { viewModel.showSnackbar(it) }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        currentUser = currentUser,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateToHealth = { viewModel.navigateTo(Screen.HEALTH) },
                        onNavigateToHelp = { viewModel.navigateTo(Screen.HELP) }
                    )
                }

                Screen.HELP -> {
                    HelpSupportScreen(
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateToHealth = { viewModel.navigateTo(Screen.HEALTH) }
                    )
                }

                Screen.HEALTH -> {
                    ApiHealthScreen(
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateToInfrastructure = { viewModel.navigateTo(Screen.INFRASTRUCTURE) }
                    )
                }

                Screen.NOT_FOUND -> {
                    NotFoundScreen(
                        onNavigateHome = { viewModel.switchTab(Screen.HOME) },
                        onNavigateDiscover = { viewModel.switchTab(Screen.DISCOVER) }
                    )
                }

                Screen.INFRASTRUCTURE -> {
                    InfrastructureScreen(
                        onBack = { viewModel.popBackStack() },
                        activeTranscodingJobs = viewModel.transcodingActiveJobs,
                        completedTranscodingJobs = viewModel.transcodingCompletedJobs,
                        generatedImages = viewModel.generatedImages,
                        isImageGenerating = viewModel.isImageGenerating,
                        generatedVideos = viewModel.generatedVideos,
                        isVideoRendering = viewModel.isVideoRendering,
                        currentAuthService = viewModel.currentAuthService,
                        currentStorageService = viewModel.currentStorageService,
                        currentAiProvider = viewModel.currentAiProvider,
                        currentPaymentAdapter = viewModel.currentPaymentAdapter,
                        currentCdnService = viewModel.currentCdnService,
                        onSwitchAiProvider = { viewModel.switchAiProvider(it) },
                        onSwitchStorage = { viewModel.switchStorageAdapter(it) },
                        onSwitchAuth = { viewModel.switchAuthAdapter(it) },
                        onSwitchPayment = { viewModel.switchPaymentAdapter(it) },
                        onSwitchCdn = { viewModel.switchCdnAdapter(it) },
                        onTriggerTestTranscode = { title, dur, isShort ->
                            viewModel.triggerTranscodingTestJob(title, dur, isShort)
                        },
                        onGenerateAiImage = { prompt, style, ratio ->
                            viewModel.generateAiImage(prompt, style, ratio)
                        },
                        onGenerateAiVideo = { prompt, style, dur ->
                            viewModel.generateAiVideo(prompt, style, dur)
                        },
                        onPublishAiVideo = { projId, title ->
                            viewModel.publishGeneratedVideoToFeed(projId, title)
                        }
                    )
                }
            }
    }

    // Modal Overlays
    if (uiState.showTipDialog && uiState.tipTargetVideo != null) {
        TipCreatorDialog(
            video = uiState.tipTargetVideo!!,
            userWalletBalance = currentUser.walletBalance,
            onDismiss = { viewModel.dismissTipDialog() },
            onSendTip = { amount, note ->
                viewModel.sendTip(amount, note)
            }
        )
    }

    if (uiState.showVyroPlusModal) {
        VyroPlusModal(
            onDismiss = { viewModel.dismissVyroPlusModal() },
            onSubscribe = {
                viewModel.dismissVyroPlusModal()
                viewModel.showSnackbar("Welcome to VYRO+ VIP Pass! ✨")
            }
        )
    }

    if (uiState.showAuthDialog) {
        AuthDialog(
            initialIsSignUp = uiState.authModeIsSignUp,
            onDismiss = { viewModel.dismissAuthDialog() },
            onAuthenticate = { email, role ->
                viewModel.signIn(email, role)
            }
        )
    }
}
