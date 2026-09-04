package com.sn00bol.basda.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0.00 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

fun formatRelativeDate(dateString: String): String {
    return try {
        // Handle strings that might have time info (from fileView.kt)
        val cleanDateString = dateString.substringBefore(' ')
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(cleanDateString) ?: return dateString
        
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }

        // Clear time components for date-only comparison
        val nowMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val targetMidnight = (target.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = nowMidnight.timeInMillis - targetMidnight.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        when {
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Yesterday"
            diffDays in 2..30 -> "$diffDays days ago"
            else -> SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        dateString
    }
}

fun isPackageInstalled(context: android.content.Context, packageName: String): Boolean {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        true
    } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
        false
    }
}

fun getPackageNameFromApk(context: android.content.Context, apkPath: String): String? {
    val pm = context.packageManager
    val info = pm.getPackageArchiveInfo(apkPath, 0)
    return info?.packageName
}
