package com.sn00bol.basda.ui.utils

import com.sn00bol.basda.R

enum class CategoryType {
    IMAGES, VIDEOS, AUDIO, DOCUMENTS, DOWNLOADS, APKS
}

data class CategoryDetail(
    val type: CategoryType,
    val title: String,
    val iconRes: Int,
    val extensions: List<String>
)

val CATEGORIES = listOf(
    CategoryDetail(
        CategoryType.IMAGES,
        "Image",
        R.drawable.img,
        listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    ),
    CategoryDetail(
        CategoryType.VIDEOS,
        "Video",
        R.drawable.video,
        listOf("mp4", "mov", "avi", "mkv", "flv", "webm", "3gp")
    ),
    CategoryDetail(
        CategoryType.AUDIO,
        "Audio",
        R.drawable.audio,
        listOf("mp3", "wav", "flac", "ogg", "m4a", "aac")
    ),
    CategoryDetail(
        CategoryType.DOCUMENTS,
        "Documents",
        R.drawable.docs,
        listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "epub")
    ),
    CategoryDetail(
        CategoryType.DOWNLOADS,
        "Download",
        R.drawable.download,
        emptyList() // Handle specialized in FileScanner
    ),
    CategoryDetail(
        CategoryType.APKS,
        "APKs",
        R.drawable.apk2,
        listOf("apk", "xapk", "apks")
    )
)
