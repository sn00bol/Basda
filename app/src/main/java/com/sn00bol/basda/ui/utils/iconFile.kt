package com.sn00bol.basda.ui.utils

import com.sn00bol.basda.R

fun getIconForExtension(extension: String?): Int {
    return when (extension?.lowercase()?.trimStart('.')) {
        "7z" -> R.drawable.z7
        "apk" -> R.drawable.apk
        "avi" -> R.drawable.avi
        "gif" -> R.drawable.gif
        "jpg", "jpeg" -> R.drawable.jpg
        "mov" -> R.drawable.mov
        "mp3" -> R.drawable.mp3
        "mp4" -> R.drawable.mp4
        "png" -> R.drawable.png
        "rar" -> R.drawable.rar
        "tar" -> R.drawable.tar
        "wav" -> R.drawable.wav
        "zip" -> R.drawable.zip
        "webp" -> R.drawable.webp
        "sh", "py", "js", "bat", "kt", "java", "c", "cpp", "html", "css", "ts", "json", "xml" -> R.drawable.script
        "folder" -> R.drawable.folder
        "folder_with_file" -> R.drawable.folderwithfile
        else -> R.drawable.notsupported
    }
}
