package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.Screen
import com.example.ui.navigation.NavigationManager
import com.example.ui.theme.*

/**
 * Responsive MainScaffold component wrapping [NavigationManager].
 *
 * Responsiveness:
 * - Mobile / Compact viewports (< 600.dp width): Provides standard TopBar and permanent BottomNavigationBar.
 * - Desktop / Expanded viewports (>= 600.dp width): Provides side NavigationRail on the left and full content area on the right.
 *
 * State Persistence:
 * - Wraps screen contents inside [SaveableStateHolder] so every screen's internal state (scroll offsets,
 *   form inputs, tabs) persists when switching between Home, Discover, Shorts, Profile, etc.
 */
@Composable
fun MainScaffold(
    navigationManager: NavigationManager,
    currentUser: User,
    unreadNotificationsCount: Int = 0,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenSearch: () -> Unit = { navigationManager.switchTab(Screen.DISCOVER) },
    onOpenNotifications: () -> Unit = { navigationManager.navigate(Screen.NOTIFICATIONS) },
    onOpenWallet: () -> Unit = { navigationManager.navigate(Screen.WALLET) },
    onOpenVyroPlus: () -> Unit = {},
    onSwitchDemoRole: (UserRole) -> Unit = {},
    onSignOut: () -> Unit = {},
    content: @Composable (Screen) -> Unit
) {
    val navState by navigationManager.navState.collectAsStateWithLifecycle()
    val currentScreen = navState.currentScreen
    val activeTab = navState.activeTab

    val saveableStateHolder = rememberSaveableStateHolder()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("main_scaffold")
    ) {
        val isDesktopOrTablet = maxWidth >= 600.dp
        val isLanding = currentScreen == Screen.LANDING
        val isShorts = currentScreen == Screen.SHORTS

        if (isDesktopOrTablet) {
            // Desktop / Tablet Layout with NavigationRail on the start
            Row(modifier = Modifier.fillMaxSize()) {
                if (!isLanding) {
                    VyroNavigationRail(
                        currentScreen = currentScreen,
                        activeTab = activeTab,
                        currentUser = currentUser,
                        unreadNotificationsCount = unreadNotificationsCount,
                        onNavigate = { navigationManager.navigate(it) },
                        onSwitchTab = { navigationManager.switchTab(it) },
                        onOpenWallet = onOpenWallet,
                        onOpenVyroPlus = onOpenVyroPlus
                    )
                }

                // Main Content Column for Desktop
                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    containerColor = VyroBackground,
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .padding(16.dp)
                                .testTag("desktop_snackbar_host")
                        )
                    },
                    topBar = {
                        if (!isLanding && !isShorts) {
                            VyroTopBar(
                                currentUser = currentUser,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onNavigate = { navigationManager.navigate(it) },
                                onOpenSearch = onOpenSearch,
                                onOpenNotifications = onOpenNotifications,
                                onOpenWallet = onOpenWallet,
                                onOpenVyroPlus = onOpenVyroPlus,
                                onSwitchDemoRole = onSwitchDemoRole,
                                onSignOut = onSignOut
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (isShorts || isLanding) 0.dp else innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        saveableStateHolder.SaveableStateProvider(key = currentScreen.name) {
                            content(currentScreen)
                        }
                    }
                }
            }
        } else {
            // Mobile Layout with Permanent Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = VyroBackground,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .padding(bottom = if (isShorts) 80.dp else 16.dp)
                            .testTag("mobile_snackbar_host")
                    )
                },
                topBar = {
                    if (!isLanding && !isShorts) {
                        VyroTopBar(
                            currentUser = currentUser,
                            unreadNotificationsCount = unreadNotificationsCount,
                            onNavigate = { navigationManager.navigate(it) },
                            onOpenSearch = onOpenSearch,
                            onOpenNotifications = onOpenNotifications,
                            onOpenWallet = onOpenWallet,
                            onOpenVyroPlus = onOpenVyroPlus,
                            onSwitchDemoRole = onSwitchDemoRole,
                            onSignOut = onSignOut
                        )
                    }
                },
                bottomBar = {
                    if (!isLanding) {
                        VyroBottomBar(
                            currentScreen = currentScreen,
                            activeTab = activeTab,
                            onNavigate = { navigationManager.navigate(it) },
                            onSwitchTab = { navigationManager.switchTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = if (isShorts || isLanding) 0.dp else innerPadding.calculateTopPadding(),
                            bottom = if (isLanding) 0.dp else innerPadding.calculateBottomPadding()
                        )
                ) {
                    saveableStateHolder.SaveableStateProvider(key = currentScreen.name) {
                        content(currentScreen)
                    }
                }
            }
        }
    }
}

/**
 * Desktop & Tablet Navigation Rail.
 */
@Composable
fun VyroNavigationRail(
    currentScreen: Screen,
    activeTab: Screen,
    currentUser: User,
    unreadNotificationsCount: Int,
    onNavigate: (Screen) -> Unit,
    onSwitchTab: (Screen) -> Unit,
    onOpenWallet: () -> Unit,
    onOpenVyroPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .testTag("nav_rail"),
        color = VyroSurface.copy(alpha = 0.98f),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: VYRO Brand Logo Mark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Minimal Diamond Logo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroBrandGradient)
                        .clickable { onSwitchTab(Screen.HOME) }
                        .testTag("nav_rail_brand_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(vertical = 4.dp),
                    color = VyroBorderSubtle
                )

                // Navigation Items
                val isHomeSelected = currentScreen == Screen.HOME || (currentScreen == Screen.WATCH && activeTab == Screen.HOME)
                RailNavItem(
                    icon = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    label = "Home",
                    isSelected = isHomeSelected,
                    onClick = { onSwitchTab(Screen.HOME) },
                    testTag = "nav_rail_home"
                )

                val isDiscoverSelected = currentScreen == Screen.DISCOVER
                RailNavItem(
                    icon = if (isDiscoverSelected) Icons.Filled.Explore else Icons.Outlined.Explore,
                    label = "Discover",
                    isSelected = isDiscoverSelected,
                    onClick = { onSwitchTab(Screen.DISCOVER) },
                    testTag = "nav_rail_discover"
                )

                // Create Button in Rail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .rotate(45f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VyroBrandGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { onNavigate(Screen.CREATE) }
                        .testTag("nav_rail_create"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.rotate(-45f)) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                val isShortsSelected = currentScreen == Screen.SHORTS
                RailNavItem(
                    icon = if (isShortsSelected) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                    label = "Shorts",
                    isSelected = isShortsSelected,
                    onClick = { onSwitchTab(Screen.SHORTS) },
                    testTag = "nav_rail_shorts"
                )

                val isLiveSelected = currentScreen == Screen.LIVE
                RailNavItem(
                    icon = if (isLiveSelected) Icons.Filled.Sensors else Icons.Outlined.Sensors,
                    label = "Live",
                    isSelected = isLiveSelected,
                    onClick = { onNavigate(Screen.LIVE) },
                    testTag = "nav_rail_live"
                )

                val isProfileSelected = currentScreen == Screen.CREATOR_STUDIO || currentScreen == Screen.CREATOR_PROFILE
                RailNavItem(
                    icon = if (isProfileSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    label = "Studio",
                    isSelected = isProfileSelected,
                    onClick = { onSwitchTab(Screen.CREATOR_STUDIO) },
                    testTag = "nav_rail_studio"
                )
            }

            // Bottom Section: Quick Utilities (Wallet, Communities, VIP)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(bottom = 8.dp),
                    color = VyroBorderSubtle
                )

                IconButton(
                    onClick = onOpenWallet,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("nav_rail_wallet")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = if (currentScreen == Screen.WALLET) VyroVioletPrimary else VyroTextMuted
                    )
                }

                IconButton(
                    onClick = { onNavigate(Screen.COMMUNITIES) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("nav_rail_communities")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = "Communities",
                        tint = if (currentScreen == Screen.COMMUNITIES) VyroVioletPrimary else VyroTextMuted
                    )
                }

                IconButton(
                    onClick = { onNavigate(Screen.SETTINGS) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("nav_rail_settings")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = if (currentScreen == Screen.SETTINGS) VyroVioletPrimary else VyroTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RailNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) VyroVioletPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VyroVioletPrimary else VyroTextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) VyroVioletLight else VyroTextMuted
        )
    }
}
