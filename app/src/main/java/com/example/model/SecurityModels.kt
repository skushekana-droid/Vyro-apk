package com.example.model

/**
 * Severity levels for defensive security assessments.
 */
enum class SecuritySeverity(val label: String, val colorHex: Long) {
    CRITICAL("Critical", 0xFFEF4444),
    HIGH("High", 0xFFF97316),
    MEDIUM("Medium", 0xFFF59E0B),
    LOW("Low", 0xFF3B82F6),
    INFORMATIONAL("Informational", 0xFF10B981)
}

/**
 * Finding verification status.
 * Stassen must clearly distinguish between confirmed, potential, informational, and unverifiable findings.
 */
enum class FindingStatus(val label: String, val badgeColorHex: Long) {
    CONFIRMED_VULNERABILITY("Confirmed Vulnerability", 0xFFEF4444),
    POTENTIAL_VULNERABILITY("Potential Vulnerability", 0xFFF59E0B),
    INFORMATIONAL_FINDING("Informational Finding", 0xFF3B82F6),
    UNABLE_TO_VERIFY("Unable to Verify", 0xFF8B5CF6)
}

/**
 * Types of targets evaluated in Stassen's Defensive Security Center.
 */
enum class SecurityTargetType(val label: String, val icon: String) {
    WEBSITE("Website & Headers", "🌐"),
    WEB_APP("Web Application", "💻"),
    API("REST / GraphQL API", "⚡"),
    SOURCE_CODE("Source Code (SAST)", "📄"),
    DEPENDENCY_MANIFEST("Dependencies & SBOM", "📦"),
    SERVER_LOG("Server & Access Logs", "📋"),
    TLS_CONFIG("TLS / SSL Configuration", "🔒")
}

/**
 * Individual defensive vulnerability finding.
 * Shows all 7 required fields:
 * 1. Severity
 * 2. Affected component
 * 3. Explanation
 * 4. Evidence
 * 5. Risk
 * 6. Recommended fix
 * 7. Verification steps
 */
data class SecurityVulnerability(
    val id: String,
    val title: String,
    val severity: SecuritySeverity,
    val status: FindingStatus,
    val affectedComponent: String,
    val explanation: String,
    val evidence: String,
    val risk: String,
    val recommendedFix: String,
    val verificationSteps: String,
    val owaspCategory: String = "OWASP Top 10",
    val cweCode: String = "CWE-Unknown"
)

/**
 * Complete Defensive Security Assessment Report.
 */
data class SecurityReport(
    val id: String,
    val title: String,
    val target: String,
    val targetType: SecurityTargetType,
    val timestamp: String,
    val authorizedConfirmation: Boolean,
    val executiveSummary: String,
    val findings: List<SecurityVulnerability>,
    val passedChecksCount: Int,
    val totalChecksCount: Int,
    val defensiveDisclaimer: String = "Defensive Assessment Notice: No automated security scan can prove the complete absence of vulnerabilities or zero-day exploits. This report reflects defense-in-depth static and configuration analysis for authorized assets only."
)

/**
 * Real-time Defensive Threat Monitor Event.
 */
data class ThreatMonitorEvent(
    val id: String,
    val timestamp: String,
    val sourceIp: String,
    val eventType: String,
    val severity: SecuritySeverity,
    val details: String,
    val defenseAction: String,
    val blocked: Boolean
)

/**
 * The 8 Core Tabs in Stassen's Security Center.
 */
enum class SecurityCenterTab(val label: String, val icon: String, val subtitle: String) {
    SCAN("Security Scan", "🛡️", "Comprehensive authorized multi-vector scanner"),
    WEBSITE_CHECK("Website Check", "🌐", "HTTP headers, TLS/SSL & cookie security"),
    API_CHECK("API Check", "⚡", "Authentication, token entropy & OWASP API Top 10"),
    CODE_SECURITY("Code Security", "💻", "Static code review, input sanitization & secrets"),
    DEPENDENCY_CHECK("Dependency Check", "📦", "Software bill of materials & CVE tracker"),
    SECURITY_REPORTS("Security Reports", "📊", "Exportable defensive compliance audits"),
    THREAT_MONITOR("Threat Monitor", "📡", "Live anomaly detection & defensive telemetry"),
    SETTINGS("Security Settings", "⚙️", "Defensive policies, authorization & rules")
}

/**
 * Defensive Security Settings and Policy Configuration.
 */
data class SecuritySettingsState(
    val requireAuthorizationConfirmations: Boolean = true,
    val defensiveModeOnly: Boolean = true,
    val enableThreatMonitorStream: Boolean = true,
    val owaspBenchmarkVersion: String = "OWASP Top 10:2021 & API:2023",
    val autoSuggestRemediations: Boolean = true,
    val responsibleDisclosureMode: Boolean = true,
    val authorizedDomains: List<String> = listOf("localhost", "*.vyro.internal", "my-authorized-app.com")
)

/**
 * Result when Stassen intercepts an offensive or dangerous request and redirects defensively.
 */
data class DefensiveRedirectResult(
    val originalQuery: String,
    val matchedPattern: String,
    val defensiveGuidance: String
)

