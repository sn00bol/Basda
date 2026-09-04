package com.sn00bol.basda.ui.utils

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sn00bol.basda.R
import com.sn00bol.basda.ui.utils.getIconForExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FileIcon(
    file: FileItem,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 28.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val extension = remember(file.name) { file.name.substringAfterLast('.', "").lowercase() }
    
    val isImage = remember(extension) {
        CATEGORIES.find { it.type == CategoryType.IMAGES }?.extensions?.contains(extension) == true
    }
    val isVideo = remember(extension) {
        CATEGORIES.find { it.type == CategoryType.VIDEOS }?.extensions?.contains(extension) == true
    }
    val isApk = remember(extension) {
        CATEGORIES.find { it.type == CategoryType.APKS }?.extensions?.contains(extension) == true
    }

    if (file.isDirectory) {
        Icon(
            painter = painterResource(
                id = if (file.hasFiles) R.drawable.folderwithfile else R.drawable.folder
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = modifier.size(iconSize)
        )
    } else if (isImage) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(file.fullPath))
                .crossfade(true)
                .build(),
            contentDescription = file.name,
            modifier = modifier
                .size(iconSize)
                .clip(RoundedCornerShape(if (iconSize > 40.dp) 12.dp else 4.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = getIconForExtension(extension)),
            fallback = painterResource(id = getIconForExtension(extension))
        )
    } else if (isVideo) {
        val videoThumbnail by produceState<Bitmap?>(initialValue = DataRepository.getBitmap(file.fullPath), file.fullPath) {
            if (value == null) {
                value = withContext(Dispatchers.IO) {
                    try {
                        val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            ThumbnailUtils.createVideoThumbnail(
                                File(file.fullPath),
                                android.util.Size(512, 512),
                                null
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            ThumbnailUtils.createVideoThumbnail(
                                file.fullPath,
                                MediaStore.Video.Thumbnails.MINI_KIND
                            )
                        }
                        bitmap?.also { DataRepository.putBitmap(file.fullPath, it) }
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }

        Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
            if (videoThumbnail != null) {
                androidx.compose.foundation.Image(
                    bitmap = videoThumbnail!!.asImageBitmap(),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (iconSize > 40.dp) 12.dp else 4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = getIconForExtension(extension)),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.video),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(iconSize / 2)
            )
        }
    } else if (isApk) {
        val apkIcon by produceState<android.graphics.drawable.Drawable?>(initialValue = DataRepository.getDrawable(file.fullPath), file.fullPath) {
            if (value == null) {
                value = withContext(Dispatchers.IO) {
                    try {
                        val pm = context.packageManager
                        val info = pm.getPackageArchiveInfo(file.fullPath, 0)
                        info?.applicationInfo?.let {
                            it.sourceDir = file.fullPath
                            it.publicSourceDir = file.fullPath
                            val drawable = it.loadIcon(pm)
                            drawable.also { DataRepository.putDrawable(file.fullPath, it) }
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }

        if (apkIcon != null) {
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(apkIcon),
                contentDescription = file.name,
                modifier = modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(if (iconSize > 40.dp) 12.dp else 4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(id = getIconForExtension(extension)),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = modifier.size(iconSize)
            )
        }
    } else {
        Icon(
            painter = painterResource(id = getIconForExtension(extension)),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = modifier.size(iconSize)
        )
    }
}

data class FileItem(
    val name: String,
    val isDirectory: Boolean = false,
    val hasFiles: Boolean = false,
    val itemCount: Int = 0,
    val lastModified: String = "",
    val size: String = "",
    val fullPath: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    onCloseClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search files...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
                
                IconButton(onClick = onToggleGridView) {
                    AnimatedContent(
                        targetState = isGridView,
                        transitionSpec = {
                            fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                        },
                        label = "LayoutIcon"
                    ) { isGrid ->
                        Icon(
                            painter = painterResource(id = if (isGrid) R.drawable.list else R.drawable.grid),
                            contentDescription = "Toggle Layout",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Close Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun DocumentList(
    items: List<FileItem>, 
    showDate: Boolean = true,
    showApkName: Boolean = true,
    onItemClick: (FileItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            FileListItem(
                file = item, 
                showDate = showDate, 
                showApkName = showApkName,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun FileListItem(
    file: FileItem,
    showDate: Boolean = true,
    showApkName: Boolean = true,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            val extension = remember(file.name) { file.name.substringAfterLast('.', "").lowercase() }
            val isApk = remember(extension) {
                CATEGORIES.find { it.type == CategoryType.APKS }?.extensions?.contains(extension) == true
            }
            
            val apkName by produceState<String?>(initialValue = DataRepository.getAppName(file.fullPath), file.fullPath) {
                if (isApk && value == null) {
                    value = withContext(Dispatchers.IO) {
                        try {
                            val pm = context.packageManager
                            val info = pm.getPackageArchiveInfo(file.fullPath, 0)
                            info?.applicationInfo?.let {
                                it.sourceDir = file.fullPath
                                it.publicSourceDir = file.fullPath
                                val name = it.loadLabel(pm).toString()
                                name.also { DataRepository.putAppName(file.fullPath, it) }
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }

            val relativeDate = remember(file.lastModified) { formatRelativeDate(file.lastModified) }
            val subtext = if (file.isDirectory) {
                if (showDate) "${file.itemCount} items | $relativeDate" else "${file.itemCount} items"
            } else {
                val sizeStr = if (isApk && showApkName && apkName != null) "$apkName | ${file.size}" else file.size
                if (showDate) "$sizeStr | $relativeDate" else sizeStr
            }
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingContent = {
            FileIcon(file = file, iconSize = 48.dp)
        },
        trailingContent = {
            if (file.isDirectory) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@Composable
fun FileGrid(
    items: List<FileItem>,
    columns: Int,
    modifier: Modifier = Modifier,
    showApkName: Boolean = true,
    onItemClick: (FileItem) -> Unit
) {
    val chunkedItems = items.chunked(columns)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    FileGridItem(
                        item = item,
                        modifier = Modifier.weight(1f),
                        showApkName = showApkName,
                        onClick = { onItemClick(item) }
                    )
                }
                if (rowItems.size < columns) {
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FileGridItem(
    item: FileItem, 
    modifier: Modifier = Modifier, 
    showApkName: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FileIcon(
            file = item,
            iconSize = 52.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (!item.isDirectory && item.size.isNotEmpty()) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val extension = remember(item.name) { item.name.substringAfterLast('.', "").lowercase() }
            val isApk = remember(extension) {
                CATEGORIES.find { it.type == CategoryType.APKS }?.extensions?.contains(extension) == true
            }
            
            val apkName by produceState<String?>(initialValue = DataRepository.getAppName(item.fullPath), item.fullPath) {
                if (isApk && value == null) {
                    value = withContext(Dispatchers.IO) {
                        try {
                            val pm = context.packageManager
                            val info = pm.getPackageArchiveInfo(item.fullPath, 0)
                            info?.applicationInfo?.let {
                                it.sourceDir = item.fullPath
                                it.publicSourceDir = item.fullPath
                                val name = it.loadLabel(pm).toString()
                                name.also { DataRepository.putAppName(item.fullPath, it) }
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }

            Text(
                text = if (isApk && showApkName && apkName != null) "$apkName | ${item.size}" else item.size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
