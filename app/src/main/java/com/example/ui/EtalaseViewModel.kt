package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.repository.CategoryProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EtalaseUiState(
    val navigationStack: List<CategoryEntity> = emptyList(),
    val subCategories: List<CategoryEntity> = emptyList(),
    val productsInCurrentCategory: List<ProductEntity> = emptyList(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false
)

class EtalaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryProductRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CategoryProductRepository(database.categoryDao(), database.productDao())
    }

    private val _navigationStack = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val navigationStack: StateFlow<List<CategoryEntity>> = _navigationStack.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Current category is the last category in the navigation stack (or null if at root)
    val currentCategory: StateFlow<CategoryEntity?> = _navigationStack
        .map { stack -> stack.lastOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All categories flow for dialog dropdown choices
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All products in the store flow
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sub-categories flow depending on currentCategory
    @OptIn(ExperimentalCoroutinesApi::class)
    val subCategories: StateFlow<List<CategoryEntity>> = currentCategory.flatMapLatest { cat ->
        if (cat == null) {
            repository.mainCategories
        } else {
            repository.getSubCategories(cat.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products in current category flow (including sub-category products)
    @OptIn(ExperimentalCoroutinesApi::class)
    val productsInCurrentCategory: StateFlow<List<ProductEntity>> = combine(
        currentCategory,
        allCategories,
        allProducts
    ) { cat, categories, products ->
        if (cat == null) {
            // At root "Utama": show all products in store
            products
        } else {
            // Find all descendant category IDs for the current category
            val descendantIds = mutableSetOf(cat.id)
            val queue = ArrayDeque<Int>()
            queue.add(cat.id)
            while (queue.isNotEmpty()) {
                val parentId = queue.removeFirst()
                val children = categories.filter { it.parentId == parentId }.map { it.id }
                for (childId in children) {
                    if (descendantIds.add(childId)) {
                        queue.add(childId)
                    }
                }
            }
            products.filter { descendantIds.contains(it.categoryId) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<ProductEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchProducts(query.trim())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToCategory(category: CategoryEntity) {
        _navigationStack.value = _navigationStack.value + category
    }

    fun navigateBack(): Boolean {
        val currentStack = _navigationStack.value
        if (currentStack.isNotEmpty()) {
            _navigationStack.value = currentStack.dropLast(1)
            return true
        }
        return false
    }

    fun navigateToBreadcrumbIndex(index: Int) {
        if (index < 0) {
            _navigationStack.value = emptyList() // Root "Utama"
        } else {
            val currentStack = _navigationStack.value
            if (index < currentStack.size) {
                _navigationStack.value = currentStack.take(index + 1)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCategory(name: String, parentId: Int?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    parentId = parentId
                )
            )
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun addProduct(name: String, brand: String, price: Double, stock: Int, categoryId: Int) {
        if (name.isBlank() || price <= 0.0) return
        viewModelScope.launch {
            repository.insertProduct(
                ProductEntity(
                    name = name.trim(),
                    brand = brand.trim().ifEmpty { "Umum" },
                    price = price,
                    stock = stock,
                    categoryId = categoryId
                )
            )
        }
    }

    fun updateProduct(id: Int, name: String, brand: String, price: Double, stock: Int, categoryId: Int) {
        if (name.isBlank() || price <= 0.0) return
        viewModelScope.launch {
            repository.updateProduct(
                ProductEntity(
                    id = id,
                    name = name.trim(),
                    brand = brand.trim().ifEmpty { "Umum" },
                    price = price,
                    stock = stock,
                    categoryId = categoryId
                )
            )
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }
}
