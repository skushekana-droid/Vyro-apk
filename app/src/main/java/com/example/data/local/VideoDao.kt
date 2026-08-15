package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY createdTimestamp DESC")
    fun getShorts(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY createdTimestamp DESC")
    fun getLongFormVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE isSavedBookmark = 1 ORDER BY createdTimestamp DESC")
    fun getBookmarkedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :creatorId ORDER BY createdTimestamp DESC")
    fun getVideosByCreator(creatorId: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("UPDATE videos SET isSavedBookmark = :isSaved WHERE id = :videoId")
    suspend fun updateBookmarkStatus(videoId: String, isSaved: Boolean)

    @Query("UPDATE videos SET isLikedByUser = :isLiked, likeCount = likeCount + :likeDelta WHERE id = :videoId")
    suspend fun updateLikeStatus(videoId: String, isLiked: Boolean, likeDelta: Int)

    @Query("UPDATE videos SET viewCount = viewCount + 1 WHERE id = :videoId")
    suspend fun incrementViewCount(videoId: String)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideo(videoId: String)
}
