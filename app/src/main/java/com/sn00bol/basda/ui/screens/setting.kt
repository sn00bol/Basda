package com.sn00bol.basda.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sn00bol.basda.BuildConfig
import com.sn00bol.basda.ui.theme.MainMenuBackground
import com.sn00bol.basda.ui.utils.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    
    var isTrashEnabled by remember { mutableStateOf(SettingsManager.isTrashEnabled) }
    var trashDays by remember { mutableIntStateOf(SettingsManager.trashDeleteDays) }
    var showHiddenFiles by remember { mutableStateOf(SettingsManager.showHiddenFiles) }
    var showTrashDaysDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    if (currentSubScreen == "permissions") {
        PermissionsSubScreen(onBack = { currentSubScreen = null })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Settings", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            containerColor = MainMenuBackground
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingsSection(title = "File Management") {
                    SettingsToggleItem(
                        title = "Trash bin",
                        subtitle = "Move deleted files to trash instead of permanent deletion",
                        checked = isTrashEnabled,
                        onCheckedChange = {
                            isTrashEnabled = it
                            SettingsManager.isTrashEnabled = it
                        },
                        icon = Icons.Default.Delete
                    )

                    if (isTrashEnabled) {
                        SettingsClickableItem(
                            title = "Auto-delete trash after",
                            subtitle = if (trashDays == -1) "Never" else "$trashDays days",
                            onClick = { showTrashDaysDialog = true },
                            icon = Icons.Default.Timer
                        )
                    }

                    SettingsToggleItem(
                        title = "Show hidden files",
                        subtitle = "Files starting with a dot (.)",
                        checked = showHiddenFiles,
                        onCheckedChange = {
                            showHiddenFiles = it
                            SettingsManager.showHiddenFiles = it
                        },
                        icon = Icons.Default.Visibility
                    )
                }

                SettingsSection(title = "Information") {
                    SettingsClickableItem(
                        title = "Permissions",
                        subtitle = "Check granted and requested permissions",
                        onClick = { currentSubScreen = "permissions" },
                        icon = Icons.Default.Lock
                    )
                    
                    SettingsClickableItem(
                        title = "Version information",
                        subtitle = BuildConfig.VERSION_NAME,
                        onClick = { },
                        icon = Icons.Default.Info
                    )
                }

                SettingsSection(title = "Help & Support") {
                    SettingsHelpItem(
                        icon = Icons.Default.BugReport,
                        title = "Report issues",
                        subtitle = "Found a bug? report it here",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://github.com/sn00bol/Dades/issues".toUri())
                            context.startActivity(intent)
                        }
                    )

                    SettingsHelpItem(
                        icon = Icons.Default.Email,
                        title = "Email",
                        subtitle = "trancongbinhan2016@gmail.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:trancongbinhan2016@gmail.com".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )

                    SettingsHelpItem(
                        icon = Icons.Default.Forum,
                        title = "Discord",
                        subtitle = "sn00bol",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://discord.com/users/870567726324805673".toUri())
                            context.startActivity(intent)
                        }
                    )

                    SettingsHelpItem(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "Telegram",
                        subtitle = "@Snoobol",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://t.me/sn00bol".toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        if (showTrashDaysDialog) {
            TrashDaysDialog(
                currentDays = trashDays,
                onDismiss = { showTrashDaysDialog = false },
                onSelect = {
                    trashDays = it
                    SettingsManager.trashDeleteDays = it
                    showTrashDaysDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSubScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Permissions", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MainMenuBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(title = "Required permissions") {
                PermissionItem(
                    name = "Image and video",
                    permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE,
                    icon = Icons.Default.Image
                )
                PermissionItem(
                    name = "Music and audio",
                    permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                        Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE,
                    icon = Icons.Default.MusicNote
                )
            }

            SettingsSection(title = "Optional permissions") {
                PermissionItem(
                    name = "Notification",
                    permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                        Manifest.permission.POST_NOTIFICATIONS else null,
                    icon = Icons.Default.Notifications
                )
            }
        }
    }
}

@Composable
fun PermissionItem(name: String, permission: String?, icon: ImageVector) {
    val context = LocalContext.current
    val isGranted = if (permission != null) {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    } else true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                if (isGranted) "Granted" else "Required",
                style = MaterialTheme.typography.bodySmall,
                color = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun SettingsHelpItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            icon != null -> Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            painter != null -> Icon(painter, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun TrashDaysDialog(currentDays: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val options = listOf(
        30 to "30 Days",
        90 to "90 Days",
        120 to "120 Days",
        -1 to "Never"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MainMenuBackground,
        title = { 
            Text(
                "Auto-delete trash after",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                options.forEach { (days, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(days) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentDays == days, 
                            onClick = { onSelect(days) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label, 
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { 
                Text("Cancel") 
            }
        }
    )
}
