package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.infrastructure.stassen.StassenBrainEngine
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StassenScreen(
    currentUser: User,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Brain engine instance
    val brainEngine = remember { StassenBrainEngine(context) }

    // Conversation and state
    var currentArea by remember { mutableStateOf(HouseArea.LIVING_ROOM) }
    var activityState by remember { mutableStateOf(StassenActivityState.IDLE) }
    var activeToolBadge by remember { mutableStateOf<String?>(null) }
    var activeTone by remember { mutableStateOf(StassenTone.FRIENDLY) }
    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Multimodal & Attachments
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var attachedDocName by remember { mutableStateOf<String?>(null) }
    var attachedDocContent by remember { mutableStateOf<String?>(null) }

    // Sheets & Dialogs
    var showPhoneModal by remember { mutableStateOf(false) }
    var showMemoryModal by remember { mutableStateOf(false) }
    var showDocPickerModal by remember { mutableStateOf(false) }
    var showIdentityModal by remember { mutableStateOf(false) }
    var showSecurityModal by remember { mutableStateOf(false) }

    // Memories State
    val memories = remember {
        mutableStateListOf(
            StassenMemoryItem("m1", "Platform", "Preferred video category is Tech & AI Cinema", "Aug 15, 2026"),
            StassenMemoryItem("m2", "Style", "Enjoys concise technical breakdowns with code snippets", "Aug 15, 2026"),
            StassenMemoryItem("m3", "Project", "Building scalable creator economy engines on VYRO", "Aug 16, 2026")
        )
    }

    // Chat History
    val messages = remember {
        mutableStateListOf(
            StassenMessage(
                id = "init_1",
                senderIsUser = false,
                text = "Hello ${currentUser.displayName}! I'm Stassen, your personal AI assistant inside VYRO. Welcome to my virtual house! I'm currently in the living room, but I can head into my workstation office for deep research, coding and analysis, check live web intelligence in the phone lounge, or analyze documents with you in the study. What are we creating or exploring today?",
                timestamp = "Just now",
                targetArea = HouseArea.LIVING_ROOM,
                activityState = StassenActivityState.IDLE
            )
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                attachedBitmap = bitmap
            } catch (e: Exception) {
                // Ignore parse failure
            }
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val textToSend = customPrompt ?: inputText.trim()
        if (textToSend.isBlank() && attachedBitmap == null && attachedDocContent == null) return

        val userMsg = StassenMessage(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            senderIsUser = true,
            text = textToSend.ifBlank { "[Attached File Analysis]" },
            timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date()),
            attachedDocumentName = attachedDocName,
            attachedImageUri = if (attachedBitmap != null) "image_attached" else null
        )
        messages.add(userMsg)

        val currentBitmap = attachedBitmap
        val currentDoc = attachedDocContent
        inputText = ""
        attachedBitmap = null
        attachedDocName = null
        attachedDocContent = null
        isProcessing = true

        scope.launch {
            listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))

            val result = brainEngine.processUserRequest(
                userPrompt = textToSend,
                conversationHistory = messages,
                activeTone = activeTone,
                memories = memories,
                currentUser = currentUser,
                attachedBitmap = currentBitmap,
                attachedDocText = currentDoc,
                onStatusChange = { newArea, newState, toolBadge ->
                    currentArea = newArea
                    activityState = newState
                    activeToolBadge = toolBadge
                }
            )

            result.newMemory?.let { newMem ->
                memories.add(0, newMem)
            }

            val assistantMsg = StassenMessage(
                id = "msg_${UUID.randomUUID().toString().take(6)}",
                senderIsUser = false,
                text = result.text,
                timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date()),
                targetArea = result.areaUsed,
                activityState = StassenActivityState.IDLE,
                actionToolBadge = result.toolBadge
            )
            messages.add(assistantMsg)
            isProcessing = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = VyroBackground,
        topBar = {
            StassenTopBar(
                currentArea = currentArea,
                activityState = activityState,
                activeTone = activeTone,
                onBack = onBack,
                onOpenIdentity = { showIdentityModal = true },
                onOpenSecurity = { showSecurityModal = true },
                onOpenPhone = { showPhoneModal = true },
                onOpenMemory = { showMemoryModal = true },
                onToneChange = { activeTone = it }
            )
        },
        bottomBar = {
            StassenInputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                onSend = { sendMessage() },
                onAttachPhoto = { photoPickerLauncher.launch("image/*") },
                onAttachDoc = { showDocPickerModal = true },
                onOpenPhone = { showPhoneModal = true },
                hasAttachedImage = attachedBitmap != null,
                attachedDocName = attachedDocName,
                onRemoveImage = { attachedBitmap = null },
                onRemoveDoc = {
                    attachedDocName = null
                    attachedDocContent = null
                },
                isProcessing = isProcessing
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("stassen_screen")
        ) {
            // State-Managed Virtual Room Context View Switcher
            StassenRoomViewSwitcher(
                currentArea = currentArea,
                activityState = activityState,
                activeToolBadge = activeToolBadge,
                onAreaSelected = { newArea ->
                    if (currentArea != newArea) {
                        currentArea = newArea
                        activityState = StassenActivityState.WALKING_TO_AREA
                        scope.launch {
                            kotlinx.coroutines.delay(350)
                            activityState = StassenActivityState.IDLE
                        }
                    }
                },
                onQuickAction = { actionPrompt ->
                    sendMessage(actionPrompt)
                }
            )

            // Conversation Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    StassenMessageBubble(
                        message = message,
                        onSpeak = { text ->
                            if (isSpeaking) {
                                brainEngine.stopSpeaking()
                                isSpeaking = false
                            } else {
                                isSpeaking = true
                                brainEngine.speak(text) {
                                    isSpeaking = false
                                }
                            }
                        },
                        isSpeaking = isSpeaking
                    )
                }

                if (isProcessing) {
                    item {
                        StassenProcessingIndicator(
                            currentArea = currentArea,
                            activityState = activityState,
                            toolBadge = activeToolBadge
                        )
                    }
                }
            }
        }
    }

    // Virtual Phone Modal Sheet
    if (showPhoneModal) {
        StassenVirtualPhoneSheet(
            currentUser = currentUser,
            onDismiss = { showPhoneModal = false },
            onRunAppAction = { appPrompt ->
                showPhoneModal = false
                currentArea = HouseArea.PHONE_LOUNGE
                sendMessage(appPrompt)
            }
        )
    }

    // Memory Inspector Modal Sheet
    if (showMemoryModal) {
        StassenMemorySheet(
            memories = memories,
            onDismiss = { showMemoryModal = false },
            onDeleteMemory = { id -> memories.removeAll { it.id == id } },
            onClearAll = { memories.clear() }
        )
    }

    // Document Analyzer Selection Modal
    if (showDocPickerModal) {
        StassenDocumentSelectorSheet(
            onDismiss = { showDocPickerModal = false },
            onSelectDocument = { name, content ->
                attachedDocName = name
                attachedDocContent = content
                showDocPickerModal = false
            }
        )
    }

    // Persistent Stassen Character Identity Inspector Sheet
    if (showIdentityModal) {
        StassenIdentityInspectorSheet(
            currentArea = currentArea,
            onDismiss = { showIdentityModal = false },
            onAskAboutIdentity = { prompt ->
                showIdentityModal = false
                sendMessage(prompt)
            }
        )
    }

    // Dedicated Cybersecurity & Web Security Center Sheet
    if (showSecurityModal) {
        StassenSecurityCenterSheet(
            onDismiss = { showSecurityModal = false },
            onSendSecurityPromptToStassen = { prompt ->
                showSecurityModal = false
                sendMessage(prompt)
            }
        )
    }
}

/**
 * Top Bar for Stassen Virtual Companion with Quick Action Controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StassenTopBar(
    currentArea: HouseArea,
    activityState: StassenActivityState,
    activeTone: StassenTone,
    onBack: () -> Unit,
    onOpenIdentity: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenMemory: () -> Unit,
    onToneChange: (StassenTone) -> Unit
) {
    var showToneMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VyroSurface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenIdentity() }
                    .padding(end = 6.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Persistent Stassen Avatar Reference with Online Pulse
                Box {
                    Image(
                        painter = painterResource(id = StassenIdentityRegistry.PRIMARY_PORTRAIT_RES),
                        contentDescription = "Stassen Canonical Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, VyroVioletPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (activityState.isBusy) VyroGoldTertiary else VyroEmerald)
                            .border(1.5.dp, VyroSurface, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Stassen",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VyroVioletDark)
                                .border(1.dp, VyroVioletPrimary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VERIFIED IDENTITY",
                                color = VyroVioletLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Text(
                        text = activityState.displayStatus,
                        color = if (activityState.isBusy) VyroCyanLight else VyroTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Quick Navigation Actions (Identity, Security, Phone, Memories, Tone)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Identity Verification Badge Action
                IconButton(
                    onClick = onOpenIdentity,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("stassen_identity_btn")
                ) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = "Persistent Identity", tint = VyroVioletLight)
                }

                // Dedicated Defensive Cybersecurity Center Action
                IconButton(
                    onClick = onOpenSecurity,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("stassen_security_center_btn")
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = "Cybersecurity Defense Center", tint = VyroEmerald)
                }

                // Virtual Phone Action
                IconButton(
                    onClick = onOpenPhone,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("stassen_phone_btn")
                ) {
                    Icon(Icons.Outlined.Smartphone, contentDescription = "Virtual Phone", tint = VyroCyanLight)
                }

                // Memory Matrix Action
                IconButton(
                    onClick = onOpenMemory,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("stassen_memory_btn")
                ) {
                    Icon(Icons.Outlined.Psychology, contentDescription = "Memory Hub", tint = VyroGoldTertiary)
                }

                // Tone Selector
                Box {
                    IconButton(
                        onClick = { showToneMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Tone Settings", tint = VyroTextPrimary)
                    }

                    DropdownMenu(
                        expanded = showToneMenu,
                        onDismissRequest = { showToneMenu = false },
                        modifier = Modifier
                            .background(VyroSurfaceElevated)
                            .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "COMMUNICATION STYLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyroTextMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        StassenTone.values().forEach { tone ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = tone.label,
                                            color = if (activeTone == tone) VyroCyanLight else VyroTextPrimary,
                                            fontWeight = if (activeTone == tone) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(text = tone.description, fontSize = 10.sp, color = VyroTextMuted)
                                    }
                                },
                                onClick = {
                                    onToneChange(tone)
                                    showToneMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * State-Managed View Switcher that transitions Stassen between virtual room contexts.
 */
@Composable
private fun StassenRoomViewSwitcher(
    currentArea: HouseArea,
    activityState: StassenActivityState,
    activeToolBadge: String?,
    onAreaSelected: (HouseArea) -> Unit,
    onQuickAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Room View Switcher Segmented Selector Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = VyroSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorder)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(HouseArea.values()) { area ->
                    val isSelected = currentArea == area
                    val emoji = when (area) {
                        HouseArea.LIVING_ROOM -> "🛋️"
                        HouseArea.OFFICE -> "💻"
                        HouseArea.STUDY -> "📚"
                        HouseArea.PHONE_LOUNGE -> "📱"
                        HouseArea.ENTERTAINMENT -> "🎵"
                        HouseArea.REST_AREA -> "🌙"
                    }
                    val shortName = when (area) {
                        HouseArea.LIVING_ROOM -> "Living Room"
                        HouseArea.OFFICE -> "Office"
                        HouseArea.STUDY -> "Library / Study"
                        HouseArea.PHONE_LOUNGE -> "Phone Lounge"
                        HouseArea.ENTERTAINMENT -> "Media Hub"
                        HouseArea.REST_AREA -> "Rest Area"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) VyroVioletPrimary else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) VyroVioletLight.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onAreaSelected(area) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(text = emoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = shortName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else VyroTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated Room Context Transition View Switcher
        AnimatedContent(
            targetState = currentArea,
            transitionSpec = {
                (fadeIn(animationSpec = tween(280)) + slideInHorizontally(animationSpec = tween(280)) { width -> width / 4 })
                    .togetherWith(
                        fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { width -> -width / 4 }
                    )
            },
            label = "RoomContextSwitcher"
        ) { targetRoom ->
            val drawableRes = when (targetRoom) {
                HouseArea.LIVING_ROOM -> R.drawable.img_stassen_house_1786868204218
                HouseArea.OFFICE -> R.drawable.img_stassen_office_1786868216401
                HouseArea.STUDY -> R.drawable.img_stassen_study_1786868226097
                HouseArea.PHONE_LOUNGE -> R.drawable.img_stassen_phone_lounge_1786868275702
                HouseArea.ENTERTAINMENT -> R.drawable.img_stassen_house_1786868204218
                HouseArea.REST_AREA -> R.drawable.img_stassen_study_1786868226097
            }

            val roomQuickActions = when (targetRoom) {
                HouseArea.LIVING_ROOM -> listOf(
                    "Brainstorm viral video concepts",
                    "Discuss modern creator economy trends",
                    "How are you doing today, Stassen?"
                )
                HouseArea.OFFICE -> listOf(
                    "🛡️ Run defensive security audit on web headers & API endpoints",
                    "OWASP Top 10 vulnerability check & secure code review",
                    "What is the best laptop for video editing in 2026?"
                )
                HouseArea.STUDY -> listOf(
                    "Write an essay on AI and digital identity",
                    "Deep analyze a technical research paper",
                    "Explain quantum computing fundamentals"
                )
                HouseArea.PHONE_LOUNGE -> listOf(
                    "Check live atmospheric weather conditions",
                    "Search live web for latest AI news",
                    "VYRO platform server health & latency"
                )
                HouseArea.ENTERTAINMENT -> listOf(
                    "Soundtrack recommendations for cinema shorts",
                    "Audio mixing techniques for voiceovers",
                    "Media trends and sound effects library"
                )
                HouseArea.REST_AREA -> listOf(
                    "Organize weekly creator production goals",
                    "Daily mindfulness & productivity reflection",
                    "Set task priorities for tomorrow"
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, VyroBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = targetRoom.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    // Live Area Info & Stassen State
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (activityState.isBusy) VyroGoldTertiary else VyroEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE CONTEXT • ${targetRoom.title.uppercase()}",
                                color = VyroCyanLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = targetRoom.description,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }

                    // Action badge on the top right
                    activeToolBadge?.let { badge ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VyroSurfaceElevated.copy(alpha = 0.9f))
                                .border(1.dp, VyroVioletPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚡", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = badge,
                                    color = VyroGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Room-specific contextual action chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(roomQuickActions) { actionPrompt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(VyroSurfaceElevated)
                                .border(1.dp, VyroBorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { onQuickAction(actionPrompt) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "✨", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = actionPrompt,
                                color = VyroTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Message Bubble in the Conversation Thread.
 */
@Composable
private fun StassenMessageBubble(
    message: StassenMessage,
    onSpeak: (String) -> Unit,
    isSpeaking: Boolean
) {
    val isUser = message.senderIsUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = StassenIdentityRegistry.PRIMARY_PORTRAIT_RES),
                contentDescription = "Stassen Character Identity",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, VyroVioletPrimary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) VyroVioletPrimary.copy(alpha = 0.9f)
                    else VyroSurfaceElevated
                )
                .border(
                    1.dp,
                    if (isUser) VyroVioletLight.copy(alpha = 0.4f) else VyroBorder,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            // Attached badge preview
            message.actionToolBadge?.let { tool ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VyroSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "🛠️ $tool", fontSize = 9.sp, color = VyroCyanLight, fontWeight = FontWeight.Bold)
                }
            }

            message.attachedDocumentName?.let { doc ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VyroSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = doc, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = message.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.timestamp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else VyroTextMuted,
                    fontSize = 10.sp
                )

                if (!isUser) {
                    IconButton(
                        onClick = { onSpeak(message.text) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = if (isSpeaking) VyroCyanLight else VyroTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated Processing State with Activity Visuals.
 */
@Composable
private fun StassenProcessingIndicator(
    currentArea: HouseArea,
    activityState: StassenActivityState,
    toolBadge: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = StassenIdentityRegistry.PRIMARY_PORTRAIT_RES),
            contentDescription = "Stassen Character Identity",
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .border(1.dp, VyroVioletPrimary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(VyroSurfaceElevated)
                .border(1.dp, VyroVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = VyroVioletPrimary,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = activityState.displayStatus,
                    color = VyroVioletLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                toolBadge?.let {
                    Text(
                        text = "Running: $it",
                        color = VyroCyanLight,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Input Bar with Voice, Attachments, Document Picker & Direct Phone Trigger.
 */
@Composable
private fun StassenInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachPhoto: () -> Unit,
    onAttachDoc: () -> Unit,
    onOpenPhone: () -> Unit,
    hasAttachedImage: Boolean,
    attachedDocName: String?,
    onRemoveImage: () -> Unit,
    onRemoveDoc: () -> Unit,
    isProcessing: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = VyroSurface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attachment chips
            if (hasAttachedImage || attachedDocName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasAttachedImage) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VyroVioletDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VyroVioletPrimary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = VyroVioletLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Attached Image", fontSize = 11.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = VyroRose,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveImage() }
                                )
                            }
                        }
                    }

                    attachedDocName?.let { doc ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VyroCyanDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VyroCyanSecondary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(doc, fontSize = 11.sp, color = Color.White, maxLines = 1)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = VyroRose,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveDoc() }
                                )
                            }
                        }
                    }
                }
            }

            // Text Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAttachPhoto, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = "Add Photo", tint = VyroTextSecondary)
                }

                IconButton(onClick = onAttachDoc, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = "Add Document", tint = VyroTextSecondary)
                }

                IconButton(onClick = onOpenPhone, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Outlined.Apps, contentDescription = "Virtual Apps", tint = VyroCyanLight)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = { Text("Ask Stassen anything...", fontSize = 13.sp, color = VyroTextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("stassen_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = VyroSurfaceElevated,
                        unfocusedContainerColor = VyroSurfaceElevated
                    ),
                    maxLines = 4
                )

                IconButton(
                    onClick = onSend,
                    enabled = (inputText.isNotBlank() || hasAttachedImage || attachedDocName != null) && !isProcessing,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() || hasAttachedImage || attachedDocName != null) VyroVioletPrimary else VyroSurfaceElevated)
                        .testTag("stassen_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() || hasAttachedImage || attachedDocName != null) Color.White else VyroTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stassen's Virtual Smartphone Sheet (Simulated Apps & Live Web Tools).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StassenVirtualPhoneSheet(
    currentUser: User,
    onDismiss: () -> Unit,
    onRunAppAction: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyroSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = VyroBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("stassen_phone_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📱", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Stassen's Virtual Phone",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Live tools, sensors, search & companion apps",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VyroTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val phoneApps = listOf(
                Pair("🛡️ Cybersecurity & Web Defense", "Perform defensive security scan, website checks, API audits & CVE tracker"),
                Pair("⛅ Live Weather Sensor", "Check current weather, atmospheric conditions, and tomorrow's forecast"),
                Pair("🌐 Live Web Search", "Search the live web for the latest developments, news, and releases"),
                Pair("💻 Laptop Comparison Engine", "What is the best laptop for video editing and programming in 2026?"),
                Pair("⚡ VYRO Infrastructure Status", "Check live server health, transcode queues, and edge latency"),
                Pair("📅 Calendar & Creator Plan", "Organize a 7-day content production schedule for my channel")
            )

            phoneApps.forEach { (title, prompt) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroSurface)
                        .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                        .clickable { onRunAppAction(prompt) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = prompt, color = VyroTextSecondary, fontSize = 11.sp, maxLines = 1)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VyroTextMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Long-Term User Preference Memory Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StassenMemorySheet(
    memories: List<StassenMemoryItem>,
    onDismiss: () -> Unit,
    onDeleteMemory: (String) -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyroSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = VyroBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("stassen_memory_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧠", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Stassen's Memory Matrix",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Learned preferences, ongoing projects & user context",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(onClick = onClearAll) {
                    Text("Clear All", color = VyroRose, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No memories saved yet. Stassen learns as you chat!", color = VyroTextMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(memories, key = { it.id }) { mem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VyroSurface)
                                .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(VyroVioletDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = mem.category, color = VyroVioletLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = mem.createdAt, color = VyroTextMuted, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = mem.content, color = Color.White, fontSize = 12.sp)
                            }

                            IconButton(onClick = { onDeleteMemory(mem.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VyroRose, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Document Selector Sheet for Multimodal Deep Reading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StassenDocumentSelectorSheet(
    onDismiss: () -> Unit,
    onSelectDocument: (String, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyroSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = VyroBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📚", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Study & Document Reader",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Choose a document for Stassen to analyze in his study",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VyroTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val sampleDocs = listOf(
                Pair(
                    "VYRO_Creator_Economy_Whitepaper_2026.pdf",
                    "This document details the shift towards decentralized media tokenomics, micro-tipping direct settlement engines, and algorithmic sovereignty for digital content creators."
                ),
                Pair(
                    "Video_Transcoding_Ladder_Architecture.txt",
                    "FFmpeg multi-bitrate ladder specifications for 4K AV1, 1080p H.265, and 720p H.264 video streams with low-latency CDN segmentation."
                ),
                Pair(
                    "Content_Creation_Monetization_Contract.docx",
                    "Standard creator agreement outlining 85/15 revenue split on digital store sales, live super-chats, and exclusive community tier memberships."
                )
            )

            sampleDocs.forEach { (name, content) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroSurface)
                        .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                        .clickable { onSelectDocument(name, content) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = content, color = VyroTextSecondary, fontSize = 11.sp, maxLines = 2)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VyroTextMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Persistent Stassen Character Identity Inspector Sheet.
 * Displays the canonical reference lock, physical profile attributes,
 * scene attire consistency, and generation limitation safeguards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StassenIdentityInspectorSheet(
    currentArea: HouseArea,
    onDismiss: () -> Unit,
    onAskAboutIdentity: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyroSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = VyroBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("stassen_identity_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VyroVioletLight, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Stassen Character Identity",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "One persistent digital human across all scenes & media",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VyroTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canonical Face Reference Hero Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VyroSurface)
                    .border(1.5.dp, VyroVioletPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Image(
                        painter = painterResource(id = StassenIdentityRegistry.PRIMARY_PORTRAIT_RES),
                        contentDescription = "Canonical Portrait",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, VyroVioletPrimary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VyroEmerald)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = "LOCKED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Canonical Reference",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, tint = VyroGoldTertiary, modifier = Modifier.size(13.dp))
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Face & biometric likeness are permanently locked to this master reference across the entire application.",
                        color = VyroTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Identity Consistency Safeguard Guarantee Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroVioletDark.copy(alpha = 0.35f))
                    .border(1.dp, VyroVioletPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Identity Consistency Policy",
                            color = VyroCyanLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• All animations, scenes, images, and videos use the same persistent Stassen identity.\n• If the system cannot guarantee exact visual identity in a particular generation, it will not substitute a different person and will explicitly indicate the limitation.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Physical Profile Attributes
            Text(
                text = "PERSISTENT BIOMETRIC ATTRIBUTES",
                color = VyroTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val attributes = listOf(
                Pair("Hair & Facial Features", StassenIdentityRegistry.PHYSICAL_IDENTITY.hairStyle + " • " + StassenIdentityRegistry.PHYSICAL_IDENTITY.eyeFeatures),
                Pair("Facial Structure", StassenIdentityRegistry.PHYSICAL_IDENTITY.facialStructure),
                Pair("Current Area Attire", StassenIdentityRegistry.PHYSICAL_IDENTITY.attireStyles[currentArea] ?: "Modern smart casual")
            )

            attributes.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VyroSurface)
                        .border(1.dp, VyroBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = VyroTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Ask Button
            Button(
                onClick = { onAskAboutIdentity("Stassen, describe your persistent digital identity and how you maintain consistent appearance across scenes.") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask Stassen About His Persistent Identity", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
