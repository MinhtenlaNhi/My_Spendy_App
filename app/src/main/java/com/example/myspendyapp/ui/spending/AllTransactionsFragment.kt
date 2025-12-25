package com.example.myspendyapp.ui.spending

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myspendyapp.R
import com.example.myspendyapp.databinding.FragmentAllTransactionsBinding
import com.example.myspendyapp.ui.dashboard.CategoryViewModel
import com.example.myspendyapp.data.db.entity.Category
import android.widget.ArrayAdapter
import java.util.ArrayList

class AllTransactionsFragment : Fragment() {

    private var _binding: FragmentAllTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SpendingViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private val allCategories = ArrayList<Category>()
    private var selectedCategoryFilter = ""
    private var isIncome: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy argument isIncome
        arguments?.let {
            isIncome = it.getBoolean("isIncome", false)
        }

        viewModel = ViewModelProvider(requireActivity())[SpendingViewModel::class.java]
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupCategoryFilter()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Set title dựa trên isIncome
        val title = if (isIncome) {
            "Danh sách các khoản thu"
        } else {
            "Danh sách các khoản chi"
        }
        binding.toolbar.title = title
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvAllTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllTransactions.adapter = transactionAdapter
    }

    private fun setupCategoryFilter() {
        val categoryNames = ArrayList<String>()
        categoryNames.add("Tất cả danh mục")

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            allCategories.clear()
            allCategories.addAll(categories)
            updateCategoryFilterList(categories, categoryNames)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.sltFilterCategory.setAdapter(adapter)
        binding.sltFilterCategory.setText("Tất cả danh mục", false)

        binding.sltFilterCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position) as String
            selectedCategoryFilter = if (selectedCategory == "Tất cả danh mục") {
                ""
            } else {
                selectedCategory
            }
            filterTransactions()
        }
    }

    private fun updateCategoryFilterList(categories: List<Category>, categoryNames: ArrayList<String>) {
        for (category in categories) {
            if (!categoryNames.contains(category.name)) {
                categoryNames.add(category.name)
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.sltFilterCategory.setAdapter(adapter)
    }

    private fun filterTransactions() {
        // Filter logic sẽ được implement trong observeViewModel
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions != null) {
                // Filter theo isIncome
                val filteredByType = transactions.filter { transactionWithCategory ->
                    transactionWithCategory.transaction.isIncome == isIncome
                }

                // Filter theo category nếu có
                val finalList = if (selectedCategoryFilter.isEmpty()) {
                    filteredByType
                } else {
                    filteredByType.filter { transactionWithCategory ->
                        transactionWithCategory.category?.name == selectedCategoryFilter
                    }
                }

                // Sắp xếp theo ngày mới nhất
                val sortedList = finalList.sortedByDescending { it.transaction.date }
                transactionAdapter.submitList(sortedList)
            } else {
                transactionAdapter.submitList(emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


