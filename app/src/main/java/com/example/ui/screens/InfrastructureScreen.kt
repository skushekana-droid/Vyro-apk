package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infrastructure.adapters.VyroIntegrationHub
import com.example.infrastructure.ai.AiProviderType
import com.example.infrastructure.auth.VyroAuthService
import com.example.infrastructure.cdn.MediaDeliveryService
import com.example.infrastructure.database.VyroDatabaseSchema
import com.example.infrastructure.deployment.VyroDeploymentTopology
import com.example.infrastructure.events.VyroEvent
import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.imagegen.ImageAspectRatio
import com.example.infrastructure.imagegen.GeneratedImageItem
import com.example.infrastructure.payment.LedgerEntry
import com.example.infrastructure.payment.PaymentAdapter
import com.example.infrastructure.storage.StorageService
import com.example.infrastructure.video.TranscodingJob
import com.example.infrastructure.video.TranscodingStep
import com.example.infrastructure.videogen.GeneratedVideoProject
import com.example.ui.theme.*
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class InfraTab(val title: String, val icon: @Composable () -> Unit) {
    TOPOLOGY("Grid Architecture", { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    TRANSCODING("Video Engine", { Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    AI_DIFFUSION("AI Gen Studio", { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    DATABASE("PostgreSQL DB", { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    LEDGER("Financial Ledger", { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    EVENT_BUS("Live Event Bus", { Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp)) }),
    DEPLOYMENT("Self-Hosting", { Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp)) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfrastructureScreen(
    onBack: () -> Unit,
    activeTranscodingJobs: StateFlow<List<TranscodingJob>>,
    completedTranscodingJobs: StateFlow<List<TranscodingJob>>,
    generatedImages: StateFlow<List<GeneratedImageItem>>,
    isImageGenerating: StateFlow<Boolean>,
    generatedVideos: StateFlow<List<GeneratedVideoProject>>,
    isVideoRendering: StateFlow<Boolean>,
    currentAuthService: StateFlow<VyroAuthService>,
    currentStorageService: StateFlow<StorageService>,
    currentAiProvider: StateFlow<AiProviderType>,
    currentPaymentAdapter: StateFlow<PaymentAdapter>,
    currentCdnService: StateFlow<MediaDeliveryService>,
    onSwitchAiProvider: (AiProviderType) -> Unit,
    onSwitchStorage: (Boolean) -> Unit,
    onSwitchAuth: (Boolean) -> Unit,
    onSwitchPayment: (Boolean) -> Unit,
    onSwitchCdn: (Boolean) -> Unit,
    onTriggerTestTranscode: (String, Int, Boolean) -> Unit,
    onGenerateAiImage: (String, String, ImageAspectRatio) -> Unit,
    onGenerateAiVideo: (String, String, Int) -> Unit,
    onPublishAiVideo: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(InfraTab.TOPOLOGY) }

    val activeJobs by activeTranscodingJobs.collectAsState()
    val completedJobs by completedTranscodingJobs.collectAsState()
    val images by generatedImages.collectAsState()
    val imgGenLoading by isImageGenerating.collectAsState()
    val videos by generatedVideos.collectAsState()
    val vidRenLoading by isVideoRendering.collectAsState()
    val authSvc by currentAuthService.collectAsState()
    val storageSvc by currentStorageService.collectAsState()
    val aiProvider by currentAiProvider.collectAsState()
    val paymentSvc by currentPaymentAdapter.collectAsState()
    val cdnSvc by currentCdnService.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VYRO",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = VyroTextPrimary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, top = 2.dp)
                                .size(6.dp)
                                .background(VyroVioletAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Independent Grid Architecture",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VyroZinc400
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("infra_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VyroTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VyroObsidianDeep
                )
            )
        },
        containerColor = VyroObsidianDeep
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = VyroObsidianSurface,
                contentColor = VyroVioletAccent,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = VyroBorderSubtle) }
            ) {
                InfraTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tab.icon()
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        selectedContentColor = VyroVioletAccent,
                        unselectedContentColor = VyroZinc400
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    InfraTab.TOPOLOGY -> TopologyTab(
                        authSvc = authSvc,
                        storageSvc = storageSvc,
                        aiProvider = aiProvider,
                        paymentSvc = paymentSvc,
                        cdnSvc = cdnSvc,
                        onSwitchAiProvider = onSwitchAiProvider,
                        onSwitchStorage = onSwitchStorage,
                        onSwitchAuth = onSwitchAuth,
                        onSwitchPayment = onSwitchPayment,
                        onSwitchCdn = onSwitchCdn
                    )
                    InfraTab.TRANSCODING -> TranscodingTab(
                        activeJobs = activeJobs,
                        completedJobs = completedJobs,
                        onTriggerTestTranscode = onTriggerTestTranscode
                    )
                    InfraTab.AI_DIFFUSION -> AiDiffusionTab(
                        images = images,
                        imgGenLoading = imgGenLoading,
                        videos = videos,
                        vidRenLoading = vidRenLoading,
                        onGenerateAiImage = onGenerateAiImage,
                        onGenerateAiVideo = onGenerateAiVideo,
                        onPublishAiVideo = onPublishAiVideo
                    )
                    InfraTab.DATABASE -> DatabaseTab()
                    InfraTab.LEDGER -> LedgerTab()
                    InfraTab.EVENT_BUS -> EventBusTab()
                    InfraTab.DEPLOYMENT -> DeploymentTab()
                }
            }
        }
    }
}

@Composable
private fun TopologyTab(
    authSvc: VyroAuthService,
    storageSvc: StorageService,
    aiProvider: AiProviderType,
    paymentSvc: PaymentAdapter,
    cdnSvc: MediaDeliveryService,
    onSwitchAiProvider: (AiProviderType) -> Unit,
    onSwitchStorage: (Boolean) -> Unit,
    onSwitchAuth: (Boolean) -> Unit,
    onSwitchPayment: (Boolean) -> Unit,
    onSwitchCdn: (Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lan, contentDescription = null, tint = VyroVioletAccent)
                        Text(
                            text = "Independent Platform Control Plane",
                            fontWeight = FontWeight.Bold,
                            color = VyroTextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VYRO owns its core application architecture. External services connect through replaceable adapter layers, guaranteeing 100% cloud portability without vendor lock-in.",
                        fontSize = 13.sp,
                        color = VyroZinc400,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Active Provider Switchers
        item {
            Text(
                text = "PLUGGABLE ADAPTER HUB",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        item {
            AdapterSwitchCard(
                title = "Authentication System",
                activeLabel = authSvc.providerName,
                isIndependent = authSvc.isIndependentProvider,
                toggleText = if (authSvc.isIndependentProvider) "Switch to Firebase Adapter" else "Switch to VYRO Native Identity",
                onToggle = { onSwitchAuth(!authSvc.isIndependentProvider) }
            )
        }

        item {
            AdapterSwitchCard(
                title = "Object Storage Layer",
                activeLabel = storageSvc.providerName,
                isIndependent = storageSvc.isIndependent,
                toggleText = if (storageSvc.providerName.contains("S3")) "Switch to Local Edge Storage" else "Switch to S3 Object Grid",
                onToggle = { onSwitchStorage(!storageSvc.providerName.contains("S3")) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Engine & Inference Gateway",
                        fontWeight = FontWeight.Bold,
                        color = VyroTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active: ${aiProvider.displayName}",
                        fontSize = 12.sp,
                        color = if (aiProvider.isSelfHosted) VyroVioletAccent else VyroZinc300
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        AiProviderType.values().forEach { provider ->
                            val isSelected = aiProvider == provider
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSwitchAiProvider(provider) },
                                label = { Text(provider.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VyroVioletAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = VyroVioletAccent,
                                    containerColor = VyroObsidianDeep,
                                    labelColor = VyroZinc400
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            AdapterSwitchCard(
                title = "Payment Processor & Bank Rails",
                activeLabel = paymentSvc.processorName,
                isIndependent = paymentSvc.isIndependent,
                toggleText = if (paymentSvc.isIndependent) "Switch to Stripe Connect" else "Switch to VYRO Direct Rails",
                onToggle = { onSwitchPayment(!paymentSvc.isIndependent) }
            )
        }

        item {
            AdapterSwitchCard(
                title = "CDN & Media Delivery Network",
                activeLabel = cdnSvc.providerName,
                isIndependent = cdnSvc.isIndependent,
                toggleText = if (cdnSvc.isIndependent) "Switch to Cloudflare CDN" else "Switch to VYRO Anycast Edge",
                onToggle = { onSwitchCdn(!cdnSvc.isIndependent) }
            )
        }
    }
}

@Composable
private fun AdapterSwitchCard(
    title: String,
    activeLabel: String,
    isIndependent: Boolean,
    toggleText: String,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = VyroTextPrimary,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (isIndependent) VyroVioletAccent.copy(alpha = 0.15f) else VyroZinc700.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isIndependent) "INDEPENDENT" else "3RD-PARTY ADAPTER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIndependent) VyroVioletAccent else VyroZinc400
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activeLabel,
                fontSize = 12.sp,
                color = VyroZinc300
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onToggle,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(toggleText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TranscodingTab(
    activeJobs: List<TranscodingJob>,
    completedJobs: List<TranscodingJob>,
    onTriggerTestTranscode: (String, Int, Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "FFMPEG ADAPTIVE LADDER QUEUE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = VyroVioletAccent
                    )
                    Text(
                        text = "Multi-resolution rendering: 360p, 480p, 720p, 1080p, 1440p, 4K",
                        fontSize = 11.sp,
                        color = VyroZinc400
                    )
                }

                Button(
                    onClick = {
                        onTriggerTestTranscode(
                            "Autonomous Cyber Robotics Ep. 4",
                            620,
                            false
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Test Job", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Jobs
        if (activeJobs.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE TRANSCODING JOBS (${activeJobs.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = VyroVioletAccent
                )
            }
            items(activeJobs) { job ->
                TranscodingJobCard(job = job, isActive = true)
            }
        }

        // 10-Step Pipeline Architecture Blueprint
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "10-Step Video Ingestion & Streaming Pipeline",
                        fontWeight = FontWeight.Bold,
                        color = VyroTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TranscodingStep.values().forEach { step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(VyroVioletAccent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${step.stepNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyroVioletAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.label,
                                fontSize = 12.sp,
                                color = VyroZinc300
                            )
                        }
                    }
                }
            }
        }

        // Completed Jobs History
        if (completedJobs.isNotEmpty()) {
            item {
                Text(
                    text = "RECENTLY PUBLISHED JOBS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = VyroZinc400
                )
            }
            items(completedJobs) { job ->
                TranscodingJobCard(job = job, isActive = false)
            }
        }
    }
}

@Composable
private fun TranscodingJobCard(job: TranscodingJob, isActive: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) VyroVioletAccent.copy(alpha = 0.5f) else VyroBorderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = job.videoTitle,
                    fontWeight = FontWeight.Bold,
                    color = VyroTextPrimary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) VyroVioletAccent.copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = job.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) VyroVioletAccent else Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { job.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = VyroVioletAccent,
                trackColor = VyroZinc800
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Step ${job.currentStep.stepNumber}/10: ${job.currentStep.name}",
                    fontSize = 11.sp,
                    color = VyroZinc400
                )
                Text(
                    text = "${job.progressPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyroTextPrimary
                )
            }

            if (job.logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VyroObsidianDeep, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    job.logs.takeLast(3).forEach { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = VyroZinc400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiDiffusionTab(
    images: List<GeneratedImageItem>,
    imgGenLoading: Boolean,
    videos: List<GeneratedVideoProject>,
    vidRenLoading: Boolean,
    onGenerateAiImage: (String, String, ImageAspectRatio) -> Unit,
    onGenerateAiVideo: (String, String, Int) -> Unit,
    onPublishAiVideo: (String, String) -> Unit
) {
    var imagePrompt by remember { mutableStateOf("Futuristic cyber creator workspace neon purple holo displays") }
    var selectedRatio by remember { mutableStateOf(ImageAspectRatio.THUMBNAIL_16_9) }
    var videoPrompt by remember { mutableStateOf("A 10-second cinematic drone shot over a glowing cyberpunk metropolis") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // AI Image Diffusion Section
        item {
            Text(
                text = "VYRO AI IMAGE DIFFUSION SERVICE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { imagePrompt = it },
                        label = { Text("Image Generation Prompt", color = VyroZinc400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletAccent,
                            unfocusedBorderColor = VyroBorderSubtle,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Aspect Ratio Format", fontSize = 11.sp, color = VyroZinc400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        ImageAspectRatio.values().forEach { ratio ->
                            FilterChip(
                                selected = selectedRatio == ratio,
                                onClick = { selectedRatio = ratio },
                                label = { Text(ratio.label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onGenerateAiImage(imagePrompt, "Cinematic Photorealism", selectedRatio) },
                        enabled = !imgGenLoading && imagePrompt.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (imgGenLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diffusing Image...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Thumbnail Asset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // AI Video Diffusion Section
        item {
            Text(
                text = "VYRO AI VIDEO GENERATION ENGINE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = videoPrompt,
                        onValueChange = { videoPrompt = it },
                        label = { Text("Video Prompt (e.g. 10s futuristic city scene)", color = VyroZinc400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletAccent,
                            unfocusedBorderColor = VyroBorderSubtle,
                            focusedTextColor = VyroTextPrimary,
                            unfocusedTextColor = VyroTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onGenerateAiVideo(videoPrompt, "Hyper-Realistic Sci-Fi", 10) },
                        enabled = !vidRenLoading && videoPrompt.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (vidRenLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rendering Video Diffusion...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Render 10-Second AI Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Generated Videos Gallery
        if (videos.isNotEmpty()) {
            item {
                Text("RENDERED AI VIDEO PROJECTS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VyroZinc400)
            }
            items(videos) { vid ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = vid.prompt,
                                fontWeight = FontWeight.Bold,
                                color = VyroTextPrimary,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = vid.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Duration: ${vid.durationSeconds}s • ${vid.resolution} • ${vid.fps} FPS", fontSize = 11.sp, color = VyroZinc400)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroTextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview Stream", fontSize = 11.sp)
                            }

                            if (!vid.isPublishedToVyro) {
                                Button(
                                    onClick = { onPublishAiVideo(vid.id, "AI Generation: ${vid.prompt.take(30)}") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletAccent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Publish to VYRO Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "✓ Published to Main Feed",
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseTab() {
    var selectedTable by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "POSTGRESQL RELATIONAL SCHEMA (${VyroDatabaseSchema.TABLES.size} TABLES)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        // Schema Tables
        items(VyroDatabaseSchema.TABLES) { table ->
            val isExpanded = selectedTable == table.tableName
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isExpanded) VyroVioletAccent else VyroBorderSubtle),
                modifier = Modifier.clickable { selectedTable = if (isExpanded) null else table.tableName }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = VyroVioletAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(table.tableName, fontWeight = FontWeight.Bold, color = VyroTextPrimary, fontSize = 14.sp)
                        }
                        Text("${table.columns.size} cols", fontSize = 11.sp, color = VyroZinc400)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(table.description, fontSize = 11.sp, color = VyroZinc400)

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = VyroBorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("COLUMNS & DATA TYPES:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = VyroVioletAccent)
                        Spacer(modifier = Modifier.height(4.dp))
                        table.columns.forEach { col ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text(col.name, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = VyroZinc200)
                                Text(col.type, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = VyroVioletAccent)
                            }
                        }

                        if (table.indexes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("INDEXES:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = VyroVioletAccent)
                            table.indexes.forEach { idx ->
                                Text("• $idx", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = VyroZinc400)
                            }
                        }
                    }
                }
            }
        }

        // Migrations
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "APPLIED MIGRATIONS (FLYWAY / LIQUIBASE COMPATIBLE)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        items(VyroDatabaseSchema.MIGRATIONS) { mig ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(mig.version, fontWeight = FontWeight.Bold, color = VyroVioletAccent, fontSize = 13.sp)
                        Text(mig.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mig.description, fontSize = 12.sp, color = VyroTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Checksum: ${mig.checksum.take(24)}...", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = VyroZinc500)
                }
            }
        }
    }
}

@Composable
private fun LedgerTab() {
    val ledgerHistory = remember { VyroIntegrationHub.paymentEngine.getLedgerHistory() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Double-Entry Cryptographic Financial Ledger",
                        fontWeight = FontWeight.Bold,
                        color = VyroTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Every tip, subscription, and marketplace commission is cryptographically signed and stored in immutable double-entry records.",
                        fontSize = 12.sp,
                        color = VyroZinc400
                    )
                }
            }
        }

        item {
            Text(
                text = "TRANSACTIONS AUDIT TRAIL",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        items(ledgerHistory) { tx ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tx.description, fontWeight = FontWeight.Bold, color = VyroTextPrimary, fontSize = 13.sp)
                        Text("+${"%.2f".format(tx.creatorNet)} ${tx.currency}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Gross: $${"%.2f".format(tx.amountGross)} • Platform 5% Fee: $${"%.2f".format(tx.platformFee)}", fontSize = 11.sp, color = VyroZinc400)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Hash: ${tx.txHash.take(32)}...", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = VyroZinc500)
                }
            }
        }
    }
}

@Composable
private fun EventBusTab() {
    val events = remember { VyroEventBus.getRecentEvents(40) }
    val df = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "LIVE DISTRIBUTED EVENT BUS STREAM",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        items(events) { evt ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(evt.type.name, fontWeight = FontWeight.Bold, color = VyroVioletAccent, fontSize = 12.sp)
                        Text(df.format(Date(evt.timestamp)), fontSize = 10.sp, color = VyroZinc500)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Source: ${evt.sourceService} • Actor: ${evt.actorId}", fontSize = 10.sp, color = VyroZinc400)
                    if (evt.payload.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = evt.payload.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = VyroZinc300
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeploymentTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "CONTAINERIZED SERVICE TOPOLOGY",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        items(VyroDeploymentTopology.DOCKER_COMPOSE_SERVICES) { s ->
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.serviceName, fontWeight = FontWeight.Bold, color = VyroTextPrimary, fontSize = 14.sp)
                        Text("${s.replicaCount} Replicas", fontSize = 11.sp, color = VyroVioletAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Image: ${s.containerImage}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = VyroZinc400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Resources: ${s.cpuAllocation} • ${s.memoryAllocation}", fontSize = 11.sp, color = VyroZinc300)
                    Text("Ports: ${s.ports.joinToString(", ")} • Healthcheck: ${s.healthCheckEndpoint}", fontSize = 10.sp, color = VyroZinc500)
                }
            }
        }

        item {
            Text(
                text = "DOCKER-COMPOSE CONFIGURATION BLUEPRINT",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = VyroVioletAccent
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VyroObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = VyroDeploymentTopology.DOCKER_COMPOSE_YML.trim(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = VyroZinc300
                    )
                }
            }
        }
    }
}
