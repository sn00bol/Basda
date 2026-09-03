package com.sn00bol.basda.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_cache")
data class FileCacheEntity(
    @PrimaryKey val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: String,
    val lastModified: Long,
    val fileType: String,
    val lastAccessed: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)
