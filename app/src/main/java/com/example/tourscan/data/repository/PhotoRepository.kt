package com.example.tourscan.data.repository

import com.example.tourscan.data.local.PhotoDao
import com.example.tourscan.data.local.PhotoEntity
import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoDao: PhotoDao) {

    suspend fun insertPhoto(photo: PhotoEntity) =
        photoDao.insert(photo)

    fun getAllPhotos(): Flow<List<PhotoEntity>> =
        photoDao.getAll()

    suspend fun getPhotoByCreatedAt(createdAt: Long): PhotoEntity? =
        photoDao.getByCreatedAt(createdAt)


    suspend fun deletePhoto(createdAt: Long) =
        photoDao.deleteByCreatedAt(createdAt)

    suspend fun deleteAllPhotos() =
        photoDao.deleteAll()
}


