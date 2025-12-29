package com.example.myspendyapp.ui.notifications

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myspendyapp.data.db.AppDatabase
import com.example.myspendyapp.data.db.dao.TransactionDao
import com.example.myspendyapp.data.models.TransactionWithCategory
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionDao: TransactionDao
    private val prefs: SharedPreferences = application.getSharedPreferences("NotificationsPrefs", 0)

    // Keys cho SharedPreferences
    private val KEY_REMINDER_ENABLED = "reminder_enabled"
    private val KEY_REMINDER_HOUR = "reminder_hour"
    private val KEY_REMINDER_MINUTE = "reminder_minute"
    private val KEY_MONTHLY_BUDGET = "monthly_budget"

    private val _totalExpenseThisMonth = MutableLiveData<Double>(0.0)
    val totalExpenseThisMonth: LiveData<Double> = _totalExpenseThisMonth

    private val _monthlyBudget = MutableLiveData<Double>(0.0)
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _isOverBudget = MutableLiveData<Boolean>(false)
    val isOverBudget: LiveData<Boolean> = _isOverBudget

    private val _warningMessage = MutableLiveData<String>("")
    val warningMessage: LiveData<String> = _warningMessage

    init {
        val db = AppDatabase.getDatabase(application)
        transactionDao = db.transactionDao()

        // Load settings từ SharedPreferences
        loadSettings()

        // Tính toán chi tiêu tháng này
        calculateMonthlyExpense()
    }

    private fun loadSettings() {
        val budget = prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        _monthlyBudget.value = budget
    }

    fun calculateMonthlyExpense() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val transactions = transactionDao.getAllTransactionsWithCategorySync()

            var totalExpense = 0.0
            transactions?.forEach { transactionWithCategory ->
                val transaction = transactionWithCategory.transaction
                val transactionDate = transaction.date
                val transactionCalendar = Calendar.getInstance().apply {
                    time = transactionDate
                }

                // Chỉ tính các giao dịch chi tiêu trong tháng hiện tại
                if (!transaction.isIncome &&
                    transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                    transactionCalendar.get(Calendar.YEAR) == currentYear
                ) {
                    totalExpense += transaction.amount
                }
            }

            _totalExpenseThisMonth.postValue(totalExpense)

            // Kiểm tra quá ngân sách
            val budget = _monthlyBudget.value ?: 0.0
            if (budget > 0) {
                val isOver = totalExpense > budget
                _isOverBudget.postValue(isOver)

                if (isOver) {
                    val overAmount = totalExpense - budget
                    _warningMessage.postValue("Cảnh báo! Bạn đã chi tiêu vượt quá ngân " +
                            "sách ${String.format("%.0f", overAmount)} đ")
                } else if (totalExpense >= budget * 0.8) {
                    val remaining = budget - totalExpense
                    _warningMessage.postValue("Cảnh báo! Bạn đã chi tiêu " +
                            "${String.format("%.0f", (totalExpense / budget * 100))}% ngân sách. " +
                            "Còn lại ${String.format("%.0f", remaining)} đ")
                } else {
                    _warningMessage.postValue("")
                }
            } else {
                _warningMessage.postValue("")
            }
        }
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        calculateMonthlyExpense()
    }

    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false)
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun getReminderHour(): Int {
        return prefs.getInt(KEY_REMINDER_HOUR, 8)
    }

    fun getReminderMinute(): Int {
        return prefs.getInt(KEY_REMINDER_MINUTE, 0)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    // Getter method for Java compatibility - only needed for isOverBudget
    // because Kotlin generates isOverBudget() instead of getIsOverBudget() for boolean properties starting with "is"
    @JvmName("getIsOverBudget")
    fun getIsOverBudget(): LiveData<Boolean> = isOverBudget
}

