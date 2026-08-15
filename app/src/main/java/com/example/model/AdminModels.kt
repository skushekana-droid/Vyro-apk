package com.example.model

enum class ReportReason {
    COPYRIGHT,
    HARMFUL_MISINFORMATION,
    SPAM_OR_SCAM,
    INAPPROPRIATE_CONTENT,
    HARASSMENT
}

enum class ModerationStatus {
    PENDING,
    APPROVED,
    FLAGGED,
    REMOVED
}

data class ModerationItem(
    val id: String,
    val contentTitle: String,
    val creatorName: String,
    val contentType: String = "Video",
    val reportCount: Int = 1,
    val reason: ReportReason = ReportReason.SPAM_OR_SCAM,
    val status: ModerationStatus = ModerationStatus.PENDING,
    val aiSafetyScore: Float = 0.92f, // 0.0 to 1.0 confidence
    val timestamp: String = "2 hours ago"
)
