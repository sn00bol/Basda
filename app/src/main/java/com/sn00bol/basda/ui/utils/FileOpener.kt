package com.sn00bol.basda.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

object FileOpener {
    fun openFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val extension = file.extension.lowercase()
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            
            if (mimeType == null) {
                mimeType = when (extension) {
                    "apk" -> "application/vnd.android.package-archive"
                    "txt" -> "text/plain"
                    "pdf" -> "application/pdf"
                    else -> "*/*"
                }
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Add this for better compatibility with newer Android versions
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            context.startActivity(Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            // Optional: Show toast error
        }
    }
}
