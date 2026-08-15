package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val country: String,
    val dateJoined: String,
    val followersCount: Long,
    val followingCount: Long,
    val totalViews: Long,
    val isCreator: Boolean,
    val isVerified: Boolean,
    val role: String,
    val membershipTier: String,
    val walletBalance: Double,
    val pendingEarnings: Double
)
