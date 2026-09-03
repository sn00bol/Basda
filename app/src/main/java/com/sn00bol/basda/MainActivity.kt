package com.sn00bol.basda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.sn00bol.basda.ui.screens.CategoryScreen
import com.sn00bol.basda.ui.screens.FileViewScreen
import com.sn00bol.basda.ui.screens.MainMenu
import com.sn00bol.basda.ui.screens.SettingsScreen
import com.sn00bol.basda.ui.theme.BasdaTheme
import com.sn00bol.basda.ui.utils.ArchiveManager
import com.sn00bol.basda.ui.utils.CategoryType
import com.sn00bol.basda.ui.utils.DataRepository
import com.sn00bol.basda.ui.utils.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ArchiveManager.init(this)
        SettingsManager.init(this)
        DataRepository.init(this)
        
        enableEdgeToEdge()
        setContent {
            BasdaTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    
    fun checkPermission(): Boolean {
        val storageManagerGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val runtimePermissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return storageManagerGranted && runtimePermissionsGranted
    }

    var hasPermissions by remember { mutableStateOf(checkPermission()) }
    var showPermissionDialog by remember { mutableStateOf(!hasPermissions) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = checkPermission()
                hasPermissions = granted
                showPermissionDialog = !granted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            DataRepository.refreshRecent(context)
        }
    }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val granted = checkPermission()
        hasPermissions = granted
        if (granted) showPermissionDialog = false
    }

    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val granted = checkPermission()
        hasPermissions = granted
        if (granted) {
            showPermissionDialog = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = "package:${context.packageName}".toUri()
            }
            manageStorageLauncher.launch(intent)
        }
    }

    if (showPermissionDialog) {
        Dialog(
            onDismissRequest = { /* Don't allow dismiss */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SdStorage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Basda needs access to your files, media, and notifications to provide a complete experience.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val permissionsToRequest = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            
                            if (permissionsToRequest.isNotEmpty()) {
                                requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                manageStorageLauncher.launch(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }
    }

    var currentScreen by remember { mutableStateOf("main_menu") }
    var selectedPath by remember { mutableStateOf("/storage/emulated/0/") }
    var selectedCategory by remember { mutableStateOf<CategoryType?>(null) }
    var selectedCategoryTitle by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == "settings") {
                    (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut())
                } else if (initialState == "settings") {
                    (slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut())
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "ScreenNavigation"
        ) { screen ->
            when (screen) {
                "main_menu" -> MainMenu(
                    hasPermissions = hasPermissions,
                    onNavigateToFileView = { path -> 
                        selectedPath = path
                        currentScreen = "file_view" 
                    },
                    onNavigateToCategory = { category, title ->
                        selectedCategory = category
                        selectedCategoryTitle = title
                        currentScreen = "category_view"
                    },
                    onSearchClick = { /* Handle Search */ },
                    onSettingsClick = { currentScreen = "settings" }
                )
                "file_view" -> FileViewScreen(
                    hasPermissions = hasPermissions,
                    initialPath = selectedPath,
                    onBack = { currentScreen = "main_menu" },
                    onSettingsClick = { currentScreen = "settings" }
                )
                "category_view" -> {
                    selectedCategory?.let { category ->
                        CategoryScreen(
                            categoryType = category,
                            title = selectedCategoryTitle,
                            hasPermissions = hasPermissions,
                            onBack = { currentScreen = "main_menu" }
                        )
                    }
                }
                "settings" -> SettingsScreen(
                    onBack = {
                        currentScreen = "main_menu" 
                    }
                )
            }
        }
    }
}
