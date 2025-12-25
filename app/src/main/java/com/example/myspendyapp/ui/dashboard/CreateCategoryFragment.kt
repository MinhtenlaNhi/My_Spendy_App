package com.example.myspendyapp.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myspendyapp.R
import com.example.myspendyapp.data.db.entity.Category
import com.example.myspendyapp.databinding.FragmentCreateCategoryBinding
import com.example.myspendyapp.databinding.ItemColorBinding
import com.example.myspendyapp.databinding.ItemIconBinding

class CreateCategoryFragment : Fragment() {

    private var _binding: FragmentCreateCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CategoryViewModel
    private var selectedIconName: String = "ic_food"
    private var selectedColor: String = "#FF6200EE"
    private var selectedImageUri: Uri? = null
    private var categoryToEdit: Category? = null

    // Danh sách icon có sẵn
    private val availableIcons = listOf(
        "ic_food", "ic_income", "ic_expense", "ic_home_black_24dp",
        "ic_dashboard_black_24dp", "ic_person", "ic_setting", "ic_info",
        "ic_support", "ic_add", "ic_calendar", "ic_color_palette"
    )

    // Danh sách màu sắc
    private val availableColors = listOf(
        "#FFFFC107", // Yellow
        "#FF03A9F4", // Light Blue
        "#FF4CAF50", // Green
        "#FFF44336", // Red
        "#FF2196F3", // Blue
        "#FFE91E63", // Pink
        "#FF00BCD4", // Teal
        "#FF9C27B0", // Purple
        "#FFFF9800", // Orange
        "#FF795548", // Brown
        "#FF607D8B", // Blue Grey
        "#FF6200EE"  // Deep Purple
    )

    // Danh sách loại danh mục (sẽ được load từ CSDL)
    private var categoryTypeSuggestions = mutableListOf<String>()

    // Launcher cho chọn ảnh
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivSelectedIcon.setImageURI(uri)
                // Lưu URI vào selectedIconName dưới dạng string
                selectedIconName = uri.toString()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)

        // Lấy category từ arguments nếu có (edit mode)
        arguments?.getLong("category_id")?.let { categoryId ->
            viewModel.getCategoryById(categoryId) { category ->
                categoryToEdit = category
                category?.let {
                    loadCategoryData(it)
                }
            }
        }

        setupCategoryTypeDropdown()
        setupCategoryNameAutocomplete()
        setupIconRecyclerView()
        setupColorRecyclerView()
        setupClickListeners()
        updateSelectedIcon()
        updateSelectedColor()
    }

    private fun loadCategoryData(category: Category) {
        binding.edtCategoryType.setText(category.type) // Loại danh mục là text tự do
        binding.edtCategoryName.setText(category.name)
        selectedIconName = category.iconName
        selectedColor = category.color
        updateSelectedIcon()
        updateSelectedColor()
        binding.tvDialogTitle.text = "Sửa danh mục"
    }

    private fun setupCategoryTypeDropdown() {
        // Loại danh mục: AutoComplete với gợi ý từ CSDL và cho phép nhập tự do
        binding.edtCategoryType.threshold = 0
        
        // Filter khi user gõ - tìm kiếm trong CSDL
        binding.edtCategoryType.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 1) {
                    // Tìm kiếm loại danh mục trong CSDL
                    viewModel.searchCategoryTypes(query) { suggestions ->
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                        binding.edtCategoryType.setAdapter(adapter)
                        if (suggestions.isNotEmpty()) {
                            binding.edtCategoryType.showDropDown()
                        }
                    }
                } else {
                    // Load tất cả loại danh mục khi query rỗng
                    viewModel.searchCategoryTypes("") { suggestions ->
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                        binding.edtCategoryType.setAdapter(adapter)
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Click để mở dropdown
        binding.edtCategoryType.setOnClickListener {
            val currentText = binding.edtCategoryType.text.toString().trim()
            viewModel.searchCategoryTypes(currentText) { suggestions ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                binding.edtCategoryType.setAdapter(adapter)
                if (suggestions.isNotEmpty()) {
                    binding.edtCategoryType.showDropDown()
                }
            }
        }
        
        // Load tất cả loại danh mục ban đầu
        viewModel.searchCategoryTypes("") { suggestions ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
            binding.edtCategoryType.setAdapter(adapter)
        }
    }

    private fun setupCategoryNameAutocomplete() {
        // Tên danh mục: AutoComplete với gợi ý từ CSDL
        binding.edtCategoryName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) { // Chỉ tìm kiếm khi có ít nhất 2 ký tự
                    viewModel.searchCategoryNames(query) { names ->
                        if (names.isNotEmpty()) {
                            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                            binding.edtCategoryName.setAdapter(adapter)
                            binding.edtCategoryName.showDropDown()
                        }
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupIconRecyclerView() {
        binding.rvIcons.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvIcons.adapter = IconAdapter { iconName ->
            selectedIconName = iconName
            selectedImageUri = null // Reset image URI khi chọn icon hệ thống
            updateSelectedIcon()
        }
    }

    private fun setupColorRecyclerView() {
        binding.rvColors.layoutManager = GridLayoutManager(requireContext(), 6)
        binding.rvColors.adapter = ColorAdapter { color ->
            selectedColor = color
            updateSelectedColor()
        }
    }

    private fun setupClickListeners() {
        // Chọn icon từ hệ thống
        binding.btnChooseIcon.setOnClickListener {
            // Icon đã được hiển thị trong RecyclerView, user chỉ cần click vào icon
            Toast.makeText(context, "Vui lòng chọn icon từ danh sách bên dưới", Toast.LENGTH_SHORT).show()
        }

        // Chọn ảnh từ gallery
        binding.btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // Chọn màu
        binding.btnChooseColor.setOnClickListener {
            // Màu đã được hiển thị trong RecyclerView, user chỉ cần click vào màu
            Toast.makeText(context, "Vui lòng chọn màu từ danh sách bên dưới", Toast.LENGTH_SHORT).show()
        }

        // Nút Hủy
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // Nút Lưu
        binding.btnSave.setOnClickListener {
            saveCategory()
        }
    }

    private fun updateSelectedIcon() {
        if (selectedImageUri != null) {
            binding.ivSelectedIcon.setImageURI(selectedImageUri)
        } else {
            val iconResId = getIconResourceId(selectedIconName)
            binding.ivSelectedIcon.setImageResource(iconResId)
            binding.ivSelectedIcon.setColorFilter(Color.WHITE)
        }
        
        // Set màu nền cho icon container
        try {
            val color = Color.parseColor(selectedColor)
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(color)
            }
            binding.ivSelectedIcon.background = drawable
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun updateSelectedColor() {
        try {
            val color = Color.parseColor(selectedColor)
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(color)
            }
            binding.viewSelectedColor.background = drawable
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun saveCategory() {
        val categoryTypeText = binding.edtCategoryType.text.toString().trim()
        val categoryName = binding.edtCategoryName.text.toString().trim()

        if (categoryTypeText.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn loại danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryName.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        // Loại danh mục là text tự do, lưu trực tiếp
        val type = categoryTypeText

        if (categoryToEdit == null) {
            // Thêm mới
            val newCategory = Category(
                name = categoryName,
                type = type,
                iconName = selectedIconName,
                color = selectedColor
            )
            viewModel.insert(newCategory)
            Toast.makeText(context, "Đã thêm danh mục thành công", Toast.LENGTH_SHORT).show()
        } else {
            // Cập nhật
            val updatedCategory = categoryToEdit!!.copy(
                name = categoryName,
                type = type,
                iconName = selectedIconName,
                color = selectedColor
            )
            viewModel.update(updatedCategory)
            Toast.makeText(context, "Đã cập nhật danh mục thành công", Toast.LENGTH_SHORT).show()
        }
        
        findNavController().popBackStack()
    }

    private fun getIconResourceId(iconName: String): Int {
        return when (iconName) {
            "ic_food" -> R.drawable.ic_food
            "ic_income" -> R.drawable.ic_income
            "ic_expense" -> R.drawable.ic_expense
            "ic_home_black_24dp" -> R.drawable.ic_home_black_24dp
            "ic_dashboard_black_24dp" -> R.drawable.ic_dashboard_black_24dp
            "ic_person" -> R.drawable.ic_person
            "ic_setting" -> R.drawable.ic_setting
            "ic_info" -> R.drawable.ic_info
            "ic_support" -> R.drawable.ic_support
            "ic_add" -> R.drawable.ic_add
            "ic_calendar" -> R.drawable.ic_calendar
            "ic_color_palette" -> R.drawable.ic_color_palette
            else -> {
                // Nếu là URI, trả về icon mặc định
                R.drawable.ic_add
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Icon Adapter
    private inner class IconAdapter(
        private val onIconSelected: (String) -> Unit
    ) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            val binding = ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return IconViewHolder(binding)
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            val iconName = availableIcons[position]
            val iconResId = getIconResourceId(iconName)
            holder.bind(iconResId, iconName == selectedIconName) {
                selectedIconName = iconName
                selectedImageUri = null
                notifyDataSetChanged()
                updateSelectedIcon()
                onIconSelected(iconName)
            }
        }

        override fun getItemCount() = availableIcons.size

        inner class IconViewHolder(private val binding: ItemIconBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(iconResId: Int, isSelected: Boolean, onClick: () -> Unit) {
                binding.ivIcon.setImageResource(iconResId)
                binding.root.isSelected = isSelected
                binding.root.setOnClickListener { onClick() }
            }
        }
    }

    // Color Adapter
    private inner class ColorAdapter(
        private val onColorSelected: (String) -> Unit
    ) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
            val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ColorViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
            val color = availableColors[position]
            holder.bind(color, color == selectedColor) {
                selectedColor = color
                notifyDataSetChanged()
                updateSelectedColor()
                updateSelectedIcon() // Cập nhật màu nền icon
                onColorSelected(color)
            }
        }

        override fun getItemCount() = availableColors.size

        inner class ColorViewHolder(private val binding: ItemColorBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(color: String, isSelected: Boolean, onClick: () -> Unit) {
                try {
                    binding.viewColor.setBackgroundColor(Color.parseColor(color))
                } catch (e: Exception) {
                    // Ignore
                }
                binding.root.isSelected = isSelected
                binding.root.setOnClickListener { onClick() }
            }
        }
    }
}

