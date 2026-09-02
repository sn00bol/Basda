package com.sn00bol.basda.ui.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import java.util.Locale

data class StorageInfo(
    val free: String,
    val total: String,
    val progress: Float
)

object StorageHelper {
    
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown"
    }

    fun getInternalStorageInfo(context: Context): StorageInfo {
        val path = Environment.getDataDirectory()
        return calculateStorage(path)
    }

    fun getExternalStorageInfo(context: Context): StorageInfo? {
        val sdCardPath = getSdCardPath(context)
        return if (sdCardPath != null) {
            calculateStorage(java.io.File(sdCardPath))
        } else {
            null
        }
    }

    fun getSdCardPath(context: Context): String? {
        val dirs = context.getExternalFilesDirs(null)
        for (file in dirs) {
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                val path = file.absolutePath
                val index = path.indexOf("/Android/data")
                if (index != -1) {
                    return path.substring(0, index)
                }
            }
        }
        return null
    }

    private fun calculateStorage(path: java.io.File): StorageInfo {
        return try {
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalSpace = totalBlocks * blockSize
            val availableSpace = availableBlocks * blockSize

            val totalGB = totalSpace.toDouble() / (1024 * 1024 * 1024)
            val freeGB = availableSpace.toDouble() / (1024 * 1024 * 1024)
            val progress = if (totalSpace > 0) (totalSpace - availableSpace).toFloat() / totalSpace else 0f

            StorageInfo(
                free = String.format(Locale.US, "%.2f GB", freeGB),
                total = String.format(Locale.US, "%.2f GB", totalGB),
                progress = progress
            )
        } catch (e: Exception) {
            StorageInfo("0.00 GB", "0.00 GB", 0f)
        }
    }
}
