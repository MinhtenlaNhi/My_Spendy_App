package com.example.myspendyapp.ui.forgotpassword;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myspendyapp.R;
import com.example.myspendyapp.ui.AuthResult;
import com.example.myspendyapp.ui.AuthViewModel;

public class FragmentResetPassword extends Fragment {

    private EditText edtPassword, edtRePassword;
    private Button btnReset;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private String phoneNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtPassword = view.findViewById(R.id.edtPassword);
        edtRePassword = view.findViewById(R.id.edtRePassword);
        btnReset = view.findViewById(R.id.btnReset);
        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo AuthViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (getArguments() != null) {
            phoneNumber = getArguments().getString("phoneNumber");
        }

        // Observe kết quả reset password
        observeResetPasswordResult();

        btnReset.setOnClickListener(v -> {
            String pass = edtPassword.getText().toString().trim();
            String confirm = edtRePassword.getText().toString().trim();

            if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(getContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: Không tìm thấy số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi method reset password từ ViewModel
            authViewModel.resetPassword(phoneNumber, pass);
        });
    }

    private void observeResetPasswordResult() {
        authViewModel.getResetPasswordResult().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof AuthResult.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                btnReset.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnReset.setEnabled(true);
            }

            if (result instanceof AuthResult.Success) {
                String successMessage = ((AuthResult.Success) result).getMessage();
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                
                // Điều hướng về màn hình đăng nhập
                androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(R.id.action_reset_password_to_login);
                
                // Reset result
                authViewModel.resetResetPasswordResult();
            } else if (result instanceof AuthResult.Error) {
                String errorMessage = ((AuthResult.Error) result).getMessage();
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                authViewModel.resetResetPasswordResult();
            }
        });
    }
}

