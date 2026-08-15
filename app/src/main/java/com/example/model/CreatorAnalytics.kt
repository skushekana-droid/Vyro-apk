package com.example.model

data class DailyMetric(
    val dayLabel: String,
    val views: Long,
    val earnings: Double,
    val subscribers: Long
)

data class AudienceDemographic(
    val country: String,
    val percentage: Float
)

data class TrafficSource(
    val sourceName: String,
    val percentage: Float
)

data class CreatorAnalytics(
    val totalViews: Long = 184520,
    val watchTimeHours: Double = 4280.5,
    val totalFollowers: Long = 24900,
    val followerGrowthRate: Double = 14.8,
    val averageEngagementRate: Double = 8.6,
    val estimatedTotalRevenue: Double = 3420.50,
    val adShareRevenue: Double = 1850.20,
    val membershipRevenue: Double = 980.00,
    val tipRevenue: Double = 410.30,
    val marketplaceRevenue: Double = 180.00,
    val dailyMetrics: List<DailyMetric> = emptyList(),
    val topGeos: List<AudienceDemographic> = emptyList(),
    val trafficSources: List<TrafficSource> = emptyList()
)
