package com.example.data.repository

import com.example.data.dao.CategoryDao
import com.example.data.dao.ProductDao
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class CategoryProductRepository(
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val mainCategories: Flow<List<CategoryEntity>> = categoryDao.getMainCategories()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getSubCategories(parentId: Int): Flow<List<CategoryEntity>> {
        return categoryDao.getSubCategories(parentId)
    }

    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(categoryId)
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    suspend fun findByBarcode(barcode: String): ProductEntity? {
        return productDao.findByBarcode(barcode)
    }

    suspend fun getCategoryById(id: Int): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(id: Int) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(id: Int) {
        productDao.deleteProductById(id)
    }
}
