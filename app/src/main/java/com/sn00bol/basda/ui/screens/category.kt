package com.sn00bol.basda.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sn00bol.basda.R
import com.sn00bol.basda.ui.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    categoryType: CategoryType,
    title: String,
    hasPermissions: Boolean = true,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLocalGridView by remember { 
        mutableStateOf(SettingsManager.getViewMode("category_${categoryType.name}")) 
    }
    
    val isGridEnabled = SettingsManager.useGlobalGrid || isLocalGridView
    
    var isLoading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    val filesState = produceState(initialValue = emptyList<FileItem>(), categoryType, hasPermissions) {
        if (hasPermissions) {
            isLoading = true
            value = withContext(Dispatchers.IO) {
                FileScanner.getFilesForCategory(context, categoryType)
            }
            isLoading = false
        } else {
            value = emptyList()
            isLoading = false
        }
    }

    val subCategories = remember { 
        listOf("All file", "Images", "Videos", "Audio", "Documents", "APKs")
    }
    
    val pagerState = rememberPagerState(pageCount = { subCategories.size })

    Scaffold(
        topBar = {
            Column {
                if (isSearchActive) {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isGridView = isGridEnabled,
                    onToggleGridView = {
                        isLocalGridView = !isLocalGridView
                        SettingsManager.setViewMode("category_${categoryType.name}", isLocalGridView)
                    },
                    onCloseClick = { 
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
                } else {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                text = title, 
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
                                SettingsManager.setViewMode("category_${categoryType.name}", isLocalGridView)
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
                                        text = { Text("Sort by date") },
                                        onClick = { showMenu = false },
                                        leadingIcon = { 
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Sort, 
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            ) 
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        onClick = { showMenu = false },
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
                
                if (categoryType == CategoryType.DOWNLOADS && !isSearchActive) {
                    SecondaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(
                                        selectedTabIndex = pagerState.currentPage,
                                        matchContentSize = true
                                    )
                                    .padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    ) {
                        subCategories.forEachIndexed { index, subTitle ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = subTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filesState.value.isEmpty()) {
                Text(
                    text = if (searchQuery.isEmpty()) "No files found" else "No results found",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else if (categoryType == CategoryType.DOWNLOADS) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { pageIndex ->
                    val subCategory = subCategories[pageIndex]
                    val filteredFiles = remember(filesState.value, searchQuery, subCategory) {
                        val allFiles = filesState.value.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        if (subCategory == "All") {
                            allFiles
                        } else {
                            val extensions = when (subCategory) {
                                "Images" -> CATEGORIES.find { it.type == CategoryType.IMAGES }?.extensions ?: emptyList()
                                "Videos" -> CATEGORIES.find { it.type == CategoryType.VIDEOS }?.extensions ?: emptyList()
                                "Audio" -> CATEGORIES.find { it.type == CategoryType.AUDIO }?.extensions ?: emptyList()
                                "Documents" -> CATEGORIES.find { it.type == CategoryType.DOCUMENTS }?.extensions ?: emptyList()
                                "APK" -> CATEGORIES.find { it.type == CategoryType.APKS }?.extensions ?: emptyList()
                                else -> emptyList()
                            }
                            allFiles.filter { it.name.substringAfterLast('.', "").lowercase() in extensions }
                        }
                    }

                    if (filteredFiles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "No files found",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        CategoryFileList(
                            files = filteredFiles,
                            categoryType = if (subCategory == "Images") CategoryType.IMAGES 
                                           else if (subCategory == "Videos") CategoryType.VIDEOS 
                                           else categoryType,
                            isGrid = isGridEnabled,
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    }
                }
            } else {
                val filteredFiles = remember(filesState.value, searchQuery) {
                    filesState.value.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                
                if (filteredFiles.isEmpty()) {
                    Text(
                        text = "No results found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    CategoryFileList(
                        files = filteredFiles, 
                        categoryType = categoryType,
                        isGrid = isGridEnabled,
                        containerColor = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryFileList(
    files: List<FileItem>,
    categoryType: CategoryType,
    isGrid: Boolean,
    containerColor: Color = Color.Transparent
) {
    val context = LocalContext.current
    val columns = SettingsManager.gridColumns
    val collapsedDates = remember { mutableStateMapOf<String, Boolean>() }
    val groupedFiles = remember(files) {
        files.groupBy { it.lastModified }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
    ) {
        groupedFiles.forEach { (date, items) ->
            val isCollapsed = collapsedDates[date] ?: false
            
            stickyHeader {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            collapsedDates[date] = !isCollapsed 
                        },
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatRelativeDate(date)} | ${items.size} item${if (items.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val rotation by animateFloatAsState(
                            targetValue = if (isCollapsed) 180f else 0f,
                            label = "rotation"
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isCollapsed) "Expand" else "Collapse",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(rotationZ = rotation)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = !isCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    AnimatedContent(
                        targetState = isGrid,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "LayoutContent"
                    ) { targetIsGrid ->
                        if (targetIsGrid) {
                            FileGrid(
                                items = items,
                                columns = columns,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                onItemClick = { 
                                    FileOpener.openFile(context, it.fullPath) 
                                }
                            )
                        } else {
                            DocumentList(
                                items = items,
                                onItemClick = { 
                                    FileOpener.openFile(context, it.fullPath) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
