package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiTaskType
import com.example.model.CreatorAnalytics
import com.example.model.User
import com.example.model.Video
import com.example.ui.theme.*

@Composable
fun CreatorStudioScreen(
    user: User,
    analytics: CreatorAnalytics,
    myVideos: List<Video>,
    isAiGenerating: Boolean,
    aiResult: String?,
    aiCurrentTask: AiTaskType,
    onGenerateAiHelp: (AiTaskType, String) -> Unit,
    onClearAiResult: () -> Unit,
    onDeleteVideo: (String) -> Unit,
    onOpenUpload: () -> Unit
) {
    var selectedStudioTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Analytics", "AI Studio 🤖", "Content")
    var customAiTopic by remember { mutableStateOf("Cybernetic Sci-Fi Filmmaking") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("creator_studio_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VYRO CREATOR STUDIO",
                        color = VyroVioletLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Creator Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                }

                Button(
                    onClick = onOpenUpload,
                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("studio_upload_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Subtabs
        item {
            TabRow(
                selectedTabIndex = selectedStudioTab,
                containerColor = VyroSurfaceElevated,
                contentColor = VyroVioletPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedStudioTab == index,
                        onClick = { selectedStudioTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedStudioTab == index) VyroCyanLight else VyroTextMuted,
                                fontWeight = if (selectedStudioTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        when (selectedStudioTab) {
            0 -> { // Dashboard Overview
                item {
                    // Revenue Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroBrandGradient)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ESTIMATED TOTAL REVENUE (30D)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${"%,.2f".format(analytics.estimatedTotalRevenue)}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Ad Revenue", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(text = "$${"%,.2f".format(analytics.adShareRevenue)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = "Subscriptions", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(text = "$${"%,.2f".format(analytics.membershipRevenue)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = "Tips & Drops", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(text = "$${"%,.2f".format(analytics.tipRevenue)}", color = VyroGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = "Store Sales", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(text = "$${"%,.2f".format(analytics.marketplaceRevenue)}", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Key Performance Metrics Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Total Views",
                            value = "${"%,d".format(analytics.totalViews)}",
                            growth = "+24.8% vs last month",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Watch Time",
                            value = "${"%,.0f".format(analytics.watchTimeHours)}h",
                            growth = "+18.2% retention",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Subscribers",
                            value = "${"%,d".format(analytics.totalFollowers)}",
                            growth = "+${analytics.followerGrowthRate}% growth",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Avg. Engagement",
                            value = "${analytics.averageEngagementRate}%",
                            growth = "Top 5% on platform",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            1 -> { // Deep Analytics
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Weekly Velocity & Traffic Sources",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        analytics.trafficSources.forEach { source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = source.sourceName, color = VyroTextSecondary, fontSize = 13.sp)
                                Text(
                                    text = "${source.percentage}%",
                                    color = VyroCyanLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            LinearProgressIndicator(
                                progress = { source.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = VyroVioletPrimary,
                                trackColor = VyroSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Top Audience Geographies",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        analytics.topGeos.forEach { geo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = geo.country, color = VyroTextSecondary, fontSize = 13.sp)
                                Text(text = "${geo.percentage}%", color = VyroGoldTertiary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            2 -> { // AI Creator Studio Suite (Gemini Power Tools)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroCyanDark.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🤖", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Gemini Creator Power Suite",
                                    color = VyroCyanLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "AI generative tools to accelerate your creator career",
                                    color = VyroTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = customAiTopic,
                            onValueChange = { customAiTopic = it },
                            label = { Text("Topic / Niche Focus") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VyroCyanSecondary,
                                unfocusedBorderColor = VyroBorder,
                                focusedTextColor = VyroTextPrimary,
                                unfocusedTextColor = VyroTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Choose AI Generator Tool:",
                            color = VyroTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val tools = listOf(
                            Pair(AiTaskType.VIRAL_TITLES, "🔥 Viral Title Generator"),
                            Pair(AiTaskType.SHORTS_SCRIPT, "🎬 45s Shorts Scriptwriter"),
                            Pair(AiTaskType.CONTENT_IDEAS, "💡 Multi-Part Series Ideas"),
                            Pair(AiTaskType.THUMBNAIL_PROMPTS, "🎨 Clickable Thumbnail Prompts"),
                            Pair(AiTaskType.CONTENT_CALENDAR, "📅 7-Day Release Schedule")
                        )

                        tools.forEach { (taskType, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VyroSurface)
                                    .border(1.dp, VyroBorder, RoundedCornerShape(10.dp))
                                    .clickable { onGenerateAiHelp(taskType, customAiTopic) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, color = VyroTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (isAiGenerating) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = VyroCyanSecondary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Gemini is generating...", color = VyroCyanLight, fontSize = 13.sp)
                            }
                        }

                        aiResult?.let { result ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VyroSurface)
                                    .border(1.dp, VyroGoldTertiary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "✨ Output", color = VyroGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(onClick = onClearAiResult, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = VyroTextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = result, color = VyroTextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            3 -> { // Content Management
                item {
                    Text(
                        text = "Published Videos & Shorts (${myVideos.size})",
                        color = VyroTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(myVideos) { video ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                color = VyroTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${"%,d".format(video.viewCount)} views • ${video.likeCount} likes • ${if (video.isShort) "Short" else "Long"}",
                                color = VyroTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { onDeleteVideo(video.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VyroRose, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    growth: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(VyroSurfaceElevated)
            .border(1.dp, VyroBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text = label, color = VyroTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = growth, color = VyroEmerald, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
