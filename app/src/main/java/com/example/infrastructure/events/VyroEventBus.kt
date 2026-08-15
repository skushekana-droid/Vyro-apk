package com.example.infrastructure.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

enum class VyroEventType {
    USER_REGISTERED,
    USER_LOGGED_IN,
    USER_SUSPENDED,
    PASSWORD_RESET_REQUESTED,
    VIDEO_UPLOADED,
    VIDEO_SCAN_PASSED,
    VIDEO_TRANSCODING_STARTED,
    VIDEO_TRANSCODING_PROGRESS,
    VIDEO_PROCESSED,
    VIDEO_PUBLISHED,
    VIDEO_LIKED,
    COMMENT_CREATED,
    FOLLOW_CREATED,
    TIP_PROCESSED,
    PAYMENT_COMPLETED,
    SUBSCRIPTION_STARTED,
    CREATOR_EARNING_RECORDED,
    PAYOUT_DISPATCHED,
    COMMUNITY_JOINED,
    REPORT_SUBMITTED,
    MODERATION_ACTION_APPLIED,
    AI_GENERATION_QUEUED,
    AI_GENERATION_PROGRESS,
    AI_GENERATION_COMPLETED,
    SECURITY_ALERT
}

data class VyroEvent(
    val eventId: String = "evt_${UUID.randomUUID().toString().take(8)}",
    val type: VyroEventType,
    val payload: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis(),
    val actorId: String = "system",
    val sourceService: String = "vyro-core"
)

object VyroEventBus {
    private val _events = MutableSharedFlow<VyroEvent>(replay = 50, extraBufferCapacity = 100)
    val events: SharedFlow<VyroEvent> = _events.asSharedFlow()

    private val eventHistory = mutableListOf<VyroEvent>()

    suspend fun emit(event: VyroEvent) {
        synchronized(eventHistory) {
            eventHistory.add(0, event)
            if (eventHistory.size > 100) eventHistory.removeAt(eventHistory.size - 1)
        }
        _events.emit(event)
    }

    suspend fun emit(type: VyroEventType, payload: Map<String, String>, actorId: String = "system", sourceService: String = "vyro-core") {
        val event = VyroEvent(
            type = type,
            payload = payload,
            actorId = actorId,
            sourceService = sourceService
        )
        emit(event)
    }

    fun getRecentEvents(limit: Int = 30): List<VyroEvent> {
        return synchronized(eventHistory) {
            eventHistory.take(limit).toList()
        }
    }

    fun clearHistory() {
        synchronized(eventHistory) {
            eventHistory.clear()
        }
    }
}
