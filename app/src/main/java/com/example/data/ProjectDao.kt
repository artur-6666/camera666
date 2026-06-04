package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ProjectWithCounts(
    @Embedded val project: ProjectEntity,
    @ColumnInfo(name = "itemCount") val itemCount: Int,
    @ColumnInfo(name = "photoCount") val photoCount: Int
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("""
        SELECT p.*,
        (SELECT COUNT(*) FROM items WHERE projectId = p.id) as itemCount,
        (SELECT COUNT(*) FROM photos WHERE projectId = p.id) as photoCount
        FROM projects p
        ORDER BY p.createdAt DESC
    """)
    fun getAllProjectsWithCounts(): Flow<List<ProjectWithCounts>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}
