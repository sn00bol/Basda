package com.sn00bol.basda.ui.utils

import android.content.Context
import android.net.Uri
import com.github.junrar.Archive
import net.sf.sevenzipjbinding.SevenZip
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory

object ArchiveManager {

    fun init(context: Context) {
        try {
            SevenZip.initSevenZipFromPlatformJAR()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listEntries(context: Context, uri: Uri): List<String> {
        val entries = mutableListOf<String>()
        val extension = getExtension(context, uri)

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                when (extension) {
                    "rar" -> {
                        val archive = Archive(inputStream)
                        archive.fileHeaders.forEach {
                            entries.add(it.fileName)
                        }
                    }
                    "7z" -> {
                        entries.add("7z listing requires physical file access")
                    }
                    else -> {
                        val ais: ArchiveInputStream<out ArchiveEntry> = ArchiveStreamFactory().createArchiveInputStream(inputStream)
                        var entry = ais.nextEntry
                        while (entry != null) {
                            entries.add(entry.name)
                            entry = ais.nextEntry
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            entries.add("Error: ${e.message}")
        }
        return entries
    }

    private fun getExtension(context: Context, uri: Uri): String {
        val path = uri.path ?: return ""
        return path.substringAfterLast('.', "").lowercase()
    }
}
