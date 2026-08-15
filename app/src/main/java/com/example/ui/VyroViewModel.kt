package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VyroRepository
import com.example.model.*
import com.example.infrastructure.adapters.VyroIntegrationHub
import com.example.infrastructure.ai.AiProviderType
import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.imagegen.ImageAspectRatio
import com.example.infrastructure.imagegen.VyroImageGenService
import com.example.infrastructure.video.VyroVideoEngine
import com.example.infrastructure.videogen.VyroVideoGenService
import com.example.ui.navigation.NavState
import com.example.ui.navigation.VyroNavController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    LANDING,
    HOME,
    DISCOVER,
    CREATE,
    SHORTS,
    WATCH,
    LIVE,
    CREATOR_STUDIO,
    CREATOR_PROFILE,
    COMMUNITIES,
    WALLET,
    PURCHASES,
    SETTINGS,
    HELP,
    HEALTH,
    NOTIFICATIONS,
    ADMIN,
    INFRASTRUCTURE,
    NOT_FOUND
}

data class UiState(
    val currentScreen: Screen = Screen.HOME,
    val activeTab: Screen = Screen.HOME,
    val selectedVideo: Video? = null,
    val selectedCreatorId: String? = null,
    val selectedCommunityId: String? = null,
    val selectedCategory: ContentCategory = ContentCategory.ALL,
    val searchQuery: String = "",
    val isFollowingFeedOnly: Boolean = false,
    val showTipDialog: Boolean = false,
    val tipTargetVideo: Video? = null,
    val showVyroPlusModal: Boolean = false,
    val showAuthDialog: Boolean = false,
    val authModeIsSignUp: Boolean = false,
    val isAiGenerating: Boolean = false,
    val aiGeneratedResult: String? = null,
    val aiCurrentTask: AiTaskType = AiTaskType.VIRAL_TITLES,
    val snackbarMessage: String? = null,
    val isShortsMuted: Boolean = false,
    val shortsCurrentIndex: Int = 0,
    val profileSelectedTab: Int = 0
)

class VyroViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : AndroidViewModel(application) {

    val repository = VyroRepository(application.applicationContext, viewModelScope)

    // Centralized Navigation Controller with state persistence across config changes & process recreation
    val navController = VyroNavController(savedStateHandle)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User> = repository.currentUser
    val videos: StateFlow<List<Video>> = repository.videos
    val communities: StateFlow<List<Community>> = repository.communities
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val walletState: StateFlow<WalletState> = repository.walletState
    val creatorAnalytics: StateFlow<CreatorAnalytics> = repository.creatorAnalytics
    val moderationItems: StateFlow<List<ModerationItem>> = repository.moderationItems
    val commentsMap: StateFlow<Map<String, List<Comment>>> = repository.commentsMap
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn

    // Synchronize navController StateFlow with UiState
    init {
        viewModelScope.launch {
            navController.navState.collect { nav ->
                _uiState.update { current ->
                    current.copy(
                        currentScreen = nav.currentScreen,
                        activeTab = nav.activeTab,
                        selectedCategory = nav.homeState.selectedCategory,
                        isFollowingFeedOnly = nav.homeState.isFollowingOnly,
                        searchQuery = nav.discoverState.searchQuery,
                        isShortsMuted = nav.shortsState.isMuted,
                        shortsCurrentIndex = nav.shortsState.currentIndex,
                        profileSelectedTab = nav.profileState.selectedTab,
                        selectedCreatorId = nav.selectedCreatorId,
                        selectedVideo = nav.selectedVideo ?: current.selectedVideo
                    )
                }
            }
        }
    }

    // Filtered Feed (Home & Search)
    val filteredVideos: StateFlow<List<Video>> = combine(
        videos,
        _uiState,
        currentUser
    ) { allVideos, state, user ->
        var list = allVideos.filter { !it.isShort }
        if (state.isFollowingFeedOnly) {
            list = list.filter { user.followedCreatorIds.contains(it.creatorId) }
        } else if (state.selectedCategory != ContentCategory.ALL && state.selectedCategory != ContentCategory.TRENDING) {
            list = list.filter { it.category == state.selectedCategory }
        }
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase().trim()
            list = list.filter {
                it.title.lowercase().contains(query) ||
                        it.creatorName.lowercase().contains(query) ||
                        it.tags.any { tag -> tag.lowercase().contains(query) }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortsList: StateFlow<List<Video>> = videos.map { list ->
        list.filter { it.isShort }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Centralized Navigation Actions
    fun navigateTo(screen: Screen) {
        navController.navigate(screen)
    }

    fun switchTab(tab: Screen) {
        navController.switchTab(tab)
    }

    fun popBackStack(): Boolean {
        return navController.popBackStack()
    }

    fun openVideo(video: Video) {
        navController.setSelectedVideo(video)
        if (video.isShort) {
            val shortIndex = shortsList.value.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
            navController.updateShortsIndex(shortIndex)
            navController.navigate(Screen.SHORTS)
        } else {
            navController.navigate(Screen.WATCH)
        }
    }

    fun openCreatorProfile(creatorId: String) {
        navController.setSelectedCreatorId(creatorId)
        navController.navigate(Screen.CREATOR_PROFILE)
    }

    fun setCategory(category: ContentCategory) {
        navController.updateHomeCategory(category)
    }

    fun setFollowingOnly(followingOnly: Boolean) {
        navController.updateHomeFollowingOnly(followingOnly)
    }

    fun setSearchQuery(query: String) {
        navController.updateDiscoverSearch(query)
    }

    fun setShortsIndex(index: Int) {
        navController.updateShortsIndex(index)
    }

    fun setProfileTab(tabIndex: Int) {
        navController.updateProfileTab(tabIndex)
    }

    fun toggleLike(videoId: String) {
        repository.toggleLike(videoId)
        _uiState.value.selectedVideo?.let { current ->
            if (current.id == videoId) {
                val isLiked = currentUser.value.likedVideoIds.contains(videoId)
                val newCount = if (isLiked) (current.likeCount - 1).coerceAtLeast(0) else current.likeCount + 1
                val updatedVideo = current.copy(likeCount = newCount)
                navController.setSelectedVideo(updatedVideo)
                _uiState.update { it.copy(selectedVideo = updatedVideo) }
            }
        }
    }

    fun toggleBookmark(videoId: String) {
        repository.toggleBookmark(videoId)
        showSnackbar("Saved to your Library")
    }

    fun toggleFollow(creatorId: String) {
        repository.toggleFollow(creatorId)
    }

    fun addComment(videoId: String, text: String) {
        repository.addComment(videoId, text)
    }

    fun toggleCommentLike(videoId: String, commentId: String) {
        repository.toggleCommentLike(videoId, commentId)
    }

    fun openTipDialog(video: Video) {
        _uiState.update { it.copy(showTipDialog = true, tipTargetVideo = video) }
    }

    fun dismissTipDialog() {
        _uiState.update { it.copy(showTipDialog = false, tipTargetVideo = null) }
    }

    fun sendTip(amount: Double, note: String) {
        val target = _uiState.value.tipTargetVideo ?: return
        val success = repository.sendTip(target.id, target.creatorName, amount, note)
        if (success) {
            dismissTipDialog()
            showSnackbar("Sent $${"%.2f".format(amount)} tip to ${target.creatorName}! 🎉")
        }
    }

    fun openVyroPlusModal() {
        _uiState.update { it.copy(showVyroPlusModal = true) }
    }

    fun dismissVyroPlusModal() {
        _uiState.update { it.copy(showVyroPlusModal = false) }
    }

    fun openAuthDialog(isSignUp: Boolean = false) {
        _uiState.update { it.copy(showAuthDialog = true, authModeIsSignUp = isSignUp) }
    }

    fun dismissAuthDialog() {
        _uiState.update { it.copy(showAuthDialog = false) }
    }

    fun signIn(email: String, role: UserRole = UserRole.CREATOR) {
        repository.signIn(email, role)
        dismissAuthDialog()
        navController.switchTab(Screen.HOME)
        showSnackbar("Welcome back to VYRO!")
    }

    fun signOut() {
        repository.signOut()
        navController.clearTo(Screen.LANDING)
    }

    fun switchDemoRole(role: UserRole) {
        repository.switchDemoAccount(role)
        showSnackbar("Switched to ${role.name.lowercase().replaceFirstChar { it.uppercase() }} Mode")
    }

    fun uploadVideo(
        title: String,
        description: String,
        category: ContentCategory,
        tags: List<String>,
        isShort: Boolean,
        visibility: ContentVisibility,
        productTitle: String?,
        productPrice: Double?
    ) {
        repository.uploadVideo(
            title = title,
            description = description,
            category = category,
            tags = tags,
            isShort = isShort,
            visibility = visibility,
            productTitle = productTitle,
            productPrice = productPrice
        )
        showSnackbar("Video published to VYRO!")
        navController.switchTab(if (isShort) Screen.SHORTS else Screen.HOME)
    }

    fun deleteVideo(videoId: String) {
        repository.deleteVideo(videoId)
        showSnackbar("Video removed from channel")
    }

    fun generateAiCreatorHelp(taskType: AiTaskType, input: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true, aiCurrentTask = taskType) }
            val result = repository.generateAiAssistance(taskType, input)
            _uiState.update { it.copy(isAiGenerating = false, aiGeneratedResult = result) }
        }
    }

    fun clearAiResult() {
        _uiState.update { it.copy(aiGeneratedResult = null) }
    }

    fun joinCommunity(communityId: String) {
        repository.joinCommunity(communityId)
    }

    fun votePoll(communityId: String, postId: String, optionIndex: Int) {
        repository.votePoll(communityId, postId, optionIndex)
    }

    fun moderateContent(itemId: String, status: ModerationStatus) {
        repository.moderateContent(itemId, status)
        showSnackbar("Moderation decision recorded")
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun toggleShortsMuted() {
        val nextMuted = !_uiState.value.isShortsMuted
        navController.updateShortsMuted(nextMuted)
    }

    // --- Independent Infrastructure Architecture State & Controls ---
    val transcodingActiveJobs = VyroVideoEngine.activeJobs
    val transcodingCompletedJobs = VyroVideoEngine.completedJobs
    val generatedImages = VyroImageGenService.history
    val isImageGenerating = VyroImageGenService.isGenerating
    val generatedVideos = VyroVideoGenService.projects
    val isVideoRendering = VyroVideoGenService.isRendering
    val activeEvents = VyroEventBus.events

    val currentAuthService = VyroIntegrationHub.currentAuthService
    val currentStorageService = VyroIntegrationHub.currentStorageService
    val currentAiProvider = VyroIntegrationHub.aiEngine.activeProviderState
    val currentPaymentAdapter = VyroIntegrationHub.currentPaymentAdapter
    val currentCdnService = VyroIntegrationHub.currentCdnService

    fun switchAiProvider(provider: AiProviderType) {
        VyroIntegrationHub.setAiProvider(provider)
        showSnackbar("Active AI Provider switched to: ${provider.displayName}")
    }

    fun switchStorageAdapter(useS3: Boolean) {
        VyroIntegrationHub.setUseS3Storage(useS3)
        showSnackbar(if (useS3) "Storage switched to S3-Compatible Object Store" else "Storage switched to Local Edge Filesystem")
    }

    fun switchAuthAdapter(useNative: Boolean) {
        VyroIntegrationHub.setUseNativeAuth(useNative)
        showSnackbar(if (useNative) "Auth switched to VYRO Independent Native Identity" else "Auth switched to Firebase Auth Adapter")
    }

    fun switchPaymentAdapter(useNative: Boolean) {
        VyroIntegrationHub.setUseNativePayment(useNative)
        showSnackbar(if (useNative) "Payment Rail switched to VYRO Direct Settlement Engine" else "Payment Rail switched to Stripe Connect Adapter")
    }

    fun switchCdnAdapter(useVyroEdge: Boolean) {
        VyroIntegrationHub.setUseVyroEdgeCdn(useVyroEdge)
        showSnackbar(if (useVyroEdge) "CDN switched to VYRO Global Anycast Edge Network" else "CDN switched to Cloudflare CDN Adapter")
    }

    fun triggerTranscodingTestJob(title: String, durationSec: Int, isShort: Boolean = false) {
        viewModelScope.launch {
            VyroVideoEngine.submitVideoJob(
                title = title,
                creatorId = currentUser.value.id,
                fileSizeMb = if (isShort) 38.4 else 340.0,
                durationSeconds = durationSec,
                isShort = isShort
            )
            showSnackbar("FFmpeg Transcoding Ladder completed for \"$title\"!")
        }
    }

    fun generateAiImage(prompt: String, style: String, aspectRatio: ImageAspectRatio) {
        viewModelScope.launch {
            VyroImageGenService.generateImage(prompt, style, aspectRatio)
            showSnackbar("AI Thumbnail generated and stored in VYRO Object Storage!")
        }
    }

    fun generateAiVideo(prompt: String, style: String, durationSec: Int) {
        viewModelScope.launch {
            VyroVideoGenService.submitVideoPrompt(prompt, style, durationSec)
            showSnackbar("AI Video diffusion rendered and ready to publish!")
        }
    }

    fun publishGeneratedVideoToFeed(projectId: String, title: String) {
        val proj = generatedVideos.value.find { it.id == projectId } ?: return
        VyroVideoGenService.markAsPublished(projectId)
        uploadVideo(
            title = title,
            description = "AI-Generated cinematic video rendered with VYRO Diffusion Engine: \"${proj.prompt}\"",
            category = ContentCategory.TECH_AI,
            tags = listOf("AIVideo", "VYRODiffusion", "SciFi"),
            isShort = false,
            visibility = ContentVisibility.PUBLIC,
            productTitle = null,
            productPrice = null
        )
    }
}
