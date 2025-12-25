package com.example.myspendyapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myspendyapp.R;
import com.example.myspendyapp.data.preferences.SessionManager;
import com.example.myspendyapp.databinding.FragmentAccountBinding;
import com.example.myspendyapp.databinding.RowAccountItemBinding;

public class FragmentAccount extends Fragment {

    private FragmentAccountBinding binding;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel và SessionManager
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        sessionManager = new SessionManager(requireContext());
        navController = Navigation.findNavController(view);

        // Hiển thị thông tin user
        loadUserInfo();

        // Setup các item menu
        setupItem(binding.btnChangeInfo.getRoot(), R.drawable.ic_person, "Thay đổi thông tin cá nhân");
        setupItem(binding.btnChangePassword.getRoot(), R.drawable.ic_lock_blue, "Thay đổi mật khẩu");
        setupItem(binding.btnSetting.getRoot(), R.drawable.ic_setting, "Cài đặt");
        setupItem(binding.btnAppInfo.getRoot(), R.drawable.ic_info, "Thông tin ứng dụng");
        setupItem(binding.btnSupport.getRoot(), R.drawable.ic_support, "Hỗ trợ");

        // Setup click listeners cho các menu items
        binding.btnChangePassword.getRoot().setOnClickListener(v -> {
            try {
                navController.navigate(R.id.action_account_to_change_password);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Setup nút logout
        binding.btnLogout.setOnClickListener(v -> handleLogout());

        // Setup nút back
        binding.btnBack.setOnClickListener(v -> navController.navigateUp());
    }

    /**
     * Load và hiển thị thông tin user từ session
     */
    private void loadUserInfo() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String phone = sessionManager.getPhone();

        if (username != null) {
            // Hiển thị username (có thể format thành tên nếu cần)
            binding.tvName.setText(username);
        }

        if (email != null && !email.isEmpty()) {
            binding.tvEmail.setText(email);
        } else if (phone != null && !phone.isEmpty()) {
            binding.tvEmail.setText(phone);
        } else {
            binding.tvEmail.setText("Chưa cập nhật thông tin");
        }
    }

    /**
     * Xử lý logout
     */
    private void handleLogout() {
        // Xóa phiên đăng nhập
        authViewModel.logout();
        sessionManager.clearSession();
        
        // Hiển thị thông báo
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        
        // Điều hướng về màn hình login và xóa back stack
        navController.navigate(R.id.action_account_to_login);
    }

    private void setupItem(View item, int iconRes, String title) {
        ImageView icon = item.findViewById(R.id.icon);
        TextView titleView = item.findViewById(R.id.title);

        icon.setImageResource(iconRes);
        titleView.setText(title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
