package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StassenMemoryDao {
    @Query("SELECT * FROM stassen_memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<StassenMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: StassenMemoryEntity)

    @Query("DELETE FROM stassen_memories WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("DELETE FROM stassen_memories")
    suspend fun clearAllMemories()
}
