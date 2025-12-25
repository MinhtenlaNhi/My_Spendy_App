package com.example.myspendyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // Loại danh mục tự do (ví dụ: "Khoản chi", "Khoản thu", "Ăn uống", ...)
    val iconName: String = "",
    val color: String = "#FF6200EE", // Màu mặc định (purple)
    val count: Int = 0
)
