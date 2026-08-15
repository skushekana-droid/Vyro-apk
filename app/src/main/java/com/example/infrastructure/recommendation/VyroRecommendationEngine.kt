package com.example.infrastructure.recommendation

import com.example.model.ContentCategory
import com.example.model.User
import com.example.model.Video

data class RecommendationSignals(
    val categoryAffinities: Map<ContentCategory, Double> = emptyMap(),
    val averageWatchPercentage: Double = 0.78,
    val likeRate: Double = 0.12,
    val shareVelocity: Double = 0.05,
    val negativeFeedbackCount: Int = 0
)

class VyroRecommendationEngine {
    val modelVersion: String = "VYRO-RecSys-v3.2-Independent"

    fun rankFeedForUser(
        videos: List<Video>,
        user: User,
        selectedCategory: ContentCategory
    ): List<Video> {
        val nonShorts = videos.filter { !it.isShort }

        return nonShorts.sortedByDescending { video ->
            var score = 0.0

            // Category match weighting
            if (selectedCategory == ContentCategory.ALL) {
                score += 50.0
            } else if (video.category == selectedCategory) {
                score += 100.0
            }

            // Follower affinity
            if (user.followedCreatorIds.contains(video.creatorId)) {
                score += 40.0
            }

            // Engagement score (likes + views + tips velocity)
            val likeRatio = if (video.viewCount > 0) video.likeCount.toDouble() / video.viewCount else 0.05
            score += (likeRatio * 200.0)
            score += (video.tipsTotalEarned.coerceAtMost(500.0) / 10.0)

            // Recency boost
            score += 20.0

            score
        }
    }
}
