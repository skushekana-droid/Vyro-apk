package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY followersCount DESC")
    fun getAllCachedUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET displayName = :displayName, bio = :bio, country = :country, websiteUrl = :websiteUrl, categoryTagsString = :categoryTags WHERE id = :userId")
    suspend fun updateProfile(userId: String, displayName: String, bio: String, country: String, websiteUrl: String, categoryTags: String)

    @Query("UPDATE users SET followersCount = followersCount + :delta WHERE id = :userId")
    suspend fun updateFollowers(userId: String, delta: Long)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, amount: Double)
}
