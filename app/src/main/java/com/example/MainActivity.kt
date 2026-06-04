package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.PdfBuilderScreen
import com.example.ui.screens.ProjectDetailScreen
import com.example.ui.screens.ProjectListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(applicationContext)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    
    val database = AppDatabase.getDatabase(context)
    val repository = AppRepository(database.projectDao(), database.itemDao(), database.photoDao())
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(repository))

    NavHost(navController = navController, startDestination = "project_list") {
        composable("project_list") {
            ProjectListScreen(viewModel, navController)
        }
        composable("project_detail/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            if (projectId != null) {
                ProjectDetailScreen(projectId, viewModel, navController)
            }
        }
        composable("item_detail/{projectId}/{itemId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            val itemId = backStackEntry.arguments?.getString("itemId")?.toLongOrNull()
            if (projectId != null && itemId != null) {
                ItemDetailScreen(projectId, itemId, viewModel, navController)
            }
        }
        composable("camera/{projectId}/{itemId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            val itemId = backStackEntry.arguments?.getString("itemId")?.toLongOrNull()
            if (projectId != null && itemId != null) {
                CameraScreen(projectId, itemId, viewModel, navController, context)
            }
        }
        composable("pdf_builder/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            if (projectId != null) {
                PdfBuilderScreen(projectId, viewModel, navController, context)
            }
        }
    }
}
