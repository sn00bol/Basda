package com.sn00bol.basda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.sn00bol.basda.ui.screens.CategoryScreen
import com.sn00bol.basda.ui.screens.FileViewScreen
import com.sn00bol.basda.ui.screens.MainMenu
import com.sn00bol.basda.ui.screens.SettingsScreen
import com.sn00bol.basda.ui.theme.BasdaTheme
import com.sn00bol.basda.ui.utils.ArchiveManager
import com.sn00bol.basda.ui.utils.CategoryType
import com.sn00bol.basda.ui.utils.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ArchiveManager.init(this)
        SettingsManager.init(this)
        
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
    var currentScreen by remember { mutableStateOf("main_menu") }
    var selectedPath by remember { mutableStateOf("/storage/emulated/0/") }
    var selectedCategory by remember { mutableStateOf<CategoryType?>(null) }
    var selectedCategoryTitle by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "main_menu" -> MainMenu(
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
                initialPath = selectedPath,
                onBack = { currentScreen = "main_menu" },
                onSettingsClick = { currentScreen = "settings" }
            )
            "category_view" -> {
                selectedCategory?.let { category ->
                    CategoryScreen(
                        categoryType = category,
                        title = selectedCategoryTitle,
                        onBack = { currentScreen = "main_menu" }
                    )
                }
            }
            "settings" -> SettingsScreen(
                onBack = { currentScreen = "main_menu" }
            )
        }
    }
}
