package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
import com.example.model.Video
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class LiveChatMessage(
    val id: String,
    val username: String,
    val text: String,
    val isSuperChat: Boolean = false,
    val tipAmount: Double = 0.0,
    val timeAgo: String = "Just now"
)

data class LiveStreamItem(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorAvatar: String,
    val viewerCount: Int,
    val category: String,
    val thumbnailResId: Int,
    val isVerified: Boolean = true
)

@Composable
fun LiveScreen(
    currentUser: User,
    onWatchStream: (LiveStreamItem) -> Unit,
    onOpenTipDialog: (String, String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    var selectedStream by remember {
        mutableStateOf<LiveStreamItem?>(
            LiveStreamItem(
                id = "stream_1",
                title = "⚡ LIVE: Building Generative 3D Worlds in Unreal Engine 5.5",
                creatorName = "Kael Orion",
                creatorAvatar = "K",
                viewerCount = 14250,
                category = "TECH & AI",
                thumbnailResId = R.drawable.vyro_thumb_cyber
            )
        )
    }

    val activeStreams = remember {
        listOf(
            LiveStreamItem("stream_1", "⚡ LIVE: Building Generative 3D Worlds in Unreal Engine 5.5", "Kael Orion", "K", 14250, "TECH & AI", R.drawable.vyro_thumb_cyber),
            LiveStreamItem("stream_2", "🔴 Modular Synthesizers & Cyberpunk Soundscapes Jam", "Elena Vance", "E", 8940, "MUSIC", R.drawable.vyro_hero_banner),
            LiveStreamItem("stream_3", "🎮 VR Cyber-Arena World Championship Finals", "Apex Simulation", "A", 23100, "GAMING", R.drawable.vyro_thumb_cyber),
            LiveStreamItem("stream_4", "💡 Creator Economy 2.0: 95% Revenue Splits Deep Dive", "Synthetix Labs", "S", 6400, "CREATOR ECONOMY", R.drawable.vyro_hero_banner)
        )
    }

    var chatInput by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                LiveChatMessage("m1", "CyberDev", "This shader rendering looks unbelievable! 🚀"),
                LiveChatMessage("m2", "Elena Vance", "⚡ SuperChat $20.00: Inspiring workflow as always!", isSuperChat = true, tipAmount = 20.0),
                LiveChatMessage("m3", "AeroVision", "What sample rate are you using for edge compute?"),
                LiveChatMessage("m4", "NeonRider", "VYRO streaming latency is insanely low today."),
                LiveChatMessage("m5", "MatrixCoder", "⚡ SuperChat $50.00: Keep pushing the boundaries!", isSuperChat = true, tipAmount = 50.0)
            )
        )
    }

    var floatingHearts by remember { mutableIntStateOf(0) }
    var showStartBroadcastDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .testTag("live_screen")
    ) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Desktop / Tablet Dual-Pane Live Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Player & Active Streams Grid
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    selectedStream?.let { stream ->
                        LivePlayerComponent(
                            stream = stream,
                            onTipClick = { onOpenTipDialog(stream.id, stream.creatorName) },
                            onReact = { floatingHearts++ }
                        )
                    }

                    Text(
                        text = "Active Broadcasts (${activeStreams.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 240.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeStreams) { stream ->
                            LiveStreamCard(
                                stream = stream,
                                isSelected = selectedStream?.id == stream.id,
                                onClick = {
                                    selectedStream = stream
                                    onWatchStream(stream)
                                }
                            )
                        }
                    }
                }

                // Right Column: Live Chat Stream
                Card(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                ) {
                    LiveChatComponent(
                        messages = chatMessages,
                        chatInput = chatInput,
                        onChatInputChange = { chatInput = it },
                        onSendMessage = {
                            if (chatInput.isNotBlank()) {
                                val newMsg = LiveChatMessage(
                                    id = "msg_${System.currentTimeMillis()}",
                                    username = currentUser.displayName,
                                    text = chatInput
                                )
                                chatMessages = chatMessages + newMsg
                                chatInput = ""
                            }
                        },
                        onSendSuperChat = { amount ->
                            val superMsg = LiveChatMessage(
                                id = "msg_sc_${System.currentTimeMillis()}",
                                username = currentUser.displayName,
                                text = "⚡ SuperChat $${"%.2f".format(amount)}: Support from ${currentUser.displayName}!",
                                isSuperChat = true,
                                tipAmount = amount
                            )
                            chatMessages = chatMessages + superMsg
                            onShowSnackbar("⚡ SuperChat $${"%.2f".format(amount)} sent!")
                        }
                    )
                }
            }
        } else {
            // Mobile Stacked Live Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Go Live Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(VyroRose)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VYRO LIVE",
                                    color = VyroRose,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Live Broadcasts",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }

                        Button(
                            onClick = { showStartBroadcastDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VyroRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("go_live_button")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Go Live", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Active Main Stream
                item {
                    selectedStream?.let { stream ->
                        LivePlayerComponent(
                            stream = stream,
                            onTipClick = { onOpenTipDialog(stream.id, stream.creatorName) },
                            onReact = {
                                floatingHearts++
                                onShowSnackbar("⚡ Sent reaction to ${stream.creatorName}!")
                            }
                        )
                    }
                }

                // Live Chat Box
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                    ) {
                        LiveChatComponent(
                            messages = chatMessages,
                            chatInput = chatInput,
                            onChatInputChange = { chatInput = it },
                            onSendMessage = {
                                if (chatInput.isNotBlank()) {
                                    val newMsg = LiveChatMessage(
                                        id = "msg_${System.currentTimeMillis()}",
                                        username = currentUser.displayName,
                                        text = chatInput
                                    )
                                    chatMessages = chatMessages + newMsg
                                    chatInput = ""
                                }
                            },
                            onSendSuperChat = { amount ->
                                val superMsg = LiveChatMessage(
                                    id = "msg_sc_${System.currentTimeMillis()}",
                                    username = currentUser.displayName,
                                    text = "⚡ SuperChat $${"%.2f".format(amount)}: Support from ${currentUser.displayName}!",
                                    isSuperChat = true,
                                    tipAmount = amount
                                )
                                chatMessages = chatMessages + superMsg
                                onShowSnackbar("⚡ SuperChat $${"%.2f".format(amount)} sent!")
                            }
                        )
                    }
                }

                // Other Live Streams
                item {
                    Text(
                        text = "More Live Now",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(activeStreams) { stream ->
                    LiveStreamCard(
                        stream = stream,
                        isSelected = selectedStream?.id == stream.id,
                        onClick = {
                            selectedStream = stream
                            onWatchStream(stream)
                        }
                    )
                }
            }
        }
    }

    // Go Live Modal
    if (showStartBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showStartBroadcastDialog = false },
            title = { Text("Start Live Broadcast", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Broadcast with ultra-low latency WebRTC edge ingest on VYRO CDN.", color = VyroTextSecondary, fontSize = 13.sp)
                    Text("⚡ Direct tipping and 95% creator revenue split enabled by default.", color = VyroGoldTertiary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStartBroadcastDialog = false
                        onShowSnackbar("Live stream pipeline initialized. You are now live!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VyroRose)
                ) {
                    Text("Start Stream")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartBroadcastDialog = false }) {
                    Text("Cancel", color = VyroTextMuted)
                }
            },
            containerColor = VyroSurfaceElevated
        )
    }
}

@Composable
private fun LivePlayerComponent(
    stream: LiveStreamItem,
    onTipClick: () -> Unit,
    onReact: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = stream.thumbnailResId),
                    contentDescription = stream.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Live Badge & Viewers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VyroRose)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "● LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "👥 ${"%,d".format(stream.viewerCount)} watching",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive Quick Actions
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(VyroGoldTertiary)
                            .clickable(onClick = onTipClick)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("⚡ Tip Creator", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VyroRose)
                            .clickable(onClick = onReact)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = "React", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Stream Info Under Player
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stream.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stream.creatorName,
                        color = VyroCyanLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${stream.category}",
                        color = VyroTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveChatComponent(
    messages: List<LiveChatMessage>,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendSuperChat: (Double) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬 Live Chat",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroGoldTertiary.copy(alpha = 0.2f))
                        .clickable { onSendSuperChat(5.0) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⚡ $5 Tip", color = VyroGoldTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroGoldTertiary.copy(alpha = 0.2f))
                        .clickable { onSendSuperChat(20.0) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⚡ $20 SuperChat", color = VyroGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = VyroBorderSubtle)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                if (msg.isSuperChat) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(VyroGoldTertiary.copy(alpha = 0.25f))
                            .border(1.dp, VyroGoldTertiary, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${msg.username} — SuperChat $${"%.2f".format(msg.tipAmount)}",
                            color = VyroGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${msg.username}: ",
                            color = VyroCyanLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = msg.text,
                            color = VyroTextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                placeholder = { Text("Say something...", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroVioletPrimary,
                    unfocusedBorderColor = VyroBorder,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VyroVioletPrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LiveStreamCard(
    stream: LiveStreamItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.5.dp,
                if (isSelected) VyroVioletPrimary else VyroBorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = stream.thumbnailResId),
                    contentDescription = stream.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(VyroRose)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.title,
                    color = VyroTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stream.creatorName,
                    color = VyroTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${"%,d".format(stream.viewerCount)} viewers",
                    color = VyroCyanLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
