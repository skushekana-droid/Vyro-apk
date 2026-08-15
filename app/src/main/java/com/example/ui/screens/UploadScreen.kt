package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.AiTaskType
import com.example.model.ContentCategory
import com.example.model.ContentVisibility
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    isAiGenerating: Boolean,
    aiResult: String?,
    aiCurrentTask: AiTaskType,
    onGenerateAiHelp: (AiTaskType, String) -> Unit,
    onClearAiResult: () -> Unit,
    onUploadSubmit: (
        title: String,
        description: String,
        category: ContentCategory,
        tags: List<String>,
        isShort: Boolean,
        visibility: ContentVisibility,
        productTitle: String?,
        productPrice: Double?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("Tech, AI, Future, VYRO") }
    var selectedCategory by remember { mutableStateOf(ContentCategory.TECH_AI) }
    var isShortFormat by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(ContentVisibility.PUBLIC) }

    // Commerce Product Linking
    var attachProduct by remember { mutableStateOf(false) }
    var productTitle by remember { mutableStateOf("") }
    var productPriceText by remember { mutableStateOf("19.99") }

    // AI Assistant input topic
    var aiTopicInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("upload_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "CREATOR STUDIO UPLOAD",
                    color = VyroVioletLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Publish Content to VYRO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Reach global audiences and attach monetization rails directly.",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Format Switch (Long-form vs VYRO Short)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroSurfaceElevated)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isShortFormat) VyroVioletPrimary else Color.Transparent)
                        .clickable { isShortFormat = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎬 Long-Form Video",
                        color = if (!isShortFormat) Color.White else VyroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isShortFormat) VyroVioletPrimary else Color.Transparent)
                        .clickable { isShortFormat = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ VYRO Short",
                        color = if (isShortFormat) Color.White else VyroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // GEMINI AI CREATOR ASSISTANT CARD (System Innovation!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroCyanDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🤖", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gemini AI Creator Assistant",
                                color = VyroCyanLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Generate viral titles, SEO descriptions & hooks instantly",
                                color = VyroTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = aiTopicInput,
                    onValueChange = { aiTopicInput = it },
                    placeholder = { Text("What is your video about? (e.g., Quantum Computing)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroCyanSecondary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val topic = if (aiTopicInput.isNotBlank()) aiTopicInput else title
                            onGenerateAiHelp(AiTaskType.VIRAL_TITLES, topic)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !isAiGenerating
                    ) {
                        Text("🔥 Viral Titles", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val topic = if (aiTopicInput.isNotBlank()) aiTopicInput else title
                            onGenerateAiHelp(AiTaskType.DESCRIPTIONS_TAGS, topic)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyroCyanDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !isAiGenerating
                    ) {
                        Text("🏷️ SEO Tags", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AI Progress or Result Box
                if (isAiGenerating) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = VyroCyanSecondary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini is analyzing viral retention patterns...",
                            color = VyroCyanLight,
                            fontSize = 12.sp
                        )
                    }
                }

                aiResult?.let { result ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(VyroSurface)
                            .border(1.dp, VyroBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ AI Generated Suggestions",
                                color = VyroGoldTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            IconButton(onClick = onClearAiResult, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = VyroTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = result,
                            color = VyroTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (aiCurrentTask == AiTaskType.VIRAL_TITLES) {
                                    val firstLine = result.lines().find { it.contains(".") }
                                        ?.substringAfter(".")?.trim() ?: result.lines().firstOrNull() ?: ""
                                    if (firstLine.isNotBlank()) title = firstLine
                                } else if (aiCurrentTask == AiTaskType.DESCRIPTIONS_TAGS) {
                                    description = result
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VyroCyanSecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply to Form", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Title Input
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Video Title") },
                placeholder = { Text("Enter a descriptive, compelling title...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroVioletPrimary,
                    unfocusedBorderColor = VyroBorder,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_title_input")
            )
        }

        // Description Input
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Timestamps") },
                placeholder = { Text("What is this video about? Add key links and timestamps...") },
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroVioletPrimary,
                    unfocusedBorderColor = VyroBorder,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Selector
        item {
            Column {
                Text(
                    text = "Category",
                    color = VyroTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(ContentCategory.TECH_AI, ContentCategory.ECONOMY, ContentCategory.MUSIC, ContentCategory.GAMING).forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) VyroVioletDark else VyroSurfaceElevated)
                                .border(1.dp, if (isSel) VyroVioletPrimary else VyroBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.displayName,
                                color = if (isSel) Color.White else VyroTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Tags Input
        item {
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. AI, Music, Tutorial, 2026") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroVioletPrimary,
                    unfocusedBorderColor = VyroBorder,
                    focusedTextColor = VyroTextPrimary,
                    unfocusedTextColor = VyroTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Attached Creator Commerce Item
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛍️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attach Creator Store Product",
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = attachProduct,
                        onCheckedChange = { attachProduct = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VyroCyanSecondary
                        )
                    )
                }

                if (attachProduct) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = productTitle,
                        onValueChange = { productTitle = it },
                        label = { Text("Product Name / Digital Good") },
                        placeholder = { Text("e.g., Cyberpunk Preset Pack 2026") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroCyanSecondary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productPriceText,
                        onValueChange = { productPriceText = it },
                        label = { Text("Price ($ USD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroCyanSecondary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Publish Action
        item {
            Button(
                onClick = {
                    val finalTitle = if (title.isNotBlank()) title else "Innovative Creation on VYRO"
                    val parsedTags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val price = if (attachProduct) productPriceText.toDoubleOrNull() else null
                    onUploadSubmit(
                        finalTitle,
                        description,
                        selectedCategory,
                        parsedTags,
                        isShortFormat,
                        visibility,
                        if (attachProduct) productTitle else null,
                        price
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_video_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Publish Content to VYRO 🚀",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
