package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusLockApplication
import com.example.data.model.BlockListEntity
import com.example.data.model.InstalledApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlockListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApplication
    private val repository = app.repository
    private val appManager = app.appManager

    val allBlockLists: StateFlow<List<BlockListEntity>> = repository.allBlockLists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current block list being edited or created
    private val _editingBlockListId = MutableStateFlow<Long?>(null)
    val editingBlockListId = _editingBlockListId.asStateFlow()

    private val _listName = MutableStateFlow("")
    val listName = _listName.asStateFlow()

    private val _listDescription = MutableStateFlow("")
    val listDescription = _listDescription.asStateFlow()

    private val _listIcon = MutableStateFlow("Shield")
    val listIcon = _listIcon.asStateFlow()

    private val _selectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val selectedPackages = _selectedPackages.asStateFlow()

    // Apps catalog
    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showSystemApps = MutableStateFlow(false)
    val showSystemApps = _showSystemApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps = _isLoadingApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = appManager.getInstalledApps(_selectedPackages.value)
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    val filteredApps = combine(_installedApps, _searchQuery, _showSystemApps, _selectedPackages) { apps, query, showSys, selected ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() || app.appName.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true)
            val matchesSys = showSys || !app.isSystemApp
            matchesQuery && matchesSys
        }.map { it.copy(isSelected = selected.contains(it.packageName)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSystemApps(show: Boolean) {
        _showSystemApps.value = show
    }

    fun toggleAppSelection(packageName: String) {
        val current = _selectedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _selectedPackages.value = current
    }

    fun selectAllFiltered() {
        val current = _selectedPackages.value.toMutableSet()
        filteredApps.value.forEach { current.add(it.packageName) }
        _selectedPackages.value = current
    }

    fun deselectAll() {
        _selectedPackages.value = emptySet()
    }

    fun startCreateNew() {
        _editingBlockListId.value = null
        _listName.value = "Custom Block List"
        _listDescription.value = "Selected applications will be locked during focus"
        _listIcon.value = "Shield"
        _selectedPackages.value = emptySet()
    }

    fun startEdit(blockList: BlockListEntity) {
        _editingBlockListId.value = blockList.id
        _listName.value = blockList.name
        _listDescription.value = blockList.description
        _listIcon.value = blockList.icon
        _selectedPackages.value = blockList.getPackageList().toSet()
    }

    fun setListName(name: String) {
        _listName.value = name
    }

    fun setListDescription(desc: String) {
        _listDescription.value = desc
    }

    fun setListIcon(icon: String) {
        _listIcon.value = icon
    }

    fun saveBlockList(onSaved: () -> Unit) {
        viewModelScope.launch {
            val packagesStr = _selectedPackages.value.joinToString(",")
            val id = _editingBlockListId.value
            if (id == null) {
                repository.insertBlockList(
                    BlockListEntity(
                        name = _listName.value.ifBlank { "Focus Block List" },
                        icon = _listIcon.value,
                        description = _listDescription.value,
                        packages = packagesStr,
                        isSystemDefault = false
                    )
                )
            } else {
                repository.updateBlockList(
                    BlockListEntity(
                        id = id,
                        name = _listName.value.ifBlank { "Focus Block List" },
                        icon = _listIcon.value,
                        description = _listDescription.value,
                        packages = packagesStr,
                        isSystemDefault = false
                    )
                )
            }
            onSaved()
        }
    }

    fun deleteBlockList(blockList: BlockListEntity) {
        viewModelScope.launch {
            repository.deleteBlockList(blockList)
        }
    }
}
