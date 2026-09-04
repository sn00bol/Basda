package com.sn00bol.basda.ui.utils

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

object FileScanner {

    fun getCategoryCount(context: Context, categoryType: CategoryType): Int {
        val detail = CATEGORIES.find { it.type == categoryType } ?: return 0
        val showHidden = SettingsManager.showHiddenFiles
        
        return when (categoryType) {
            CategoryType.DOWNLOADS -> {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir.listFiles()?.count { 
                    it.isFile && (showHidden || !it.name.startsWith("."))
                } ?: 0
            }
            else -> {
                queryMediaStoreCount(context, detail.extensions)
            }
        }
    }

    fun getFilesForCategory(context: Context, categoryType: CategoryType): List<FileItem> {
        val detail = CATEGORIES.find { it.type == categoryType } ?: return emptyList()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val showHidden = SettingsManager.showHiddenFiles

        return when (categoryType) {
            CategoryType.DOWNLOADS -> {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir.listFiles()?.filter { 
                    it.isFile && (showHidden || !it.name.startsWith("."))
                }?.map {
                    FileItem(
                        name = it.name,
                        isDirectory = false,
                        lastModified = dateFormat.format(Date(it.lastModified())),
                        size = formatFileSize(it.length()),
                        fullPath = it.absolutePath
                    )
                }?.sortedByDescending { it.fullPath } ?: emptyList()
            }
            else -> {
                queryMediaStoreFiles(context, detail.extensions)
            }
        }
    }

    fun getRecentFiles(context: Context, limit: Int = 100): List<FileItem> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val files = mutableListOf<FileItem>()

        // Get all known extensions from CATEGORIES
        val allExtensions = CATEGORIES.flatMap { it.extensions }.map { it.lowercase() }.toSet()
        
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} NOT LIKE '.*'"
        
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit * 2) // Query more to filter later
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            }
            context.contentResolver.query(uri, projection, queryArgs, null)
        } else {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )
        }

        cursor?.use { c ->
            val nameIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dataIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateIndex = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (c.moveToNext() && files.size < limit) {
                val name = c.getString(nameIndex) ?: ""
                val path = c.getString(dataIndex) ?: ""
                val size = c.getLong(sizeIndex)
                val date = c.getLong(dateIndex) * 1000

                val extension = name.substringAfterLast('.', "").lowercase()
                
                // Extremely strict filtering for Recent tab:
                val isHidden = name.startsWith(".")
                val isSystemPath = path.contains("/Android/data/") || 
                                  path.contains("/Android/obb/") || 
                                  path.contains("/.thumbnails/")
                val hasKnownExtension = allExtensions.contains(extension)
                val isZeroByte = size <= 0

                if (extension.isNotEmpty() && hasKnownExtension && !isHidden && !isSystemPath && !isZeroByte) {
                    files.add(
                        FileItem(
                            name = name,
                            isDirectory = false,
                            lastModified = dateFormat.format(Date(date)),
                            size = formatFileSize(size),
                            fullPath = path
                        )
                    )
                }
            }
        }
        return files
    }

    private fun queryMediaStoreCount(context: Context, extensions: List<String>): Int {
        val uri = MediaStore.Files.getContentUri("external")
        val showHidden = SettingsManager.showHiddenFiles
        
        var selection = extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }
        if (!showHidden) {
            selection = "($selection) AND (${MediaStore.Files.FileColumns.DISPLAY_NAME} NOT LIKE '.*')"
        }
        
        val selectionArgs = extensions.map { "%.$it" }.toTypedArray()
        
        context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Files.FileColumns._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            return cursor.count
        }
        return 0
    }

    private fun queryMediaStoreFiles(context: Context, extensions: List<String>): List<FileItem> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val showHidden = SettingsManager.showHiddenFiles
        
        var selection = extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }
        if (!showHidden) {
            selection = "($selection) AND (${MediaStore.Files.FileColumns.DISPLAY_NAME} NOT LIKE '.*')"
        }
        
        val selectionArgs = extensions.map { "%.$it" }.toTypedArray()
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val files = mutableListOf<FileItem>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val path = cursor.getString(dataIndex)
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex) * 1000 // MediaStore stores seconds

                files.add(
                    FileItem(
                        name = name,
                        isDirectory = false,
                        lastModified = dateFormat.format(Date(date)),
                        size = formatFileSize(size),
                        fullPath = path
                    )
                )
            }
        }
        return files
    }
}
