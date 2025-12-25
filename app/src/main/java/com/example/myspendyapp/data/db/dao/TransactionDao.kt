package com.example.myspendyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myspendyapp.data.db.entity.Transaction
import com.example.myspendyapp.data.models.TransactionWithCategory

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategoryId(categoryId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): LiveData<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsWithCategorySync(): List<TransactionWithCategory>

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM transactions t INNER JOIN categories c ON t.categoryId = c.id WHERE c.type = :typeName")
    fun getTotalByType(typeName: String): LiveData<Double>
}
