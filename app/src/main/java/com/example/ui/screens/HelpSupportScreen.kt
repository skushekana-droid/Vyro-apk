package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.theme.*

data class FaqItem(
    val question: String,
    val answer: String,
    val category: String
)

@Composable
fun HelpSupportScreen(
    onShowSnackbar: (String) -> Unit,
    onNavigateToHealth: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedIndex by remember { mutableIntStateOf(-1) }
    var ticketSubject by remember { mutableStateOf("") }
    var ticketMessage by remember { mutableStateOf("") }
    var showTicketDialog by remember { mutableStateOf(false) }

    val faqs = remember {
        listOf(
            FaqItem(
                question = "How does the 95/5 Creator Revenue Split work?",
                answer = "VYRO allocates 95% of all ad revenue, subscription passes, tipping, and digital marketplace commerce directly to the content creator. Only 5% is retained for infrastructure and edge delivery costs. Payouts are computed via our double-entry ledger in real time.",
                category = "Monetization"
            ),
            FaqItem(
                question = "What video formats and resolutions does the transcoding pipeline support?",
                answer = "The VYRO video engine automatically renders multi-resolution ladder streams (1080p60, 720p60, 480p, 360p) with HLS master playlists, fragmented MP4 containers, AAC audio extraction, and 10x10 thumbnail storyboard sprite sheets.",
                category = "Technical & Video"
            ),
            FaqItem(
                question = "How do I generate thumbnails and AI video clips in the Diffusion Lab?",
                answer = "Navigate to Creator Studio or Infrastructure Control Plane. Enter your prompt, select your preferred aspect ratio (16:9, 9:16, 1:1), and trigger AI generation. You can instantly publish generated videos directly to the public VYRO feed.",
                category = "AI Studio"
            ),
            FaqItem(
                question = "How do Instant Wallet Tips work?",
                answer = "Viewers can tip creators directly using their VYRO ledger balance or linked payment cards. Tips bypass third-party app store holds and settle directly into the creator's ledger with instant payout capability.",
                category = "Monetization"
            ),
            FaqItem(
                question = "Is my account protected with Two-Factor Authentication?",
                answer = "Yes. VYRO Auth Engine supports Argon2id / BCrypt native credential hashing, rotating refresh tokens, session revocation across all active devices, and time-based OTP two-factor verification.",
                category = "Security"
            )
        )
    }

    val categories = listOf("All", "Monetization", "Technical & Video", "AI Studio", "Security")

    val filteredFaqs = faqs.filter { item ->
        val matchesCategory = (selectedCategory == "All" || item.category == selectedCategory)
        val matchesQuery = (searchQuery.isBlank() ||
                item.question.contains(searchQuery, ignoreCase = true) ||
                item.answer.contains(searchQuery, ignoreCase = true))
        matchesCategory && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("help_support_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "HELP CENTER & KNOWLEDGE BASE",
                    color = VyroGoldTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Help & Support",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Browse documentation, frequently asked questions, or connect with our support team.",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search help articles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VyroGoldTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroGoldTertiary,
                    unfocusedBorderColor = VyroBorder,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Categories Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) VyroGoldTertiary else VyroSurfaceElevated)
                            .border(1.dp, if (isSel) VyroGoldTertiary else VyroBorderSubtle, RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.Black else VyroTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // FAQ Accordions
        items(filteredFaqs.indices.toList()) { index ->
            val item = filteredFaqs[index]
            val isExpanded = expandedIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expandedIndex = if (isExpanded) -1 else index },
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isExpanded) VyroGoldTertiary.copy(alpha = 0.5f) else VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.question,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = VyroGoldTertiary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            HorizontalDivider(color = VyroBorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.answer,
                                color = VyroTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Contact Support Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Need direct engineer assistance?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Submit a ticket to the VYRO platform operations team with 24/7 SLA response.",
                        color = VyroTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showTicketDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Ticket", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToHealth,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceHighlight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = VyroCyanLight)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("System Health", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showTicketDialog) {
        AlertDialog(
            onDismissRequest = { showTicketDialog = false },
            title = { Text("Open Support Ticket", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ticketSubject,
                        onValueChange = { ticketSubject = it },
                        label = { Text("Subject / Issue Topic") },
                        placeholder = { Text("e.g. Transcoding ladder queue delay") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ticketMessage,
                        onValueChange = { ticketMessage = it },
                        label = { Text("Details") },
                        placeholder = { Text("Provide details...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTicketDialog = false
                        ticketSubject = ""
                        ticketMessage = ""
                        onShowSnackbar("Support ticket #TK-${System.currentTimeMillis().toString().takeLast(5)} opened! An engineer will respond shortly.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary)
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTicketDialog = false }) {
                    Text("Cancel", color = VyroTextMuted)
                }
            },
            containerColor = VyroSurfaceElevated
        )
    }
}
