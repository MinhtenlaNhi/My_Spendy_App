package com.example.myspendyapp.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myspendyapp.databinding.FragmentCategoryListBinding;
import com.example.myspendyapp.data.db.entity.Category;

public class CategoryListFragment extends Fragment {

    private FragmentCategoryListBinding binding;
    private CategoryViewModel categoryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        CategoryAdapter adapter = new CategoryAdapter(categoryViewModel);
        
        // Set click listener
        adapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                // Navigate đến CreateCategoryFragment với category để edit
                android.os.Bundle bundle = new android.os.Bundle();
                bundle.putLong("category_id", category.getId());
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(com.example.myspendyapp.R.id.action_category_to_create_category, bundle);
            }

            @Override
            public void onCategoryLongClick(Category category, View view) {
                showPopupMenu(category, view);
            }
        });
        
        // Sử dụng GridLayoutManager với 3 cột
        androidx.recyclerview.widget.GridLayoutManager layoutManager = 
            new androidx.recyclerview.widget.GridLayoutManager(getContext(), 3);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        // Hiển thị tất cả categories
        categoryViewModel.allCategories.observe(getViewLifecycleOwner(), adapter::submitList);
    }

    private void showPopupMenu(Category category, View view) {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(view.getContext(), view);
        popupMenu.getMenu().add("Sửa");
        popupMenu.getMenu().add("Xóa");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().toString().equals("Sửa")) {
                android.os.Bundle bundle = new android.os.Bundle();
                bundle.putLong("category_id", category.getId());
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(com.example.myspendyapp.R.id.action_category_to_create_category, bundle);
            } else if (item.getTitle().toString().equals("Xóa")) {
                showDeleteConfirmationDialog(category);
            }
            return true;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(Category category) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục \"" + category.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    categoryViewModel.delete(category);
                    android.widget.Toast.makeText(requireContext(), "Đã xóa danh mục \"" + category.getName() + "\"", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
