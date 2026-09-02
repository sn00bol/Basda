package com.sn00bol.basda.ui.utils

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.sn00bol.basda.ui.screens.FileItem
import java.text.SimpleDateFormat
import java.util.*

object FileScanner {

    fun getCategoryCount(context: Context, categoryType: CategoryType): Int {
        val detail = CATEGORIES.find { it.type == categoryType } ?: return 0
        
        return when (categoryType) {
            CategoryType.DOWNLOADS -> {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir.listFiles()?.count { it.isFile } ?: 0
            }
            else -> {
                queryMediaStoreCount(context, detail.extensions)
            }
        }
    }

    fun getFilesForCategory(context: Context, categoryType: CategoryType): List<FileItem> {
        val detail = CATEGORIES.find { it.type == categoryType } ?: return emptyList()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return when (categoryType) {
            CategoryType.DOWNLOADS -> {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadDir.listFiles()?.filter { it.isFile }?.map {
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

    private fun queryMediaStoreCount(context: Context, extensions: List<String>): Int {
        val uri = MediaStore.Files.getContentUri("external")
        val selection = extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }
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
        
        val selection = extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }
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
