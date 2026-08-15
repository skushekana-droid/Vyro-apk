package com.example.infrastructure.api

import com.example.infrastructure.common.ApiResponse
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class ApiRouteInfo(
    val path: String,
    val method: String,
    val description: String,
    val isProtected: Boolean = true,
    val rateLimitPerMinute: Int = 120,
    val responseTimeMs: Long = 18
)

object VyroApiGateway {
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()

    val ROUTES: List<ApiRouteInfo> = listOf(
        ApiRouteInfo("/api/auth/register", "POST", "Independent user registration with password hashing & salt", false),
        ApiRouteInfo("/api/auth/login", "POST", "Credential verification, 2FA prompt, JWT token issuance", false),
        ApiRouteInfo("/api/auth/session/revoke", "POST", "Revoke active device session across all edges", true),
        ApiRouteInfo("/api/users/profile", "GET", "Fetch user profile from PostgreSQL relational table", true),
        ApiRouteInfo("/api/videos/feed", "GET", "Ranked feed from VyroRecommendationEngine", false),
        ApiRouteInfo("/api/videos/upload", "POST", "Initiate video upload to S3 storage & trigger transcoding", true),
        ApiRouteInfo("/api/shorts/stream", "GET", "Vertical micro-content stream with edge caching", false),
        ApiRouteInfo("/api/comments/create", "POST", "Post discussion comment with automated moderation filter", true),
        ApiRouteInfo("/api/follows/toggle", "POST", "Follow/unfollow creator & update follower graph", true),
        ApiRouteInfo("/api/search/query", "GET", "Full-text lexical & semantic vector search", false),
        ApiRouteInfo("/api/communities/hub", "GET", "Creator communities, channels, and exclusive VIP forums", true),
        ApiRouteInfo("/api/notifications/stream", "GET", "Real-time user event notification stream", true),
        ApiRouteInfo("/api/creator/analytics", "GET", "Creator revenue, retention, CPM, and engagement analytics", true),
        ApiRouteInfo("/api/ai/creator-assistant", "POST", "Invoke VyroAiEngine for titles, scripts, and descriptions", true),
        ApiRouteInfo("/api/ai/image-diffusion", "POST", "Trigger text-to-image thumbnail generator queue", true),
        ApiRouteInfo("/api/ai/video-diffusion", "POST", "Trigger text-to-video rendering pipeline", true),
        ApiRouteInfo("/api/media/hls/manifest", "GET", "Generate signed dynamic ABR HLS streaming manifest", false),
        ApiRouteInfo("/api/payments/tip", "POST", "Record double-entry ledger tip transaction & instant payout", true),
        ApiRouteInfo("/api/payments/ledger", "GET", "Fetch immutable financial ledger transaction audit trail", true),
        ApiRouteInfo("/api/admin/audit-logs", "GET", "Platform health, system event bus logs, and moderation queue", true)
    )

    fun checkRateLimit(clientIp: String): Boolean {
        val counter = requestCounts.computeIfAbsent(clientIp) { AtomicInteger(0) }
        return counter.incrementAndGet() <= 120
    }

    suspend fun <T> dispatchRequest(
        routePath: String,
        action: suspend () -> T
    ): ApiResponse<T> {
        delay(35) // Gateway network hop
        return try {
            val result = action()
            ApiResponse.ok(result)
        } catch (e: Exception) {
            ApiResponse.error(500, e.message ?: "Internal Server Error")
        }
    }
}
