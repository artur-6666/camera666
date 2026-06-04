package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ItemWithCounts(
    @Embedded val item: ItemEntity,
    @ColumnInfo(name = "beforeCount") val beforeCount: Int,
    @ColumnInfo(name = "afterCount") val afterCount: Int,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String?
)

@Dao
interface ItemDao {
    @Query("""
        SELECT i.*, 
        (SELECT COUNT(*) FROM photos WHERE itemId = i.id AND type = 'BEFORE') as beforeCount,
        (SELECT COUNT(*) FROM photos WHERE itemId = i.id AND type = 'AFTER') as afterCount,
        (SELECT filePath FROM photos WHERE itemId = i.id ORDER BY createdAt DESC LIMIT 1) as thumbnailPath
        FROM items i 
        WHERE i.projectId = :projectId 
        ORDER BY i.createdAt DESC
    """)
    fun getItemsForProjectWithCounts(projectId: Long): Flow<List<ItemWithCounts>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Long): Flow<ItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Delete
    suspend fun deleteItem(item: ItemEntity)
}
