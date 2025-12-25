package com.example.myspendyapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myspendyapp.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    private val numberFormat = DecimalFormat("#,###")
    private lateinit var monthlySummaryAdapter: MonthlySummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        setupChart()
        setupMonthlySummariesRecyclerView()
        setupTabClickListeners()
        observeViewModel()
    }

    private fun setupMonthlySummariesRecyclerView() {
        monthlySummaryAdapter = MonthlySummaryAdapter()
        binding.recyclerViewMonthlySummaries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = monthlySummaryAdapter
        }
    }

    private fun setupChart() {
        val chart = binding.chartThuChi
        
        // Cấu hình chart cơ bản
        chart.description.isEnabled = false
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawGridBackground(false)
        
        // Cấu hình X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.textSize = 10f
        
        // Cấu hình Y-axis trái
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 10f
        
        // Ẩn Y-axis phải
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Hiển thị legend
        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 12f
        
        // Tắt animation
        chart.animateY(500)
    }

    private fun setupTabClickListeners() {
        binding.tvNgay.setOnClickListener {
            selectTab(0)
            monthlySummaryAdapter.setPeriodType(HomeViewModel.PeriodType.DAY)
            viewModel.loadData(HomeViewModel.PeriodType.DAY)
        }
        
        binding.tvTuan.setOnClickListener {
            selectTab(1)
            monthlySummaryAdapter.setPeriodType(HomeViewModel.PeriodType.WEEK)
            viewModel.loadData(HomeViewModel.PeriodType.WEEK)
        }
        
        binding.tvThang.setOnClickListener {
            selectTab(2)
            monthlySummaryAdapter.setPeriodType(HomeViewModel.PeriodType.MONTH)
            viewModel.loadData(HomeViewModel.PeriodType.MONTH)
        }
    }

    private fun selectTab(index: Int) {
        // Reset background của tất cả tabs
        binding.tvNgay.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_unselected)
        binding.tvTuan.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_unselected)
        binding.tvThang.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_unselected)
        
        // Set background cho tab được chọn
        when (index) {
            0 -> binding.tvNgay.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_selected)
            1 -> binding.tvTuan.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_selected)
            2 -> binding.tvThang.setBackgroundResource(com.example.myspendyapp.R.drawable.bg_tab_selected)
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyStatistics.observe(viewLifecycleOwner) { stats ->
            if (stats.isNotEmpty()) {
                updateChart(stats)
                updateMonthlySummaries(stats)
            }
        }

        viewModel.currentPeriodType.observe(viewLifecycleOwner) { periodType ->
            monthlySummaryAdapter.setPeriodType(periodType)
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTongThu.text = "+ ${formatNumber(income)}"
            updateBalance()
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvTongChi.text = "- ${formatNumber(expense)}"
            updateBalance()
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        val balance = income - expense
        binding.tvConLai.text = formatNumber(balance)
    }

    private fun updateChart(stats: List<com.example.myspendyapp.data.models.MonthlyStatistics>) {
        val chart = binding.chartThuChi
        
        // Tạo BarEntries cho income và expense
        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        val periodType = viewModel.currentPeriodType.value ?: HomeViewModel.PeriodType.MONTH
        
        stats.forEachIndexed { index, stat ->
            incomeEntries.add(BarEntry(index.toFloat(), stat.income.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), stat.expense.toFloat()))
            
            // Tạo label phù hợp với period type
            val label = when (periodType) {
                HomeViewModel.PeriodType.DAY -> {
                    // stat.month là dayOfMonth, stat.year chứa year * 100 + month
                    val dayOfMonth = stat.month
                    val month = stat.year % 100
                    val year = stat.year / 100
                    "$dayOfMonth/$month"
                }
                HomeViewModel.PeriodType.WEEK -> {
                    // stat.month là weekOfYear
                    "Tuần ${stat.month}"
                }
                HomeViewModel.PeriodType.MONTH -> {
                    "Tháng ${stat.month}"
                }
            }
            labels.add(label)
        }
        
        // Tạo BarDataSet
        val incomeDataSet = BarDataSet(incomeEntries, "Thu")
        incomeDataSet.color = 0xFF9C27B0.toInt() // Purple
        incomeDataSet.setDrawValues(false)
        
        val expenseDataSet = BarDataSet(expenseEntries, "Chi")
        expenseDataSet.color = 0xFFF44336.toInt() // Red
        expenseDataSet.setDrawValues(false)
        
        // Tạo BarData với group spacing
        val barData = BarData(incomeDataSet, expenseDataSet)
        barData.barWidth = 0.35f
        
        chart.data = barData
        chart.groupBars(0f, 0.06f, 0.02f)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.invalidate()
    }

    private fun updateMonthlySummaries(stats: List<com.example.myspendyapp.data.models.MonthlyStatistics>) {
        // Đảo ngược danh sách để hiển thị tháng mới nhất trước
        monthlySummaryAdapter.submitList(stats.reversed())
    }

    private fun formatNumber(number: Double): String {
        return numberFormat.format(number).replace(",", ".")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

