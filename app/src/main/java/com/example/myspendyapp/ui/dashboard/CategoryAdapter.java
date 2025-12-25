package com.example.myspendyapp.ui.dashboard;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myspendyapp.R;
import com.example.myspendyapp.data.db.entity.Category;
import androidx.navigation.Navigation;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final CategoryViewModel viewModel;
    private OnCategoryClickListener onCategoryClickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category, View view);
    }

    public CategoryAdapter(CategoryViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getIconName().equals(newItem.getIconName()) &&
                   oldItem.getColor().equals(newItem.getColor());
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_grid, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout flIconContainer;
        private final ImageView ivCategoryIcon;
        private final TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconContainer = itemView.findViewById(R.id.flIconContainer);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }

        public void bind(Category category) {
            tvCategoryName.setText(category.getName());
            
            // Set màu nền cho icon container
            try {
                int color = Color.parseColor(category.getColor());
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(8f * itemView.getContext().getResources().getDisplayMetrics().density);
                drawable.setColor(color);
                flIconContainer.setBackground(drawable);
            } catch (IllegalArgumentException e) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(8f * itemView.getContext().getResources().getDisplayMetrics().density);
                drawable.setColor(Color.parseColor("#FF6200EE"));
                flIconContainer.setBackground(drawable);
            }

            // Set icon
            int iconResId = getIconResourceId(category.getIconName());
            ivCategoryIcon.setImageResource(iconResId);
            ivCategoryIcon.setColorFilter(Color.WHITE);

            // Xóa listener cũ để tránh duplicate
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
            
            // Đảm bảo itemView có thể nhận click
            itemView.setClickable(true);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            
            // Click để sửa
            itemView.setOnClickListener(v -> {
                android.util.Log.d("CategoryAdapter", "Click on category: " + category.getName());
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(category);
                } else {
                    navigateToEdit(category, v);
                }
            });
            
            // Long click để hiển thị menu sửa/xóa
            itemView.setOnLongClickListener(v -> {
                android.util.Log.d("CategoryAdapter", "Long click on category: " + category.getName());
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryLongClick(category, v);
                } else {
                    showPopupMenu(category, v);
                }
                return true;
            });
        }

        private int getIconResourceId(String iconName) {
            switch (iconName) {
                case "ic_food": return R.drawable.ic_food;
                case "ic_income": return R.drawable.ic_income;
                case "ic_expense": return R.drawable.ic_expense;
                case "ic_home_black_24dp": return R.drawable.ic_home_black_24dp;
                case "ic_dashboard_black_24dp": return R.drawable.ic_dashboard_black_24dp;
                case "ic_person": return R.drawable.ic_person;
                case "ic_setting": return R.drawable.ic_setting;
                case "ic_info": return R.drawable.ic_info;
                case "ic_support": return R.drawable.ic_support;
                case "ic_add": return R.drawable.ic_add;
                case "ic_calendar": return R.drawable.ic_calendar;
                case "ic_color_palette": return R.drawable.ic_color_palette;
                default: return R.drawable.ic_food;
            }
        }

        private void navigateToEdit(Category category, View view) {
            // Navigate đến CreateCategoryFragment với category để edit
            try {
                android.os.Bundle bundle = new android.os.Bundle();
                bundle.putLong("category_id", category.getId());
                androidx.navigation.NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_category_to_create_category, bundle);
            } catch (Exception e) {
                // Nếu không tìm được NavController từ view, thử tìm từ activity
                android.util.Log.e("CategoryAdapter", "Error navigating: " + e.getMessage());
                if (view.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                    androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) view.getContext();
                    androidx.fragment.app.Fragment fragment = activity.getSupportFragmentManager()
                            .findFragmentById(android.R.id.content);
                    if (fragment != null) {
                        android.os.Bundle bundle = new android.os.Bundle();
                        bundle.putLong("category_id", category.getId());
                        androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(fragment.requireView());
                        navController.navigate(R.id.action_category_to_create_category, bundle);
                    }
                }
            }
        }

        private void showPopupMenu(Category category, View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenu().add("Sửa");
            popupMenu.getMenu().add("Xóa");
            
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals("Sửa")) {
                    navigateToEdit(category, view);
                } else if (item.getTitle().toString().equals("Xóa")) {
                    showDeleteConfirmationDialog(category, view);
                }
                return true;
            });
            popupMenu.show();
        }

        private void showDeleteConfirmationDialog(Category category, View view) {
            new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục \"" + category.getName() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        viewModel.delete(category);
                        Toast.makeText(view.getContext(), "Đã xóa danh mục \"" + category.getName() + "\"", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }
}
