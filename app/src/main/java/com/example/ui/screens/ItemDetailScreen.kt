package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.data.PhotoEntity
import com.example.data.PhotoType
import com.example.ui.MainViewModel
import java.io.File
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridItemSpan

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ItemDetailScreen(projectId: Long, itemId: Long, viewModel: MainViewModel, navController: NavController) {
    LaunchedEffect(itemId) {
        viewModel.setCurrentItem(itemId)
    }

    val item by viewModel.currentItem.collectAsStateWithLifecycle()
    val photos by viewModel.currentItemPhotos.collectAsStateWithLifecycle()

    val beforePhotos = photos.filter { it.type == PhotoType.BEFORE }
    val afterPhotos = photos.filter { it.type == PhotoType.AFTER }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Item Details", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("camera/$projectId/$itemId") }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (beforePhotos.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("Before Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
                items(beforePhotos, key = { it.id }) { photo ->
                    PhotoThumbnail(photo = photo, onDelete = { viewModel.deletePhoto(photo) })
                }
            }

            if (afterPhotos.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("After Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                }
                items(afterPhotos, key = { it.id }) { photo ->
                    PhotoThumbnail(photo = photo, onDelete = { viewModel.deletePhoto(photo) })
                }
            }

            if (photos.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No photos taken yet. Tap the camera button to add some!")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoThumbnail(photo: PhotoEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val file = File(photo.filePath)
            if (file.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .size(Size.ORIGINAL)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = photo.type.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Image Not Found")
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Photo", tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(4.dp))
                }
            }
            
            if (photo.label.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                ) {
                    Text(photo.label, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
