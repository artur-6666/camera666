package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val projects = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertProject(name: String, description: String = "") = viewModelScope.launch {
        repository.insertProject(ProjectEntity(name = name, description = description))
    }

    fun deleteProject(project: ProjectEntity) = viewModelScope.launch {
        repository.deleteProject(project)
    }

    // Current Project Items
    private val _currentProjectId = MutableStateFlow<Long?>(null)
    
    val currentProject = _currentProjectId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(null)
        else repository.getProjectById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentProjectItems = _currentProjectId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.getItemsForProjectWithCounts(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentProject(id: Long) {
        _currentProjectId.value = id
    }

    fun insertItem(name: String, code: String = "", note: String = "") = viewModelScope.launch {
        _currentProjectId.value?.let { projectId ->
            repository.insertItem(ItemEntity(projectId = projectId, name = name, code = code, note = note))
        }
    }

    fun deleteItem(item: ItemEntity) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    // Current Item Photos
    private val _currentItemId = MutableStateFlow<Long?>(null)

    val currentItem = _currentItemId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(null)
        else repository.getItemById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentItemPhotos = _currentItemId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.getPhotosForItem(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentItem(id: Long) {
        _currentItemId.value = id
    }

    fun insertPhoto(projectId: Long, itemId: Long, filePath: String, type: PhotoType) = viewModelScope.launch {
        repository.insertPhoto(PhotoEntity(projectId = projectId, itemId = itemId, filePath = filePath, type = type))
    }

    fun deletePhoto(photo: PhotoEntity) = viewModelScope.launch {
        repository.deletePhoto(photo)
    }
    
    fun getProjectPhotos(projectId: Long) = repository.getPhotosForProject(projectId)
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
