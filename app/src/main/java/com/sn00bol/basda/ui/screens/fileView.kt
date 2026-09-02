package com.sn00bol.basda.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sn00bol.basda.R
import com.sn00bol.basda.ui.utils.getIconForExtension
import com.sn00bol.basda.ui.utils.formatFileSize
import com.sn00bol.basda.ui.utils.SettingsManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    initialPath: String = "/storage/emulated/0"
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val normalizedInitialPath = remember(initialPath) {
        if (initialPath.endsWith("/") && initialPath.length > 1) initialPath.dropLast(1) else initialPath
    }
    
    var currentPath by remember(normalizedInitialPath) { mutableStateOf(normalizedInitialPath) }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    var hasStoragePermission by remember { mutableStateOf(checkPermission()) }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasStoragePermission = checkPermission()
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
    }

    val files = remember(currentPath, searchQuery, hasStoragePermission, SettingsManager.showHiddenFiles) {
        if (!hasStoragePermission) return@remember emptyList()
        
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { 
                                    showMenu = false
                                    onSettingsClick()
                                },
                                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(46.dp),
                placeholder = { 
                    Text(
                        "Search files...", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        IconButton(onClick = { /* Handle Voice Search */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Voice Search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                )
            )

            // Current path: Breadcrumb clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                if (!hasStoragePermission) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "No Permission",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Not gain storage permission",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You haven't gain storage permission so you cannot view anything here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                    manageStorageLauncher.launch(intent)
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Gain permission now")
                        }
                    }
                } else if (files.isEmpty() && searchQuery.isEmpty()) {
                    Text(
                        text = "Empty folder",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(files) { file ->
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

data class FileItem(
    val name: String,
    val isDirectory: Boolean = false,
    val hasFiles: Boolean = false,
    val itemCount: Int = 0,
    val lastModified: String = "",
    val size: String = "",
    val fullPath: String = ""
)

@Composable
fun FileListItem(
    file: FileItem,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
        },
        supportingContent = {
            val subtext = if (file.isDirectory) {
                "${file.itemCount} item${if (file.itemCount > 1) "s" else ""} | ${file.lastModified}"
            } else {
                "${file.size} | ${file.lastModified}"
            }
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        leadingContent = {
            if (file.isDirectory) {
                Icon(
                    painter = painterResource(
                        id = if (file.hasFiles) R.drawable.folderwithfile else R.drawable.folder
                    ),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                val extension = file.name.substringAfterLast('.', "")
                Icon(
                    painter = painterResource(id = getIconForExtension(extension)),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        trailingContent = {
            if (file.isDirectory) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}
