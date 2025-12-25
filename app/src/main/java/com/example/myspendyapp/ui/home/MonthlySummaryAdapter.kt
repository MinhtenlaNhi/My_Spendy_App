package com.example.myspendyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myspendyapp.data.models.MonthlyStatistics
import com.example.myspendyapp.databinding.ItemMonthlySummaryBinding
import java.text.DecimalFormat

class MonthlySummaryAdapter(
    private var periodType: HomeViewModel.PeriodType = HomeViewModel.PeriodType.MONTH
) : ListAdapter<MonthlyStatistics, MonthlySummaryAdapter.MonthlySummaryViewHolder>(
    MonthlySummaryDiffCallback()
) {

    private val numberFormat = DecimalFormat("#,###")

    fun setPeriodType(periodType: HomeViewModel.PeriodType) {
        this.periodType = periodType
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlySummaryViewHolder {
        val binding = ItemMonthlySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MonthlySummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthlySummaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MonthlySummaryViewHolder(
        private val binding: ItemMonthlySummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: MonthlyStatistics) {
            // Tạo label phù hợp với period type
            val title = when (periodType) {
                HomeViewModel.PeriodType.DAY -> {
                    // stat.month là dayOfMonth, stat.year chứa year * 100 + month
                    val dayOfMonth = stat.month
                    val month = stat.year % 100
                    val year = stat.year / 100
                    "$dayOfMonth/$month/$year"
                }
                HomeViewModel.PeriodType.WEEK -> {
                    // stat.month là weekOfYear
                    "Tuần ${stat.month}"
                }
                HomeViewModel.PeriodType.MONTH -> {
                    "Tháng ${stat.month}"
                }
            }
            binding.tvMonthTitle.text = title
            binding.tvMonthIncome.text = "+ ${formatNumber(stat.income)}"
            binding.tvMonthExpense.text = "- ${formatNumber(stat.expense)}"
            binding.tvMonthBalance.text = formatNumber(stat.balance)
        }

        private fun formatNumber(number: Double): String {
            return numberFormat.format(number).replace(",", ".")
        }
    }
}

class MonthlySummaryDiffCallback : DiffUtil.ItemCallback<MonthlyStatistics>() {
    override fun areItemsTheSame(oldItem: MonthlyStatistics, newItem: MonthlyStatistics): Boolean {
        return oldItem.month == newItem.month && oldItem.year == newItem.year
    }

    override fun areContentsTheSame(oldItem: MonthlyStatistics, newItem: MonthlyStatistics): Boolean {
        return oldItem == newItem
    }
}

