package com.example.myspendyapp.data.models

data class MonthlyStatistics(
    val month: Int, // 1-12
    val year: Int,
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense
}


