package com.example.tourscan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val uri: String,
    val analyzed: Boolean = false,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)


