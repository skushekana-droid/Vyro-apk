package com.example.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.SavedStateHandle
import com.example.model.ContentCategory
import com.example.model.Video
import com.example.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents an entry in the navigation backstack with optional metadata and timestamp.
 */
data class NavStackEntry(
    val screen: Screen,
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Encapsulates persistent state for individual top-level tabs.
 */
data class HomeTabState(
    val selectedCategory: ContentCategory = ContentCategory.ALL,
    val isFollowingOnly: Boolean = false
)

data class DiscoverTabState(
    val searchQuery: String = "",
    val selectedTag: String? = null
)

data class ShortsTabState(
    val currentIndex: Int = 0,
    val isMuted: Boolean = false
)

data class ProfileTabState(
    val selectedTab: Int = 0,
    val selectedCreatorId: String? = null
)

/**
 * Consolidated navigation state managed by [VyroNavController].
 */
data class NavState(
    val currentScreen: Screen = Screen.HOME,
    val activeTab: Screen = Screen.HOME,
    val backStack: List<NavStackEntry> = listOf(NavStackEntry(Screen.HOME)),
    val homeState: HomeTabState = HomeTabState(),
    val discoverState: DiscoverTabState = DiscoverTabState(),
    val shortsState: ShortsTabState = ShortsTabState(),
    val profileState: ProfileTabState = ProfileTabState(),
    val selectedVideo: Video? = null,
    val selectedCreatorId: String? = null
) {
    val canGoBack: Boolean get() = backStack.size > 1 || currentScreen != Screen.HOME
}

/**
 * Centralized Navigation Controller for VYRO.
 * Manages view switching between Home, Discover, Shorts, and Profile/Studio,
 * maintains backstack history, and persists navigation state across configuration changes
 * and process death using [SavedStateHandle].
 */
class VyroNavController(
    private val savedStateHandle: SavedStateHandle? = null
) {
    companion object {
        private const val KEY_CURRENT_SCREEN = "vyro_nav_current_screen"
        private const val KEY_ACTIVE_TAB = "vyro_nav_active_tab"
        private const val KEY_BACKSTACK_SCREENS = "vyro_nav_backstack_screens"
        private const val KEY_HOME_CATEGORY = "vyro_nav_home_cat"
        private const val KEY_HOME_FOLLOWING = "vyro_nav_home_following"
        private const val KEY_DISCOVER_QUERY = "vyro_nav_discover_query"
        private const val KEY_DISCOVER_TAG = "vyro_nav_discover_tag"
        private const val KEY_SHORTS_INDEX = "vyro_nav_shorts_index"
        private const val KEY_SHORTS_MUTED = "vyro_nav_shorts_muted"
        private const val KEY_PROFILE_TAB = "vyro_nav_profile_tab"
        private const val KEY_SELECTED_CREATOR_ID = "vyro_nav_selected_creator_id"
    }

    private val _navState = MutableStateFlow(restoreInitialState())
    val navState: StateFlow<NavState> = _navState.asStateFlow()

    val currentScreen: Screen get() = _navState.value.currentScreen
    val activeTab: Screen get() = _navState.value.activeTab
    val canGoBack: Boolean get() = _navState.value.canGoBack

    private fun restoreInitialState(): NavState {
        val screenName = savedStateHandle?.get<String>(KEY_CURRENT_SCREEN)
        val initialScreen = screenName?.let { runCatching { Screen.valueOf(it) }.getOrNull() } ?: Screen.HOME

        val tabName = savedStateHandle?.get<String>(KEY_ACTIVE_TAB)
        val initialTab = tabName?.let { runCatching { Screen.valueOf(it) }.getOrNull() } ?: Screen.HOME

        val backStackScreens = savedStateHandle?.get<List<String>>(KEY_BACKSTACK_SCREENS)
        val initialBackStack = if (!backStackScreens.isNullOrEmpty()) {
            backStackScreens.mapNotNull { name ->
                runCatching { Screen.valueOf(name) }.getOrNull()?.let { NavStackEntry(it) }
            }.ifEmpty { listOf(NavStackEntry(initialScreen)) }
        } else {
            listOf(NavStackEntry(initialScreen))
        }

        val homeCatName = savedStateHandle?.get<String>(KEY_HOME_CATEGORY)
        val homeCat = homeCatName?.let { runCatching { ContentCategory.valueOf(it) }.getOrNull() } ?: ContentCategory.ALL
        val isFollowingOnly = savedStateHandle?.get<Boolean>(KEY_HOME_FOLLOWING) ?: false

        val discoverQuery = savedStateHandle?.get<String>(KEY_DISCOVER_QUERY) ?: ""
        val discoverTag = savedStateHandle?.get<String>(KEY_DISCOVER_TAG)

        val shortsIndex = savedStateHandle?.get<Int>(KEY_SHORTS_INDEX) ?: 0
        val isShortsMuted = savedStateHandle?.get<Boolean>(KEY_SHORTS_MUTED) ?: false

        val profileTab = savedStateHandle?.get<Int>(KEY_PROFILE_TAB) ?: 0
        val selectedCreatorId = savedStateHandle?.get<String>(KEY_SELECTED_CREATOR_ID)

        return NavState(
            currentScreen = initialScreen,
            activeTab = initialTab,
            backStack = initialBackStack,
            homeState = HomeTabState(
                selectedCategory = homeCat,
                isFollowingOnly = isFollowingOnly
            ),
            discoverState = DiscoverTabState(
                searchQuery = discoverQuery,
                selectedTag = discoverTag
            ),
            shortsState = ShortsTabState(
                currentIndex = shortsIndex,
                isMuted = isShortsMuted
            ),
            profileState = ProfileTabState(
                selectedTab = profileTab,
                selectedCreatorId = selectedCreatorId
            ),
            selectedCreatorId = selectedCreatorId
        )
    }

    private fun persistState(state: NavState) {
        savedStateHandle?.apply {
            set(KEY_CURRENT_SCREEN, state.currentScreen.name)
            set(KEY_ACTIVE_TAB, state.activeTab.name)
            set(KEY_BACKSTACK_SCREENS, state.backStack.map { it.screen.name })
            set(KEY_HOME_CATEGORY, state.homeState.selectedCategory.name)
            set(KEY_HOME_FOLLOWING, state.homeState.isFollowingOnly)
            set(KEY_DISCOVER_QUERY, state.discoverState.searchQuery)
            set(KEY_DISCOVER_TAG, state.discoverState.selectedTag)
            set(KEY_SHORTS_INDEX, state.shortsState.currentIndex)
            set(KEY_SHORTS_MUTED, state.shortsState.isMuted)
            set(KEY_PROFILE_TAB, state.profileState.selectedTab)
            set(KEY_SELECTED_CREATOR_ID, state.selectedCreatorId)
        }
    }

    /**
     * Switch between top-level primary tabs (HOME, DISCOVER, SHORTS, PROFILE/STUDIO).
     * Maintains persistent tab state across switches and handles root reset if tab is re-selected.
     */
    fun switchTab(targetTab: Screen) {
        _navState.update { current ->
            val isSameTab = current.activeTab == targetTab && current.currentScreen == targetTab
            val updatedBackStack = if (isSameTab) {
                // Tapping active tab again pops back to that tab's root
                listOf(NavStackEntry(targetTab))
            } else {
                // If moving between tabs, append or replace with target tab in history
                val filtered = current.backStack.filterNot { it.screen == targetTab }
                filtered + NavStackEntry(targetTab)
            }

            val newState = current.copy(
                currentScreen = targetTab,
                activeTab = targetTab,
                backStack = updatedBackStack
            )
            persistState(newState)
            newState
        }
    }

    /**
     * Navigate to an arbitrary screen with optional parameters.
     */
    fun navigate(
        destination: Screen,
        params: Map<String, String> = emptyMap(),
        addToBackStack: Boolean = true,
        singleTop: Boolean = true
    ) {
        _navState.update { current ->
            if (singleTop && current.currentScreen == destination) {
                return@update current
            }

            // Determine if destination matches a primary tab
            val newActiveTab = when (destination) {
                Screen.HOME -> Screen.HOME
                Screen.DISCOVER -> Screen.DISCOVER
                Screen.SHORTS -> Screen.SHORTS
                Screen.CREATOR_STUDIO, Screen.CREATOR_PROFILE -> Screen.CREATOR_STUDIO
                else -> current.activeTab
            }

            val entry = NavStackEntry(destination, params)
            val newBackStack = if (addToBackStack) {
                current.backStack + entry
            } else {
                listOf(entry)
            }

            val newState = current.copy(
                currentScreen = destination,
                activeTab = newActiveTab,
                backStack = newBackStack
            )
            persistState(newState)
            newState
        }
    }

    /**
     * Navigates back in history.
     * Returns true if back navigation was handled, or false if already at top root.
     */
    fun popBackStack(): Boolean {
        var handled = false
        _navState.update { current ->
            if (current.backStack.size > 1) {
                val newBackStack = current.backStack.dropLast(1)
                val previousEntry = newBackStack.last()
                val newActiveTab = when (previousEntry.screen) {
                    Screen.HOME -> Screen.HOME
                    Screen.DISCOVER -> Screen.DISCOVER
                    Screen.SHORTS -> Screen.SHORTS
                    Screen.CREATOR_STUDIO, Screen.CREATOR_PROFILE -> Screen.CREATOR_STUDIO
                    else -> current.activeTab
                }

                handled = true
                val newState = current.copy(
                    currentScreen = previousEntry.screen,
                    activeTab = newActiveTab,
                    backStack = newBackStack
                )
                persistState(newState)
                newState
            } else if (current.currentScreen != Screen.HOME) {
                // If backstack has 1 item but we are not on Home, fallback to Home tab
                handled = true
                val newState = current.copy(
                    currentScreen = Screen.HOME,
                    activeTab = Screen.HOME,
                    backStack = listOf(NavStackEntry(Screen.HOME))
                )
                persistState(newState)
                newState
            } else {
                handled = false
                current
            }
        }
        return handled
    }

    /**
     * Resets the navigation state and backstack to a given screen (defaulting to HOME).
     */
    fun clearTo(screen: Screen = Screen.HOME) {
        _navState.update { current ->
            val newState = current.copy(
                currentScreen = screen,
                activeTab = if (screen == Screen.DISCOVER || screen == Screen.SHORTS || screen == Screen.CREATOR_STUDIO) screen else Screen.HOME,
                backStack = listOf(NavStackEntry(screen))
            )
            persistState(newState)
            newState
        }
    }

    // --- State Retaining Mutators for Primary Views ---

    fun updateHomeCategory(category: ContentCategory) {
        _navState.update { current ->
            val newHomeState = current.homeState.copy(selectedCategory = category, isFollowingOnly = false)
            val newState = current.copy(homeState = newHomeState)
            persistState(newState)
            newState
        }
    }

    fun updateHomeFollowingOnly(followingOnly: Boolean) {
        _navState.update { current ->
            val newHomeState = current.homeState.copy(isFollowingOnly = followingOnly)
            val newState = current.copy(homeState = newHomeState)
            persistState(newState)
            newState
        }
    }

    fun updateDiscoverSearch(query: String, tag: String? = null) {
        _navState.update { current ->
            val newDiscoverState = current.discoverState.copy(searchQuery = query, selectedTag = tag)
            val newState = current.copy(discoverState = newDiscoverState)
            persistState(newState)
            newState
        }
    }

    fun updateShortsIndex(index: Int) {
        _navState.update { current ->
            val newShortsState = current.shortsState.copy(currentIndex = index)
            val newState = current.copy(shortsState = newShortsState)
            persistState(newState)
            newState
        }
    }

    fun updateShortsMuted(isMuted: Boolean) {
        _navState.update { current ->
            val newShortsState = current.shortsState.copy(isMuted = isMuted)
            val newState = current.copy(shortsState = newShortsState)
            persistState(newState)
            newState
        }
    }

    fun updateProfileTab(tabIndex: Int) {
        _navState.update { current ->
            val newProfileState = current.profileState.copy(selectedTab = tabIndex)
            val newState = current.copy(profileState = newProfileState)
            persistState(newState)
            newState
        }
    }

    fun setSelectedVideo(video: Video?) {
        _navState.update { it.copy(selectedVideo = video) }
    }

    fun setSelectedCreatorId(creatorId: String?) {
        _navState.update { current ->
            val newState = current.copy(selectedCreatorId = creatorId)
            persistState(newState)
            newState
        }
    }
}

val LocalVyroNavController = compositionLocalOf<VyroNavController> {
    error("VyroNavController not provided in CompositionLocal")
}

/**
 * Convenient alias for [VyroNavController] to fulfill NavigationManager contract.
 */
typealias NavigationManager = VyroNavController
val LocalNavigationManager = LocalVyroNavController
