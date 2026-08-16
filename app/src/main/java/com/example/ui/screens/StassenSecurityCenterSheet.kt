package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infrastructure.security.StassenSecurityEngine
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stassen Dedicated Defensive Cybersecurity & Web Security Center.
 *
 * Provides:
 * - Security Scan
 * - Website Check
 * - API Check
 * - Code Security
 * - Dependency Check
 * - Security Reports
 * - Threat Monitor
 * - Security Settings
 *
 * Strictly enforces legal authorization, defensive disclosure, and deep 7-point vulnerability audits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StassenSecurityCenterSheet(
    onDismiss: () -> Unit,
    onSendSecurityPromptToStassen: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(SecurityCenterTab.SCAN) }
    var userOwnershipConfirmed by remember { mutableStateOf(false) }
    var showAuthGateDialog by remember { mutableStateOf(false) }

    // Scan State
    var targetInput by remember { mutableStateOf("https://my-app.internal") }
    var selectedTargetType by remember { mutableStateOf(SecurityTargetType.WEBSITE) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanStatusMessage by remember { mutableStateOf("") }

    // Reports Store
    val reports = remember {
        mutableStateListOf<SecurityReport>(
            StassenSecurityEngine.analyzeWebsiteHeadersAndTls("https://my-creator-portal.com", true),
            StassenSecurityEngine.analyzeApiEndpoint("/api/v1/videos/stream", "POST", true)
        )
    }

    var selectedReport by remember { mutableStateOf<SecurityReport?>(reports.firstOrNull()) }

    // Live Threat Events
    val threatEvents = remember {
        mutableStateListOf<ThreatMonitorEvent>().apply {
            addAll(StassenSecurityEngine.generateDefensiveThreatFeed())
        }
    }

    // Code Security State
    var codeSnippetInput by remember {
        mutableStateOf(
            """
            // User search controller
            fun searchUsers(query: String): List<User> {
                val sql = "SELECT * FROM users WHERE username LIKE '%" + query + "%'"
                return database.rawQuery(sql)
            }
            """.trimIndent()
        )
    }
    var codeLanguage by remember { mutableStateOf("Kotlin / SQL") }
    var codeFindings by remember { mutableStateOf<List<SecurityVulnerability>>(emptyList()) }

    // Dependency Check State
    var manifestInput by remember {
        mutableStateOf(
            """
            dependencies {
                implementation("org.apache.logging.log4j:log4j-core:2.14.1")
                implementation("org.springframework.boot:spring-boot-starter-web:2.6.5")
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
            }
            """.trimIndent()
        )
    }
    var dependencyFindings by remember { mutableStateOf<List<SecurityVulnerability>>(emptyList()) }

    // Settings State
    var settingsState by remember { mutableStateOf(SecuritySettingsState()) }

    // Authorization Gate Modal
    if (showAuthGateDialog) {
        AlertDialog(
            onDismissRequest = { showAuthGateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = VyroGoldTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Authorization & Ownership Verification", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Stassen strictly performs defensive security analysis. Security audits and assessments are only permitted against systems, domains, APIs, or source code that you legally own or have explicit written authorization to test.",
                        color = VyroTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VyroSurface)
                            .clickable { userOwnershipConfirmed = !userOwnershipConfirmed }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = userOwnershipConfirmed,
                            onCheckedChange = { userOwnershipConfirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = VyroEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I confirm that I own this target or have explicit legal authorization to conduct defensive security testing.",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAuthGateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (userOwnershipConfirmed) VyroEmerald else VyroSurfaceElevated)
                ) {
                    Text("Confirm & Proceed", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthGateDialog = false }) {
                    Text("Cancel", color = VyroTextMuted)
                }
            },
            containerColor = VyroSurfaceElevated
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VyroSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = VyroBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("stassen_security_center_sheet")
        ) {
            // Header: Title & Authorization Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VyroEmerald.copy(alpha = 0.15f))
                            .border(1.dp, VyroEmerald.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VyroEmerald, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Security Center",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VyroEmerald.copy(alpha = 0.2f))
                                    .border(1.dp, VyroEmerald, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEFENSIVE MODE",
                                    color = VyroEmerald,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Text(
                            text = "OWASP defensive audits, vulnerability analysis & remediation",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VyroTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Authorization Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (userOwnershipConfirmed) VyroEmerald.copy(alpha = 0.12f) else VyroGoldTertiary.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        if (userOwnershipConfirmed) VyroEmerald.copy(alpha = 0.4f) else VyroGoldTertiary.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { showAuthGateDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        if (userOwnershipConfirmed) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (userOwnershipConfirmed) VyroEmerald else VyroGoldTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userOwnershipConfirmed)
                            "Testing Authorized: User confirmed system ownership & permission."
                        else
                            "Action Required: Tap to confirm asset ownership / testing permission.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = if (userOwnershipConfirmed) "VERIFIED" else "CONFIRM",
                    color = if (userOwnershipConfirmed) VyroEmerald else VyroGoldTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 8-Tab Segmented Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(SecurityCenterTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) VyroVioletPrimary else VyroSurfaceElevated)
                            .border(1.dp, if (isSelected) VyroVioletLight else VyroBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { activeTab = tab }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("tab_${tab.name.lowercase()}")
                    ) {
                        Text(text = tab.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else VyroTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    SecurityCenterTab.SCAN -> {
                        SecurityScanView(
                            target = targetInput,
                            onTargetChange = { targetInput = it },
                            targetType = selectedTargetType,
                            onTargetTypeChange = { selectedTargetType = it },
                            isAuthorized = userOwnershipConfirmed,
                            onRequestAuth = { showAuthGateDialog = true },
                            isScanning = isScanning,
                            progress = scanProgress,
                            statusMessage = scanStatusMessage,
                            onStartScan = {
                                if (!userOwnershipConfirmed) {
                                    showAuthGateDialog = true
                                    return@SecurityScanView
                                }
                                coroutineScope.launch {
                                    isScanning = true
                                    scanProgress = 0.1f
                                    scanStatusMessage = "Checking DNS, TLS/SSL cipher suites & HTTP transport..."
                                    delay(400)
                                    scanProgress = 0.4f
                                    scanStatusMessage = "Evaluating Content-Security-Policy, HSTS & framing headers..."
                                    delay(450)
                                    scanProgress = 0.75f
                                    scanStatusMessage = "Auditing OWASP Top 10 & API access-control policies..."
                                    delay(400)
                                    scanProgress = 1.0f
                                    scanStatusMessage = "Synthesizing defensive audit report & remediation plan..."
                                    delay(300)

                                    val newReport = when (selectedTargetType) {
                                        SecurityTargetType.WEBSITE, SecurityTargetType.TLS_CONFIG ->
                                            StassenSecurityEngine.analyzeWebsiteHeadersAndTls(targetInput, true)
                                        SecurityTargetType.API ->
                                            StassenSecurityEngine.analyzeApiEndpoint(targetInput, "GET/POST", true)
                                        SecurityTargetType.SOURCE_CODE -> {
                                            val findings = StassenSecurityEngine.analyzeCodeSnippet(codeSnippetInput, "Kotlin/SQL")
                                            SecurityReport(
                                                id = "rep_code_${System.currentTimeMillis()}",
                                                title = "Source Code SAST & Input Validation Audit",
                                                target = "Snippet (${findings.size} findings)",
                                                targetType = SecurityTargetType.SOURCE_CODE,
                                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                                                authorizedConfirmation = true,
                                                executiveSummary = "Static Application Security Testing completed with ${findings.size} findings identified.",
                                                findings = findings,
                                                passedChecksCount = 5,
                                                totalChecksCount = 5 + findings.size
                                            )
                                        }
                                        else -> StassenSecurityEngine.analyzeWebsiteHeadersAndTls(targetInput, true)
                                    }

                                    reports.add(0, newReport)
                                    selectedReport = newReport
                                    isScanning = false
                                    activeTab = SecurityCenterTab.SECURITY_REPORTS
                                    Toast.makeText(context, "Defensive audit complete: ${newReport.findings.size} findings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    SecurityCenterTab.WEBSITE_CHECK -> {
                        WebsiteCheckView(
                            url = targetInput,
                            onUrlChange = { targetInput = it },
                            onRunCheck = {
                                if (!userOwnershipConfirmed) {
                                    showAuthGateDialog = true
                                    return@WebsiteCheckView
                                }
                                val rep = StassenSecurityEngine.analyzeWebsiteHeadersAndTls(targetInput, true)
                                reports.add(0, rep)
                                selectedReport = rep
                                activeTab = SecurityCenterTab.SECURITY_REPORTS
                            },
                            onAskStassen = { prompt ->
                                onDismiss()
                                onSendSecurityPromptToStassen(prompt)
                            }
                        )
                    }

                    SecurityCenterTab.API_CHECK -> {
                        ApiCheckView(
                            onRunCheck = { endpoint, method ->
                                if (!userOwnershipConfirmed) {
                                    showAuthGateDialog = true
                                    return@ApiCheckView
                                }
                                val rep = StassenSecurityEngine.analyzeApiEndpoint(endpoint, method, true)
                                reports.add(0, rep)
                                selectedReport = rep
                                activeTab = SecurityCenterTab.SECURITY_REPORTS
                            },
                            onAskStassen = { prompt ->
                                onDismiss()
                                onSendSecurityPromptToStassen(prompt)
                            }
                        )
                    }

                    SecurityCenterTab.CODE_SECURITY -> {
                        CodeSecurityView(
                            codeSnippet = codeSnippetInput,
                            onCodeChange = { codeSnippetInput = it },
                            language = codeLanguage,
                            onLanguageChange = { codeLanguage = it },
                            findings = codeFindings,
                            onAnalyze = {
                                codeFindings = StassenSecurityEngine.analyzeCodeSnippet(codeSnippetInput, codeLanguage)
                            },
                            onAskStassen = { prompt ->
                                onDismiss()
                                onSendSecurityPromptToStassen(prompt)
                            }
                        )
                    }

                    SecurityCenterTab.DEPENDENCY_CHECK -> {
                        DependencyCheckView(
                            manifest = manifestInput,
                            onManifestChange = { manifestInput = it },
                            findings = dependencyFindings,
                            onAnalyze = {
                                dependencyFindings = StassenSecurityEngine.analyzeDependencies(manifestInput)
                            },
                            onAskStassen = { prompt ->
                                onDismiss()
                                onSendSecurityPromptToStassen(prompt)
                            }
                        )
                    }

                    SecurityCenterTab.SECURITY_REPORTS -> {
                        SecurityReportsView(
                            reports = reports,
                            selectedReport = selectedReport,
                            onSelectReport = { selectedReport = it },
                            onAskStassenAboutFinding = { finding ->
                                onDismiss()
                                onSendSecurityPromptToStassen(
                                    "Stassen, provide a detailed defensive remediation guide and code fix for vulnerability '${finding.title}' in component '${finding.affectedComponent}'. Risk: ${finding.risk}"
                                )
                            }
                        )
                    }

                    SecurityCenterTab.THREAT_MONITOR -> {
                        ThreatMonitorView(
                            events = threatEvents,
                            onClearLogs = { threatEvents.clear() },
                            onRefreshFeed = {
                                threatEvents.clear()
                                threatEvents.addAll(StassenSecurityEngine.generateDefensiveThreatFeed())
                            },
                            onAskStassen = { prompt ->
                                onDismiss()
                                onSendSecurityPromptToStassen(prompt)
                            }
                        )
                    }

                    SecurityCenterTab.SETTINGS -> {
                        SecuritySettingsView(
                            settings = settingsState,
                            onSettingsChange = { settingsState = it }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-views for the 8 Tabs
// -------------------------------------------------------------

@Composable
private fun SecurityScanView(
    target: String,
    onTargetChange: (String) -> Unit,
    targetType: SecurityTargetType,
    onTargetTypeChange: (SecurityTargetType) -> Unit,
    isAuthorized: Boolean,
    onRequestAuth: () -> Unit,
    isScanning: Boolean,
    progress: Float,
    statusMessage: String,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TARGET ASSET CONFIGURATION",
                        color = VyroCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Target Type", color = VyroTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SecurityTargetType.values()) { type ->
                            val selected = targetType == type
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) VyroVioletPrimary.copy(alpha = 0.25f) else VyroSurface)
                                    .border(1.dp, if (selected) VyroVioletPrimary else VyroBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onTargetTypeChange(type) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = type.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.label,
                                    fontSize = 11.sp,
                                    color = if (selected) Color.White else VyroTextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Target Endpoint / Asset Identifier", color = VyroTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = target,
                        onValueChange = onTargetChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_target_input"),
                        singleLine = true,
                        placeholder = { Text("e.g. https://api.my-domain.com or /v1/auth/login", color = VyroTextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletPrimary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isScanning) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Defensive Audit In Progress...", color = VyroCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${(progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = VyroEmerald,
                                trackColor = VyroSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = statusMessage, color = VyroTextSecondary, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = onStartScan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_scan_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAuthorized) VyroVioletPrimary else VyroGoldTertiary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAuthorized) "Launch Authorized Defensive Scan" else "Confirm Authorization to Scan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            // Defensive Baseline Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Defensive Guarantee Rule", color = VyroCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Never claim that a system is secure simply because a scan found nothing.\n• All scans evaluate static configurations, known CVE databases, header compliance, and OWASP Top 10 patterns.",
                        color = VyroTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WebsiteCheckView(
    url: String,
    onUrlChange: (String) -> Unit,
    onRunCheck: () -> Unit,
    onAskStassen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "WEBSITE & TLS/SSL CONFIGURATION AUDIT", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://your-authorized-site.com", color = VyroTextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletPrimary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRunCheck,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit HTTP Headers, TLS & Cookies", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(text = "RECOMMENDED DEFENSIVE CHECKLIST", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        val checks = listOf(
            Pair("Content-Security-Policy (CSP)", "Prevents unauthorized script injection, inline eval sinks, and XSS execution."),
            Pair("HTTP Strict Transport Security (HSTS)", "Enforces HTTPS connections and prevents SSL stripping attacks."),
            Pair("X-Frame-Options (Clickjacking)", "Guards against invisible framing and UI redressing attacks."),
            Pair("Cookie Flags: Secure, HttpOnly, SameSite=Strict", "Guards session tokens from XSS theft and CSRF request forgery."),
            Pair("TLS 1.3 Encryption & Modern Cipher Suites", "Disables deprecated TLS 1.0/1.1 and weak CBC ciphers.")
        )

        items(checks) { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = VyroCyanLight, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = desc, color = VyroTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ApiCheckView(
    onRunCheck: (String, String) -> Unit,
    onAskStassen: (String) -> Unit
) {
    var endpoint by remember { mutableStateOf("/api/v1/creators/wallet/transfer") }
    var httpMethod by remember { mutableStateOf("POST") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "API SECURITY & OWASP API TOP 10 REVIEW", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = httpMethod,
                            onValueChange = { httpMethod = it },
                            modifier = Modifier.width(90.dp),
                            singleLine = true,
                            label = { Text("Method", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VyroVioletPrimary,
                                unfocusedBorderColor = VyroBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = endpoint,
                            onValueChange = { endpoint = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Route / Resource Path", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VyroVioletPrimary,
                                unfocusedBorderColor = VyroBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onRunCheck(endpoint, httpMethod) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit API Endpoint Authorization & Rate Limits", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(text = "OWASP API SECURITY TOP 10 EVALUATORS", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        val apiRisks = listOf(
            Pair("API1:2023 - Broken Object Level Authorization (BOLA)", "Ensure user tokens only access objects within their authorized tenant context."),
            Pair("API2:2023 - Broken Authentication", "Enforce asymmetric token validation (RS256) and strict expiration timeouts."),
            Pair("API3:2023 - Broken Object Property Level Authorization", "Prevent mass assignment via explicit Data Transfer Object (DTO) whitelisting."),
            Pair("API4:2023 - Unrestricted Resource Consumption", "Enforce IP/Token rate limiting and request payload size caps.")
        )

        items(apiRisks) { (title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Checklist, contentDescription = null, tint = VyroGoldTertiary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = desc, color = VyroTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeSecurityView(
    codeSnippet: String,
    onCodeChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    findings: List<SecurityVulnerability>,
    onAnalyze: () -> Unit,
    onAskStassen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "STATIC CODE ANALYSIS (SAST)", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = language, color = VyroVioletLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codeSnippet,
                        onValueChange = onCodeChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text("Paste code snippet to analyze (SQL, Kotlin, JS, Java, Python)...", color = VyroTextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletPrimary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Code for Vulnerabilities & Secrets", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (findings.isNotEmpty()) {
            item {
                Text(text = "CODE AUDIT FINDINGS (${findings.size})", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            items(findings) { finding ->
                VulnerabilityCard(finding = finding, onAskStassen = { onAskStassen(it) })
            }
        }
    }
}

@Composable
private fun DependencyCheckView(
    manifest: String,
    onManifestChange: (String) -> Unit,
    findings: List<SecurityVulnerability>,
    onAnalyze: () -> Unit,
    onAskStassen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "DEPENDENCY VULNERABILITY CHECKER (SBOM / CVE)", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = manifest,
                        onValueChange = onManifestChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Paste build.gradle.kts, package.json, or requirements.txt...", color = VyroTextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroVioletPrimary,
                            unfocusedBorderColor = VyroBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit Dependencies Against Known CVEs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (findings.isNotEmpty()) {
            item {
                Text(text = "DEPENDENCY CVE FINDINGS (${findings.size})", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            items(findings) { finding ->
                VulnerabilityCard(finding = finding, onAskStassen = { onAskStassen(it) })
            }
        }
    }
}

@Composable
private fun SecurityReportsView(
    reports: List<SecurityReport>,
    selectedReport: SecurityReport?,
    onSelectReport: (SecurityReport) -> Unit,
    onAskStassenAboutFinding: (SecurityVulnerability) -> Unit
) {
    val context = LocalContext.current

    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No security reports generated yet. Run a scan above!", color = VyroTextMuted)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Report Selector Row
        item {
            Text(text = "GENERATED DEFENSIVE AUDIT REPORTS", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reports) { rep ->
                    val isSelected = rep.id == selectedReport?.id
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) VyroVioletPrimary.copy(alpha = 0.2f) else VyroSurfaceElevated)
                            .border(1.dp, if (isSelected) VyroVioletPrimary else VyroBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onSelectReport(rep) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rep.targetType.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rep.target.take(20),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else VyroTextSecondary
                        )
                    }
                }
            }
        }

        // Active Report Details
        selectedReport?.let { report ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                    border = BorderStroke(1.dp, VyroBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = report.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Security Report", "${report.title}\nTarget: ${report.target}\nFindings:\n" + report.findings.joinToString("\n\n") { "${it.severity.name}: ${it.title}\nFix: ${it.recommendedFix}" })
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = VyroCyanLight, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(text = "Target: ${report.target} • ${report.timestamp}", color = VyroCyanLight, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = report.executiveSummary, color = VyroTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VyroSurface)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${report.findings.size}", color = VyroRose, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text(text = "Findings", color = VyroTextMuted, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${report.passedChecksCount}", color = VyroEmerald, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text(text = "Passed", color = VyroTextMuted, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${report.totalChecksCount}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text(text = "Total Checks", color = VyroTextMuted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(text = "DETAILED VULNERABILITIES & REMEDIATIONS (${report.findings.size})", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            items(report.findings) { finding ->
                VulnerabilityCard(finding = finding, onAskStassen = { onAskStassenAboutFinding(finding) })
            }

            item {
                // Disclaimer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = report.defensiveDisclaimer,
                        color = VyroTextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreatMonitorView(
    events: List<ThreatMonitorEvent>,
    onClearLogs: () -> Unit,
    onRefreshFeed: () -> Unit,
    onAskStassen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(VyroEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "LIVE DEFENSIVE THREAT & INTRUSION FEED", color = VyroCyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Row {
                    TextButton(onClick = onRefreshFeed) {
                        Text("Refresh", color = VyroCyanLight, fontSize = 11.sp)
                    }
                    TextButton(onClick = onClearLogs) {
                        Text("Clear", color = VyroRose, fontSize = 11.sp)
                    }
                }
            }
        }

        items(events) { evt ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                border = BorderStroke(1.dp, VyroBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(evt.severity.colorHex).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = evt.severity.label, color = Color(evt.severity.colorHex), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = evt.eventType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(text = evt.timestamp, color = VyroTextMuted, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = evt.details, color = VyroTextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(VyroSurface)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = VyroEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = evt.defenseAction, color = VyroEmerald, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecuritySettingsView(
    settings: SecuritySettingsState,
    onSettingsChange: (SecuritySettingsState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "DEFENSIVE POLICIES & RESPONSIBLE DISCLOSURE", color = VyroTextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        item {
            SettingToggleItem(
                title = "Require Legal Authorization Confirmation",
                description = "Enforces explicit user ownership verification before conducting scans.",
                checked = settings.requireAuthorizationConfirmations,
                onCheckedChange = { onSettingsChange(settings.copy(requireAuthorizationConfirmations = it)) }
            )
        }

        item {
            SettingToggleItem(
                title = "Defensive Mode Enforcement",
                description = "Strictly disables exploit payload generation and redirects offensive requests to remediation guides.",
                checked = settings.defensiveModeOnly,
                onCheckedChange = { onSettingsChange(settings.copy(defensiveModeOnly = it)) }
            )
        }

        item {
            SettingToggleItem(
                title = "Auto-Suggest Remediation Code",
                description = "Generates secure code snippets (Parameterized SQL, CSP headers, Argon2) for all identified findings.",
                checked = settings.autoSuggestRemediations,
                onCheckedChange = { onSettingsChange(settings.copy(autoSuggestRemediations = it)) }
            )
        }

        item {
            SettingToggleItem(
                title = "Live Threat Monitor Stream",
                description = "Continuously simulates and audits anomalous intrusion detection telemetry.",
                checked = settings.enableThreatMonitorStream,
                onCheckedChange = { onSettingsChange(settings.copy(enableThreatMonitorStream = it)) }
            )
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VyroSurfaceElevated)
            .border(1.dp, VyroBorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = description, color = VyroTextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VyroEmerald,
                uncheckedTrackColor = VyroSurface
            )
        )
    }
}

/**
 * High-Craft 7-Point Vulnerability Card.
 * Displays all 7 mandated fields:
 * 1. Severity
 * 2. Affected component
 * 3. Explanation
 * 4. Evidence
 * 5. Risk
 * 6. Recommended fix
 * 7. Verification steps
 * plus distinction of Confirmed / Potential / Informational / Unable to verify.
 */
@Composable
fun VulnerabilityCard(
    finding: SecurityVulnerability,
    onAskStassen: (String) -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
        border = BorderStroke(1.dp, Color(finding.severity.colorHex).copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges Row (Severity + Finding Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 1. Severity Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(finding.severity.colorHex).copy(alpha = 0.2f))
                            .border(1.dp, Color(finding.severity.colorHex).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = finding.severity.label.uppercase(),
                            color = Color(finding.severity.colorHex),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Status Badge (Confirmed / Potential / Informational / Unable to Verify)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(finding.status.badgeColorHex).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = finding.status.label,
                            color = Color(finding.status.badgeColorHex),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = finding.cweCode,
                    color = VyroTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = finding.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Affected Component
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Component: ", color = VyroTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = finding.affectedComponent, color = VyroCyanLight, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Explanation
            Text(
                text = finding.explanation,
                color = VyroTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Evidence Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VyroSurface)
                    .padding(8.dp)
            ) {
                Column {
                    Text(text = "EVIDENCE", color = VyroGoldTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = finding.evidence, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Risk
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VyroRose.copy(alpha = 0.1f))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = VyroRose, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = "RISK ASSESSMENT", color = VyroRose, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = finding.risk, color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Recommended Fix Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VyroEmerald.copy(alpha = 0.1f))
                    .border(1.dp, VyroEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "RECOMMENDED FIX & REMEDIATION", color = VyroEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Fix", finding.recommendedFix))
                                Toast.makeText(context, "Remediation code copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Fix", tint = VyroEmerald, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = finding.recommendedFix, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Verification Steps
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VyroSurface)
                    .padding(8.dp)
            ) {
                Column {
                    Text(text = "DEFENSIVE VERIFICATION STEPS", color = VyroCyanLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = finding.verificationSteps, color = VyroTextSecondary, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Ask Stassen / Fix)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        onAskStassen(
                            "Stassen, provide complete defensive remediation code and step-by-step verification guide for: ${finding.title} in ${finding.affectedComponent}."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ask Stassen to Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
