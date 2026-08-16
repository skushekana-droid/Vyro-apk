package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyroTopBar(
    currentUser: User,
    unreadNotificationsCount: Int,
    onNavigate: (Screen) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenVyroPlus: () -> Unit,
    onSwitchDemoRole: (UserRole) -> Unit,
    onSignOut: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VyroSurface.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Clean Minimal Brand Logo
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigate(Screen.HOME) }
                    .padding(vertical = 2.dp)
                    .testTag("vyro_logo_button")
            ) {
                Text(
                    text = "VYRO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = (-0.8).sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(VyroVioletPrimary)
                )
            }

            // Right Action Items
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Shortcut
                IconButton(
                    onClick = { onNavigate(Screen.LIVE) },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Live Broadcasts",
                        tint = VyroRose
                    )
                }

                // Search
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search VYRO",
                        tint = VyroTextPrimary
                    )
                }

                // Wallet Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorder, RoundedCornerShape(20.dp))
                        .clickable { onOpenWallet() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("top_wallet_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$${"%,.0f".format(currentUser.walletBalance)}",
                            color = VyroGoldTertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Notifications
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_notifications_button")
                ) {
                    BadgedBox(badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = VyroVioletPrimary) {
                                Text(
                                    text = "$unreadNotificationsCount",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = VyroTextPrimary
                        )
                    }
                }

                // User Avatar Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VyroVioletDark)
                            .border(1.5.dp, VyroVioletPrimary, CircleShape)
                            .clickable { showUserMenu = true }
                            .testTag("user_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false },
                        modifier = Modifier
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = currentUser.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = VyroTextPrimary
                                    )
                                    Text(
                                        text = "${currentUser.username} • ${currentUser.role.name}",
                                        fontSize = 11.sp,
                                        color = VyroCyanLight
                                    )
                                }
                            },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.CREATOR_PROFILE)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = VyroVioletPrimary)
                            }
                        )
                        HorizontalDivider(color = VyroBorder)

                        DropdownMenuItem(
                            text = { Text("Stassen AI Assistant", color = VyroVioletLight, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.STASSEN)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = VyroVioletPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Live Broadcasts", color = VyroRose, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.LIVE)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = VyroRose)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Creator Studio", color = VyroTextPrimary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.CREATOR_STUDIO)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = VyroVioletLight)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Communities & Guilds", color = VyroTextPrimary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.COMMUNITIES)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = VyroCyanSecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Purchases & Subscriptions", color = VyroTextPrimary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.PURCHASES)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = VyroGoldTertiary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("VYRO+ VIP Pass", color = VyroGoldTertiary, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showUserMenu = false
                                onOpenVyroPlus()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null, tint = VyroGoldTertiary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Settings & Config", color = VyroTextPrimary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.SETTINGS)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = VyroTextSecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Help & Support", color = VyroTextPrimary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.HELP)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = VyroCyanLight)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("API & System Health", color = VyroCyanLight, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.HEALTH)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = VyroCyanLight)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Admin Moderation Hub", color = VyroTextSecondary) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.ADMIN)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Security, contentDescription = null, tint = VyroTextSecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Infrastructure Control Plane", color = VyroVioletAccent, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showUserMenu = false
                                onNavigate(Screen.INFRASTRUCTURE)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = VyroVioletAccent)
                            }
                        )

                        HorizontalDivider(color = VyroBorder)

                        Text(
                            text = "SWITCH DEMO ROLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyroTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (currentUser.role == role) VyroCyanLight else VyroTextSecondary,
                                        fontWeight = if (currentUser.role == role) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    showUserMenu = false
                                    onSwitchDemoRole(role)
                                }
                            )
                        }

                        HorizontalDivider(color = VyroBorder)

                        DropdownMenuItem(
                            text = { Text("Sign Out", color = VyroRose) },
                            onClick = {
                                showUserMenu = false
                                onSignOut()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = VyroRose)
                            }
                        )
                    }
                }
            }
        }
    }
}
