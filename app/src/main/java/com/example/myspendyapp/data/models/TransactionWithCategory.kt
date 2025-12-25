package com.example.myspendyapp.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myspendyapp.data.db.entity.Category
import com.example.myspendyapp.data.db.entity.Transaction
data class TransactionWithCategory(
    @Embedded
    val transaction: Transaction,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)
