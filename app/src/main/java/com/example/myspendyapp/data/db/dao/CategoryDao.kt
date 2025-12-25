package com.example.myspendyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myspendyapp.data.db.entity.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT DISTINCT type FROM categories WHERE type LIKE :query || '%' ORDER BY type ASC LIMIT 10")
    suspend fun searchCategoryTypes(query: String): List<String>

    @Query("SELECT DISTINCT name FROM categories WHERE name LIKE :query || '%' ORDER BY name ASC LIMIT 10")
    suspend fun searchCategoryNames(query: String): List<String>
}
