package com.example.myspendyapp.ui.spending

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myspendyapp.data.db.entity.Category
import com.example.myspendyapp.data.db.entity.Transaction
import com.example.myspendyapp.databinding.FragmentCreateExpenseBinding
import com.example.myspendyapp.ui.dashboard.CategoryViewModel
import java.util.Date

class CreateExpenseFragment : Fragment() {

    private var _binding: FragmentCreateExpenseBinding? = null
    private val binding get() = _binding!!

    // Sử dụng requireActivity() để share ViewModel với các fragment khác
    private val transactionViewModel: SpendingViewModel by viewModels()
    private lateinit var categoryViewModel: CategoryViewModel

    private var expenseCategories: List<Category> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo CategoryViewModel với requireActivity() để share với các fragment khác
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        setupCategoryDropdown()
        setupDatePicker()

        binding.btnSave.setOnClickListener {
            saveExpenseTransaction()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        binding.sltDate.setText(dateFormat.format(calendar.time))

        binding.sltDate.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.sltDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setupCategoryDropdown() {
        // Observe tất cả categories từ database - hiển thị tất cả
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { allCategories ->
            expenseCategories = allCategories
            val categoryNames = allCategories.map { it.name }
            
            // Debug log để kiểm tra
            android.util.Log.d("CreateExpenseFragment", "Categories loaded: ${categoryNames.size}")
            
            // Tạo adapter với danh sách category names
            val adapter = ArrayAdapter(
                requireContext(), 
                android.R.layout.simple_dropdown_item_1line, 
                categoryNames
            )
            binding.sltCategory.setAdapter(adapter)
            
            // Set threshold = 0 để dropdown hiển thị ngay khi click
            binding.sltCategory.threshold = 0
            
            // Reset hint nếu có categories
            if (categoryNames.isNotEmpty()) {
                binding.sltCategory.hint = "Chọn danh mục"
            } else {
                binding.sltCategory.hint = "Chưa có danh mục. Vui lòng tạo danh mục trước"
            }
            
            // Cho phép click để mở dropdown
            binding.sltCategory.setOnClickListener {
                if (categoryNames.isNotEmpty()) {
                    // Clear text để trigger dropdown
                    binding.sltCategory.setText("")
                    binding.sltCategory.showDropDown()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Chưa có danh mục. Vui lòng tạo danh mục trước", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            // Cho phép focus để mở dropdown
            binding.sltCategory.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && categoryNames.isNotEmpty()) {
                    binding.sltCategory.showDropDown()
                }
            }
        }
    }

    private fun saveExpenseTransaction() {
        val amountStr = binding.edtAmount.text.toString().trim()
        val note = binding.edtContent.text.toString().trim()
        val categoryName = binding.sltCategory.text.toString().trim()
        val dateStr = binding.sltDate.text.toString().trim()

        if (amountStr.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryName.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        // Tìm danh mục đã chọn để lấy ID
        val selectedCategory = expenseCategories.find { it.name == categoryName }
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Danh mục không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amount = amountStr.toDouble()
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show()
                return
            }

            // Parse date từ string
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = if (dateStr.isNotBlank()) {
                try {
                    dateFormat.parse(dateStr) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
            } else {
                Date()
            }

            val newTransaction = Transaction(
                amount = amount,
                note = note,
                date = date,
                categoryId = selectedCategory.id,
                isIncome = false // Đây là khoản chi
            )
            transactionViewModel.addTransaction(newTransaction)

            Toast.makeText(requireContext(), "Đã lưu khoản chi thành công", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
