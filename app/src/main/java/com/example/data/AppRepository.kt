package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val projectDao: ProjectDao,
    private val itemDao: ItemDao,
    private val photoDao: PhotoDao
) {
    val allProjects: Flow<List<ProjectWithCounts>> = projectDao.getAllProjectsWithCounts()

    fun getProjectById(id: Long) = projectDao.getProjectById(id)
    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    fun getItemsForProjectWithCounts(projectId: Long) = itemDao.getItemsForProjectWithCounts(projectId)
    fun getItemById(id: Long) = itemDao.getItemById(id)
    suspend fun insertItem(item: ItemEntity) = itemDao.insertItem(item)
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    fun getPhotosForItem(itemId: Long) = photoDao.getPhotosForItem(itemId)
    fun getPhotosForProject(projectId: Long) = photoDao.getPhotosForProject(projectId)
    suspend fun insertPhoto(photo: PhotoEntity) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
    suspend fun updatePhoto(photo: PhotoEntity) = photoDao.updatePhoto(photo)
}
