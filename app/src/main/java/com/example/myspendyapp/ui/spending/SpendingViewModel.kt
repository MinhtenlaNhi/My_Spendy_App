package com.example.myspendyapp.ui.spending

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myspendyapp.data.db.AppDatabase
import com.example.myspendyapp.data.db.dao.TransactionDao
import com.example.myspendyapp.data.db.entity.Transaction
import com.example.myspendyapp.data.models.TransactionWithCategory
import kotlinx.coroutines.launch

class SpendingViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: TransactionDao
    
    private val _transactions = MediatorLiveData<List<TransactionWithCategory>>()
    @JvmField
    val transactions: LiveData<List<TransactionWithCategory>> = _transactions
    
    private val _totalIncome = MutableLiveData<Double>(0.0)
    @JvmField
    val totalIncome: LiveData<Double> = _totalIncome
    
    private val _totalExpense = MutableLiveData<Double>(0.0)
    @JvmField
    val totalExpense: LiveData<Double> = _totalExpense

    init {
        val db = AppDatabase.getDatabase(application)
        transactionDao = db.transactionDao()
        
        // Lấy transactions từ database với category
        val sourceTransactions = transactionDao.getAllTransactionsWithCategory()
        
        // Sử dụng MediatorLiveData để observe transactions và tính toán
        _transactions.addSource(sourceTransactions) { transactionList ->
            _transactions.value = transactionList
            
            // Tính tổng income và expense
            var income = 0.0
            var expense = 0.0
            
            transactionList?.forEach { transactionWithCategory ->
                val amount = transactionWithCategory.transaction.amount
                
                if (transactionWithCategory.transaction.isIncome) {
                    income += amount
                } else {
                    expense += amount
                }
            }
            
            _totalIncome.postValue(income)
            _totalExpense.postValue(expense)
        }
        
        // Khởi tạo giá trị ban đầu
        _totalIncome.value = 0.0
        _totalExpense.value = 0.0
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
        }
    }
}
