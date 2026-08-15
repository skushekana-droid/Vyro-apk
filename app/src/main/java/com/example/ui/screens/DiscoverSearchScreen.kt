package com.example.ui.screens

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
import com.example.model.ContentCategory
import com.example.model.Video
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.*

@Composable
fun DiscoverSearchScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onCreatorClick: (String) -> Unit,
    onSelectTag: (String) -> Unit
) {
    val trendingTags = listOf("AI", "GenerativeVideo", "CreatorEconomy", "Synthesizer", "SciFi", "VRGaming", "Productivity", "4KHDR", "QuantumComputing")
    val featuredCreators = listOf(
        Pair("Elena Vance", "@elenavance • 142K subs"),
        Pair("Synthetix Labs", "@synthetix_lab • 112K subs"),
        Pair("Nexus AI Research", "@nexus_ai • 95K subs"),
        Pair("Apex Simulation", "@apex_sim • 84K subs")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("discover_search_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search videos, creators, tags on VYRO...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = VyroCyanLight)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = VyroTextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VyroVioletPrimary,
                unfocusedBorderColor = VyroBorder,
                focusedTextColor = VyroTextPrimary,
                unfocusedTextColor = VyroTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discover_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trending Tags Section (Horizontal scroller)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingTags) { tag ->
                val isSelected = searchQuery.equals(tag, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) VyroCyanDark else VyroSurfaceElevated)
                        .border(1.dp, if (isSelected) VyroCyanLight else VyroBorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            onSearchChange(tag)
                            onSelectTag(tag)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "#$tag", color = if (isSelected) Color.White else VyroCyanLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (searchQuery.isEmpty()) {
            // Featured Creators Directory when no active query
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
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
                            text = "⭐ Featured Creators",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        featuredCreators.forEach { (name, details) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(VyroBrandGradient),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = name, color = VyroTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = details, color = VyroTextMuted, fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = { onCreatorClick(name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("View", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Trending Releases",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(videos) { video ->
                    VideoFeedCard(
                        video = video,
                        isLiked = false,
                        isBookmarked = false,
                        onVideoClick = { onVideoClick(video) },
                        onCreatorClick = { onCreatorClick(video.creatorId) },
                        onLikeClick = {},
                        onBookmarkClick = {},
                        onShareClick = {},
                        onTipClick = {}
                    )
                }
            }
        } else {
            // Search Results with Adaptive Grid
            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching for creators, #AI, #SciFi, or #Synthesizer",
                            color = VyroTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "Search Results for \"$searchQuery\" (${videos.size})",
                    color = VyroCyanLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoFeedCard(
                            video = video,
                            isLiked = false,
                            isBookmarked = false,
                            onVideoClick = { onVideoClick(video) },
                            onCreatorClick = { onCreatorClick(video.creatorId) },
                            onLikeClick = {},
                            onBookmarkClick = {},
                            onShareClick = {},
                            onTipClick = {}
                        )
                    }
                }
            }
        }
    }
}
