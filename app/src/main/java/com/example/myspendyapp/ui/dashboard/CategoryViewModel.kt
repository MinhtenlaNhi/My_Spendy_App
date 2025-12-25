package com.example.myspendyapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myspendyapp.data.db.AppDatabase
import com.example.myspendyapp.data.db.dao.CategoryDao
import com.example.myspendyapp.data.db.entity.Category
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao: CategoryDao
    @JvmField
    val allCategories: LiveData<List<Category>>

    init {
        val db = AppDatabase.getDatabase(application)
        categoryDao = db.categoryDao()
        allCategories = categoryDao.getAllCategories()
    }

    fun insert(category: Category) {
        viewModelScope.launch {
            categoryDao.insert(category)
        }
    }

    fun update(category: Category) {
        viewModelScope.launch {
            categoryDao.update(category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun getCategoryById(categoryId: Long, callback: (Category?) -> Unit) {
        viewModelScope.launch {
            val category = categoryDao.getCategoryById(categoryId)
            callback(category)
        }
    }

    fun searchCategoryTypes(query: String, callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            // Tìm kiếm các loại danh mục đã có trong database
            val types = categoryDao.searchCategoryTypes(query)
            callback(types)
        }
    }

    fun searchCategoryNames(query: String, callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            val names = categoryDao.searchCategoryNames(query)
            callback(names)
        }
    }
}
