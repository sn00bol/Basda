package com.sn00bol.basda.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sn00bol.basda.R
import com.sn00bol.basda.ui.theme.BluePrimary
import com.sn00bol.basda.ui.theme.DarkBlueStorage
import com.sn00bol.basda.ui.theme.LightBlueStorage
import com.sn00bol.basda.ui.utils.CATEGORIES
import com.sn00bol.basda.ui.utils.CategoryDetail
import com.sn00bol.basda.ui.utils.CategoryType
import com.sn00bol.basda.ui.utils.DataRepository
import com.sn00bol.basda.ui.utils.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainMenu(
    hasPermissions: Boolean = true,
    onNavigateToFileView: (path: String) -> Unit,
    onNavigateToCategory: (CategoryType, String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    val internalStorage = remember(hasPermissions) {
        com.sn00bol.basda.ui.utils.StorageHelper.getInternalStorageInfo(context)
    }
    val externalStorage = remember(hasPermissions) {
        com.sn00bol.basda.ui.utils.StorageHelper.getExternalStorageInfo(context)
    }
    val sdCardPath = remember(hasPermissions) {
        com.sn00bol.basda.ui.utils.StorageHelper.getSdCardPath(context)
    }

    LaunchedEffect(hasPermissions, SettingsManager.showHiddenFiles) {
        if (hasPermissions) {
            DataRepository.refreshRecent(context)
        }
    }

    val recentFilesFromDb by DataRepository.getRecentFilesFromDb()?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val recentFiles = recentFilesFromDb

    var isRecentGridView by remember {
        mutableStateOf(SettingsManager.getViewMode("recent"))
    }

    val isGridEnabled = SettingsManager.useGlobalGrid || isRecentGridView

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (pagerState.currentPage == 1) {
                        IconButton(onClick = {
                            isRecentGridView = !isRecentGridView
                            SettingsManager.setViewMode("recent", isRecentGridView)
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
                                text = { Text("Adjust menu layout") },
                                onClick = {
                                    showMenu = false
                                },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Outlined.Edit, 
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(imageVector = if (pagerState.currentPage == 0) Icons.Default.Folder else Icons.Outlined.Folder, contentDescription = "Files") },
                    label = { Text("Files") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(imageVector = if (pagerState.currentPage == 1) Icons.Default.History else Icons.Outlined.History, contentDescription = "Recent") },
                    label = { Text("Recent") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            if (page == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StorageCard(
                                title = "Internal storage",
                                free = internalStorage.free,
                                total = internalStorage.total,
                                progress = internalStorage.progress,
                                containerColor = DarkBlueStorage,
                                modifier = Modifier.weight(if (externalStorage != null && sdCardPath != null) 2.2f else 1f),
                                onClick = { onNavigateToFileView("/storage/emulated/0") }
                            )

                            if (externalStorage != null && sdCardPath != null) {
                                StorageCard(
                                    title = "SD Card",
                                    free = externalStorage.free,
                                    total = externalStorage.total,
                                    progress = externalStorage.progress,
                                    containerColor = LightBlueStorage,
                                    modifier = Modifier.weight(1.8f),
                                    showTotal = false,
                                    onClick = { onNavigateToFileView(sdCardPath) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .height(200.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(CATEGORIES) { category ->
                                        CategoryCard(
                                            detail = category,
                                            hasPermissions = hasPermissions,
                                            onClick = {
                                                onNavigateToCategory(category.type, category.title)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                MainMenuActionItem(
                                    icon = Icons.Outlined.Lock,
                                    title = "Secret folder",
                                    onClick = { /* Blank action */ }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                MainMenuActionItem(
                                    icon = Icons.Outlined.Delete,
                                    title = "Recently deleted",
                                    onClick = { /* Blank action */ }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                MainMenuActionItem(
                                    icon = Icons.Outlined.Analytics,
                                    title = "Analysis storage",
                                    onClick = { /* Blank action */ }
                                )
                            }
                        }
                    }
                }
            } else {
                if (recentFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No recent files",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        CategoryFileList(
                            files = recentFiles,
                            categoryType = CategoryType.DOWNLOADS,
                            isGrid = isGridEnabled,
                            containerColor = MaterialTheme.colorScheme.background,
                            showDate = false,
                            showApkName = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CategoryCard(
    detail: CategoryDetail,
    hasPermissions: Boolean = true,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val count = if (hasPermissions) {
        DataRepository.getCategoryCount(context, detail.type)
    } else {
        0
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = detail.iconRes),
                    contentDescription = detail.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun StorageCard(
    title: String,
    free: String,
    total: String,
    progress: Float,
    containerColor: Color,
    modifier: Modifier = Modifier,
    showTotal: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = free,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showTotal) {
                        val displayTotal = remember(total) {
                            total.replace(Regex("""\.\d{2}"""), "")
                        }
                        Text(
                            text = " | $displayTotal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            }
        }
    }
}