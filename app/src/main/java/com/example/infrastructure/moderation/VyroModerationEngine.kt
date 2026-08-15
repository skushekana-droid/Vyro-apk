package com.example.infrastructure.moderation

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import com.example.model.ModerationItem
import com.example.model.ModerationStatus
import com.example.model.ReportReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ModerationActionType {
    APPROVE,
    REMOVE_CONTENT,
    AGE_GATE,
    WARN_CREATOR,
    SUSPEND_USER
}

class VyroModerationEngine {
    private val _reports = MutableStateFlow<List<ModerationItem>>(emptyList())
    val reports: StateFlow<List<ModerationItem>> = _reports.asStateFlow()

    suspend fun submitReport(
        reporterId: String,
        targetId: String,
        targetTitle: String,
        reason: String,
        details: String
    ): ModerationItem {
        val item = ModerationItem(
            id = "rep_${UUID.randomUUID().toString().take(8)}",
            contentTitle = targetTitle,
            creatorName = "Reported Creator",
            contentType = "Video",
            reportCount = 1,
            reason = ReportReason.SPAM_OR_SCAM,
            status = ModerationStatus.PENDING,
            aiSafetyScore = 0.75f,
            timestamp = "Just now"
        )
        _reports.value = listOf(item) + _reports.value

        VyroEventBus.emit(
            VyroEventType.REPORT_SUBMITTED,
            mapOf("reportId" to item.id, "targetId" to targetId, "reason" to reason),
            actorId = reporterId,
            sourceService = "vyro-moderation"
        )
        return item
    }

    suspend fun resolveReport(
        reportId: String,
        moderatorId: String,
        action: ModerationActionType,
        notes: String
    ) {
        _reports.value = _reports.value.map {
            if (it.id == reportId) {
                it.copy(status = if (action == ModerationActionType.APPROVE) ModerationStatus.APPROVED else ModerationStatus.REMOVED)
            } else it
        }

        VyroEventBus.emit(
            VyroEventType.MODERATION_ACTION_APPLIED,
            mapOf("reportId" to reportId, "action" to action.name, "moderatorId" to moderatorId, "notes" to notes),
            actorId = moderatorId,
            sourceService = "vyro-moderation"
        )
    }
}
