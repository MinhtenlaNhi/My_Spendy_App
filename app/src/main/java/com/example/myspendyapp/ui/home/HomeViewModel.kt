package com.example.myspendyapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myspendyapp.data.db.AppDatabase
import com.example.myspendyapp.data.db.dao.TransactionDao
import com.example.myspendyapp.data.models.MonthlyStatistics
import com.example.myspendyapp.data.models.TransactionWithCategory
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: TransactionDao
    val monthlyStatistics: MutableLiveData<List<MonthlyStatistics>> = MutableLiveData()
    val totalIncome: MutableLiveData<Double> = MutableLiveData(0.0)
    val totalExpense: MutableLiveData<Double> = MutableLiveData(0.0)
    val currentPeriodType: MutableLiveData<PeriodType> = MutableLiveData(PeriodType.MONTH)

    enum class PeriodType {
        DAY, WEEK, MONTH
    }

    init {
        val db = AppDatabase.getDatabase(application)
        transactionDao = db.transactionDao()
        loadData(PeriodType.MONTH)
    }

    fun loadData(periodType: PeriodType) {
        currentPeriodType.value = periodType
        viewModelScope.launch {
            val transactions = transactionDao.getAllTransactionsWithCategorySync()
            
            when (periodType) {
                PeriodType.MONTH -> {
                    val stats = calculateMonthlyStatistics(transactions)
                    monthlyStatistics.postValue(stats)
                    calculateTotalsFromStats(stats)
                }
                PeriodType.WEEK -> {
                    val stats = calculateWeeklyStatistics(transactions)
                    monthlyStatistics.postValue(stats)
                    calculateTotalsFromStats(stats)
                }
                PeriodType.DAY -> {
                    val stats = calculateDailyStatistics(transactions)
                    monthlyStatistics.postValue(stats)
                    calculateTotalsFromStats(stats)
                }
            }
        }
    }

    private fun calculateMonthlyStatistics(transactions: List<TransactionWithCategory>): List<MonthlyStatistics> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        // Tạo map để lưu thống kê theo tháng (12 tháng gần nhất)
        val statsMap = mutableMapOf<Pair<Int, Int>, MonthlyStatistics>()

        // Khởi tạo 12 tháng gần nhất với giá trị 0
        for (i in 0 until 12) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MONTH, -i)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            statsMap[Pair(month, year)] = MonthlyStatistics(month, year, 0.0, 0.0)
        }

        // Tính toán thống kê từ transactions
        transactions.forEach { transactionWithCategory ->
            calendar.time = transactionWithCategory.transaction.date
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val key = Pair(month, year)

            if (statsMap.containsKey(key)) {
                val current = statsMap[key]!!
                val amount = transactionWithCategory.transaction.amount
                val isIncome = transactionWithCategory.transaction.isIncome

                val newStats = if (isIncome) {
                    current.copy(income = current.income + amount)
                } else {
                    current.copy(expense = current.expense + amount)
                }
                statsMap[key] = newStats
            }
        }

        // Sắp xếp theo tháng (từ cũ đến mới)
        return statsMap.values.sortedWith(compareBy({ it.year }, { it.month }))
    }

    private fun calculateWeeklyStatistics(transactions: List<TransactionWithCategory>): List<MonthlyStatistics> {
        val calendar = Calendar.getInstance()
        val statsMap = mutableMapOf<Pair<Int, Int>, MonthlyStatistics>()

        // Khởi tạo 4 tuần gần nhất
        for (i in 0 until 4) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.WEEK_OF_YEAR, -i)
            
            // Lấy tuần của năm (1-52)
            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            
            // Sử dụng weekOfYear làm "month" (để tương thích với MonthlyStatistics)
            statsMap[Pair(weekOfYear, year)] = MonthlyStatistics(weekOfYear, year,
                0.0, 0.0)
        }

        // Tính toán thống kê từ transactions
        transactions.forEach { transactionWithCategory ->
            calendar.time = transactionWithCategory.transaction.date
            val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            val key = Pair(weekOfYear, year)

            if (statsMap.containsKey(key)) {
                val current = statsMap[key]!!
                val amount = transactionWithCategory.transaction.amount
                val isIncome = transactionWithCategory.transaction.isIncome

                val newStats = if (isIncome) {
                    current.copy(income = current.income + amount)
                } else {
                    current.copy(expense = current.expense + amount)
                }
                statsMap[key] = newStats
            }
        }

        // Sắp xếp theo tuần (từ cũ đến mới)
        return statsMap.values.sortedWith(compareBy({ it.year }, { it.month }))
    }

    private fun calculateDailyStatistics(transactions: List<TransactionWithCategory>): List<MonthlyStatistics> {
        val calendar = Calendar.getInstance()
        val statsMap = mutableMapOf<String, MonthlyStatistics>()

        // Khởi tạo 30 ngày gần nhất
        for (i in 0 until 30) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            
            // Sử dụng key string để lưu day-month-year
            val key = "$dayOfMonth-$month-$year"
            // Lưu dayOfMonth vào month field, và encode month vào year (year * 100 + month)
            statsMap[key] = MonthlyStatistics(dayOfMonth, year * 100 + month, 0.0, 0.0)
        }

        // Tính toán thống kê từ transactions
        transactions.forEach { transactionWithCategory ->
            calendar.time = transactionWithCategory.transaction.date
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val key = "$dayOfMonth-$month-$year"

            if (statsMap.containsKey(key)) {
                val current = statsMap[key]!!
                val amount = transactionWithCategory.transaction.amount
                val isIncome = transactionWithCategory.transaction.isIncome

                val newStats = if (isIncome) {
                    current.copy(income = current.income + amount)
                } else {
                    current.copy(expense = current.expense + amount)
                }
                statsMap[key] = newStats
            }
        }

        // Sắp xếp theo ngày (từ cũ đến mới)
        // year field chứa year * 100 + month, nên sort theo đó
        return statsMap.values.sortedWith(compareBy({ it.year }, { it.month }))
    }

    private fun calculateTotalsFromStats(stats: List<MonthlyStatistics>) {
        var income = 0.0
        var expense = 0.0

        stats.forEach { stat ->
            income += stat.income
            expense += stat.expense
        }

        totalIncome.postValue(income)
        totalExpense.postValue(expense)
    }
}

