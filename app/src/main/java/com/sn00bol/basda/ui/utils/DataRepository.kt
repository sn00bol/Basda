package com.sn00bol.basda.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.mutableStateMapOf
import com.sn00bol.basda.db.AppDao
import com.sn00bol.basda.db.AppDatabase
import com.sn00bol.basda.db.FileCacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object DataRepository {
    private var appDao: AppDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _categoryCounts = mutableStateMapOf<CategoryType, Int>()
    val categoryCounts: Map<CategoryType, Int> = _categoryCounts

    private val bitmapCache = object : LruCache<String, Bitmap>(5 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val drawableCache = LruCache<String, Drawable>(50)

    fun init(context: Context) {
        if (appDao == null) {
            appDao = AppDatabase.getDatabase(context).appDao()
            // Sync recent files in background if permissions exist
            scope.launch {
                refreshRecent(context)
            }
        }
    }

    fun refreshRecent(context: Context) {
        scope.launch {
            val mediaStoreFiles = FileScanner.getRecentFiles(context, limit = 50)
            mediaStoreFiles.forEach { fileItem ->
                appDao?.insertFileCache(fileItem.toEntity())
            }
            
            // Also refresh category counts
            CATEGORIES.forEach { category ->
                val count = FileScanner.getCategoryCount(context, category.type)
                _categoryCounts[category.type] = count
            }
        }
    }

    fun getCategoryCount(context: Context, type: CategoryType): Int {
        if (!_categoryCounts.containsKey(type)) {
            scope.launch {
                val count = FileScanner.getCategoryCount(context, type)
                _categoryCounts[type] = count
            }
            return 0
        }
        return _categoryCounts[type] ?: 0
    }


    fun getRecentFilesFromDb(): Flow<List<FileItem>>? {
        return appDao?.getRecentFiles()?.map { entities ->
            entities.map { it.toFileItem() }
        }
    }

    fun addToRecent(fileItem: FileItem) {
        scope.launch {
            val entity = fileItem.toEntity()
            appDao?.insertFileCache(entity)
        }
    }
    
    fun updateLastAccessed(path: String) {
        scope.launch {
            appDao?.updateLastAccessed(path, System.currentTimeMillis())
        }
    }

    fun getBitmap(path: String): Bitmap? = bitmapCache.get(path)
    fun putBitmap(path: String, bitmap: Bitmap) {
        bitmapCache.put(path, bitmap)
    }

    fun getDrawable(path: String): Drawable? = drawableCache.get(path)
    fun putDrawable(path: String, drawable: Drawable) {
        drawableCache.put(path, drawable)
    }

    private fun FileCacheEntity.toFileItem(): FileItem {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return FileItem(
            name = name,
            isDirectory = isDirectory,
            lastModified = dateFormat.format(java.util.Date(lastModified)),
            size = size,
            fullPath = path
        )
    }

    private fun FileItem.toEntity(): FileCacheEntity {
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val timestamp = try {
            dateFormat.parse(lastModified)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        
        return FileCacheEntity(
            path = fullPath,
            name = name,
            isDirectory = isDirectory,
            size = size,
            lastModified = timestamp,
            fileType = if (isDirectory) "directory" else "file",
            lastAccessed = System.currentTimeMillis(),
            thumbnailPath = null
        )
    }
}
