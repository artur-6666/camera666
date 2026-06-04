package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.util.PdfGenerator
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfBuilderScreen(projectId: Long, viewModel: MainViewModel, navController: NavController, context: Context) {
    LaunchedEffect(projectId) {
        viewModel.setCurrentProject(projectId)
    }

    val project by viewModel.currentProject.collectAsStateWithLifecycle()
    val items by viewModel.currentProjectItems.collectAsStateWithLifecycle()
    val photos by viewModel.getProjectPhotos(projectId).collectAsStateWithLifecycle(initialValue = emptyList())
    
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate PDF Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (project == null) {
                CircularProgressIndicator()
            } else {
                Text("Project: ${project!!.name}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Items to include: ${items.size}", style = MaterialTheme.typography.bodyLarge)
                Text("Total photos: ${photos.size}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(32.dp))

                if (isGenerating) {
                    CircularProgressIndicator()
                    Text("Generating PDF...", modifier = Modifier.padding(top = 8.dp))
                } else if (generatedPdfFile != null) {
                    Text("PDF Generated Successfully!", color = MaterialTheme.colorScheme.primary)
                    Text("Saved to: Documents/BeforeAfterReports/", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", generatedPdfFile!!)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            errorMessage = "No app found to open PDF."
                        }
                    }) {
                        Text("Open PDF")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", generatedPdfFile!!)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share PDF")
                    }
                } else {
                    Button(
                        onClick = {
                            isGenerating = true
                            scope.launch {
                                generatedPdfFile = PdfGenerator(context).generatePdf(project!!, items.map { it.item }, photos)
                                isGenerating = false
                                if (generatedPdfFile == null) {
                                    errorMessage = "Failed to generate PDF."
                                }
                            }
                        }
                    ) {
                        Text("Generate PDF")
                    }
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
