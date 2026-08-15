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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Community
import com.example.ui.theme.*

@Composable
fun CommunitiesScreen(
    communities: List<Community>,
    onToggleJoin: (String) -> Unit,
    onVotePoll: (communityId: String, postId: String, optionIndex: Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("communities_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "COMMUNITIES & HUBS",
                    color = VyroCyanLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Interest Guilds & Discussions",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Connect directly with creators, participate in polls, and unlock exclusive content.",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        items(communities) { comm ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Guild Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VyroBrandGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comm.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = comm.name,
                                color = VyroTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${comm.handle} • ${"%,d".format(comm.membersCount)} members",
                                color = VyroTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { onToggleJoin(comm.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (comm.isJoined) VyroSurface else VyroVioletPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (comm.isJoined) "Joined" else "Join Guild",
                            color = if (comm.isJoined) VyroCyanLight else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = comm.description,
                    color = VyroTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Recent Discussion / Poll
                comm.recentPosts.forEach { post ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VyroSurface)
                            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                color = VyroCyanLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = " • ${post.timeAgo}",
                                color = VyroTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = post.content,
                            color = VyroTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Interactive Poll
                        if (post.pollOptions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val totalVotes = post.pollVotes.sum().coerceAtLeast(1)
                            post.pollOptions.forEachIndexed { index, option ->
                                val votesForOption = post.pollVotes.getOrElse(index) { 0 }
                                val pct = (votesForOption.toFloat() / totalVotes * 100).toInt()
                                val isSelected = post.userVotedOptionIndex == index

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) VyroVioletDark else VyroSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) VyroVioletPrimary else VyroBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (post.userVotedOptionIndex == null) {
                                                onVotePoll(comm.id, post.id, index)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option,
                                            color = if (isSelected) Color.White else VyroTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (post.userVotedOptionIndex != null) {
                                            Text(
                                                text = "$pct%",
                                                color = VyroCyanLight,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
