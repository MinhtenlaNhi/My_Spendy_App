package com.example.myspendyapp.ui.spending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myspendyapp.R
import com.example.myspendyapp.data.models.TransactionWithCategory
import com.example.myspendyapp.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        fun bind(item: TransactionWithCategory) {
            val transaction = item.transaction
            val category = item.category

            // Lấy icon từ category
            val iconRes = getIconResourceId(category.iconName)
            binding.ivTransactionIcon.setImageResource(iconRes)
            binding.ivTransactionIcon.setColorFilter(android.graphics.Color.WHITE)
            
            // Set màu nền cho icon container
            try {
                val color = android.graphics.Color.parseColor(category.color)
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                val cornerRadius = 8f * binding.root.context.resources.displayMetrics.density
                drawable.cornerRadius = cornerRadius
                drawable.setColor(color)
                binding.flIconContainer.background = drawable
            } catch (e: IllegalArgumentException) {
                // Nếu màu không hợp lệ, dùng màu mặc định
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                val cornerRadius = 8f * binding.root.context.resources.displayMetrics.density
                drawable.cornerRadius = cornerRadius
                drawable.setColor(ContextCompat.getColor(binding.root.context, R.color.purple_500))
                binding.flIconContainer.background = drawable
            }

            binding.tvTransactionCategory.text = category.name
            binding.tvTransactionNote.text = transaction.note
            binding.tvTransactionDate.text = dateFormat.format(transaction.date)

            val formattedAmount = currencyFormat.format(transaction.amount)

            // Sử dụng isIncome từ transaction để xác định income/expense
            val isIncome = transaction.isIncome
            if (!isIncome) {
                binding.tvTransactionAmount.text = "- $formattedAmount"
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
            } else {
                binding.tvTransactionAmount.text = "+ $formattedAmount"
                binding.tvTransactionAmount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
            }
        }
        
        private fun getIconResourceId(iconName: String): Int {
            return when (iconName) {
                "ic_food" -> R.drawable.ic_food
                "ic_car" -> R.drawable.ic_car
                "ic_check" -> R.drawable.ic_check
                "ic_education" -> R.drawable.ic_education
                "ic_gift" -> R.drawable.ic_gift
                "ic_home" -> R.drawable.ic_home
                "ic_income" -> R.drawable.ic_income
                "ic_inves" -> R.drawable.ic_invest
                "ic_lightning" -> R.drawable.ic_lightning
                "ic_medicine" -> R.drawable.ic_medicine
                "ic_piggy" -> R.drawable.ic_piggy
                "ic_protect" -> R.drawable.ic_protect
                "ic_shopping" -> R.drawable.ic_shopping
                else -> {
                    // Icon mặc định nếu không tìm thấy
                    R.drawable.ic_add
                }
            }
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
    override fun areItemsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
        return oldItem.transaction.id == newItem.transaction.id
    }

    override fun areContentsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
        return oldItem == newItem
    }
}
