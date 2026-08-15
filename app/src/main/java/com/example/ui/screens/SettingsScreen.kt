package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentUser: User,
    onShowSnackbar: (String) -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    var videoQuality by remember { mutableStateOf("4K 60FPS (HDR)") }
    var autoPlay by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var dataSaver by remember { mutableStateOf(false) }
    var audioBitrate by remember { mutableStateOf("320 kbps Studio Master") }
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var hlsAdaptiveBitrate by remember { mutableStateOf(true) }
    var cacheSizeMb by remember { mutableDoubleStateOf(428.5) }
    var isClearingCache by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "PREFERENCES & HARDWARE",
                    color = VyroVioletLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Settings & Config",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Configure video streaming engines, audio bitrates, storage cache, and privacy.",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Playback & Video Quality
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = VyroCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Playback & Video Quality",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Default Stream Quality:", color = VyroTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val qualities = listOf("4K 60FPS (HDR)", "1080p Full HD", "720p HD", "Auto (Bandwidth Aware)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        qualities.take(2).forEach { q ->
                            val isSel = videoQuality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) VyroVioletPrimary else VyroSurfaceHighlight)
                                    .clickable {
                                        videoQuality = q
                                        onShowSnackbar("Quality set to $q")
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(q, color = if (isSel) Color.White else VyroTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        qualities.drop(2).forEach { q ->
                            val isSel = videoQuality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) VyroVioletPrimary else VyroSurfaceHighlight)
                                    .clickable {
                                        videoQuality = q
                                        onShowSnackbar("Quality set to $q")
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(q, color = if (isSel) Color.White else VyroTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Play Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-play Next Video", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Continuous playback in feed and watch view", color = VyroTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoPlay,
                            onCheckedChange = {
                                autoPlay = it
                                onShowSnackbar(if (it) "Auto-play enabled" else "Auto-play disabled")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VyroVioletPrimary
                            )
                        )
                    }

                    // HLS Adaptive Bitrate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("HLS Master Ladder Switching", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Dynamic resolution switching on network jitter", color = VyroTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hlsAdaptiveBitrate,
                            onCheckedChange = { hlsAdaptiveBitrate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VyroCyanSecondary
                            )
                        )
                    }
                }
            }
        }

        // Audio & Codecs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = VyroVioletLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audio & Codec Pipeline",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Audio Bitrate Profile:", color = VyroTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf("320 kbps Studio Master", "192 kbps High Quality", "128 kbps Standard Data Saver").forEach { bitrate ->
                        val isSel = audioBitrate == bitrate
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) VyroVioletDark.copy(alpha = 0.4f) else Color.Transparent)
                                .clickable {
                                    audioBitrate = bitrate
                                    onShowSnackbar("Audio profile set to $bitrate")
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { audioBitrate = bitrate },
                                colors = RadioButtonDefaults.colors(selectedColor = VyroVioletPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(bitrate, color = if (isSel) Color.White else VyroTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Storage & Cache
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = VyroGoldTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Storage & Local Edge Cache",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Temporary Video & Sprite Cache", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Fast preview frames and offline HLS chunks", color = VyroTextMuted, fontSize = 11.sp)
                        }
                        Text(
                            text = "${"%.1f".format(cacheSizeMb)} MB",
                            color = VyroGoldTertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            cacheSizeMb = 0.0
                            onShowSnackbar("Local edge cache cleared successfully.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceHighlight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = VyroRose)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Edge Cache (${"%.1f".format(cacheSizeMb)} MB)", color = VyroRose, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Notifications & Privacy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = VyroCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notifications & Activity",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Creator Drops & Tips Alerts", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Instant push notifications for received tips and uploads", color = VyroTextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = pushNotifications,
                            onCheckedChange = { pushNotifications = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VyroVioletPrimary
                            )
                        )
                    }
                }
            }
        }

        // Quick Links to Health & Support
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToHealth,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceHighlight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = VyroCyanLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("API Health", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = onNavigateToHelp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceHighlight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = VyroGoldTertiary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Help & FAQ", color = VyroGoldTertiary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
