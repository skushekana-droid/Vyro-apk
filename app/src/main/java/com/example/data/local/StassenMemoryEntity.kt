package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stassen_memories")
data class StassenMemoryEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val content: String,
    val createdAt: String
)
