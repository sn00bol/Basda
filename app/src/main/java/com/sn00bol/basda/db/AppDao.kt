package com.sn00bol.basda.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.sn00bol.basda.db.UserEntity
import com.sn00bol.basda.db.FileCacheEntity

@Dao
interface AppDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    // File Cache Queries
    @Query("SELECT * FROM file_cache ORDER BY lastModified DESC LIMIT 50")
    fun getRecentFiles(): Flow<List<FileCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFileCache(file: FileCacheEntity)

    @Query("SELECT * FROM file_cache WHERE path = :path")
    suspend fun getFileCache(path: String): FileCacheEntity?

    @Query("UPDATE file_cache SET lastAccessed = :timestamp WHERE path = :path")
    suspend fun updateLastAccessed(path: String, timestamp: Long)

    @Query("DELETE FROM file_cache")
    suspend fun clearAllRecent()
}
