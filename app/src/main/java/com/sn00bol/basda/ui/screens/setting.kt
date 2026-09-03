package com.sn00bol.basda.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sn00bol.basda.BuildConfig
import com.sn00bol.basda.ui.utils.AppTheme
import com.sn00bol.basda.ui.utils.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    
    var showTrashDaysDialog by remember { mutableStateOf(false) }
    var showGridColumnsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    BackHandler {
        when (currentSubScreen) {
            "licenses" -> currentSubScreen = "app_info"
            "app_info" -> currentSubScreen = null
            else -> onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (targetState == "app_info" || targetState == "licenses" || (initialState == null && targetState != null)) {
                    (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut())
                } else {
                    (slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut())
                }
            },
            label = "SettingsNavigation"
        ) { subScreen ->
            when (subScreen) {
                "app_info" -> AppInfoSubScreen(
                    onBack = { currentSubScreen = null },
                    onNavigateToLicenses = { currentSubScreen = "licenses" }
                )
                "licenses" -> LicenseSubScreen(
                    onBack = { currentSubScreen = "app_info" }
                )
                else -> {
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
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = Color.Unspecified,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                                    actionIconContentColor = Color.Unspecified
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsSection {
                                SettingsClickableItem(
                                    title = "App theme",
                                    subtitle = when(SettingsManager.appTheme) {
                                        AppTheme.SYSTEM -> "Follow system"
                                        AppTheme.LIGHT -> "Light"
                                        AppTheme.DARK -> "Dark"
                                    },
                                    onClick = { showThemeDialog = true },
                                    icon = Icons.Default.Palette
                                )
                            }

                            SettingsSection {
                                SettingsToggleItem(
                                    title = "Trash bin",
                                    subtitle = "Move deleted files to trash instead of permanent deletion",
                                    checked = SettingsManager.isTrashEnabled,
                                    onCheckedChange = {
                                        SettingsManager.isTrashEnabled = it
                                    },
                                    icon = Icons.Default.Delete
                                )

                                if (SettingsManager.isTrashEnabled) {
                                    SettingsClickableItem(
                                        title = "Auto-delete trash after",
                                        subtitle = if (SettingsManager.trashDeleteDays == -1) "Never" else "${SettingsManager.trashDeleteDays} days",
                                        onClick = { showTrashDaysDialog = true },
                                        icon = Icons.Default.Timer
                                    )
                                }

                                SettingsToggleItem(
                                    title = "Show hidden files",
                                    subtitle = "Files starting with a dot (.)",
                                    checked = SettingsManager.showHiddenFiles,
                                    onCheckedChange = {
                                        SettingsManager.showHiddenFiles = it
                                    },
                                    icon = Icons.Default.Visibility
                                )

                                SettingsToggleItem(
                                    title = "Global grid view",
                                    subtitle = "Apply grid layout to all views",
                                    checked = SettingsManager.useGlobalGrid,
                                    onCheckedChange = {
                                        SettingsManager.useGlobalGrid = it
                                    },
                                    icon = Icons.Default.GridView
                                )

                                SettingsClickableItem(
                                    title = "Grid columns",
                                    subtitle = "${SettingsManager.gridColumns}x${SettingsManager.gridColumns}",
                                    onClick = { showGridColumnsDialog = true },
                                    icon = Icons.Default.ViewColumn
                                )
                            }

                            SettingsSection {
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
                                    painter = androidx.compose.ui.res.painterResource(id = com.sn00bol.basda.R.drawable.discord),
                                    title = "Discord",
                                    subtitle = "sn00bol",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW,
                                            "https://discord.com/users/870567726324805673".toUri())
                                        context.startActivity(intent)
                                    }
                                )

                                SettingsHelpItem(
                                    painter = androidx.compose.ui.res.painterResource(id = com.sn00bol.basda.R.drawable.telegram),
                                    title = "Telegram",
                                    subtitle = "@Snoobol",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, "https://t.me/sn00bol".toUri())
                                        context.startActivity(intent)
                                    }
                                )


                                SettingsClickableItem(
                                    title = "App information",
                                    subtitle = "Details about the app and licenses",
                                    onClick = { currentSubScreen = "app_info" },
                                    icon = Icons.Default.Info
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showTrashDaysDialog) {
            TrashDaysDialog(
                currentDays = SettingsManager.trashDeleteDays,
                onDismiss = { showTrashDaysDialog = false },
                onSelect = {
                    SettingsManager.trashDeleteDays = it
                    showTrashDaysDialog = false
                }
            )
        }

        if (showGridColumnsDialog) {
            GridColumnsDialog(
                currentColumns = SettingsManager.gridColumns,
                onDismiss = { showGridColumnsDialog = false },
                onSelect = {
                    SettingsManager.gridColumns = it
                    showGridColumnsDialog = false
                }
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                currentTheme = SettingsManager.appTheme,
                onDismiss = { showThemeDialog = false },
                onSelect = {
                    SettingsManager.appTheme = it
                    showThemeDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoSubScreen(
    onBack: () -> Unit,
    onNavigateToLicenses: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "System Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Basda",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "v${BuildConfig.VERSION_NAME} ALPHA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToLicenses,
                modifier = Modifier
                    .height(48.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                Text("Open source licenses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseSubScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var licenseText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        licenseText = withContext(Dispatchers.IO) {
            try {
                context.assets.open("LICENSE").bufferedReader().use { it.readText() }
            } catch (_: Exception) {
                "License file not found."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Licenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = licenseText,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
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
        Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
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
        Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
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
            icon != null -> Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
            painter != null -> Icon(painter, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun ThemeDialog(currentTheme: AppTheme, onDismiss: () -> Unit, onSelect: (AppTheme) -> Unit) {
    val options = listOf(
        AppTheme.SYSTEM to "Follow system",
        AppTheme.LIGHT to "Light",
        AppTheme.DARK to "Dark"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "App theme",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                options.forEach { (theme, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onSelect(theme) },
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GridColumnsDialog(currentColumns: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val options = listOf(
        3 to "3x3",
        4 to "4x4"
    )
    var customValue by remember { mutableStateOf(if (currentColumns !in listOf(3, 4)) currentColumns.toString() else "") }
    var isCustomSelected by remember { mutableStateOf(currentColumns !in listOf(3, 4)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Grid columns",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                options.forEach { (cols, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                isCustomSelected = false
                                onSelect(cols) 
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isCustomSelected && currentColumns == cols,
                            onClick = { 
                                isCustomSelected = false
                                onSelect(cols) 
                            },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustomSelected = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isCustomSelected,
                        onClick = { isCustomSelected = true },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (isCustomSelected) {
                    OutlinedTextField(
                        value = customValue,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                customValue = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = { Text("Enter columns") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isCustomSelected) {
                        val value = customValue.toIntOrNull()
                        if (value != null && value > 0) {
                            onSelect(value)
                        }
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cancel")
            }
        }
    )
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
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                "Auto-delete trash",
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
