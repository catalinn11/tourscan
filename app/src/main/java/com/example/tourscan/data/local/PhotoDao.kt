package com.example.tourscan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PhotoEntity)

    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE createdAt = :createdAt LIMIT 1")
    suspend fun getByCreatedAt(createdAt: Long): PhotoEntity?


    @Query("DELETE FROM photos WHERE createdAt = :createdAt")
    suspend fun deleteByCreatedAt(createdAt: Long)

    @Query("DELETE FROM photos")
    suspend fun deleteAll()

}



