package com.sn00bol.basda.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sn00bol.basda.R
import com.sn00bol.basda.ui.utils.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    hasPermissions: Boolean = true,
    initialPath: String = "/storage/emulated/0"
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var entryVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entryVisible = true
    }

    val normalizedInitialPath = remember(initialPath) {
        if (initialPath.endsWith("/") && initialPath.length > 1) initialPath.dropLast(1) else initialPath
    }
    
    var currentPath by remember(normalizedInitialPath) { mutableStateOf(normalizedInitialPath) }

    var isLocalGridView by remember(currentPath) { 
        mutableStateOf(SettingsManager.getViewMode("file_view_$currentPath")) 
    }
    
    val isGridEnabled = SettingsManager.useGlobalGrid || isLocalGridView

    BackHandler {
        val file = java.io.File(currentPath)
        val parent = file.parent

        if (currentPath == normalizedInitialPath || currentPath == "/storage/emulated/0") {
            onBack()
        } else if (parent != null && parent.startsWith("/storage")) {
            currentPath = parent
        } else {
            onBack()
        }
    }

    val files = remember(currentPath, searchQuery, SettingsManager.showHiddenFiles, hasPermissions) {
        if (!hasPermissions || currentPath.isEmpty()) return@remember emptyList()
        
        try {
            val root = java.io.File(currentPath)
            if (root.exists() && root.isDirectory) {
                val dateFormat = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
                val showHidden = SettingsManager.showHiddenFiles
                
                root.listFiles()?.filter { 
                    if (!showHidden) !it.name.startsWith(".") else true 
                }?.map {
                    val itemCount = if (it.isDirectory) it.list()?.size ?: 0 else 0
                    val lastModified = dateFormat.format(Date(it.lastModified()))
                    
                    FileItem(
                        name = it.name,
                        isDirectory = it.isDirectory,
                        hasFiles = if (it.isDirectory) itemCount > 0 else false,
                        itemCount = itemCount,
                        lastModified = lastModified,
                        size = if (!it.isDirectory) formatFileSize(it.length()) else "",
                        fullPath = it.absolutePath
                    )
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isGridView = isGridEnabled,
                    onToggleGridView = {
                        isLocalGridView = !isLocalGridView
                        SettingsManager.setViewMode("file_view_$currentPath", isLocalGridView)
                    },
                    onCloseClick = { 
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentPath) {
                                "/storage/emulated/0" -> "Internal storage"
                                normalizedInitialPath -> {
                                    if (currentPath.contains("emulated")) "Internal storage" else "SD Card"
                                }
                                else -> java.io.File(currentPath).name
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            val file = java.io.File(currentPath)
                            val parent = file.parent

                            if (currentPath == normalizedInitialPath || currentPath == "/storage/emulated/0") {
                                onBack()
                            } else if (parent != null && parent.startsWith("/storage")) {
                                currentPath = parent
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            isLocalGridView = !isLocalGridView
                            SettingsManager.setViewMode("file_view_$currentPath", isLocalGridView)
                        }) {
                            AnimatedContent(
                                targetState = isGridEnabled,
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
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                offset = androidx.compose.ui.unit.DpOffset(x = (0).dp, y = (-50).dp),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Trash bin") },
                                    onClick = { 
                                        showMenu = false
                                        // Handle Trash bin
                                    },
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Outlined.Delete, 
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        ) 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { 
                                        showMenu = false
                                        onSettingsClick()
                                    },
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Outlined.Settings, 
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        ) 
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current path: Breadcrumb clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.homefolder),
                    contentDescription = "Back to Menu",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBack() },
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                val rootName = remember(normalizedInitialPath) {
                    if (normalizedInitialPath.contains("emulated")) "Internal storage" else "SD Card"
                }

                val relativePath = currentPath.removePrefix(normalizedInitialPath).trim('/')
                val isRootCurrent = relativePath.isEmpty()

                BreadcrumbItem(
                    text = rootName,
                    isCurrent = isRootCurrent,
                    onClick = { currentPath = normalizedInitialPath }
                )

                if (!isRootCurrent) {
                    val folders = relativePath.split('/')
                    var accumulatedPath = normalizedInitialPath
                    
                    folders.forEachIndexed { index, folder ->
                        accumulatedPath += "/$folder"
                        val targetPath = accumulatedPath
                        val isLast = index == folders.size - 1
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        
                        BreadcrumbItem(
                            text = folder,
                            isCurrent = isLast,
                            onClick = { currentPath = targetPath }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = entryVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                        initialOffsetY = { it / 40 },
                        animationSpec = tween(500)
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = currentPath to files,
                            transitionSpec = {
                                val (targetPath, targetFiles) = targetState
                                val (initialPath, _) = initialState
                                
                                if (targetFiles.isEmpty() && searchQuery.isEmpty()) {
                                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                                } else {
                                    val isForward = targetPath.length > initialPath.length
                                    if (isForward) {
                                        (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(350)) + fadeIn())
                                            .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(350)) + fadeOut())
                                    } else {
                                        (slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(350)) + fadeIn())
                                            .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(350)) + fadeOut())
                                    }
                                }
                            },
                            label = "FolderNavigation"
                        ) { (_, currentFiles) ->
                            if (currentFiles.isEmpty() && searchQuery.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Empty folder",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                if (isGridEnabled) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp)
                                    ) {
                                        val columns = SettingsManager.gridColumns
                                        val chunkedFiles = currentFiles.chunked(columns)
                                        items(items = chunkedFiles) { rowFiles ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowFiles.forEach { fileItem ->
                                                    FileGridItem(
                                                        item = fileItem,
                                                        modifier = Modifier.weight(1f),
                                                        onClick = {
                                                            if (fileItem.isDirectory) {
                                                                currentPath = if (currentPath.endsWith("/")) {
                                                                    currentPath + fileItem.name
                                                                } else {
                                                                    currentPath + "/" + fileItem.name
                                                                }
                                                            } else {
                                                                FileOpener.openFile(context, fileItem.fullPath)
                                                            }
                                                        }
                                                    )
                                                }
                                                if (rowFiles.size < columns) {
                                                    repeat(columns - rowFiles.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(currentFiles) { file ->
                                            FileListItem(file = file, onClick = {
                                                if (file.isDirectory) {
                                                    currentPath = if (currentPath.endsWith("/")) {
                                                        currentPath + file.name
                                                    } else {
                                                        currentPath + "/" + file.name
                                                    }
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadcrumbItem(
    text: String,
    isCurrent: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(onClick = onClick)
        )
    }
}
